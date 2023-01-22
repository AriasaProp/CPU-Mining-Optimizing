package com.ariasaproject.cpumininglearn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.GeneralSecurityException;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class Worker extends Observable implements Runnable {
  private static final long WORK_TIMEOUT = 60 * 1000; // ms

  public static enum Notification {
    SYSTEM_ERROR,
    PERMISSION_ERROR,
    CONNECTION_ERROR,
    AUTHENTICATION_ERROR,
    COMMUNICATION_ERROR,
    LONG_POLLING_FAILED,
    LONG_POLLING_ENABLED,
    NEW_BLOCK_DETECTED,
    NEW_WORK,
    POW_TRUE,
    POW_FALSE,
    TERMINATED
  };

  private URL url;
  private String auth;
  private long scanTime; // ms
  private long retryPause; // ms
  private int nThreads;
  private double throttleFactor;

  private volatile Work curWork = null;
  private URL lpUrl = null;
  private HttpURLConnection lpConn = null;
  private AtomicLong hashes = new AtomicLong(0L);
  private final ConsoleMessage console_log;

  public Worker(URL url, String auth, long scanMillis, long pauseMillis, int nThreads, double throttle, ConsoleMessage cm) {
    this.console_log = cm;
    this.url = url;
    this.auth = auth;
    this.scanTime = scanMillis;
    this.retryPause = pauseMillis;
    if (nThreads < 0) throw new IllegalArgumentException();
    this.nThreads = nThreads;
    if (throttle <= 0.0 || throttle > 1.0) throw new IllegalArgumentException();
    this.throttleFactor = 1.0 / throttle - 1.0;
  }

  public long getRetryPause() {
    return retryPause;
  }

  public long getHashes() {
    return hashes.get();
  }

  private volatile boolean running = false;

  public synchronized void stop() {
    running = false;
    this.notifyAll();
  }

  final long THROTTLE_WAIT_TIME = 100L * 1000000L; // ns

  public void run() {
    Thread[] threads;
    running = true;
    synchronized (this) {
      threads = new Thread[1 + nThreads];
      final int step = (int) Math.pow(2, Math.ceil(Math.log(nThreads) / Math.log(2) + 1e-10));
      for (int i = 0; i < nThreads; ++i) {
        final int index = i;
      	threads[1 + i] = new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  Hasher hasher = new Hasher();
                  int nonce = index;
                  long dt, t0 = System.nanoTime();
                  while (running) {
                    try {
                      if (curWork.meetsTarget(nonce, hasher)) {
                        // submit nonce
                        try {
                          boolean result = curWork.submit(nonce);
                          setChanged();
                          notifyObservers(
                              result ? Notification.POW_TRUE : Notification.POW_FALSE);
                        } catch (IOException e) {
                        }
                        if (lpUrl == null) {
                          synchronized (Worker.this) {
                            curWork = null;
                            Worker.this.notify();
                          }
                        }
                      }
                      nonce += step;
                      hashes.incrementAndGet();
                      if (throttleFactor > 0.0
                          && (dt = System.nanoTime() - t0) > THROTTLE_WAIT_TIME) {
                        LockSupport.parkNanos(Math.max(0L, (long) (throttleFactor * dt)));
                        t0 = System.nanoTime();
                      }
                    } catch (NullPointerException e) {
                      try {
                        Thread.sleep(1L);
                      } catch (InterruptedException ie) {
                      }
                    }
                  }
                } catch (GeneralSecurityException e) {
                  setChanged();
                  notifyObservers(Notification.SYSTEM_ERROR);
                  stop();
                }
              }
            });
        threads[1 + i].start();
      }
      do {
        try {
          if (curWork == null || curWork.getAge() >= WORK_TIMEOUT || lpUrl == null) {
            curWork = getWork();
            if (lpUrl == null) {
              try {
                if ((lpUrl = curWork.getLongPollingURL()) != null) {
                  (threads[0] = new Thread(new LongPoller())).start();
                  setChanged();
                  notifyObservers(Notification.LONG_POLLING_ENABLED);
                }
              } catch (Exception e) {
              }
            }
            setChanged();
            notifyObservers(Notification.NEW_WORK);
          }
          if (!running) break;
          wait(Math.min(scanTime, Math.max(1L, WORK_TIMEOUT - curWork.getAge())));
        } catch (InterruptedException e) {
          running = false;
        } catch (NullPointerException e) {
        }
      } while (running);
      running = false;
    }
    if (lpConn != null) lpConn.disconnect();
    try {
      for (Thread t : threads) if (t != null) t.join();
    } catch (InterruptedException e) {
    }
    curWork = null;
    setChanged();
    notifyObservers(Notification.TERMINATED);
  }

  private synchronized Work getWork() {
    while (running) {
      try {
        return new Work(url.openConnection(), url, auth);
      } catch (Exception e) {
        if (!running) break;
      	console_log.sendLog(4, e.getMessage());
        setChanged();
        if (e instanceof IllegalArgumentException) {
          notifyObservers(Notification.AUTHENTICATION_ERROR);
          stop();
          break;
        } else if (e instanceof AccessControlException) {
          notifyObservers(Notification.PERMISSION_ERROR);
          stop();
          break;
        } else if (e instanceof IOException) {
          notifyObservers(Notification.CONNECTION_ERROR);
        } else {
          notifyObservers(Notification.COMMUNICATION_ERROR);
        }
        try {
          curWork = null;
          this.wait(retryPause);
        } catch (InterruptedException ie) {
        }
      }
    }
    return null;
  }

  private class LongPoller implements Runnable {
    private static final int READ_TIMEOUT = 30 * 60 * 1000; // ms

    public void run() {
      while (running) {
        try {
          lpConn = (HttpURLConnection) lpUrl.openConnection();
          lpConn.setReadTimeout(READ_TIMEOUT);
          curWork = new Work(lpConn, url, auth);
          if (!running) break;
          synchronized (Worker.this) {
            setChanged();
            notifyObservers(Notification.NEW_BLOCK_DETECTED);
            setChanged();
            notifyObservers(Notification.NEW_WORK);
            // Worker.this.notify();
          }
        } catch (SocketTimeoutException e) {
        } catch (Exception e) {
          if (!running) break;
          setChanged();
          notifyObservers(Notification.LONG_POLLING_FAILED);
          try {
            Thread.sleep(retryPause);
          } catch (InterruptedException ie) {
          }
        }
      }
      lpUrl = null;
      lpConn = null;
    }
  }
}

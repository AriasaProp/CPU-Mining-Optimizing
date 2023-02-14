package com.ariasaproject.cpuminingopt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Observable;

public class CpuMiningWorker extends Observable implements IMiningWorker {
	private static final int Number_Of_Thread = 3;
	//Runtime.getRuntime().getAvailableProcessors();
  private int _thread_priorirty;
  private Worker[] _workr_thread;
  private long _retrypause;

  private class EventList extends ArrayList<IWorkerEvent> {
    void invokeNonceFound(MiningWork i_work, int i_nonce) {
      for (IWorkerEvent i : this) {
        i.onNonceFound(i_work, i_nonce);
      }
    }
  }
  
  class Worker extends Thread implements Runnable {
    MiningWork _work;
    int _start;
    public long number_of_hashed;

    public Worker() {}

    public void setWork(MiningWork i_work, int i_start) {
      _work = i_work;
      _start = i_start;
    }
    private static final int NUMBER_OF_ROUND = 1; // Original: 100
    @Override
    public void run() {
      this.number_of_hashed = 0;
      try {
        int nonce = _start;
        MiningWork work = _work;
        byte[] header = work.header.refHex();
        byte[] target = work.target.refHex();
        Hasher hasher = new Hasher();
        for (;;) {
          for (long i = NUMBER_OF_ROUND - 1; i >= 0; i--) {
            if (hasher.hashCheck(header, target, nonce))
              CpuMiningWorker.this._as_listener.invokeNonceFound(work, nonce);
            nonce += Number_Of_Thread;//steps to prove all number checked
          }
          this.number_of_hashed += NUMBER_OF_ROUND;
          Thread.sleep(1);
        }
      } catch (GeneralSecurityException e) {
        Console.send(4, "Exception on work: " + e.getMessage());
        setChanged();
        notifyObservers(Notification.SYSTEM_ERROR);
        stopWork();
      } catch (InterruptedException ignored) {}
      long curr_time = System.currentTimeMillis();
      double delta_time = Math.max(1, curr_time - _last_time) / 1000.0;
      _speed = ((double) number_of_hashed / delta_time);
      setChanged();
      notifyObservers(Notification.SPEED);
      _last_time = curr_time;
    }
  }

  public CpuMiningWorker(long retry_pause, int priority) {
    _thread_priorirty = priority;
    _retrypause = retry_pause;
    _workr_thread = new Worker[Number_Of_Thread];
    for (int i = Number_Of_Thread; --i >= 0;) {
      _workr_thread[i] = new Worker();
    }
  }

  private long _last_time = 0;
  private long _num_hashed = 0;
  private long _tot_hashed = 0;
  private double _speed = 0;

  @Override
  public boolean doWork(MiningWork i_work) {
    int i;
    if (i_work != null) {
      stopWork();
      long hashes = 0;
      for (i = Number_Of_Thread; --i >= 0;) {
        hashes += _workr_thread[i].number_of_hashed;
      }
      _num_hashed = hashes;
      _tot_hashed += _num_hashed;
      double delta_time = Math.max(1, System.currentTimeMillis() - _last_time) / 1000.0;
      _speed = ((double) _num_hashed / delta_time);
      setChanged();
      notifyObservers(Notification.SPEED);
    }
    _last_time = System.currentTimeMillis();
    for (i = Number_Of_Thread; --i >= 0;) {
      _workr_thread[i].setWork(i_work, i);
      _workr_thread[i].setPriority(_thread_priorirty);
      if (!_workr_thread[i].isAlive()) {
        try {
          _workr_thread[i].start();
        } catch (IllegalThreadStateException e) {
          _workr_thread[i].interrupt();
        }
      }
    }
    return true;
  }

  @Override
  public void stopWork() {
    for (Worker t : _workr_thread) {
      if (t != null) {
        t.interrupt();
      }
    }
  }

  @Override
  public int getProgress() {
    return 0;
  }

  @Override
  public long getNumberOfHash() {
    return _tot_hashed;
  }

  public double get_speed() {
    return _speed;
  }

  public boolean getThreadsStatus() {
    for (Worker t : _workr_thread) {
      if (t != null) {
        if (t.isAlive()) return true;
      }
    }
    return false;
  }

  private EventList _as_listener = new EventList();

  public void addListener(IWorkerEvent i_listener) {
    _as_listener.add(i_listener);
  }
}

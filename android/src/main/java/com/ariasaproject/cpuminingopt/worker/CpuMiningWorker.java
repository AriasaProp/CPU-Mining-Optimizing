package com.ariasaproject.cpuminingopt.worker;

import com.ariasaproject.cpuminingopt.Console;
import com.ariasaproject.cpuminingopt.MiningWork;
import com.ariasaproject.cpuminingopt.hasher.Hasher;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Observable;

public class CpuMiningWorker extends Observable implements IMiningWorker {
  private int _number_of_thread;
  private int _thread_priorirty;
  private Worker[] _workr_thread;
  private long _retrypause;

  private class EventList extends ArrayList<IWorkerEvent> {
    private static final long serialVersionUID = -4176908211058342478L;

    void invokeNonceFound(MiningWork i_work, int i_nonce) {
      Console.send(0, "Nonce found! +" + ((0xffffffffffffffffL) & i_nonce));
      for (IWorkerEvent i : this) {
        i.onNonceFound(i_work, i_nonce);
      }
    }
  }

  private class Worker extends Thread implements Runnable {
    CpuMiningWorker _parent;

    MiningWork _work;
    int _start;
    int _step;
    public long number_of_hashed;

    public Worker(CpuMiningWorker i_parent) {
      this._parent = i_parent;
    }
    
    public void setWork(MiningWork i_work, int i_start, int i_step) {
      this._work = i_work;
      this._start = i_start;
      this._step = i_step;
    }

    private static final int NUMBER_OF_ROUND = 1; // Original: 100
    public volatile boolean running = false;

    @Override
    public void run() {
      running = true;
      this.number_of_hashed = 0;
      try {
        int nonce = this._start;
        MiningWork work = this._work;
        Hasher hasher = new Hasher();
        byte[] target = work.target.refHex();
        while (true) {
          for (long i = NUMBER_OF_ROUND - 1; i >= 0; i--) {
            byte[] hash = hasher.hash(work.header.refHex(), nonce);
            for (int i2 = hash.length - 1; i2 >= 0; i2--) {
              if ((hash[i2] & 0xff) > (target[i2] & 0xff)) {
                break;
              }
              if ((hash[i2] & 0xff) < (target[i2] & 0xff)) {
                this._parent._as_listener.invokeNonceFound(work, nonce);
                break;
              }
            }
            nonce += this._step;
          }
          this.number_of_hashed += NUMBER_OF_ROUND;
          Thread.sleep(10L);
        }
      } catch (GeneralSecurityException e) {
        e.printStackTrace();
        setChanged();
        notifyObservers(Notification.SYSTEM_ERROR);
        stopWork();
      } catch (InterruptedException e) {
        Console.send(0, "Thread killed. #Hashes=" + this.number_of_hashed);
        calcSpeedPerThread(number_of_hashed);
        _last_time = System.currentTimeMillis();
      }
    }
  }
  
  public void calcSpeedPerThread(long numOfHashes) {
    long curr_time = System.currentTimeMillis();
    double delta_time = Math.max(1, curr_time - this._last_time) / 1000.0;
    double speed_calc = ((double) numOfHashes / delta_time);
    _speed = (double) speed_calc;
    setChanged();
    notifyObservers(Notification.SPEED);
  }

  public CpuMiningWorker(int i_number_of_thread, long retry_pause, int priority) {
    _thread_priorirty = priority;
    this._retrypause = retry_pause;
    this._number_of_thread = i_number_of_thread;
    this._workr_thread = new Worker[10];
    for (int i = this._number_of_thread - 1; i >= 0; i--) {
      this._workr_thread[i] = new Worker(this);
    }
  }

  private long _last_time = 0;
  private long _num_hashed = 0;
  private long _tot_hashed = 0;
  private double _speed = 0;

  @Override
  public boolean doWork(MiningWork i_work) {
    if (i_work != null) {
      this.stopWork();
      long hashes = 0;
      for (int i = this._number_of_thread - 1; i >= 0; i--) {
        hashes += this._workr_thread[i].number_of_hashed;
      }
      _num_hashed = hashes;
      _tot_hashed += _num_hashed;
      double delta_time = Math.max(1, System.currentTimeMillis() - this._last_time) / 1000.0;
      _speed = ((double) _num_hashed / delta_time);
      setChanged();
      notifyObservers(Notification.SPEED);
    }
    this._last_time = System.currentTimeMillis();
    for (int i = this._number_of_thread - 1; i >= 0; i--) {
      this._workr_thread[i] = null;
      System.gc();
      this._workr_thread[i] = new Worker(this);
    }
    for (int i = this._number_of_thread - 1; i >= 0; i--) {
      this._workr_thread[i].setWork(i_work, (int) i, this._number_of_thread);
      _workr_thread[i].setPriority(_thread_priorirty);
      if (_workr_thread[i].isAlive() == false) {
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
        Console.send(0, "Worker: Killing thread ID: " + t.getId());
        t.interrupt();
      }
    }
    Console.send(0, "Worker: Threads killed");
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
        if (t.isAlive() == true) return true;
      }
    }
    return false;
  }

  public void ConsoleWrite(String c) {
    Console.send(0, c);
  }

  private EventList _as_listener = new EventList();

  public void addListener(IWorkerEvent i_listener) {
    this._as_listener.add(i_listener);
    return;
  }
}

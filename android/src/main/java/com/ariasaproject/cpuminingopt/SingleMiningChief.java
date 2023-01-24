package com.ariasaproject.cpuminingopt;

import static com.ariasaproject.cpuminingopt.Constants.STATUS_CONNECTING;
import static com.ariasaproject.cpuminingopt.Constants.STATUS_ERROR;
import static com.ariasaproject.cpuminingopt.Constants.STATUS_MINING;
import static com.ariasaproject.cpuminingopt.Constants.STATUS_NOT_MINING;
import static com.ariasaproject.cpuminingopt.Constants.STATUS_TERMINATED;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.ariasaproject.cpuminingopt.connection.IConnectionEvent;
import com.ariasaproject.cpuminingopt.connection.IMiningConnection;
import com.ariasaproject.cpuminingopt.connection.StratumMiningConnection;
import com.ariasaproject.cpuminingopt.worker.CpuMiningWorker;
import com.ariasaproject.cpuminingopt.worker.IMiningWorker;
import com.ariasaproject.cpuminingopt.worker.IWorkerEvent;
import java.util.EventListener;
import java.util.Observable;
import java.util.Observer;

public class SingleMiningChief implements Observer {

  private static final long DEFAULT_SCAN_TIME = 5000;
  private static final long DEFAULT_RETRY_PAUSE = 30000;

  private IMiningConnection mc;
  private IMiningWorker imw;
  private long lastWorkTime;
  private long lastWorkHashes;
  private float speed = 0; // khash/s
  public long accepted = 0;
  public long rejected = 0;
  public int priority = 1;
  private Handler mainHandler;
  public IMiningConnection _connection;
  public IMiningWorker _worker;
  private EventListener _eventlistener;

  public String status = STATUS_NOT_MINING;
  final int MSG_UIUPDATE = 1;
  final int MSG_TERMINATED = 2;
  final int MSG_SPEED_UPDATE = 3;
  final int MSG_STATUS_UPDATE = 4;
  final int MSG_ACCEPTED_UPDATE = 5;
  final int MSG_REJECTED_UPDATE = 6;
  final int MSG_CONSOLE_UPDATE = 7;

  public class EventListener extends Observable implements IConnectionEvent, IWorkerEvent {
    private SingleMiningChief _parent;
    private int _number_of_accept;
    private int _number_of_all;

    EventListener(SingleMiningChief i_parent) {
      this._parent = i_parent;
      this.resetCounter();
    }

    public void resetCounter() {
      this._number_of_accept = this._number_of_all = 0;
    }

    @Override
    public void onNewWork(MiningWork i_work) {
      try {
        Console.send(0, "New work detected!");
        setChanged();
        notifyObservers(IMiningWorker.Notification.NEW_BLOCK_DETECTED);
        setChanged();
        notifyObservers(IMiningWorker.Notification.NEW_WORK);
        synchronized (this) {
          this._parent._worker.doWork(i_work);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onSubmitResult(MiningWork i_listener, int i_nonce, boolean i_result) {
      this._number_of_accept += (i_result ? 1 : 0);
      this._number_of_all++;
      setChanged();
      notifyObservers(
          i_result ? IMiningWorker.Notification.POW_TRUE : IMiningWorker.Notification.POW_FALSE);
    }

    public boolean onDisconnect() {
      return false;
    }

    @Override
    public void onNonceFound(MiningWork i_work, int i_nonce) {
      try {
        this._parent._connection.submitWork(i_work, i_nonce);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public SingleMiningChief(IMiningConnection i_connection, IMiningWorker i_worker, Handler h)
      throws Exception {
    status = STATUS_CONNECTING;
    speed = 0.0f;
    mainHandler = h;
    this._connection = i_connection;
    this._worker = i_worker;
    this._eventlistener = new EventListener(this);
    this._connection.addListener(this._eventlistener);
    this._worker.addListener(this._eventlistener);
  }

  public void startMining() {
    Console.send(0, "Miner: Starting worker thread, priority: " + priority);
    ((StratumMiningConnection) _connection).addObserver(this);
    ((CpuMiningWorker) _worker).addObserver(this);
    MiningWork first_work = this._connection.connect();
    this._eventlistener.resetCounter();
    if (first_work != null) {
      synchronized (this) {
        this._worker.doWork(first_work);
      }
    }
  }

  public void stopMining() {
    Console.send(0, "Miner: Worker stopping...");
    Console.send(0, "Miner: Worker cooling down");
    Console.send(0, "Miner: This can take a few minutes");
    this._connection.disconnect();
    this._worker.stopWork();
    speed = 0;
  }

  public void update(Observable o, Object arg) {
    Message msg = new Message();
    Bundle bundle = new Bundle();
    msg.setData(bundle); // ensure msg has something

    IMiningWorker.Notification n = (IMiningWorker.Notification) arg;
    if (n == IMiningWorker.Notification.SYSTEM_ERROR) {
      Console.send(0, "Miner: System error");
      status = STATUS_ERROR;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.PERMISSION_ERROR) {
      Console.send(0, "Miner: Permission error");
      status = STATUS_ERROR;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.TERMINATED) {
      Console.send(0, "Miner: Worker terminated");
      status = STATUS_TERMINATED;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);

      msg = new Message();
      msg.arg1 = MSG_TERMINATED;
      mainHandler.sendMessage(msg);

      msg = new Message();
      bundle = new Bundle();
      bundle.putFloat("speed", 0);
      msg.arg1 = MSG_SPEED_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.CONNECTING) {
      Console.send(0, "Miner: Worker connecting");
      status = STATUS_CONNECTING;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);

    } else if (n == IMiningWorker.Notification.AUTHENTICATION_ERROR) {
      status = STATUS_ERROR;
      Console.send(0, "Miner: Authentication error");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.CONNECTION_ERROR) {
      status = STATUS_ERROR;
      Console.send(0, "Miner: Connection error");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.COMMUNICATION_ERROR) {
      status = STATUS_ERROR;
      Console.send(0, "Miner: Communication error");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.LONG_POLLING_FAILED) {
      status = STATUS_NOT_MINING;
      Console.send(0, "Miner: Long polling failed");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.LONG_POLLING_ENABLED) {
      status = STATUS_MINING;
      Console.send(0, "Miner: Long polling enabled");
      Console.send(0, "Miner: Speed updates as work is completed");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.NEW_BLOCK_DETECTED) {
      status = STATUS_MINING;
      Console.send(0, "Miner: Detected new block");
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.POW_TRUE) {
      status = STATUS_MINING;
      Console.send(0, "Miner: PROOF OF WORK RESULT: true");
      accepted += 1;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);

      msg = new Message();
      bundle = new Bundle();
      msg.arg1 = MSG_ACCEPTED_UPDATE;
      bundle.putLong("accepted", accepted);
      mainHandler.sendMessage(msg);

    } else if (n == IMiningWorker.Notification.POW_FALSE) {
      status = STATUS_MINING;
      rejected += 1;
      bundle.putString("status", status);
      msg.arg1 = MSG_STATUS_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);

      msg = new Message();
      bundle = new Bundle();
      msg.arg1 = MSG_REJECTED_UPDATE;
      bundle.putLong("rejected", rejected);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.SPEED) {
      if (status.equals(STATUS_TERMINATED) || status.equals(STATUS_NOT_MINING)) {
        speed = 0;
      } else {
        speed = (float) ((CpuMiningWorker) _worker).get_speed();
      }
      bundle.putFloat("speed", speed);
      msg.arg1 = MSG_SPEED_UPDATE;
      msg.setData(bundle);
      mainHandler.sendMessage(msg);
    } else if (n == IMiningWorker.Notification.NEW_WORK) {
      if (lastWorkTime > 0L) {
        long hashes = _worker.getNumberOfHash() - lastWorkHashes;
        speed = (float) ((CpuMiningWorker) _worker).get_speed();
        status = STATUS_MINING;
        Console.send(0, "Miner: " + String.format("%d Hashes, %.6f Hash/s", hashes, speed));
        bundle.putString("status", status);
        msg.arg1 = MSG_STATUS_UPDATE;
        msg.setData(bundle);
        mainHandler.sendMessage(msg);
        msg = new Message();
        bundle = new Bundle();
        bundle.putFloat("speed", speed);
        msg.arg1 = MSG_SPEED_UPDATE;
        msg.setData(bundle);
        mainHandler.sendMessage(msg);
      }
      lastWorkTime = System.currentTimeMillis();
      lastWorkHashes = _worker.getNumberOfHash();
    }
  }
}

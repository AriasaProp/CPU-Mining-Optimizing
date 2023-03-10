package com.ariasaproject.cpuminingopt;

import static com.ariasaproject.cpuminingopt.Constants.MSG_RESULT_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_SPEED_UPDATE;
import static com.ariasaproject.cpuminingopt.Constants.MSG_STATUS_UPDATE;
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
  private final Handler mainHandler;
  public IMiningConnection _connection;
  public IMiningWorker _worker;
  private EventListener _eventlistener;

  public String status = STATUS_NOT_MINING;

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
        _parent._worker.doWork(i_work);
      } catch (Exception e) {
        Console.send(0, "Exception : " + e);
      }
    }

    @Override
    public void onSubmitResult(MiningWork i_listener, int i_nonce, boolean i_result) {
      if (i_result) this._number_of_accept++;
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
        Console.send(4, "Error on Submiting Founded nonce! : " + e.getMessage());
      }
    }
  }

  public SingleMiningChief(IMiningConnection i_connection, IMiningWorker i_worker, Handler h)
      throws Exception {
    status = STATUS_CONNECTING;
    speed = 0.0f;
    mainHandler = h;
    _connection = i_connection;
    _worker = i_worker;
    this._eventlistener = new EventListener(this);
    _connection.addListener(this._eventlistener);
    _worker.addListener(this._eventlistener);
  }

  public void startMining() {
    Console.send(0, "Miner starting worker thread, priority: " + priority);
    ((StratumMiningConnection) _connection).addObserver(this);
    ((CpuMiningWorker) _worker).addObserver(this);
    MiningWork first_work = _connection.connect();
    this._eventlistener.resetCounter();
    if (first_work != null) {
      _worker.doWork(first_work);
    }
  }

  public void stopMining() {
    Console.send(0, "Miner worker on stopping, This can take a few minutes");
    _connection.disconnect();
    _worker.stopWork();
    speed = 0;
  }

  @Override
  public void update(Observable o, Object arg) {
    Message msg = mainHandler.obtainMessage();
    msg.arg1 = 0;
    Bundle bundle = new Bundle();

    if (arg instanceof IMiningWorker.Notification) {
      switch ((IMiningWorker.Notification) arg) {
        case SYSTEM_ERROR:
          Console.send(4, "Miner: System error");
          status = STATUS_ERROR;
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case PERMISSION_ERROR:
          Console.send(4, "Miner: Permission error");
          status = STATUS_ERROR;
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case TERMINATED:
          Console.send(4, "Miner: Worker terminated");
          status = STATUS_TERMINATED;
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          bundle.putFloat("speed", 0);
          msg.arg1 |= MSG_SPEED_UPDATE;
          break;
        case CONNECTING:
          Console.send(1, "Miner: Worker connecting");
          status = STATUS_CONNECTING;
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case AUTHENTICATION_ERROR:
          status = STATUS_ERROR;
          Console.send(4, "Miner: Authentication error");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case CONNECTION_ERROR:
          status = STATUS_ERROR;
          Console.send(4, "Miner: Connection error");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case COMMUNICATION_ERROR:
          status = STATUS_ERROR;
          Console.send(4, "Miner: Communication error");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case LONG_POLLING_FAILED:
          status = STATUS_NOT_MINING;
          Console.send(4, "Miner: Long polling failed");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case LONG_POLLING_ENABLED:
          status = STATUS_MINING;
          Console.send(1, "Miner: Long polling enabled");
          Console.send(1, "Miner: Speed updates as work is completed");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case NEW_BLOCK_DETECTED:
          status = STATUS_MINING;
          Console.send(1, "Miner: Detected new block");
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          break;
        case POW_TRUE:
          status = STATUS_MINING;
          Console.send(2, "Miner: PROOF OF WORK RESULT: true");
          accepted += 1;
          bundle.putString("status", status);
          msg.arg1 |= MSG_STATUS_UPDATE;
          msg.arg1 |= MSG_RESULT_UPDATE;
          bundle.putString("result", accepted + " / " + (accepted + rejected));
          break;
        case POW_FALSE:
          status = STATUS_MINING;
          rejected += 1;
          msg.arg1 |= MSG_STATUS_UPDATE;
          bundle.putString("status", status);
          msg.arg1 |= MSG_RESULT_UPDATE;
          bundle.putString("result", accepted + " / " + (accepted + rejected));
          break;
        case SPEED:
          if (status.equals(STATUS_TERMINATED) || status.equals(STATUS_NOT_MINING)) {
            speed = 0;
          } else {
            speed = (float) ((CpuMiningWorker) _worker).get_speed();
          }
          bundle.putFloat("speed", speed);
          msg.arg1 |= MSG_SPEED_UPDATE;
          break;
        case NEW_WORK:
          if (lastWorkTime > 0L) {
            long hashes = _worker.getNumberOfHash() - lastWorkHashes;
            speed = (float) ((CpuMiningWorker) _worker).get_speed();
            status = STATUS_MINING;
            bundle.putString("status", status);
            msg.arg1 |= MSG_STATUS_UPDATE;
            bundle.putFloat("speed", speed);
            msg.arg1 |= MSG_SPEED_UPDATE;
          }
          lastWorkTime = System.currentTimeMillis();
          lastWorkHashes = _worker.getNumberOfHash();
          break;
        default:
          break;
      }
    }
    msg.setData(bundle);
    mainHandler.sendMessage(msg);
  }
}

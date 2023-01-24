package com.ariasaproject.cpuminingopt.worker;

import com.ariasaproject.cpuminingopt.MiningWork;

public interface IMiningWorker {
  public enum Notification {
    SYSTEM_ERROR,
    PERMISSION_ERROR,
    CONNECTION_ERROR,
    AUTHENTICATION_ERROR,
    COMMUNICATION_ERROR,
    LONG_POLLING_FAILED,
    LONG_POLLING_ENABLED,
    CONNECTING,
    NEW_BLOCK_DETECTED,
    SPEED,
    NEW_WORK,
    POW_TRUE,
    POW_FALSE,
    TERMINATED
  };
  public boolean doWork(MiningWork i_work) throws RuntimeException;
  public void stopWork() throws RuntimeException;
  public int getProgress();
  public long getNumberOfHash();
  public void addListener(IWorkerEvent i_listener) throws RuntimeException;
}

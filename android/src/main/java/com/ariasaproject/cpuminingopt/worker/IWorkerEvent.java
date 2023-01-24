package com.ariasaproject.cpuminingopt.worker;

import com.ariasaproject.cpuminingopt.MiningWork;

public interface IWorkerEvent {
  public void onNonceFound(MiningWork i_work, int i_nonce);
}

package com.ariasaproject.cpuminingopt;

public interface IWorkerEvent {
  public void onNonceFound(MiningWork i_work, int i_nonce);
}

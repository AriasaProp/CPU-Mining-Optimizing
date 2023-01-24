package com.example.android.stratumminer.worker;

import com.example.android.stratumminer.MiningWork;

public interface IWorkerEvent {
  public void onNonceFound(MiningWork i_work, int i_nonce);
}

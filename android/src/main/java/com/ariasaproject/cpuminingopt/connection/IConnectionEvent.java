package com.ariasaproject.cpuminingopt.connection;

import com.ariasaproject.cpuminingopt.MiningWork;

public interface IConnectionEvent {
  public void onNewWork(MiningWork i_new_work);

  public void onSubmitResult(MiningWork i_listener, int i_nonce, boolean i_result);
}

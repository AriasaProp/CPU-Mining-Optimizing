package com.example.android.stratumminer.connection;

public interface IMiningConnection {
  public void addListener(IConnectionEvent i_listener) throws MinyaException;
  public MiningWork connect() throws RuntimeException;
  public void disconnect() throws RuntimeException;
  public MiningWork getWork() throws RuntimeException;
  public void submitWork(MiningWork i_work, int i_nonce) throws MinyaException;
}

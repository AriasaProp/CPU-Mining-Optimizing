package com.ariasaproject.cpuminingopt.connection;

import com.ariasaproject.cpuminingopt.MiningWork;

public interface IMiningConnection {
	public enum Notification {
		SIGNAL_READ,
		SIGNAL_ERROR
	};
	
  public void addListener(IConnectionEvent i_listener) throws RuntimeException;

  public MiningWork connect() throws RuntimeException;

  public void disconnect() throws RuntimeException;

  public MiningWork getWork() throws RuntimeException;

  public void submitWork(MiningWork i_work, int i_nonce) throws RuntimeException;
}

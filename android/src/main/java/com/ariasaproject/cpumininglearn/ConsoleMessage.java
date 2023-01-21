package com.ariasaproject.cpumininglearn;

public interface ConsoleMessage {
  public enum Message {
    DEBUG,
    INFO,
    SUCCESS,
    WARNING,
    ERROR
  };

  public void sendLog(Message lvl, String msg);
}

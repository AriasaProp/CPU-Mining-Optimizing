package com.ariasaproject.cpumininglearn;

import com.ariasaproject.cpumininglearn.Miner;

public class Main {
	
  private static final String DEFAULT_URL = "https://us2.litecoinpool.org:8080/";
  private static final String DEFAULT_AUTH = "Ariasa.1:1";
  private static final long DEFAULT_SCAN_TIME = 5000;
  private static final long DEFAULT_RETRY_PAUSE = 10000;
  
  public static void main(String[] args) {
    System.out.println("Hello World");
    System.out.println("...");
    
    String url = DEFAULT_URL;
    String auth = DEFAULT_AUTH;
    int nThread = Runtime.getRuntime().availableProcessors();
    double throttle = 1.0;
    long scanTime = DEFAULT_SCAN_TIME;
    long retryPause = DEFAULT_RETRY_PAUSE;

    try {
      Miner m = new Miner(url, auth, scanTime, retryPause, nThread, throttle);
      m.join();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    System.out.println("By cruel World! :-)");
  }
}

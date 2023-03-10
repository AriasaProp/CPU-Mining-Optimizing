package com.ariasaproject.cpuminingopt;

public class Constants {

  public static final int MSG_UIUPDATE = 1;
  public static final int MSG_STARTED = 2;
  public static final int MSG_TERMINATED = 4;
  public static final int MSG_SPEED_UPDATE = 8;
  public static final int MSG_STATUS_UPDATE = 16;
  public static final int MSG_RESULT_UPDATE = 32;

  public static final String PREF_URL = "URL";
  public static final String PREF_USER = "USER";
  public static final String PREF_PASS = "PASS";
  public static final String PREF_THROTTLE = "THROTTLE";
  public static final String PREF_SCANTIME = "SCANTIME";
  public static final String PREF_RETRYPAUSE = "RETRYPAUSE";
  public static final String PREF_SERVICE = "SERVICE";
  public static final String PREF_PRIORITY = "PRIORITY";
  public static final String PREF_BACKGROUND = "BACKGROUND";
  public static final String PREF_SCREEN = "SCREEN_AWAKE";

  public static final int DEFAULT_PRIORITY = 1;
  public static final int DEFAULT_THREAD = 1;
  public static final long DEFAULT_SCANTIME = 500;
  public static final long DEFAULT_RETRYPAUSE = 500;
  public static final float DEFAULT_THROTTLE = 1;

  public static final String CLIENT_NAME_STRING = "LTCteMiner";

  public static final String STATUS_NOT_MINING = "Not Mining";
  public static final String STATUS_MINING = "Mining";
  public static final String STATUS_ERROR = "Error";
  public static final String STATUS_TERMINATED = "Terminated";
  public static final String STATUS_CONNECTING = "Connecting";
}

package com.alibaba.csb.sdk;

/**
 * Created by wiseking on 18/6/15.
 */
public class SdkLogger {
  private static boolean DEBUG = Boolean.getBoolean("csb.sdk.DEBUG") || Boolean.getBoolean("http.caller.DEBUG");

  // for performance considering, pls add this check method before invoke print method
  public static boolean isLoggable() {
    return DEBUG;
  }

  public static void print(String msg) {
    if(isLoggable()) {
      System.out.println(msg);
    }
  }
}

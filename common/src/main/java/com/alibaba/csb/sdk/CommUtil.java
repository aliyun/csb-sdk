package com.alibaba.csb.sdk;

import java.io.*;
import java.net.URL;

/**
 * Created by wiseking on 2017/9/4.
 */
public class CommUtil {
  private static final String VER_FILE = "/csb-sdk-version.properties";

  private static String readFileAsText(Reader reader) throws IOException {
    char[] arr = new char[8*1024]; // 8K at a time
    StringBuffer buf = new StringBuffer();
    int numChars;

    while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
      buf.append(arr, 0, numChars);
    }

    return buf.toString();
  }

  public static String geCurrenttVersionFile() throws IOException {
    InputStream in = CommUtil.class.getResourceAsStream(VER_FILE);
    if (in != null) {
      return readFileAsText(new InputStreamReader(in));
    } else {
      return "no verbose version info";
    }
  }
}

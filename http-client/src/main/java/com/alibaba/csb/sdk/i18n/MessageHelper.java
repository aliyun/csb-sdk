package com.alibaba.csb.sdk.i18n;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by wiseking on 18/4/23.
 */
public class MessageHelper {
  private static Locale DEFAULT_LOCALE = new Locale("zh","CN");
  private static ResourceBundle rb = null;

  static {
    rb = ResourceBundle.getBundle("i18n/resource", DEFAULT_LOCALE);
    if(rb == null)
      throw new RuntimeException("BAD: CAN NOT found i18n MessageResource file!!!");
  }

  public static String getMessage(String key, String... params) {
    return getMessage(key, Locale.CHINA, params);
  }

  public static String getMessage(String key, Locale locale, String... params) {
    String ret = new MessageFormat(rb.getString(key),locale).format(params);
//    try {
//      return new String(ret.getBytes("UTF-8"), "GBK");
//    } catch (UnsupportedEncodingException e) {
//      //e.printStackTrace();
//    }
    return ret;
  }
}

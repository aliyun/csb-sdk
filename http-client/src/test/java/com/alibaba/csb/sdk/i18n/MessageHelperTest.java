package com.alibaba.csb.sdk.i18n;


import junit.framework.TestCase;
import org.junit.Test;

import java.text.MessageFormat;

/**
 * Created by wiseking on 18/4/23.
 */
public class MessageHelperTest extends TestCase {
  @Test
  public void testMessageFormat() {
    String[] obj = {"abc"};
    System.out.println(new MessageFormat("wiseking {0} test").format(obj));
  }
}
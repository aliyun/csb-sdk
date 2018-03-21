package com.alibaba.csb.sdk;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * //新的命令行调用方式
 * Created by wiseking on 18/1/9.
 */
public class CmdHttpCallerTest extends TestCase {
  @Test
  public void testCGet() {
    CmdHttpCaller.main(new String[] { "-url", "http://abc:123?a=b&c=abc",
        "-api", "aaa",
        "-version", "1.0.0",
        "-method", "cget",
        "-ak", "ak",
        "-sk", "sk",
        "-H", "name1:abc1",
        "-D", "param1=value1",
        "-nonce"});
  }
}
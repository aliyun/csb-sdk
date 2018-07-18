package com.alibaba.csb.sdk;

import org.junit.Test;

/**
 * Created by wiseking on 18/7/6.
 */
public class HttpClientTest {
  @Test
  public void testLarge() throws HttpCallerException {
    HttpParameters.Builder builder = HttpParameters.newBuilder();
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<40000000; i++) {
      sb.append("a");
    }
    builder.putParamsMap("arg0", sb.toString());
    builder.putParamsMap("inHeader", "abc");
    builder.api("ci-http2ws-echo");
    builder.version("1.0.0");
    builder.method("post");
    builder.requestURL("http://10.101.12.144:8086/CSB?arg1=abc");

    String res = HttpCaller.invoke(builder.build());
    System.out.println( res.substring(0, Math.min(10000, res.length())) );
  }
}

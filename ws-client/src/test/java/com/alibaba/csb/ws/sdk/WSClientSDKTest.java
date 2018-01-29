package com.alibaba.csb.ws.sdk;

import com.alibaba.csb.sdk.CsbSDKConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by wiseking on 18/1/29.
 */
public class WSClientSDKTest {
  @Test
  public void testSignHeaders() {
    WSParams params = WSParams.create();

    params.api("api");
    params.version("1.0.0");
    params.accessKey("ak");
    params.secretKey("sk");
    params.nonce(true);
    params.fingerPrinter("wiseking");
    Map<String, String> headers = WSClientSDK.generateSignHeaders(params);

    System.out.println(headers);
    Assert.assertTrue("must include api", headers.containsKey(CsbSDKConstants.API_NAME_KEY));
  }

  @Test
  public void testSignHeaders2() {
    WSParams params = WSParams.create();

    //params.api("api");
    //params.version("1.0.0");
    //params.nonce(true);
    params.accessKey("ak");
    params.secretKey("sk");
    params.fingerPrinter("wiseking");
    params.timestamp(false);
    Map<String, String> headers = WSClientSDK.generateSignHeaders(params);

    System.out.println(headers);
    Assert.assertTrue("can not include api", !headers.containsKey(CsbSDKConstants.API_NAME_KEY));
  }
}
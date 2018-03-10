package com.alibaba.csb.sdk.http;

import com.alibaba.csb.sdk.ContentBody;
import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.sdk.HttpParameters;
import com.alibaba.csb.sdk.HttpCallerException;
import org.junit.Assert;
import org.junit.Test;

import java.lang.StringBuffer;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个Http SDK编程示例，如何使用HttpCaller向服务端发送POST/GET的请求
 *
 * @author liaotian.wq
 */
public class HttpSDKTest {
  private String url;
  private static final String apiName = "PING";
  private static final String version = "vcsb";
  private static final String versionWS = "vcsb.ws";

  private static final String arg0 = "testa";

  //security related params
  private static final String ak = "ak";
  private static final String sk = "sk";

  @org.junit.Before
  public void prepareUrl() {
    String bhost = System.getProperty("bhost");

    if (bhost == null) {
      Assert.fail("please define the sysetm param bhost, e.g. mvn test -Dbhost=10.125.60.151");
    } else if (bhost.indexOf(":") <= 0) {
      bhost += ":8086";
    }

    url = String.format("http://%s/CSB", bhost);
    System.out.println("invoke broker address=" + url);
  }

  /**
   * for http2http case, backend service is a http/restful service
   *
   * @throws HttpCallerException
   */
  @Test
  public void callWithHttpSDK() throws HttpCallerException {
    System.out.println("testJson request url:" + url);
    System.out.println("apiName:" + apiName);
    System.out.println("ak:" + ak);
    System.out.println("sk:" + sk);
    System.out.println("arg0:" + arg0);

    // Prepare the reuqest params
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", arg0);
    // 使用GET方式调用  你可以把这个请求参数放到请求URL中，即 newUrl = url+"?name="+arg0
    String ret = HttpCaller.doGet(url, apiName, version, params, ak, sk);
    System.out.println("retStr = " + ret);
    Assert.assertTrue("Not correct response", ret != null && ret.startsWith("Hi " + arg0 + ", greeting from CSB broker"));

    params.put("name", arg0);
    //使用POST方式调用
    String ret2 = HttpCaller.doPost(url, apiName, version, params, ak, sk);
    System.out.println("retStr2 = " + ret2);
    System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret)); //返回串的字符集装换
    Assert.assertTrue("Not correct response", ret != null && ret.startsWith("Hi " + arg0 + ", greeting from CSB broker"));
  }

  @Test
  public void invokeWithHttpSDK() throws HttpCallerException {
    System.out.println("testJson request url:" + url);
    System.out.println("apiName:" + apiName);
    System.out.println("ak:" + ak);
    System.out.println("sk:" + sk);
    System.out.println("arg0:" + arg0);

    HttpParameters.Builder builder = HttpParameters.newBuilder();
    builder.requestURL(url) // 设置请求的URL,可以拼接URL请求参数
        .api(apiName) // 设置服务名
        .version(version) // 设置版本号
        .method("post") // 设置调用方式
        .accessKey(ak).secretKey(sk); // 设置accessKey 和 设置secretKey


    // 设置请求参数
    builder.clearParamsMap(); //清空旧的请求参数
    builder.putParamsMap("name", arg0);

    String ret = HttpCaller.invoke(builder.build());
    System.out.println("retStr = " + ret);
    Assert.assertTrue("Not correct response", ret != null && ret.startsWith("Hi " + arg0 + ", greeting from CSB broker"));
  }

  /**
   * for http2ws case, backend service is a webservice service
   *
   * @throws HttpCallerException
   */
  @Test
  public void callWithHttpSDK4HTTP2WS() throws HttpCallerException {
    System.out.println("testJson request url:" + url);
    System.out.println("apiName:" + apiName);
    System.out.println("ak:" + ak);
    System.out.println("sk:" + sk);
    System.out.println("arg0:" + arg0);

    // Prepare the reuqest params
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", arg0); // 普通的串对象

    String ret = HttpCaller.doGet(url, apiName, versionWS, params, ak, sk);
    System.out.println("retStr = " + ret);

    System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret));
  }

  //-------- 其他调用参考

  @Test
  public void httpContentBody() throws HttpCallerException {
    String ct = "{\"name\":\"wiseking\"}";
    HttpParameters.Builder builder = HttpParameters.newBuilder();
    builder.requestURL(url+"?name=test") // 设置请求的URL,可以拼接URL请求参数
        .api(apiName) // 设置服务名
        .version(version) // 设置版本号
        .method("post") // 设置调用方式, 如果使用ContentBody对象，必须为 post
        .accessKey(ak).secretKey(sk); // 设置accessKey 和 设置secretKey

    //测试1 以json string的方式ContentBody发送请求到restful接入端口
    builder.contentBody(new ContentBody(ct));  //注意： 这个json串会被接入的restful服务的 @RequestBody String ct 参数所处理
    String ret = HttpCaller.invoke(builder.build(), null);
    System.out.println("retStr = " + ret);

    //测试2 以byte[]的方式ContentBody发送请求到restful接入端口
    String ctFile = HttpSDKTest.class.getResource("/").getFile() + "ct.txt";
    System.out.println("------ctFile="+ctFile);
    byte[] bct = HttpCaller.readFileAsByteArray(ctFile); //将文件的内容以装换为二进制数组
    builder.contentBody(new ContentBody(bct));  //注意： 这个json串会被接入的restful服务的 @RequestBody byte[] ct 参数所处理
    StringBuffer sb = new StringBuffer();
    ret = HttpCaller.invoke(builder.build(), sb);
    System.out.println("retStr = " + ret);
    System.out.println("response http headers = " + sb.toString());
  }

}

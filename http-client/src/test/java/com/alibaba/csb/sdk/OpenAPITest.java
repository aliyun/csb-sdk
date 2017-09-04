package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.SortedParamList;
import org.junit.Test;

/**
 * Created by wiseking on 2017/9/4.
 */
public class OpenAPITest {
  public static final String ak = "REPLACE-AK";
  public static final String sk = "REPLACE-SK";
  @Test
  public void testListCsb()  {
    try {
    HttpParameters.Builder hp = HttpParameters.newBuilder();
    hp.api("/api/csbinstance/listCsbs").requestURL("http://lcsb.daily.taobao.net:8080/api/csbinstance/listCsbs");
    hp.version("1.1.0.0").accessKey(ak).secretKey(sk);
    hp.method("get");
    String ret = null;
      ret = HttpCaller.invoke(hp.build());
    System.out.println("ret="+ret);
    } catch (HttpCallerException e) {
      e.printStackTrace();
    }
  }


  @Test
  public void testAddProject()  {
    try{
    String data = "{\"description\":\"openapi test\",\"projectName\":\"lt-wiseking\"}";
    HttpParameters.Builder hp = HttpParameters.newBuilder();
    hp.api("/api/project/createorupdate").requestURL("http://lcsb.daily.taobao.net:8080/api/project/createorupdate?csbId=175");
    hp.version("1.1.0.0").accessKey(ak).secretKey(sk);
    hp.method("post");
    hp.putParamsMap("data", data);
    String ret = HttpCaller.invoke(hp.build());
    System.out.println("ret="+ret);
  } catch (HttpCallerException e) {
    e.printStackTrace();
  }
  }
}

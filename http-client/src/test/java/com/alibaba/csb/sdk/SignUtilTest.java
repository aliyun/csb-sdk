package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.SignUtil;
import org.junit.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;


/**
 * Created by wiseking on 2017/10/13.
 */
public class SignUtilTest {
  @Test
  public void getSignature() {
    String sk = "Bzw2YO0HBXFMpcd9CN8tzeNrmf0=" ;
    HashMap<String,List<String>>  params = new HashMap<String,List<String>>();
    params.put("name", Arrays.asList("wiseking"));
    params.put("_api_timestamp", Arrays.asList("1507884348630"));
    params.put("_api_name", Arrays.asList("testa"));
    params.put("_api_version", Arrays.asList("1.0.0"));
    params.put("_api_access_key", Arrays.asList("4f196fb61c1f46ffbf71691ffad35dbb"));
    String res = SignUtil.signMultiValueParams(params, sk);
    System.out.println("res="+res);
  }
}

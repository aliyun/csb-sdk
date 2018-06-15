package com.alibaba.csb.sdk.security;

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.security.spi.SignService;

import java.util.*;

/**
 * Singleton default impl for SignService
 * Created by wiseking on 18/6/15.
 */
public class DefaultSignServiceImpl implements SignService {
  private static final Random random = new Random(System.currentTimeMillis());
  private static boolean DEBUG = Boolean.getBoolean("csb.sdk.DEBUG") || Boolean.getBoolean("http.caller.DEBUG");

  private static DefaultSignServiceImpl singleton = new DefaultSignServiceImpl();

  private DefaultSignServiceImpl() {}

  public static DefaultSignServiceImpl getInstance(){
    return singleton;
  }

  @Override
  public Map<String, String> signParamsMap(Map<String, List<String>> paramsMap,
                                           String apiName, String version,
                                           String accessKey, String securityKey,
                                           boolean timestampFlag, boolean nonceFlag, Map<String, String> extSignHeaders) {
    Map<String, List<String>> newParamsMap = new HashMap<String, List<String>>();
    Map<String, String> headerParamsMap = new HashMap<String, String>();

    if (paramsMap != null) {
      newParamsMap.putAll(paramsMap);
    }

    // put apiName
    if (apiName != null) {
      newParamsMap.put(CsbSDKConstants.API_NAME_KEY, Arrays.asList(apiName));
      headerParamsMap.put(CsbSDKConstants.API_NAME_KEY, apiName);
    }
    // put version
    if (version != null) {
      newParamsMap.put(CsbSDKConstants.VERSION_KEY, Arrays.asList(version));
      headerParamsMap.put(CsbSDKConstants.VERSION_KEY, version);
    }

    // put timestamp
    String timestampStr = System.getProperty("timestamp");
    if(timestampStr == null) {
      Long ts = System.currentTimeMillis();
      timestampStr = ts.toString();
    }

    if (nonceFlag && CsbSDKConstants.isNonceEnabled) {
      // put nonce
      String nonceStr = System.getProperty("nonce");
      if (nonceStr == null) {
        Long nonce = random.nextLong();
        nonceStr = nonce.toString();
      }
      newParamsMap.put(CsbSDKConstants.NONCE_KEY, Arrays.asList(nonceStr));
      headerParamsMap.put(CsbSDKConstants.NONCE_KEY, nonceStr);
    }


    if (timestampFlag) {
      newParamsMap.put(CsbSDKConstants.TIMESTAMP_KEY, Arrays.asList(timestampStr));
      headerParamsMap.put(CsbSDKConstants.TIMESTAMP_KEY, timestampStr);
    }

    if (extSignHeaders != null) {
      for(Map.Entry<String,String> kv:extSignHeaders.entrySet()) {
        newParamsMap.put(kv.getKey(), Arrays.asList(kv.getValue()));
        headerParamsMap.put(kv.getKey(), kv.getValue());
      }
    }

    // last step, put accessKey & the generated signature
    if (accessKey != null) {
      headerParamsMap.put(CsbSDKConstants.ACCESS_KEY, accessKey);
      newParamsMap.put(CsbSDKConstants.ACCESS_KEY, Arrays.asList(accessKey));
      // ensure the signature and security are not sent !!
      newParamsMap.remove(CsbSDKConstants.SIGNATURE_KEY);
      newParamsMap.remove(CsbSDKConstants.SECRET_KEY);
      long currT = System.currentTimeMillis();
      String signKey = signMultiValueParams(newParamsMap, securityKey);
      if (DEBUG) {
        System.out.println("sign parameters:");
        boolean first = true;
        for (String key:newParamsMap.keySet()) {
          if (!first) {
            System.out.print(",");
          }
          System.out.print(String.format("%s=%s",key, newParamsMap.get(key)));
          first = false;

        }
        System.out.println("\nsignature:" + signKey +
            "\ncosts time =" + (System.currentTimeMillis() - currT) + "ms");
      }
      headerParamsMap.put(CsbSDKConstants.SIGNATURE_KEY, signKey);
    }

    return headerParamsMap;
  }
  /**
   * convert parameter to Signature requried ParamNode format
   * @param map
   * @return
   */
  private List<ParamNode> convertSingleValueParms(Map<String, String> map) {
    List<ParamNode> pnList = new ArrayList<ParamNode>();

    String key;
    for(Map.Entry<String, String> entry : map.entrySet()) {
      key = entry.getKey();
      ParamNode node = new ParamNode(key, entry.getValue());
      pnList.add(node);
    }

    return pnList;
  }

  /**
   * convert parameter to Signature requried ParamNode format
   * @param map
   * @return
   */
  private List<ParamNode> convertMultiValueParams(Map<String, List<String>> map) {
    List<ParamNode> pnList = new ArrayList<ParamNode>();

    String key;
    for(Map.Entry<String, List<String>> entry : map.entrySet()) {
      key = entry.getKey();
      List<String> vlist = entry.getValue();
      if (vlist == null) {
        ParamNode node = new ParamNode(key, null);
        pnList.add(node);
      } else {
        for (String v:vlist) {
          ParamNode node = new ParamNode(key, v);
          pnList.add(node);
        }
      };
    }

    return pnList;
  }

  /**
   * Signature single value parameter list with security key
   * @param paramsMap
   * @param secretKey
   * @return
   */
  public String sign(Map<String, String> paramsMap, String secretKey)  {
    List<ParamNode> paramNodeList = convertSingleValueParms(paramsMap);
    return sign(paramNodeList, secretKey);
  }

  /**
   * Signature multiple values parameter list with security key
   * @param newParamsMap
   * @param secretKey
   * @return
   */
  public String signMultiValueParams(Map<String, List<String>> newParamsMap,String secretKey)  {
    List<ParamNode> paramNodeList = convertMultiValueParams(newParamsMap);
    return sign(paramNodeList, secretKey);
  }


  private String sign(List<ParamNode> paramNodeList,String secretKey)
  {
    if (paramNodeList==null) {
      paramNodeList = new ArrayList<ParamNode>();
    }

    return SpasSigner.sign(paramNodeList, secretKey);
  }
}

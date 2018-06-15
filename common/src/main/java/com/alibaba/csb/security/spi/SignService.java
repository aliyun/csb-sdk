package com.alibaba.csb.security.spi;

import java.util.List;
import java.util.Map;

/**
 * SPI interface, provide different signature implementations.
 *
 * Created on 18/6/15.
 */
public interface SignService {
  /**
   * 将制定的参数记性签名处理
   * @param paramsMap      待签名的请求参数key=values键值对
   * @param apiName        CSB服务名
   * @param version        CSB服务版本
   * @param accessKey      accessKey, 在后端认证系统(如:DAuth), 通过accessKey获取进行签名的securityKey
   * @param securityKey    securityKey 进行签名的安全吗
   * @param timestampFlag  是否当前系统的时间戳参与签名
   * @param nonceFlag      是否防重放随机数参与签名
   * @param extSignHeaders 附加的参与签名的key=value键值对
   * @return 将生成的签名及一些关键字段以key=value的方式返回
   */
  Map<String, String> signParamsMap(Map<String, List<String>> paramsMap, String apiName, String version,
                                   String accessKey, String securityKey,
                                   boolean timestampFlag, boolean nonceFlag,
                                   Map<String, String> extSignHeaders);


}

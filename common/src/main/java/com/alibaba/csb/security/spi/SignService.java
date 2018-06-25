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
   * @param securityKey    securityKey 进行签名的安全码
   */
  String generateSignature(final Map<String, List<String>> paramsMap, final String securityKey);

}

package com.alibaba.csb.security.spi;

import com.alibaba.csb.sdk.security.SortedParamList;

/**
 * SPI interface, provide different signature implementations.
 * <p>
 * Created on 18/6/15.
 */
public interface SignService {
    /**
     * 将制定的参数记性签名处理
     *
     * @param paramNodeList 待签名的请求参数key=values键值对
     * @param accessKey     进行签名的凭证识别码
     * @param secretKey     进行签名的安全码
     */
    String generateSignature(final SortedParamList paramNodeList, final String accessKey, final String secretKey);
}

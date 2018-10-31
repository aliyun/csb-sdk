package com.alibaba.csb.security.spi;

import com.alibaba.csb.sdk.security.ParamNode;

import java.util.List;

/**
 * SPI interface, provide different verify signature implementations.
 */
public interface VerifySignService {
    /**
     * 将制定的参数进行验签
     *
     * @param paramNodeList 已签名的请求参数key=values键值对列表，不包含签名 _api_signature 的key值。
     * @param accessKey     进行签名的凭证识别码
     * @param signature     签名串
     */
    boolean verifySignature(final List<ParamNode> paramNodeList, final String accessKey, final String signature);

}

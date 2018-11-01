package com.alibaba.csb.security.spi;

import com.alibaba.csb.sdk.security.SortedParamList;

/**
 * CSB服务端请求验签接口
 */
public interface VerifySignService {

    /**
     * 对客户端请求参数进行验签处理
     *
     * @param paramNodeList 已签名的请求参数key=values键值对列表，不包含签名 _api_signature 的键值对。
     * @param accessKey     进行签名的凭证识别码
     * @param signature     签名串
     * @return 验签是否成功
     */
    boolean verifySignature(final SortedParamList paramNodeList, final String accessKey, final String signature);

}

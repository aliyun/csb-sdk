package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;

/**
 * 客户端签名示例服务
 * Created by wiseking on 18/6/15.
 */
public class SampleSignImpl implements SignService {

    /**
     * 客户端请求参数进行签名处理
     *
     * @param paramNodeList 已排序的待签名请求参数key=values键值对列表
     * @param accessKey     进行签名的凭证识别码
     * @param secretKey     进行签名的安全码
     * @return 客户端签名串
     */
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey) {
        String reqStr = paramNodeList.toString();
        System.out.println("SampleSignImpl签名，请求参数列表串：" + reqStr);
        return accessKey.substring(0, 1); //模拟签名算法
    }

    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey, SpasSigner.SigningAlgorithm algorithm) {
        return generateSignature(paramNodeList, accessKey, secretKey);
    }
}

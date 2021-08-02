package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;

/**
 * Singleton default impl for SignService
 * Created by wiseking on 18/6/15.
 */
public class DefaultSignServiceImpl implements SignService {
    private static DefaultSignServiceImpl singleton = new DefaultSignServiceImpl();

    protected DefaultSignServiceImpl() {
    }

    public static DefaultSignServiceImpl getInstance() {
        return singleton;
    }

    @Override
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey) {
        return SpasSigner.sign(paramNodeList, secretKey);
    }

    @Override
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey, SpasSigner.SigningAlgorithm algorithm) {
        return SpasSigner.sign(paramNodeList, secretKey, algorithm);
    }
}

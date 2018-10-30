package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;

import java.util.*;

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
    public String generateSignature(List<ParamNode> paramNodeList, final String accessKey, final String secretKey) {
        return SpasSigner.sign(paramNodeList, secretKey);
    }

}

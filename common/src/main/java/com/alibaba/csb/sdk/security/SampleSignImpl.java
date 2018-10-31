package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;

/**
 * 客户端签名示例服务
 * Created by wiseking on 18/6/15.
 */
public class SampleSignImpl implements SignService {
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey) {
        return paramNodeList.toString().substring(0, 10);
    }

}

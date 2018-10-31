package com.alibaba.csb.sdk.security;

import com.alibaba.csb.security.spi.SignService;

/**
 * 客户端签名示例服务
 * Created by wiseking on 18/6/15.
 */
public class SampleSignImpl implements SignService {
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey) {
        String reqStr = paramNodeList.toString();
        System.out.println("请求参数列表：" + reqStr);
        return reqStr.substring(0, 10);
    }

}

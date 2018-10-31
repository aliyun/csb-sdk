package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.SortedParamList;
import com.alibaba.csb.security.spi.SignService;
import com.alibaba.fastjson.JSON;

/**
 * 客户端签名示例服务
 * Created by wiseking on 18/6/15.
 */
public class SampleSignImpl implements SignService {
    public String generateSignature(SortedParamList paramNodeList, final String accessKey, final String secretKey) {
        String reqStr = JSON.toJSONString(paramNodeList);
        System.out.println("请求：\n" + reqStr);
        return reqStr.substring(0, 10);
    }

}

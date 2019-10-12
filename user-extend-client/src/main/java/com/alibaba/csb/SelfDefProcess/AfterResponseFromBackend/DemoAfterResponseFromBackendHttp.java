package com.alibaba.csb.SelfDefProcess.AfterResponseFromBackend;

import com.alibaba.csb.SelfDefProcess.SelfDefProcessException;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by tingbin.ctb
 * 2019/9/24-11:33.
 */
public class DemoAfterResponseFromBackendHttp implements AfterResponseFromBackendHttp {
    public Object process(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoAfterResponseFromBackendHttp.process contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(RESPONSE_HEADERS);
        headers.put("addRspHeader", "rspheader1");

        Object body = contextMap.get(RESPONSE_BODY);
        if (body instanceof String) { //json和其它文本
            body += " + response_bbb"; //设置新的响应结果文本
        } else if (body instanceof InputStream) {
            ;
        }
        return body;
    }
}

package com.alibaba.csb.SelfDefProcess.AfterResponseFromBackendHttp;

import com.alibaba.csb.SelfDefProcess.SelfDefProcessException;

import java.util.Map;

/**
 * Created by tingbin.ctb
 * 2019/9/24-11:33.
 */
public class DemoAfterResponseFromBackendHttp implements AfterResposeFromBackendHttp {
    public void process(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoAfterResponseFromBackendHttp.process contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(RESPONSE_HEADERS);
        headers.put("addTestHeader", "abc#@!");
    }
}

package com.alibaba.csb.SelfDefProcess.BeforeSend2Backend;

import com.alibaba.csb.SelfDefProcess.SelfDefProcessException;

import java.util.Map;

/**
 * Created by tingbin.ctb
 * 2019/9/24-11:33.
 */
public class DemoBeforeSend2BackendHttp implements BeforeSend2BackendHttp {
    public void process(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoBeforeSend2BackendHttp.process contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(REQUEST_HEADERS);
        headers.put("addTestHeader", "abc#@!");
    }
}

package com.alibaba.csb.selfDefProcess.BeforeSend2Backend;

import com.alibaba.csb.selfDefProcess.SelfDefProcessException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by tingbin.ctb
 * 2019/9/24-11:33.
 */
public class DemoBeforeSend2BackendHttp implements BeforeSend2BackendHttp {
    public Object process(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoBeforeSend2BackendHttp.process contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(REQUEST_HEADERS);
        headers.put("addReqHeader", "reqHeader1");//增加http请求头

        Map<String, List<String>> querys = (Map<String, List<String>>) contextMap.get(REQUEST_HTTP_QUERYS);
        querys.put("query1", Arrays.asList("queryValue1"));

        Object body = contextMap.get(REQUEST_BODY);
        if (body instanceof Map) { //form表单提交的请求
            ((Map) body).put("field1", Arrays.asList("value1"));
        } else if (body instanceof String) { //json和其它文本
            body += " + aaa";  //设置新的请求文本
        } else if (body instanceof InputStream) {
            ;
        }
        return body;
    }
}

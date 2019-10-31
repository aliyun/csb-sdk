package com.alibaba.csb.selfDefProcess.udfmp;

import com.alibaba.csb.selfDefProcess.SelfDefProcessException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoMessageProcessInterceptor implements ServerMessageProcessInterceptor {

    /**
     * 请求消息处理:收到客户请求后调用
     *
     * @return
     */
    public void requestProcess(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoMessageProcessInterceptor.requestProcess contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(REQUEST_HEADERS);
        headers.put("addReqHeader", "reqHeader1");//增加http请求头

        Map<String, List<String>> querys = (Map<String, List<String>>) contextMap.get(REQUEST_HTTP_QUERYS);
        querys.put("query1", Arrays.asList("queryValue1")); //修改http query

        contextMap.put(SELF_CONTEXT_PREFIX + "Obj1", "self1");//保存自定义上下文

        if ("true".equals(headers.get("mockFlag"))) {
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("mockResponse", "true");
            contextMap.put(RESPONSE_HEADERS,responseHeaders);//设置返回结果httpheaders
            contextMap.put(RESPONSE_BODY, "模拟响应结果");//直接返回模拟结果。
        } else {
            Object body = contextMap.get(REQUEST_BODY);
            if (body instanceof Map) { //form表单提交的请求
                ((Map) body).put("field1", Arrays.asList("value1"));
            } else if (body instanceof String) { //json和其它文本
                body += " + aaa";  //设置新的请求文本
            }
            contextMap.put(REQUEST_BODY, body);
        }
    }

    /**
     * 响应消息处理:向客户发送响应结果之前调用
     *
     * @return
     */
    public void responseProcess(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoMessageProcessInterceptor.responseProcess contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(RESPONSE_HEADERS);
        headers.put("addRspHeader", "rspheader1");//增加http响应头

        Object body = contextMap.get(RESPONSE_BODY);
        if (body instanceof String) { //json和其它文本
            body += " + response_bbb"; //设置新的响应结果文本
        } else if (body instanceof InputStream) {
            ;
        }
        contextMap.put(RESPONSE_BODY, body);
    }
}

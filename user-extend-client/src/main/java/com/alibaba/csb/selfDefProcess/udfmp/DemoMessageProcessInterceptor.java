package com.alibaba.csb.selfDefProcess.udfmp;

import com.alibaba.csb.selfDefProcess.SelfDefProcessException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class DemoMessageProcessInterceptor implements ServerMessageProcessInterceptor {

    /**
     * 请求消息处理:收到客户请求后调用
     *
     * @return
     */
    public Object requestProcess(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoMessageProcessInterceptor.requestProcess contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(REQUEST_HEADERS);
        headers.put("addReqHeader", "reqHeader1");

        contextMap.put(SELF_CONTEXT_PREFIX + "Obj1", "self1");//保存自定义上下文

        Object body = contextMap.get(REQUEST_BODY);
        if (body instanceof Map) { //form表单提交的请求
            ((Map) body).put("field1", Arrays.asList("value1"));
        } else if (body instanceof String) { //json和其它文本
            body += " + aaa";  //设置新的请求文本
        }
        return body;
    }

    /**
     * 响应消息处理:向客户发送响应结果之前调用
     *
     * @return
     */
    public Object responseProcess(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoMessageProcessInterceptor.responseProcess contextMap: " + contextMap);
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

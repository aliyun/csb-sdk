package com.alibaba.csb.sdk;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Http Return 对象 包含调用的返回结果，并且包含一些诊断相关的信息包括：
 * <pre>
 *
 *
 * 1. response                调用的返回值
 * 2. responseHeaders         返回的http headers，其中key值_inner_ecsb_broker_ip指向调用的broker的IP地址
 * 3. responseHttpStatus      返回的http状态
 *
 * 4. diagnosticInfo { // Map类型 包括如下key信息：
 *       requestHeaders          请求headers
 *       signParams              参与签名的字段
 *       startTime               调用的起始时间
 *       endTime                 调用的结束时间
 *       totalInvokeTime         本地调用的用时(ms)
 *       requestSize             本次请求的大小
 *       responseSize            本次响应的大小
 *
 * }
 *
 * </pre>
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * @since 2018
 */
public class HttpReturn {
    public int httpCode;
    public String responseHttpStatus;
    /**
     * 请使用 responseEntity
     */
    @Deprecated
    @Setter
    private String response;
    @Getter
    @Setter
    private HttpEntity responseEntity;
    /**
     * 请使用 respHttpHeaderMap
     */
    @Deprecated
    public String responseHeaders;
    @Getter
    @Setter
    private Map<String, String> respHttpHeaderMap;
    public Map<String, String> diagnosticInfo = new HashMap<String, String>(); //定义成Map类型，方便增减新的诊断项
    public boolean diagnosticFlag;

    public HttpReturn() {
    }

    public HttpReturn(String response) {
        this.response = response;
    }

    public String getResponseString() throws HttpCallerException {
        try {
            return EntityUtils.toString(responseEntity);
        } catch (IOException e) {
            throw new HttpCallerException(e.getMessage(), e);
        }
    }

    public byte[] getResponseBytes() throws HttpCallerException {
        try {
            return EntityUtils.toByteArray(responseEntity);
        } catch (IOException e) {
            throw new HttpCallerException(e.getMessage(), e);
        }
    }

    public String getResponse() throws HttpCallerException {
        if (response != null) {
            return response;
        } else if (responseEntity != null) {
            return getResponseString();
        }

        return null;
    }
}

package com.alibaba.csb.sdk;

import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;

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
    public String response;
    public byte[] responseBytes;
    /**
     * 请使用 respHttpHeaderMap
     */
    @Deprecated
    public String responseHeaders;
    public Map<String, String> respHttpHeaderMap;
    public Map<String, String> diagnosticInfo = new HashMap<String, String>(); //定义成Map类型，方便增减新的诊断项
    public boolean diagnosticFlag;

    public HttpReturn() {
    }

    public HttpReturn(String response) {
        this.response = response;
    }

    /**
     * 不管响应类型是文本还是二进制，始终转换为string输出
     */
    public String getResponseStr() {
        if (response != null) {
            return response;
        } else if (responseBytes != null) {
            try {
                String charset = HTTP.UTF_8;//没有返回contentType，则默认是utf-8，以便兼容历史http2ws无返回contentType的场景。
                if (respHttpHeaderMap != null) {
                    String contentTypeStr = respHttpHeaderMap.get(HTTP.CONTENT_TYPE);
                    if (contentTypeStr != null && contentTypeStr.equals("") == false) {
                        ContentType contentType = ContentType.parse(contentTypeStr);
                        if (contentType != null && contentType.getCharset() != null) {
                            charset = contentType.getCharset().name();
                        }
                    }
                }
                return new String(responseBytes, charset);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }

}

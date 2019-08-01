package com.alibaba.csb.sdk.internel;

import com.alibaba.csb.sdk.ContentBody;
import com.alibaba.csb.sdk.HttpReturn;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;

import java.util.List;
import java.util.Map;

/**
 * Created by wiseking on 18/6/20.
 */
public class DiagnosticHelper {
    public static final String DIAGNOSTIC_REQUEST_HEADERS = "requestHeaders";      //请求headers
    public static final String DIAGNOSTIC_SIGN_PARAMS = "signParams";              //参与签名的字段
    public static final String DIAGNOSTIC_START_TIME = "startTime";               //调用的起始时间
    public static final String DIAGNOSTIC_END_TIME = "endTime";                 //调用的结束时间
    public static final String DIAGNOSTIC_INVOKE_TIME = "totalInvokeTime";         //本地调用的用时(ms)
    public static final String DIAGNOSTIC_REQUEST_SIZE = "requestSize";             //本次请求的大小
    public static final String DIAGNOSTIC_RESPONSE_SIZE = "responseSize";            //本次响应的大小
    public static final String DIAGNOSTIC_BROKER_IP = "brokerIp";                //处理本次请求的BrokerIP地址

    public static void calcResponseSize(HttpReturn ret) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }

        long size = 0;
        if (ret.response != null) {
            ret.diagnosticInfo.put(DIAGNOSTIC_RESPONSE_SIZE, String.valueOf(ret.response.length()));
        }
    }

    public static void calcRequestSize(HttpReturn ret, String requestURL, Map<String, List<String>> paramsMap, ContentBody cb) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }

        long size = 0;
        if (requestURL != null) {
            size += requestURL.length();
        }

        if (paramsMap != null) {
            for (Map.Entry<String, List<String>> kv : paramsMap.entrySet()) {
                if (kv.getKey() != null) {
                    size += kv.getKey().length();
                }
                for (String value : kv.getValue()) {
                    if (value != null) {
                        size += value.length();
                    }
                }
            }
        }

        if (cb != null) {
            if (cb.getContentType().equals(ContentType.APPLICATION_OCTET_STREAM)) {
                size += cb.getBytesContentBody().length;
            } else {
                size += cb.getStrContentBody().length();
            }
        }

        ret.diagnosticInfo.put(DIAGNOSTIC_REQUEST_SIZE, String.valueOf(size));
    }

    public static void setStartTime(HttpReturn ret, long startT) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }
        ret.diagnosticInfo.put(DIAGNOSTIC_START_TIME, String.valueOf(startT));
    }

    public static void setInvokeTime(HttpReturn ret, long invokeTime) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }
        ret.diagnosticInfo.put(DIAGNOSTIC_INVOKE_TIME, String.valueOf(invokeTime));
    }

    public static void setEndTime(HttpReturn ret, long endTime) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }
        ret.diagnosticInfo.put(DIAGNOSTIC_END_TIME, String.valueOf(endTime));
    }

    public static void setRequestHeaders(HttpReturn ret, Header[] allHeaders) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }

        if (allHeaders != null && allHeaders.length > 0) {
            StringBuffer sb = new StringBuffer();
            for (Header h : allHeaders) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(h.getName()).append(":").append(h.getValue());
            }
            ret.diagnosticInfo.put(DIAGNOSTIC_REQUEST_HEADERS, sb.toString());
        }
    }


    public static void setSignDiagnosticInfo(HttpReturn ret, StringBuffer signDiagnosticInfo) {
        if (ret == null || !ret.diagnosticFlag) {
            return;
        }

        if (signDiagnosticInfo != null) {
            ret.diagnosticInfo.put(DIAGNOSTIC_SIGN_PARAMS, signDiagnosticInfo.toString());
        }
    }

    public static StringBuffer getSignDiagnosticInfo(HttpReturn ret) {
        if (ret == null || !ret.diagnosticFlag) {
            return null;
        }

        return new StringBuffer();
    }
}

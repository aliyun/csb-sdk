package com.alibaba.csb.trace;

import com.alibaba.csb.sdk.CsbSDKConstants;

public class TraceData {
    public static final String TRACEID_KEY = CsbSDKConstants.TRACEID_KEY;
    public static final String RPCID_KEY = CsbSDKConstants.RPCID_KEY;
    public static final String REQUESTID_KEY = CsbSDKConstants.REQUESTID_KEY;
    public static final String RPCID_DEFAULT = "0";

    private String traceId;
    private String rpcId;
    private String bizId;
    private String requestId;
    private Strategy strategy;

    public TraceData(Strategy strategy, String traceId, String rpcId) {
        this.strategy = strategy;
        this.traceId = traceId;
        this.rpcId = rpcId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRpcId() {
        return rpcId;
    }

    public void setRpcId(String rpcId) {
        this.rpcId = rpcId;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Strategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "TraceData{" +
                "strategy=" + strategy +
                ",traceId='" + traceId + '\'' +
                ", rpcId='" + rpcId + '\'' +
                ", bizId='" + bizId + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }

    public enum Strategy {
        Request, Filter, EagleEye
    }
}
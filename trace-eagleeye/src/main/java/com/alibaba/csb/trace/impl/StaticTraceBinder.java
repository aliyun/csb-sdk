package com.alibaba.csb.trace.impl;

import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.trace.TraceBinder;
import com.alibaba.csb.trace.TraceContext;
import com.alibaba.csb.trace.TraceData;
import com.taobao.eagleeye.EagleEye;

/**
 *
 */
public class StaticTraceBinder implements TraceBinder {
    private static final TraceBinder SINGLETON = new StaticTraceBinder();

    public static TraceBinder getSingleton() {
        return SINGLETON;
    }

    public TraceData getTraceData() {
        TraceData traceData = new TraceData(TraceData.Strategy.EagleEye, EagleEye.getTraceId(), EagleEye.getRpcId());
        String bizId = EagleEye.getUserData(HttpCaller.bizIdKey());
        if (bizId != null) {
            traceData.setBizId(bizId);
        } else {
            traceData.setBizId(TraceContext.getBizId());
        }
        return traceData;
    }
}

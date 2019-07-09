package com.alibaba.csb.trace.impl;

import com.alibaba.csb.trace.TraceBinder;
import com.alibaba.csb.trace.TraceContext;
import com.alibaba.csb.trace.TraceData;

public class DefaultTraceBinder implements TraceBinder {
    private static final TraceBinder SINGLETON = new DefaultTraceBinder();

    public static TraceBinder getSingleton() {
        return SINGLETON;
    }

    public TraceData getTraceData() {
        if (TraceContext.getContext() == null) {
            return null;
        }
        TraceData traceData = new TraceData(TraceData.Strategy.Filter, TraceContext.getTraceId(), TraceContext.getRpcId());
        traceData.setBizId(TraceContext.getBizId());
        return traceData;
    }
}
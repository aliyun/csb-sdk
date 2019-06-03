package com.alibaba.csb.trace;

public class TraceContext {
    private static final ThreadLocal<TraceData> threadLocal = new ThreadLocal<TraceData>();

    public static void startTrace(TraceData traceData) {
        threadLocal.set(traceData);
    }

    public static void endTrace() {
        threadLocal.set(null);
    }

    public static TraceData getContext() {
        return threadLocal.get();
    }


    public static String getTraceId() {
        TraceData traceData = getContext();
        if (traceData == null) {
            return null;
        }
        return traceData.getTraceId();
    }

    public static String getRpcId() {
        TraceData traceData = getContext();
        if (traceData == null) {
            return null;
        }
        return traceData.getRpcId();
    }

    public static String getBizId() {
        TraceData traceData = getContext();
        if (traceData == null) {
            return null;
        }
        return traceData.getBizId();
    }
}

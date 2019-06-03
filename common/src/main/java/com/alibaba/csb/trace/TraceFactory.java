package com.alibaba.csb.trace;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

public class TraceFactory {
    private static final String DEFAULT_TRACE_BINDER_CLASS = "com.alibaba.csb.trace.impl.DefaultTraceBinder";
    private static final String TRACE_BINDER_CLASS = "com.alibaba.csb.trace.impl.StaticTraceBinder";
    private static final String TRACE_BINDER_PATH = TRACE_BINDER_CLASS.replace('.', '/') + ".class";

    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;
    static volatile int INITIALIZATION_STATE = UNINITIALIZED;
    static volatile Class<?> traceBinderClass = null;

    static Set<URL> findPossibleStaticTraceBinderPathSet() {
        Set<URL> binderPaths = new LinkedHashSet<URL>();
        try {
            ClassLoader classLoader = TraceFactory.class.getClassLoader();
            Enumeration<URL> paths;
            if (classLoader == null) {
                paths = ClassLoader.getSystemResources(TRACE_BINDER_PATH);
            } else {
                paths = classLoader.getResources(TRACE_BINDER_PATH);
            }
            while (paths.hasMoreElements()) {
                URL path = paths.nextElement();
                binderPaths.add(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return binderPaths;
    }

    public static TraceData getTraceData() {
        if (INITIALIZATION_STATE == UNINITIALIZED) {
            synchronized (TraceFactory.class) {
                if (INITIALIZATION_STATE == UNINITIALIZED) {
                    INITIALIZATION_STATE = ONGOING_INITIALIZATION;
                    performInitialization();
                }
            }
        }
        try {
            TraceBinder traceBinder = (TraceBinder) traceBinderClass.getMethod("getSingleton").invoke(traceBinderClass);
            return traceBinder.getTraceData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void performInitialization() {
        Set<URL> traceBinderPathSet = findPossibleStaticTraceBinderPathSet();
        if (traceBinderPathSet.size() > 1) {
            throw new RuntimeException(String.format("csb trace error, traceBinderPath:%s", traceBinderPathSet));
        }
        String traceBinder = traceBinderPathSet.isEmpty() ? DEFAULT_TRACE_BINDER_CLASS : TRACE_BINDER_CLASS;
        try {
            traceBinderClass = Class.forName(traceBinder);
            traceBinderClass.getMethod("getSingleton").invoke(traceBinderClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        INITIALIZATION_STATE = SUCCESSFUL_INITIALIZATION;
    }

}

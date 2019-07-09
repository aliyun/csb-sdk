package com.alibaba.csb.trace;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csb.sdk.HttpCaller;

public class TraceFilter implements Filter {
    public TraceFilter() {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = httpRequest.getHeader(TraceData.TRACEID_KEY);
        String rpcId = httpRequest.getHeader(TraceData.RPCID_KEY);
        String bizId = httpRequest.getHeader(HttpCaller.bizIdKey());
        String requestId = httpRequest.getHeader(TraceData.REQUESTID_KEY);

        TraceData traceData = new TraceData(TraceData.Strategy.Filter, traceId, rpcId);
        traceData.setBizId(bizId);
        traceData.setRequestId(requestId);
        TraceContext.startTrace(traceData);
        try {
            chain.doFilter(request, httpResponse);
        } finally {
            TraceContext.endTrace();
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }
}


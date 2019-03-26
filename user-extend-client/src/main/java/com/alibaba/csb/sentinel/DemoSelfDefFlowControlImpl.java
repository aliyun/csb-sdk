package com.alibaba.csb.sentinel;

import java.util.Map;

public class DemoSelfDefFlowControlImpl implements SelfDefFlowControl {

    public void process(Map<String, Object> contextMap) throws LimitExceedException {
        System.out.println("自定义流控逻辑" + contextMap.toString());
        throw new LimitExceedException("自定义流控限制当前请求: " + contextMap.get(TRACE_ID));
    }
}

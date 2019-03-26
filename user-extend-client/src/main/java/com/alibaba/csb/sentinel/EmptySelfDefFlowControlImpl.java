package com.alibaba.csb.sentinel;

import java.util.Arrays;

public class EmptySelfDefFlowControlImpl implements SelfDefFlowControl {

    @Override
    public void process(String traceId, String csbInstanceName, String csbBrokerIp, String serviceName, String serviceVersioin, String serviceGroupName,
                        String clientUserId, String clientCredentialName, String clientAK) throws LimitExceedException {
        System.out.println("自定义流控逻辑" + Arrays.toString(new String[]{traceId, csbInstanceName, csbBrokerIp, serviceName, serviceVersioin, serviceGroupName,
                clientUserId, clientCredentialName, clientAK}));
        throw new LimitExceedException("自定义流控限制当前请求:" + traceId);
    }
}

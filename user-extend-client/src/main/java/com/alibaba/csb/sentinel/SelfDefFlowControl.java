package com.alibaba.csb.sentinel;

import java.util.Map;

/**
 * 用户自定义流控接口
 * 通过SPI方式加载，用户将符合 SelfDefFlowControl 接口定义的类打jar包放到broker的 patchLib 目录下。
 * 要求jar 内有 META-INF/service/com/alibaba/csb/sentinel/SelfDefFlowControl 文件，且文件内容为“用户自定义流控实现类完整名”
 * <p>
 * Created by tingbin.ctb
 * 2019/3/25-18:03.
 */
public interface SelfDefFlowControl {
    static final String TRACE_ID = "_inner_ecsb_trace_id"; //CSB服务请求唯一标识
    static final String CSB_INTERNAL_NAME = "_csb_internal_name_"; // CSB实例名
    static final String CSB_BROKER_IP = "_csb_broker_ip"; //CSB Broker节点的IP
    static final String API_NAME = "_api_name"; //CSB服务名
    static final String API_VERION = "_api_version"; // CSB服务版本号
    static final String API_GROUP = "_api_group"; // CSB服务所属服务组名
    static final String USER_ID = "userId"; //服务访问者用户Id
    static final String CREDENTIAL_NAME = "credentail_name"; //服务访问者凭证名
    static final String ACCESS_KEY = "_api_access_key";  //服务访问者的ak
    static final String REMOTE_REAL_IP = "_remote_real_ip";  //服务访问者IP

    /**
     * 服务请求上下文信息map，各信息的key见上述常量定义
     *
     * @throws LimitExceedException 如果流控异常，则终止服务后续处理流程，将异常信息返回给CSB客户端
     */
    void process(Map<String, Object> contextMap) throws LimitExceedException;
}

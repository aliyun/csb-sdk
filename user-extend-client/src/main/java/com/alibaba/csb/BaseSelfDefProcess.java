package com.alibaba.csb;

/**
 * 用户自定义流控接口
 * 通过SPI方式加载，用户将符合 SelfDefFlowControl 接口定义的类打jar包放到broker的 patchLib 目录下。
 * 要求jar 内有 META-INF/services/com/alibaba/csb/sentinel/SelfDefFlowControl 文件，且文件内容为“用户自定义流控实现类完整名”
 * <p>
 * Created by tingbin.ctb
 * 2019/3/25-18:03.
 */
public interface BaseSelfDefProcess {
    /**
     * CSB服务请求唯一标识
     */
    String TRACE_ID = "_inner_ecsb_trace_id";
    /**
     * CSB实例名
     */
    String CSB_INTERNAL_NAME = "_csb_internal_name_";
    /**
     * CSB Broker节点的IP
     */
    String CSB_BROKER_IP = "_csb_broker_ip";
    /**
     * CSB服务名
     */
    String API_NAME = "_api_name";
    /**
     * CSB服务版本号
     */
    String API_VERION = "_api_version";
    /**
     * CSB服务所属服务组名
     */
    String API_GROUP = "_api_group";
    /**
     * 服务访问者用户Id
     */
    String USER_ID = "userId";
    /**
     * 服务访问者凭证名
     */
    String CREDENTIAL_NAME = "credentail_name";
    /**
     * 服务访问者的ak
     */
    String ACCESS_KEY = "_api_access_key";
    /**
     * 服务访问者IP
     */
    String REMOTE_PEER_IP = "_remote_peer_ip";
    /**
     * 后端业务服务提供者IP
     */
    String BACKEND_REAL_IP = "_remote_real_ip";

    /**
     * 后端业务服务的http地址
     */
    String BACKEND_URL = "backend_url";
    /**
     * 请求后端业务服务的http方法：POST、GET等
     */
    String BACKEND_METHOD = "backend_method";
    /**
     * 请求后端业务服务的http头
     */
    String REQUEST_HEADERS = "request_headers";
    /**
     * 后端业务服务响应的http头
     */
    String RESPONSE_HEADERS = "response_headers";
    /**
     * 请求后端业务服务的http体：byte[]
     */
    String REQUEST_BODY = "request_body";
    /**
     * 后端业务服务的响应http体：byte[]
     */
    String RESPONSE_BODY = "response_body";
}

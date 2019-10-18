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
     * 请求业务id : String
     */
    String BIZ_ID = "_biz_id";

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
     * 服务拥有者用户Id
     */
    String API_OWNER_ID = "_api_owner_id";

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
     * 请求后端业务服务的http query：map《String,List《String》》
     */
    String REQUEST_HTTP_QUERYS = "request_http_querys";
    /**
     * 请求后端业务服务的http头map《String,String》
     */
    String REQUEST_HEADERS = "request_headers";
    /**
     * 后端业务服务响应的http头map《String,String》
     */
    String RESPONSE_HEADERS = "response_headers";
    /**
     * 请求后端业务服务的http体：Object。
     * 1. 如果请求是form表单，则是Map《String，List《String》》对象。
     * 2. 如果请求是非form的文本请求，则是String对象。
     * 3. 其它请求，则是InputStream或byte[]对象
     */
    String REQUEST_BODY = "request_body";
    /**
     * 后端业务服务的响应http体：Object
     * 1. 如果响应是文本，则是String对象。
     * 3. 其它请求，则是InputStream或byte[]对象
     */
    String RESPONSE_BODY = "response_body";

    /**
     * 开放协议
     */
    String SERVER_PROTOCO = "server_protoco";
    /**
     * 后端服务协议
     */
    String BACKEND_PROTOCO = "backend_Protoco";

    /**
     * 响应结果异常：后端业务服务返回的异常，或csb处理响应结果时产生的异常。可能为空
     */
    String RESPONSE_EXCEPTION = "response_exception";

    /**
     * 用户自定义上下文数据前缀，在请求消息自定义扩展时，用户可以向 contextMap里put数据，以便在后续该请求的自定义处理逻辑里使用。
     */
    String SELF_CONTEXT_PREFIX = "_self_";
}

package com.alibaba.csb.selfDefProcess.BeforeSend2Backend;

import com.alibaba.csb.BaseSelfDefProcess;
import com.alibaba.csb.selfDefProcess.SelfDefProcessException;

import java.util.Map;

/**
 * csb broker发送请求给后端http业务服务前，执行此逻辑。
 * Created by tingbin.ctb
 * 2019/9/25-18:03.
 */
public interface BeforeSend2BackendHttp extends BaseSelfDefProcess {
    /**
     * 自定义处理逻辑，用户可以：
     * <ul>
     * <li>  增加、修改、删除：请求头</li>
     * <li>  增加、修改、删除：query参数</li>
     * <li>  修改：通过 contextMap.put(RESPONSE_BODY,body)，达到修改body的目标。如果是form请求，则直接body是map《String，List《String》》。如果是非form的文本请求，则body是String。其它请求，则是InputStream或byte[]对象</li>
     * <li>  抛出异常，以中止服务处理，异常消息将直接返回给CSB客户端</li>
     * </ul>
     *
     * @param contextMap 服务请求上下文信息map，各信息的key见 BaseSelfDefProcess 常量定义:
     *                   <ul>
     *                   <li> _inner_ecsb_trace_id {@link com.alibaba.csb.BaseSelfDefProcess#TRACE_ID}</li>
     *                   <li> _csb_internal_name_  {@link com.alibaba.csb.BaseSelfDefProcess#CSB_INTERNAL_NAME}</li>
     *                   <li>_csb_broker_ip  {@link com.alibaba.csb.BaseSelfDefProcess#CSB_BROKER_IP}</li>
     *                   <li>_api_name  {@link com.alibaba.csb.BaseSelfDefProcess#API_NAME}</li>
     *                   <li>_api_version  {@link com.alibaba.csb.BaseSelfDefProcess#API_VERION}</li>
     *                   <li>_api_group  {@link com.alibaba.csb.BaseSelfDefProcess#API_GROUP}</li>
     *                   <li>userId  {@link com.alibaba.csb.BaseSelfDefProcess#USER_ID}</li>
     *                   <li>credentail_name  {@link com.alibaba.csb.BaseSelfDefProcess#CREDENTIAL_NAME}</li>
     *                   <li>_api_access_key  {@link com.alibaba.csb.BaseSelfDefProcess#ACCESS_KEY}</li>
     *                   <li>_remote_peer_ip  {@link com.alibaba.csb.BaseSelfDefProcess#REMOTE_PEER_IP}</li>
     *                   <li>_remote_real_ip  {@link com.alibaba.csb.BaseSelfDefProcess#BACKEND_REAL_IP}</li>
     *                   <li>backend_url  {@link com.alibaba.csb.BaseSelfDefProcess#BACKEND_URL}</li>
     *                   <li>backend_method  {@link com.alibaba.csb.BaseSelfDefProcess#BACKEND_METHOD}</li>
     *                   <li>request_http_querys {@link com.alibaba.csb.BaseSelfDefProcess#REQUEST_HTTP_QUERYS}</li>
     *                   <li>request_headers  {@link com.alibaba.csb.BaseSelfDefProcess#REQUEST_HEADERS}</li>
     *                   <li>request_body  {@link com.alibaba.csb.BaseSelfDefProcess#REQUEST_BODY}</li>
     *                   </ul>
     * @throws SelfDefProcessException
     */
    void process(final Map<String, Object> contextMap) throws SelfDefProcessException;
}

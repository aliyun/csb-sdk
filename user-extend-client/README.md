# CSB用户自定义扩展说明
## 自定义流控
### 功能描述
CSB在流控处理逻辑中，自动调用用户自定义的流控逻辑，用户根据服务名、请求者accessKey等信息判断是否允许当前请求继续执行。

例如用户可以基于此扩展功能机制实现按天的流量配额等功能。

### 扩展接口定义
```java
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
    static final String REMOTE_PEER_IP = "_remote_peer_ip";  //服务访问者IP

    /**
     * 服务请求上下文信息map，各信息的key见上述常量定义
     *
     * @throws LimitExceedException 如果流控异常，则终止服务后续处理流程，将异常信息返回给CSB客户端
     */
    void process(Map<String, Object> contextMap) throws LimitExceedException;
}
```
用户的自定义扩展java代码实现此接口即可。

如果用户希望终止当前请求，则抛出 `LimitExceedException`异常即可。此时CSB客户端将收到的响应结果为“`LimitExceedException的异常信息描述`”。
### 使用说明
本扩展功能基于Java SPI规范实现：
* [引用接口包 user-extend-client.1.1.6.0.jar](http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/user-extend-client-1.1.6.0.jar) 
* 实现`com.alibaba.csb.sentinel.SelfDefFlowControl`的 `process` 方法。
* 在用户jar包的classpath路径下定义`META-INF/services/com.alibaba.csb.sentinel.SelfDefFlowControl`文件，文件内容如下：
```text
#用户自定义流控扩展逻辑Java实现类全名，示例如下
com.alibaba.csb.sentinel.DemoSelfDefFlowControlImpl
```
* 用户将扩展逻辑打成jar包，上传到CSB Broker的Docker内`/home/admin/cloud-gateway/patchlib`目录内。
* 重启docker实例。
### demo示例
```java
public class DemoSelfDefFlowControlImpl implements SelfDefFlowControl {

    public void process(Map<String, Object> contextMap) throws LimitExceedException {
        System.out.println("自定义流控逻辑" + contextMap.toString());
        throw new LimitExceedException("自定义流控限制当前请求: " + contextMap.get(TRACE_ID));
    }
}
```

## 转发请求给后端业务服务前的自定义处理
### 功能描述
在CSB broker转发请求给后端业务服务前，自动调用用户自定义的处理逻辑。用户可根据CSB实例名、CSB服务名、CSB凭证、后端业务服务地址、请求头、请求体等信息进行逻辑处理：
1. 修改、增加、删除请求头。
2. 抛出异常，以便中止服务处理，不再转发请求给后端业务服务。

### 条件与约束
当前仅支持后端业务服务是HTTP/HTTPS的服务。

### 扩展接口定义
```java
public interface BeforeSend2BackendHttp extends BaseSelfDefProcess {
    /**
     * 自定义处理逻辑，用户可以：
     * <ul>
     * <li>  增加、修改、删除：请求头</li>
     * <li>  抛出异常，以中止服务处理，异常消息将直接返回给CSB客户端</li>
     * </ul>
     *
     * @param contextMap 服务请求上下文信息map，各信息的key见 BaseSelfDefProcess 常量定义:
     *                   <ul>
     *                   <li> _inner_ecsb_trace_id CSB服务请求唯一标识</li>
     *                   <li> _csb_internal_name_  CSB实例名</li>
     *                   <li>_csb_broker_ip  CSB Broker节点的IP</li>
     *                   <li>_api_name  CSB服务名</li>
     *                   <li>_api_version  CSB服务版本号</li>
     *                   <li>_api_group  CSB服务所属服务组名</li>
     *                   <li>userId  服务访问者用户Id</li>
     *                   <li>credentail_name  服务访问者凭证名</li>
     *                   <li>_api_access_key  服务访问者的ak</li>
     *                   <li>_remote_peer_ip  服务访问者IP</li>
     *                   <li>backend_url  后端业务服务的http地址</li>
     *                   <li>backend_method  请求后端业务服务的http方法：POST、GET等</li>
     *                   <li>request_headers  请求后端业务服务的http头</li>
     *                   <li>request_body  请求后端业务服务的http体：byte[]</li>
     *                   </ul>
     * @throws SelfDefProcessException
     */
    void process(final Map<String, Object> contextMap) throws SelfDefProcessException;
}

```

### 使用说明
本扩展功能基于Java SPI规范实现：
* [引用接口包 user-extend-client.1.1.6.0.jar](http://middleware-udp.oss-cn-beijing.aliyuncs.com/components/csb/CSB-SDK/user-extend-client-1.1.6.0.jar) 
* 实现`com.alibaba.csb.SelfDefProcess.BeforeSend2Backend.BeforeSend2BackendHttp`的 `process` 方法。
* 在用户jar包的classpath路径下定义`META-INF/services/com.alibaba.csb.SelfDefProcess.BeforeSend2Backend.BeforeSend2BackendHttp`文件，文件内容如下：
```text
#用户自定义扩展逻辑Java实现类全名
com.abc.csb.BeforeSend2BackendHttpClass
```
* 用户将扩展逻辑打成jar包，上传到CSB Broker的Docker内`/home/admin/cloud-gateway/patchlib`目录内。
* 重启docker实例。
### demo示例
```java
public class DemoBeforeSend2BackendHttp implements BeforeSend2BackendHttp {
    public void process(Map<String, Object> contextMap) throws SelfDefProcessException {
        System.out.println("DemoBeforeSend2BackendHttp.process contextMap: " + contextMap);
        Map<String, String> headers = (Map<String, String>) contextMap.get(REQUEST_HEADERS);
        headers.put("addTestHeader", "abc#@!");
    }
}
```
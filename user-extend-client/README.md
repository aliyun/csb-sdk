# CSB用户自定义扩展说明
目前仅提供“**自定义流控**”扩展。
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
    static final String REMOTE_REAL_IP = "_remote_real_ip";  //服务访问者IP

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
* 引用接口包
```xml
<dependency>
    <groupId>com.alibaba.csb.sdk</groupId>
    <artifactId>user-extend-client</artifactId>
    <version>1.1.2.0-SNAPSHOT</version>
</dependency>
```
* 实现`com.alibaba.csb.sentinel.SelfDefFlowControl`的 `process` 方法。
* 在用户jar包的classpath路径下定义`META-INF/service/com.alibaba.csb.sentinel.SelfDefFlowControl`文件，文件内容如下：
```text
#用户自定义流控扩展逻辑Java实现类全名，示例如下
com.alibaba.csb.sentinel.DemoSelfDefFlowControlImpl
```
* 用户将扩展逻辑打成jar包，上传到CSB Broker的Docker内`/home/admin/cloud-gateway/patchlib`目录内。
* 重启docker实例。

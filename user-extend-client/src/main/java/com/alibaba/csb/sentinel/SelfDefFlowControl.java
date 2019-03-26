package com.alibaba.csb.sentinel;

/**
 * 用户自定义流控接口
 * 通过SPI方式加载，用户将符合 SelfDefFlowControl 接口定义的类打jar包放到broker的 patchLib 目录下。
 * 要求jar 内有 META-INF/service/com/alibaba/csb/sentinel/SelfDefFlowControl 文件，且文件内容为“用户自定义流控实现类完整名”
 * <p>
 * Created by tingbin.ctb
 * 2019/3/25-18:03.
 */
public interface SelfDefFlowControl {

    /**
     * @param traceId              CSB服务请求唯一标识
     * @param csbInstanceName      CSB实例名
     * @param csbBrokerIp          CSB Broker节点的IP
     * @param serviceName          CSB服务名
     * @param serviceVersioin      CSB服务版本号
     * @param serviceGroupName     CSB服务所属服务组名
     * @param clientUserId         服务访问者用户Id
     * @param clientCredentialName 服务访问者凭证名
     * @param clientAK             服务访问者的ak
     * @throws LimitExceedException 如果流控异常，则终止服务后续处理流程，将异常信息返回给CSB客户端
     */
    void process(String traceId, String csbInstanceName, String csbBrokerIp, String serviceName, String serviceVersioin, String serviceGroupName,
                 String clientUserId, String clientCredentialName, String clientAK) throws LimitExceedException;
}

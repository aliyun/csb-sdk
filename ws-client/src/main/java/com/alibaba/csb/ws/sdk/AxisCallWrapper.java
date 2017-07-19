package com.alibaba.csb.ws.sdk;

import javax.xml.soap.MimeHeaders;

/**
 * <pre>
 * Axis客户端Call的wrapper类， 用以在发送soap请求前，将CSB所要求的签名信息存放到http header里， 具体用法：
 * 
 *   //设置服务调用的安全信息
 *   String apiName = "PING";  //要调用的服务名称
 *   String apiVersion = "vcsb.ws";  //要调用的服务版本
 *   String ak = "xxxx"; //订购服务的accessKey
 *   String sk = "xxxx"; //订购服务的secrectKey
 *   
 *   Service service = new Service();
 *   // 首先，构造封装Call对象
 *   Call call = AxisCallWrapper.createCallWrapper(service, ak, sk, apiName, apiVersion); 
 *   
 *   // 然后，使用封装Call对象进行方法调用
 *   call.setTargetEndpointAddress("http://localhost:9081/PING/vcsb.ws/ws2ws");
 *   call.setOperationName(new QName("http://hc.wsprocess.csb.alibaba.com/", "ping"));
 *   
 *   call.addParameter("arg0", // 设置要传递的参数
 *   	org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
 *   
 *   Object[] args = { "wiseking" };
 *  
 *   Object ret = call.invoke(args);
 *   System.out.println("ret=" + ret);
 * 
 * 注意，如果要正确使用这个类，需要在你的编译和运行环境中包含axis依赖， 如：
 * 
 * &lt;dependency>
 *		&lt;groupId>axis&lt;/groupId>
 *		&lt;artifactId>axis&lt;/artifactId>
 *		&lt;version>1.4&lt;/version>
 *	&lt;/dependency>
 *
 *	&lt;dependency>
 *		&lt;groupId>org.apache.axis&lt;/groupId>
 *		&lt;artifactId>axis-jaxrpc&lt;/artifactId>
 *		&lt;version>1.4&lt;/version>
 *	&lt;/dependency>
 * </pre>
 * 
 * @author liaotian.wq 2017年7月19日
 *
 */
public class AxisCallWrapper extends org.apache.axis.client.Call {
	private String ak;
	private String sk;
	private String apiName;
	private String apiVersion;
	private String fingerStr;

	public static org.apache.axis.client.Call createCallWrapper(org.apache.axis.client.Service service, String ak,
			String sk, String api, String apiVersion) {
		AxisCallWrapper call = new AxisCallWrapper(service);
		call.ak = ak;
		call.sk = sk;
		call.apiName = api;
		call.apiVersion = apiVersion;
		//TODO: fingerStr 可以根据自己的安全需要进行动态生成
		call.fingerStr = "axisCaller";

		return call;
	}

	private AxisCallWrapper(org.apache.axis.client.Service service) {
		super(service);
	}

	@Override
	public void setRequestMessage(org.apache.axis.Message msg) {
		super.setRequestMessage(msg);

		//每次调用时，将安全相关的信息放到soap请求的http header里
		MimeHeaders mimeHeaders = msg.getMimeHeaders();
		WSClientSDK.addHttpHeaders(mimeHeaders, ak, sk, apiName, apiVersion, fingerStr, System.currentTimeMillis());
	}
}
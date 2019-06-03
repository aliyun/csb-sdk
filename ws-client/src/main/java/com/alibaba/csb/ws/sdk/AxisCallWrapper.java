package com.alibaba.csb.ws.sdk;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Map;
import javax.xml.soap.MimeHeaders;

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.utils.IPUtils;
import com.alibaba.csb.utils.LogUtils;
import com.alibaba.csb.utils.TraceIdUtils;

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
	private WSParams params;

	/**
	 *
	 * @param service
	 * @param ak
	 * @param sk
	 * @param api
	 * @param apiVersion
	 * @return
	 *
	 * @deprecated use WSParams as the requet parameter
	 */
	public static org.apache.axis.client.Call createCallWrapper(org.apache.axis.client.Service service, String ak,
			String sk, String api, String apiVersion) {
		AxisCallWrapper call = new AxisCallWrapper(service);
		call.params = WSParams.create();
		call.params.api(api);
		call.params.version(apiVersion);
		call.params.accessKey(ak);
		call.params.secretKey(sk);
		call.params.timestamp(true);
		call.params.fingerPrinter("axisCaller");

		return call;
	}

	public static org.apache.axis.client.Call createCallWrapper(org.apache.axis.client.Service service, WSParams params) {
		AxisCallWrapper call = new AxisCallWrapper(service);
		call.params = params;
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
		addHttpHeaders(mimeHeaders, params);
	}

	private boolean addHttpHeaders(MimeHeaders mimeHeaders, WSParams params) {
		if(params.getTraceId()==null){
			params.traceId(TraceIdUtils.generate());
		}
		mimeHeaders.addHeader(CsbSDKConstants.TRACEID_KEY, params.getTraceId());
		if (params.getRpcId() == null) {
			params.rpcId(TraceData.RPCID_DEFAULT);
		}
		mimeHeaders.addHeader(CsbSDKConstants.RPCID_KEY, params.getRpcId());
		mimeHeaders.addHeader(WSClientSDK.bizIdKey(), params.getBizId());
		mimeHeaders.addHeader(CsbSDKConstants.REQUESTID_KEY, params.getRequestId());
		if (mimeHeaders != null) {
			Map<String, String> headers =WSClientSDK.generateSignHeaders(params);
			for(Map.Entry<String,String> kv:headers.entrySet()) {
				mimeHeaders.addHeader(kv.getKey(), kv.getValue());
			}
			return true;
		}

		return false;
	}

	@Override
	public Object invoke(Object[] params) throws RemoteException {
		long startTime = System.currentTimeMillis();
		int code = 200;
		String endpoint = super.getTargetEndpointAddress();
		String operation = super.getOperationName().getLocalPart();
		String msg = null;
		try {
			Object result = super.invoke(params);
			return result;
		} catch (RemoteException e) {
			code = 500;
			msg = e.getMessage();
			throw e;
		} finally {
			log(startTime, endpoint, operation, code, msg);
		}
	}

	private void log(long startTime, String endpoint, String operation, int code, String msg) {
		long endTime = System.currentTimeMillis();
		try {
			int qidx = endpoint.indexOf("?");
			String url = qidx > -1 ? endpoint.substring(0, qidx) : endpoint;

			int cidx = url.indexOf(":");
			int pidx = url.indexOf(":", cidx + 2);
			if (pidx < 0) {
				pidx = url.indexOf("/", cidx);
			}
			String dest = url.substring(cidx + 3, pidx);

			String method = operation.substring(operation.indexOf("}") + 1);
			LogUtils.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", new Object[]{startTime, endTime, endTime - startTime
					, "WS", IPUtils.getLocalHostIP(), dest
					, params.getBizId(), params.getRequestId()
					, params.getTraceId(), params.getRpcId()
					, params.getApi(), params.getVersion()
					, defaultValue(params.getAk()), defaultValue(params.getSk()), method
					, url, code, "", defaultValue(msg)});
		} catch (Throwable e) {
			LogUtils.exception(MessageFormat.format("csb invoke error, api:{0}, version:{1}", params.getApi(), defaultValue(params.getSk())), e);
		}
	}

	private String defaultValue(String val) {
		return val == null ? "" : val.trim();
	}
}
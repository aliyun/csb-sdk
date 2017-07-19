package com.alibaba.csb.ws.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.soap.MimeHeaders;
import javax.xml.ws.BindingProvider;

import com.alibaba.csb.sdk.security.SignUtil;
import com.alibaba.csb.ws.sdk.internal.BindingDynamicProxyHandler;
import com.alibaba.csb.ws.sdk.internal.SOAPHeaderHandler;

/**
 * Bind accessKey, secretKey into the WS client caller (i.e. Dispath, Proxy)
 * 
 * <p>
 * The tool adds the security headers (as http headers) internally when sending a soap
 * request, the headers include the following:
 * 
 * <pre>
 * {@code
 * _api_name:api_name
 * _api_version:api_version
 * _api_access_key:abc   //the access key 
 * _api_timestamp:1464603102715  //the current timestamp of invoker machine
 * _api_fingerprint:invoke   // the invocation web service method 
 * _api_signature:FLHVBMMjaxMPUHy+2VCGPvRNcao= //the signature string calculated with ak, timestamp, fingerprint and sk 
 *}
 * </pre>
 * 
 * <pre>
 * {@code
 * Usage:
 * 
 *   //obtain dispatch or proxy
 *   Dispatch patch = ...
 *   
 *   
 *   //bind the dispatch with ak/sk
 *   String ak = "xxxxx";
 *   String sk = "xxxxx";
 *   String apiName = "xxxxx";
 *   String apiVersion = "xxxxx";
 *   dispatch = WSClientSDK.bind(dispatch, ak, sk, apiName, apiVersion);
 * 
 *   //invoke the methods with the returned dispatch
 *   ret = dispatch.invoke(...);
 * 
 * }
 * </pre>
 * 
 * @author Alibaba Middleware CSB Team 
 * @author liaotian.wq
 * @since  2016
 * 
 * 
 */
public class WSClientSDK {
	private static boolean warmupFlag = false;
	private static final String BOUND_HANDLER_KEY = "__DynamicProxyHandler";
	public static final boolean PRINT_SIGNINFO = Boolean.getBoolean("WSClientSDK.print.signinfo");
	
	/**
	 * 加载HttpSDK所需要的类 (如，签名相关的)
	 * 
	 * 注意：这是一个高时间代价的启动方法，建议在整个JVM范围内，在使用HttpCaller调用具体的服务前，调用且只调用一次
	 */
	public static synchronized void warmup() {
		if (warmupFlag) {
			return; 
		}
		SignUtil.sign(new HashMap<String,String>(), "sk");
		warmupFlag = true;
	}
	
	/**
	 * 给proxy/dispath 绑定ak/sk安全对
	 * 
	 * @param proxy        客户端proxy或者dispatch
	 * @param accessKey    accessKey
	 * @param secretKey    secretKey
	 * @return             封装了accessKey和secretKey的proxy或者dispath, 调用逻辑要使用这个返回进行WS方法调用
	 * @throws WSClientException  
	 * @deprecated 使用bind（proxy, accesskey, secretKey, apiName, apiVersion）
	 */
	public static <T> T bind(T proxy, String accessKey, String secretKey) throws WSClientException {
		return bind(proxy, accessKey, secretKey, null, null);
	}
	
	public static <T> T bind(T proxy, String accessKey, String secretKey, String apiName, String apiVersion, boolean printHeaders) throws WSClientException {
		validateProxy(proxy);
		
		BindingDynamicProxyHandler handler = getHandler((BindingProvider)proxy);
		handler.setASK(accessKey, secretKey, apiName, apiVersion, printHeaders);
		return handler.bind(proxy);
	}
	
	/**
	 * 给proxy/dispath 绑定ak/sk安全对，及要调用的apiName和apiVersion
	 * 
	 * @param proxy        客户端proxy或者dispatch
	 * @param accessKey    accessKey
	 * @param secretKey    secretKey
	 * @param apiName      服务名
	 * @param apiVersion   服务版本
	 * @return             封装了accessKey和secretKey的proxy或者dispath, 调用逻辑要使用这个返回进行WS方法调用
	 * @throws WSClientException  
	 */
	public static <T> T bind(T proxy, String accessKey, String secretKey, String apiName, String apiVersion) throws WSClientException {
		return bind(proxy, accessKey, secretKey, apiName, apiVersion, false);
	}
	
	/**
	 * 设置直接从服务端返回预定义的MockResponse 
	 * @param proxy    客户端proxy或者dispatch
	 * @param isMock   是否使用mock
	 * @return         封装了mock标志的proxy或者dispath
	 * @throws WSClientException  
	 */
	public static <T> T setResponseMock(T proxy, boolean isMock) throws WSClientException {
		validateProxy(proxy);

		BindingDynamicProxyHandler handler = getHandler((BindingProvider)proxy);
		handler.setMock(isMock);
		return handler.bind(proxy);
	}
	
	private static BindingDynamicProxyHandler getHandler(BindingProvider bp) 
	{
		BindingDynamicProxyHandler handler = (BindingDynamicProxyHandler)bp.getRequestContext().get(BOUND_HANDLER_KEY);
		if (handler != null) {
			return handler;
		}
		handler = new BindingDynamicProxyHandler();
		//hold the reference of the handler, make they have the same lifecycle
		bp.getRequestContext().put(BOUND_HANDLER_KEY, handler); 
		
		return handler;
	}
	
	private static void validateProxy(Object proxy) throws WSClientException{
		if (proxy == null) {
			throw new WSClientException("proxy parameter is null");
		}

		if (!(proxy instanceof BindingProvider)) {
			throw new WSClientException("proxy is not a legal soap client");
		}
	}

	/**
	 * 手动生成签名值
	 * 
	 * @param ak           accessKey
	 * @param sk           secretKey
	 * @param apiName      服务名
	 * @param apiVersion   服务版本
	 * @param fingerStr    指纹串
	 * @param timestamp    时间戳
	 * @return             根据输入参数信息生成的签名串
	 */
	public static String genSignature(String ak, String sk, String apiName, String apiVersion,  String fingerStr, long timestamp) {
		return SOAPHeaderHandler.generateSignature(ak, sk, apiName, apiVersion, fingerStr, String.valueOf(timestamp));
	}
	
	/**
	 * <pre>
	 * 设置安全相关的headers到MimeHeaders对象内，以便在调用的时候传递该http header信息
	 * 
	 * 用法：
	 *   import org.apache.axis.client.Call;
	 *   ...
	 *   
	 *   Service service = new Service();
	 *   Call call = (Call)service.createCall();
	 *   ....
	 *   
	 *   MessageContext msgContext = call.getMessageContext();
	 *   MimeHeaders hd = msgContext.getMessage().getMimeHeaders();
	 *   
	 *   call.invoke(...);
	 * 
	 * </pre>
	 * @param mimeHeaders
	 * @param ak
	 * @param sk
	 * @param apiName
	 * @param apiVersion
	 * @param fingerStr
	 * @param timestamp
	 * @return
	 */
	public static boolean addHttpHeaders(MimeHeaders mimeHeaders, String ak, String sk, String apiName, String apiVersion,  String fingerStr, long timestamp) {
		if (mimeHeaders != null) {
			Map<String, String> headers = SOAPHeaderHandler.generateSignHeaders(ak, sk, apiName, apiVersion, fingerStr, String.valueOf(timestamp));
			for(Entry<String,String> kv:headers.entrySet()) {
				mimeHeaders.addHeader(kv.getKey(), kv.getValue());
			}
			return true;
		}
		
		return false;
	}
}

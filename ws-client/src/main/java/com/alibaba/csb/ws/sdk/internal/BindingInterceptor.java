package com.alibaba.csb.ws.sdk.internal;

import java.util.*;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.utils.TraceIdUtils;
import com.alibaba.csb.ws.sdk.WSClientException;
import com.alibaba.csb.ws.sdk.WSClientSDK;
import com.alibaba.csb.ws.sdk.WSParams;

//import org.apache.cxf.headers.Header;
//import org.apache.cxf.jaxb.JAXBDataBinding;

/**
 * Client invocation Interceptor, to set security related info into RequestContext of binding
 * @author liaotian.wq 2017年1月12日
 *
 */
public class BindingInterceptor {
	// put signature related headers into soap header 
	//-Dws.sdk.headers.insoap=true is kept for backwards compatible 
	private static boolean HEADERS_INSOAP = Boolean.getBoolean("ws.sdk.headers.insoap");

	private List<Handler> handlers;
	private Handler shh;
	private WSParams wsparams = WSParams.create();

	/* packaged */ BindingInterceptor() {

	}

	public void setMock(boolean mock) {
		wsparams.mockRequest(mock);
	}

	/* packaged */ List<Handler> before(Object pxy) throws JAXBException {
		// 拦截器BindingInterceptor方法调用:before()!");
		if (!(pxy instanceof BindingProvider)) {
			throw new WSClientException("proxy is not a legal soap client, can not do the interceptor");
		}
		BindingProvider proxy = (BindingProvider) pxy;
		Map<String, List<String>> requestHeaders = (Map<String, List<String>>) proxy.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
		if (requestHeaders == null) {
			requestHeaders = new HashMap<String, List<String>>();
            proxy.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
		}
		addTraceHeaders(requestHeaders, wsparams);
		// put security info into http request headers for over-proxy invocation
		setSecretHeaders(requestHeaders, wsparams);

		// skip this soap header logic
		if (HEADERS_INSOAP) {
			shh = new SOAPHeaderHandler(wsparams);

			BindingProvider bp = (BindingProvider) proxy;
			handlers = bp.getBinding().getHandlerChain();
			List<Handler> newHandlers = new ArrayList<Handler>();
			if (handlers != null) {
				newHandlers.addAll(handlers);
			}
			newHandlers.add(shh);
			// tip, must set the handleList again, or the handler will not
			// run!!!
			bp.getBinding().setHandlerChain(newHandlers);
		}
		return handlers;
	}

	private void setSecretHeaders(Map<String, List<String>> requestHeaders, WSParams params) {
		//Add HTTP request Headers
		Map<String, List<String>> secHeaders = SOAPHeaderHandler.genSecrectHeaders(params);
		requestHeaders.putAll(secHeaders);
		/*
		if (dumpHeaders) {
			System.out.println("--HTTP Headers---");
			for(Entry<String, List<String>> kv:secHeaders.entrySet()) {
				System.out.println(String.format("%s=%s",kv.getKey(), kv.getValue()));
			}
			System.out.println("-----------------");
		}*/
	}

	/* packaged */ public void after(Object proxy) {
		// System.out.println("remove headers ....");
		// 拦截器BindingInterceptor方法调用:after()!");
		if (!(proxy instanceof BindingProvider)) {
			throw new WSClientException("proxy is not a legal soap client, can not do the interceptor");
		}

		// TODO: this is not work, can not clear the new-added handler!
		if (shh != null) {
			BindingProvider bp = (BindingProvider) proxy;
			bp.getBinding().getHandlerChain().remove(shh);

			bp.getBinding().setHandlerChain(handlers);
		}
	}

	private void addTraceHeaders(Map<String, List<String>> requestHeaders, WSParams params) {
		if (!requestHeaders.containsKey(CsbSDKConstants.TRACEID_KEY)) { //优先使用-H/putHeader的方式
			if (params.getTraceId() == null) {
				params.traceId(TraceIdUtils.generate());
			}
			requestHeaders.put(CsbSDKConstants.TRACEID_KEY, Arrays.asList(params.getTraceId()));
		}
		if (!requestHeaders.containsKey(CsbSDKConstants.RPCID_KEY)) { //优先使用-H/putHeader的方式
			if (params.getRpcId() == null) {
				params.rpcId(TraceData.RPCID_DEFAULT);
			}
			requestHeaders.put(CsbSDKConstants.RPCID_KEY, Arrays.asList(params.getRpcId()));
		}
		if (!requestHeaders.containsKey(WSClientSDK.bizIdKey())) { //优先使用-H/putHeader的方式
			requestHeaders.put(WSClientSDK.bizIdKey(), Arrays.asList(params.getBizId()));
		}
		requestHeaders.put(CsbSDKConstants.REQUESTID_KEY, Arrays.asList(params.getRequestId()));
	}

	public void setWSParams(WSParams wsparams) {
		this.wsparams = wsparams;
	}
}

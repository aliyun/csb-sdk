package com.alibaba.csb.ws.sdk.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

//import org.apache.cxf.headers.Header;
//import org.apache.cxf.jaxb.JAXBDataBinding;
import static com.alibaba.csb.sdk.CsbSDKConstants.*;
import com.alibaba.csb.ws.sdk.WSClientException;
import com.alibaba.csb.ws.sdk.WSParams;

/**
 * Client invocation Interceptor, to set security related info into RequestContext of binding
 * @author liaotian.wq 2017年1月12日
 *
 */
public class BindingInterceptor {
	// put signature related headers into soap header 
	//-Dws.sdk.headers.insoap=true is kept for backwards compatible 
	private static boolean HEADERS_INSOAP = Boolean.getBoolean("ws.sdk.headers.insoap");
	private static boolean SKIP_SIGN_APINAME = Boolean.getBoolean("ws.sdk.skip.sign.apiname");

	private List<Handler> handlers;
	private Handler shh;
	private WSParams wsparams = WSParams.create();

	/* packaged */ BindingInterceptor() {

	}

	public void setMock(boolean mock) {
		wsparams.mockRequest(mock);
	}

	/* packaged */ List<Handler> before(Object proxy, String fingerStr) throws JAXBException {
		// 拦截器BindingInterceptor方法调用:before()!");
		if (!(proxy instanceof BindingProvider)) {
			throw new WSClientException("proxy is not a legal soap client, can not do the interceptor");
		}
		if (wsparams!=null && SKIP_SIGN_APINAME) {
			wsparams.api(null);
		}
		// put security info into http request headers for over-proxy invocation
		setSecrectHeaders((BindingProvider)proxy, wsparams, fingerStr);

		// skip this soap header logic
		if (HEADERS_INSOAP) {
			shh = new SOAPHeaderHandler(wsparams, fingerStr);

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

	private void setSecrectHeaders(BindingProvider proxy, WSParams params, String fingerStr) {
		//Add HTTP request Headers
		Map<String, List<String>> requestHeaders = (Map<String, List<String>>)proxy.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
		
		if (requestHeaders == null) {
			requestHeaders = new HashMap<String, List<String>>();
		}
		
		Map<String, List<String>> secHeaders = SOAPHeaderHandler.genSecrectHeaders(params, fingerStr);
		requestHeaders.putAll(secHeaders);
		/*
		if (dumpHeaders) {
			System.out.println("--HTTP Headers---");
			for(Entry<String, List<String>> kv:secHeaders.entrySet()) {
				System.out.println(String.format("%s=%s",kv.getKey(), kv.getValue()));
			}
			System.out.println("-----------------");
		}*/
		proxy.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);

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

	public void setWSParams(WSParams wsparams) {
		this.wsparams = wsparams;
	}
}

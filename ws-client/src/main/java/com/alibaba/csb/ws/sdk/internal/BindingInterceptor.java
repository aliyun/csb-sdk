package com.alibaba.csb.ws.sdk.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

//import org.apache.cxf.headers.Header;
//import org.apache.cxf.jaxb.JAXBDataBinding;
import static com.alibaba.csb.sdk.CsbSDKConstants.*;
import com.alibaba.csb.ws.sdk.WSClientException;

/**
 * Client invocation Interceptor, to set security related info into RequestContext of binding
 * @author liaotian.wq 2017年1月12日
 *
 */
public class BindingInterceptor {

	private String accessKey;
	private String secretKey;
	private String apiName;
	private String apiVersion;

	private boolean isMock;

	private List<Handler> handlers;
	private Handler shh;

	/* packaged */ BindingInterceptor() {

	}

	/* packaged */ void setASK(String ak, String sk) {
		accessKey = ak;
		secretKey = sk;
	}

	/* packaged */ void setMock(boolean isMock) {
		this.isMock = isMock;
	}
	
	/* packaged */ void setApiName(String apiName) {
		this.apiName = apiName;
	}	
	
	/* packaged */ void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	/* packaged */ List<Handler> before(Object proxy, String fingerStr) throws JAXBException {
		// 拦截器BindingInterceptor方法调用:before()!");
		if (!(proxy instanceof BindingProvider)) {
			throw new WSClientException("proxy is not a legal soap client, can not do the interceptor");
		}
		
		// put security info into http request headers for over-proxy invocation
		setSecrectHeaders((BindingProvider)proxy, accessKey, secretKey, apiName, apiVersion, fingerStr, isMock);

		shh = new SOAPHeaderHandler(accessKey, secretKey, apiName, apiVersion, fingerStr, isMock);

		BindingProvider bp = (BindingProvider) proxy;
		handlers = bp.getBinding().getHandlerChain();
		List<Handler> newHandlers = new ArrayList<Handler>();
		if (handlers != null) {
			newHandlers.addAll(handlers);
		}
		newHandlers.add(shh);
		// tip, must set the handleList again, or the handler will not run!!!
		bp.getBinding().setHandlerChain(newHandlers);

		return handlers;

	}

	private void setSecrectHeaders(BindingProvider proxy, String accessKey, String secretKey, String apiName, String apiVersion, String fingerStr, boolean isMock) {
		//Add HTTP request Headers
		Map<String, List<String>> requestHeaders = (Map<String, List<String>>)proxy.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
		
		if (requestHeaders == null) {
			requestHeaders = new HashMap<String, List<String>>();
		}
		
		Map<String, List<String>> secHeaders = SOAPHeaderHandler.genSecrectHeaders(accessKey, secretKey, apiName, apiVersion, fingerStr, isMock);
		requestHeaders.putAll(secHeaders);
		
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
}

package com.alibaba.csb.ws.sdk.internal;

import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.Handler;

import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;
import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.ws.sdk.WSParams;

/**
 * Client invocation Interceptor, to set security related info into RequestContext of binding
 *
 * @author fuzhao.fz 2019年5月26日
 */
public class AxisStubInterceptor {
    private WSParams wsParams;

    private List<Handler> handlers;
    private Handler shh;

    /* packaged */ AxisStubInterceptor() {

    }

    /* packaged */ void setWSParams(WSParams wsParams) {
        this.wsParams = wsParams;
    }

    /* packaged */ List<Handler> before(Object proxy, String fingerStr) throws JAXBException {
        Stub stub = (Stub) proxy;
        stub._getCall().addHeader(new SOAPHeaderElement(CsbSDKConstants.API_NAME_KEY, wsParams.getApi()));
        return handlers;
    }

    /* packaged */
    public void after(Object proxy) {
    }
}

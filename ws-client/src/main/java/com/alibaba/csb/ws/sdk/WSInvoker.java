package com.alibaba.csb.ws.sdk;

import com.alibaba.csb.utils.IPUtils;
import com.alibaba.csb.utils.LogUtils;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiseking on 17/12/27.
 */
public class WSInvoker {

    /**
     * 创建 soap dispatch
     *
     * @param params
     * @param ns
     * @param sname
     * @param pname
     * @param isSoap12
     * @param wa
     * @param ea
     * @return
     * @throws Exception
     */
    public static Dispatch<SOAPMessage> createDispatch(WSParams params, String ns, String sname,
                                                       String pname, boolean isSoap12, String wa, String ea) throws Exception {
        return createDispatch(params, ns, sname, pname, null, isSoap12, ea);
    }

    /**
     * 创建 soap dispatch
     *
     * @param params
     * @param nameSpace
     * @param serviceName
     * @param portName
     * @param isSoap12
     * @param endpoint
     * @return
     * @throws Exception
     */
    public static Dispatch<SOAPMessage> createDispatch(WSParams params, String nameSpace, String serviceName, String portName, String soapActionUri, boolean isSoap12, String endpoint) {
        // Service Qname as defined in the WSDL.
        QName sName = new QName(nameSpace, serviceName);

        // Port QName as defined in the WSDL.
        QName pName = new QName(nameSpace, portName);

        // Create a dynamic Service instance
        Service service = Service.create(sName);

        if (!isSoap12) {
            service.addPort(pName, SOAPBinding.SOAP11HTTP_BINDING, endpoint);
        } else {
            service.addPort(pName, SOAPBinding.SOAP12HTTP_BINDING, endpoint);
        }

        // Create a dispatch instance
        Dispatch<SOAPMessage> dispatch = service.createDispatch(pName, SOAPMessage.class, Service.Mode.MESSAGE);

        if (soapActionUri != null) {
            dispatch.getRequestContext().put(Dispatch.SOAPACTION_USE_PROPERTY, new Boolean(true));
            dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY, soapActionUri.trim());
        }

        dispatch.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        dispatch = WSClientSDK.bind(dispatch, params);
        return dispatch;
    }


    /**
     * 创建请求soap message
     *
     * @param isSoap12
     * @param reqSoap
     * @return
     * @throws Exception
     */
    public static SOAPMessage createSOAPMessage(boolean isSoap12, String reqSoap) {
        try {
            // Add a port to the Service
            SOAPMessage request = null;
            InputStream is = new ByteArrayInputStream(reqSoap.getBytes());
            if (!isSoap12) {
                // covert string to soap message
                request = MessageFactory.newInstance().createMessage(null, is);
            } else {
                request = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(null, is);
            }

            return request;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * set http request headers into dispatch
     *
     * @param dispatch
     * @param requestHeaders array element as "key:value"
     */
    public static void setHttpHeaders(Dispatch<SOAPMessage> dispatch, Map<String, String> requestHeaders) {
        if (requestHeaders == null || requestHeaders.size() == 0) {
            return;
        }
        Map<String, List<String>> httpHeaders = (Map<String, List<String>>) dispatch.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
        if (httpHeaders == null) {
            httpHeaders = new HashMap<String, List<String>>();
            dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
        }
        //Add HTTP request Headers
        for (Map.Entry<String, String> kv : requestHeaders.entrySet()) {
            httpHeaders.put(kv.getKey(), Arrays.asList(kv.getValue()));
        }
    }

    /**
     * ws调用，把一个soap文本发送到后端服务，并把返回soap转换为字符串返回
     *
     * @param params
     * @param ns
     * @param sname
     * @param pname
     * @param isSoap12
     * @param wa
     * @param ea
     * @param reqSoap
     * @param httpHeaders
     * @return
     * @throws Exception
     */
    public static String invokeSoapString(WSParams params, String ns, String sname,
                                          String pname, boolean isSoap12, String wa, String ea, String reqSoap, Map<String, String> httpHeaders) throws Exception {
        return invokeSoapString(params, ns, sname, pname, null, isSoap12, ea, reqSoap, httpHeaders);
    }

    /**
     * ws调用，把一个soap文本发送到后端服务，并把返回soap转换为字符串返回
     *
     * @param params
     * @param ns
     * @param sname
     * @param pname
     * @param isSoap12
     * @param ea
     * @param reqSoap
     * @param httpHeaders
     * @return
     * @throws Exception
     */
    public static String invokeSoapString(WSParams params, String ns, String sname, String pname, String soapAction,
                                          boolean isSoap12, String ea, String reqSoap, Map<String, String> httpHeaders) throws Exception {
        Dispatch<SOAPMessage> dispatch = WSInvoker.createDispatch(params, ns, sname, pname, soapAction, isSoap12, ea);
        SOAPMessage request = WSInvoker.createSOAPMessage(isSoap12, reqSoap);
        setHttpHeaders(dispatch, httpHeaders);

        int code = 200;
        String msg = null;
        SOAPMessage reply = null;
        long startTime = System.currentTimeMillis();
        try {
            reply = dispatch.invoke(request);
        } catch (Exception e) {
            code = 500;
            msg = e.getMessage();
            throw e;
        } finally {
            log(params, startTime, ea, sname, code, msg);
        }
        return DumpSoapUtil.dumpSoapMessage(reply);
    }

    public static void log(WSParams params, long startTime, String endpoint, String operation, int code, String msg) {
        long endTime = System.currentTimeMillis();
        try {
            int qidx = endpoint.indexOf("?");
            String url = qidx > -1 ? endpoint.substring(0, qidx) : endpoint;

            int cidx = url.indexOf(":");
            int pidx = url.indexOf(":", cidx + 2);
            if (pidx < 0) {
                pidx = url.indexOf("/", cidx + 2);
            }
            String dest = url.substring(cidx + 3, pidx);

            String method = operation.substring(operation.indexOf("}") + 1);
            LogUtils.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", new Object[]{startTime, endTime, endTime - startTime
                    , "WS", IPUtils.getLocalHostIP(), dest
                    , params.getBizId(), params.getRequestId()
                    , params.getTraceId(), params.getRpcId()
                    , params.getApi(), params.getVersion()
                    , defaultValue(params.getAk()), method
                    , url, code, "", defaultValue(msg)});
        } catch (Throwable e) {
            LogUtils.exception(MessageFormat.format("csb invoke error, api:{0}, version:{1}", params.getApi(), defaultValue(params.getSk())), e);
        }
    }

    private static String defaultValue(String val) {
        return val == null ? "" : val.trim();
    }
}

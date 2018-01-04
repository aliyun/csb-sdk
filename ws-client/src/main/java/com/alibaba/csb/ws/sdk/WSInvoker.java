package com.alibaba.csb.ws.sdk;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by wiseking on 17/12/27.
 */
public class WSInvoker {

  /**
   * 创建 soap dispatch
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
    // Service Qname as defined in the WSDL.
    QName serviceName = new QName(ns, sname);

    // Port QName as defined in the WSDL.
    QName portName = new QName(ns, pname);

    // Create a dynamic Service instance
    Service service = Service.create(serviceName);

    if (!isSoap12) {
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wa);
    } else {
      service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, wa);
    }

    // Create a dispatch instance
    Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);

    BindingProvider bp = (BindingProvider) dispatch;
    bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ea);

    // 使用SDK给dispatch设置 ak和sk !!!
    if (params != null && params.getAk() !=null) {
      dispatch = WSClientSDK.bind(dispatch, params);
    }

    return  dispatch;
  }

  /**
   * 创建请求soap message
   * @param isSoap12
   * @param reqSoap
   * @return
   * @throws Exception
   */
  public static SOAPMessage createSOAPMessage(boolean isSoap12,  String reqSoap) throws Exception {

    // Add a port to the Service
    SOAPMessage request = null;
    InputStream is = new ByteArrayInputStream(reqSoap.getBytes());
    if (!isSoap12) {
      // covert string to soap message
      request = MessageFactory.newInstance().createMessage(null, is);
    } else {
      request = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(null, is);
    }

    return  request;
  }

  /**
   * ws调用，把一个soap文本发送到后端服务，并把返回soap转换为字符串返回
   * @param params
   * @param ns
   * @param sname
   * @param pname
   * @param isSoap12
   * @param wa
   * @param ea
   * @param reqSoap
   * @return
   * @throws Exception
   */
  public static String invokeSoapString(WSParams params, String ns, String sname,
                                        String pname, boolean isSoap12, String wa, String ea, String reqSoap) throws Exception {
    Dispatch<SOAPMessage> dispatch = WSInvoker.createDispatch(params, ns, sname, pname, isSoap12, wa, ea);
    SOAPMessage request = WSInvoker.createSOAPMessage(isSoap12, reqSoap);
    SOAPMessage reply = dispatch.invoke(request);

    return DumpSoapUtil.dumpSoapMessage(reply);
  }
}

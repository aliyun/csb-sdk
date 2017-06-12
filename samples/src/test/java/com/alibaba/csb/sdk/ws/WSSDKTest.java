package com.alibaba.csb.sdk.ws;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import com.alibaba.csb.sdk.util.DumpSoapUtil;
import com.alibaba.csb.ws.sdk.WSClientSDK;

import csb.ping.ws2restful.PING;
import csb.ping.ws2restful.Ws2RestfulPortType;

/**
 * 一个WebService客户端调用的实例，用户可以使用标准的Proxy或者Dispath方式调用WebService服务，如果需要AK签名或其他操作(如，mock返回),
 * 则使用WSClientSDK对proxy或者dispatch进行wrapper设置, 这样在调用服务时会将请求SOAP进行签名处理。
 * 
 * @author liaotian.wq
 *
 */
public class WSSDKTest {
	private static final String arg0 = "testa";

	// security related params
	private static final String ak = "ak";
	private static final String sk = "sk";
	private static final String PING_NS = "http://ws2restful.PING.csb/";
	
	private static String wsdlAddr, endpointAddr;
	private static String wsdlWS2WSAddr, endpointWS2WSAddr;
	
	private String host = System.getProperty("bhost");
	private String port = System.getProperty("broker.wsport", "9081");
	
	private static final String reqSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:test=\"http://ws2restful.PING.csb/\">\n"
			+ "<soapenv:Header/>\n" + "<soapenv:Body>\n" + "   <test:ws2restful>\n" + "      <name>abc</name>\n"
			+ "   </test:ws2restful>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>\n";

	@org.junit.Before
	public void prepareUrl() {
		
		if (host == null) {
			Assert.fail("please define the sysetm parameter host,"+
					"\n e.g. mvn test -Dbhost=10.125.60.151 -Dbroker.wsport=9081");
		}
		wsdlAddr = String.format("http://%s:%s/PING/vcsb/ws2restful?wsdl", host, port);
		endpointAddr = String.format("http://%s:%s/PING/vcsb/ws2restful", host, port);
		wsdlWS2WSAddr = String.format("http://%s:%s/PING/vcsb.ws/ws2ws?wsdl", host, port);
		endpointWS2WSAddr = String.format("http://%s:%s/PING/vcsb.ws/ws2ws", host, port);
		
		System.out.println("invoke broker wsdl addr=" + wsdlAddr);
		System.out.println("invoke broker wsdl endpoint=" + endpointAddr);
		
		System.out.println("invoke broker ws2ws wsdl addr=" + wsdlAddr);
		System.out.println("invoke broker ws2ws wsdl endpoint=" + endpointAddr);
	}
	
	@Test
	/**
	 * Call WS with proxy client
	 * 
	 * @throws Exception
	 */
	public void testWithProxy() throws Exception {
		// Create the service client endpoint
		PING service = new PING(new URL(wsdlAddr));

		// Get the proxy port
		Ws2RestfulPortType port = service.getWs2RestfulPort();
		BindingProvider bp = (BindingProvider)port;  
        SOAPBinding binding = (SOAPBinding)bp.getBinding();  
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddr); 
        
		// 使用SDK将AK, SK传输的调用client端
		port = WSClientSDK.bind(port, ak, sk, "PING", "vcsb");
		
		// Call the method
		Object rtn = port.ws2Restful(arg0);

		// Print the response
		System.out.println(rtn);
	}

	@Test
	/**
	 * Call WS with dispatch client
	 * The test wsdl is "http://%s/PING/vcsb/ws2restful?wsdl"
	 */
	public void testWithDispath() throws Exception{
		// Service Qname as defined in the WSDL.
		QName serviceName = new QName(PING_NS, "PING");

		// Port QName as defined in the WSDL.
		QName portName = new QName(PING_NS, "ws2restfulPortType");

		// Create a dynamic Service instance
		Service service = Service.create(serviceName);

		// Add a port to the Service
		service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wsdlAddr);

		// Create a dispatch instance
		Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);

		// covert string to soap message
		String req = reqSoap;

		InputStream is = new ByteArrayInputStream(req.getBytes());
		SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
		BindingProvider bp = (BindingProvider)dispatch;  
        SOAPBinding binding = (SOAPBinding)bp.getBinding();  
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddr); 

		// 使用SDK给dispatch设置 ak和sk !!!
		dispatch = WSClientSDK.bind(dispatch, ak, sk, "PING", "vcsb");
		System.out.println("Send out the request: " + reqSoap);

		// Invoke the endpoint synchronously
		// Invoke endpoint operation and read response
		SOAPMessage reply = dispatch.invoke(request);
		reply = dispatch.invoke(request);
		
		if (reply != null)
			System.out.println("Response from invoke:" + DumpSoapUtil.dumpSoapMessage("response", reply));
		else
			System.out.println("Response from invoke is null");
	}
	
	
	@Test
	/**
	 * Call WS with dispatch client The test wsdl is
	 * "http://%s/PING/vcsb.ws/ws2ws?wsdl"
	 */
	public void testWS2WSWithDispath() throws Exception {
		String ns = "http://hc.wsprocess.csb.alibaba.com/";

		// Service Qname as defined in the WSDL.
		QName serviceName = new QName(ns, "WSHealthCheckServiceService");

		// Port QName as defined in the WSDL.
		QName portName = new QName(ns, "WSHealthCheckServicePort");

		// Create a dynamic Service instance
		Service service = Service.create(serviceName);

		// Add a port to the Service
		
		service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wsdlWS2WSAddr);

		// Create a dispatch instance
		Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);

		// covert string to soap message

		String req = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:hc=\"http://hc.wsprocess.csb.alibaba.com/\"> \n" 
				+ "<soapenv:Header/>\n" + "<soapenv:Body>\n"
				+ "<hc:ping>\n" + "<arg0>wiseking</arg0>\n" + "</hc:ping>\n" 
				+ "</soapenv:Body>\n"
				+ "</soapenv:Envelope>";

		InputStream is = new ByteArrayInputStream(req.getBytes());
		SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);

		// 使用SDK给dispatch设置 ak和sk !!!
		dispatch = WSClientSDK.bind(dispatch, ak, sk, "PING", "vcsb.ws");
		System.out.println("Send out the request: " + req);

		// Invoke the endpoint synchronously
		// Invoke endpoint operation and read response
		SOAPMessage reply = dispatch.invoke(request);
		reply = dispatch.invoke(request);

		if (reply != null)
			System.out.println("Response from invoke:" + DumpSoapUtil.dumpSoapMessage("response", reply));
		else
			System.out.println("Response from invoke is null");
	}
}

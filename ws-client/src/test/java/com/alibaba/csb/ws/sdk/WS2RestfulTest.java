package com.alibaba.csb.ws.sdk;

import java.net.URL;

import org.junit.Test;

import csb.ci_servlet_pub.ws2restful.CiServletPub;
import csb.ci_servlet_pub.ws2restful.Ws2RestfulPortType;

public class WS2RestfulTest {
	
	@Test
	public void testCmdWsCaller3() throws Exception {
		String ak = "ak";
		String sk = "sk";
		String apiName = "ci-servlet-pub";
		String apiVersion = "1.0.0";
		String wsdlAddr = "http://localhost:9081/ci-servlet-pub/1.0.0/ws2restful?wsdl";
		CiServletPub service = new CiServletPub(new URL(wsdlAddr));
		Ws2RestfulPortType port = service.getWs2RestfulPort();
		WSClientSDK.bind(port, ak, sk, apiName, apiVersion);
		String ret = (String)port.ws2Restful("wiseking");
		System.out.println("ret=" + ret);
	}
}

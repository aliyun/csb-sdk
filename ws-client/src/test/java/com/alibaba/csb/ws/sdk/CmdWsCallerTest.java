package com.alibaba.csb.ws.sdk;

import org.junit.Test;

public class CmdWsCallerTest {
	private static final String reqSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:test=\"http://ws2restful.PING.csb/\">\n"
			+ "<soapenv:Header/>\n" + "<soapenv:Body>\n" + "   <test:ws2restful>\n" + "      <name>abc</name>\n"
			+ "   </test:ws2restful>\n" + "</soapenv:Body>\n" + "</soapenv:Envelope>\n";
	
	@Test
	public void testCmdWsCaller() {
		String[] args = {
				"-soap12", "true",
				"-ak", "ak",
				"-sk", "sk",
				"-api", "PING",
				"-version", "vcsb",
				"-wa", "http://11.239.187.178:9081/PING/vcsb/ws2restful?wsdl",
				"-ea", "http://11.239.187.178:9081/PING/vcsb/ws2restful",
				"-ns", "http://ws2restful.PING.csb/",
				"-sname","PING",
				"-pname","ws2restfulPortType",
				"-d",
				"-rd", reqSoap
				
		};
		CmdWsCaller.main(args);
	}
}

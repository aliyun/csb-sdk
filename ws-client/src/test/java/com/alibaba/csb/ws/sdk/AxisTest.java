package com.alibaba.csb.ws.sdk;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.junit.Test;

public class AxisTest {
	

	@Test
	public void testCmdWsCaller() throws Exception {
		String ak = "ak";
		String sk = "sk";
		String apiName = "PING";
		String apiVersion = "vcsb.ws";

		Service service = new Service();
		Call call = AxisCallWrapper.createCallWrapper(service, ak, sk, apiName, apiVersion);
		call.setTargetEndpointAddress("http://localhost:9081/PING/vcsb.ws/ws2ws");
		call.setOperationName(new QName("http://hc.wsprocess.csb.alibaba.com/", "ping"));
		//call.setEncodingStyle(null);

		call.addParameter("arg0", // 设置要传递的参数
				org.apache.axis.encoding.XMLType.XSD_STRING, javax.xml.rpc.ParameterMode.IN);
		Object[] args = { "wiseking" };
		Object ret = call.invoke(args);
		System.out.println("ret=" + ret);
	}
}

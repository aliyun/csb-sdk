package com.alibaba.csb.sdk.ws.mtom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import javax.activation.DataHandler;  
import javax.activation.DataSource;
import javax.activation.FileDataSource;  
import javax.xml.namespace.QName;  
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPBinding;

import com.alibaba.csb.ws.sdk.WSClientSDK;
import com.alibaba.csb.ws.wsimpl.AttachmentWS;
import com.alibaba.csb.ws.wsimpl.AttachmentWSService;
/**
 * mtom case refers to : https://blogs.oracle.com/vijaya/
 *
 *
 *
 *
 * @author liaotian.wq 2017年1月10日
 *
 */

/*
Mtom WS服务接入端端的参考代码


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.ws.soap.MTOM;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jws.WebMethod;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceException;

@MTOM
@javax.jws.WebService
public class AttachmentWS {
	@WebMethod
	public String echoBinaryAsString(byte[] bytes) {
		String ret = "empty bytes array";
		if(bytes != null && bytes.length>0) {
			ret = new String(bytes);
		}
		System.out.println("---- echoBinaryAsString: rtn=" + ret);
		return ret;
	}

	@WebMethod
	public byte[] echoStringAsBinary(String str) {
		if (str == null)
			str = "null";
		System.out.println("---- echoStringAsBinary: request=" + str);
		return str.getBytes();
	}

	// Use @XmlMimeType to map to DataHandler on the client side
	public void fileUpload(String fileName, @XmlMimeType("application/octet-stream") DataHandler data) {
		try {
			OutputStream os = new FileOutputStream(new File(fileName));
			data.writeTo(os);
			System.out.println("---- fileUpload: writeTo=" + fileName);
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

	@XmlMimeType("application/octet-stream")
	@WebMethod
	public DataHandler fileDownload(String filename) {
		System.out.println("---- fileDownload: file=" + filename);
		return new DataHandler(new FileDataSource(filename));
	}
}

*/

public class MtomSDKTest {
	private String ak = "ak";
	private String sk = "sk";
	private String wsdlAddr = System.getProperty("wsdl.addr");
	private String endpointAddr = System.getProperty("endpoint.addr");
	
	@org.junit.Before
	public void prepareUrl() {
		wsdlAddr = System.getProperty("wsdl.addr");
		endpointAddr = System.getProperty("endpoint.addr");
		
		if (wsdlAddr == null || endpointAddr == null) {
			Assert.fail("please define the sysetm parameters  wsdl.addr and endpoint.addr,"+
					"\n e.g. mvn test -Dwsdl.addr=http://10.125.60.151/sn/sv/ws2ws?wsdl -Dendpoint.addr=http://10.125.60.151/sn/sv/ws2ws");
		}
		System.out.println("invoke broker wsdl addr=" + wsdlAddr);
		System.out.println("invoke broker wsdl endpoint=" + endpointAddr);
	}
	
	public void testNormalWS2WS() throws Exception {
		//TODO
	}
	
	@Test
	/**
	 * Call MTOM/WS with static proxy client
	 * 
	 * @throws Exception
	 */
	public void testWithProxy() throws Exception {
		// Create the service client endpoint
		AttachmentWSService service = new AttachmentWSService(new URL(wsdlAddr));

		// Get the proxy port
		AttachmentWS port = service.getAttachmentWSPort(new javax.xml.ws.soap.MTOMFeature());
		
		// Set enable MTOM
		BindingProvider bp = (BindingProvider)port;  
        SOAPBinding binding = (SOAPBinding)bp.getBinding();  
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddr); 
        binding.setMTOMEnabled(true);  
        
		// 使用SDK将AK, SK传输的调用client端
		port = WSClientSDK.bind(port, ak, sk, "ci-ws2ws-mtom-import", "1.0.0");
		int times = Integer.getInteger("times", 1);
		for (int i=0; i<times; i++) {
			System.out.println("----- Execute the test with #"+i+" time");
			String testString = "this is a mtom String";
			String retStr = port.echoBinaryAsString(testString.getBytes());
			System.out.println("return Str="+retStr);
			Assert.assertTrue("Not equal", testString.equals(retStr));
			
			byte[] retBytes = port.echoStringAsBinary(testString);
			Assert.assertTrue("Not equal", testString.equals(new String(retBytes)));
			System.out.println("return Str="+retBytes);
			
			final File f = new File(MtomSDKTest.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "com/alibaba/csb/sdk/ws/mtom/MtomSDKTest.class");
			String upFile = "/tmp/new.class";
			// file upload
			port.fileUpload(upFile,  new DataHandler(new FileDataSource(f)));
			// file download
			DataHandler dh = port.fileDownload(upFile);
			OutputStream os = new FileOutputStream(new File(upFile));
			dh.writeTo(os);
			/**/
		}
	}
}

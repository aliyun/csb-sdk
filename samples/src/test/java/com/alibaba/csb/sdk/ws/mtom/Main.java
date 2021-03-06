package com.alibaba.csb.sdk.ws.mtom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;


import com.alibaba.csb.ws.sdk.WSClientSDK;
import com.alibaba.csb.ws.wsimpl.AttachmentWS;
import com.alibaba.csb.ws.wsimpl.AttachmentWSService;


import com.alibaba.csb.ws.sdk.WSClientSDK;

public class Main {
		private String ak = "ak";
		private String sk = "sk";
		private String wsdlAddr = System.getProperty("wsdl.addr");
		private String endpointAddr = System.getProperty("endpoint.addr");
		
		public void prepareUrl() {
			wsdlAddr = System.getProperty("wsdl.addr");
			endpointAddr = System.getProperty("endpoint.addr");
			
			if (wsdlAddr == null || endpointAddr == null) {
				System.out.println("please define the sysetm parameters  wsdl.addr and endpoint.addr,"+
						"\n e.g. mvn test -Dwsdl.addr=http://10.125.60.151/sn/sv/ws2ws?wsdl -Dendpoint.addr=http://10.125.60.151/sn/sv/ws2ws");
				return;
			}
			System.out.println("invoke broker wsdl addr=" + wsdlAddr);
			System.out.println("invoke broker wsdl endpoint=" + endpointAddr);
		}
		
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
			port = WSClientSDK.bind(port, ak, sk);
			
			String testString = "this is a mtom String";
			String retStr = port.echoBinaryAsString(testString.getBytes());
			System.out.println("return Str="+retStr);
			//Assert.assertTrue("Not equal", testString.equals(retStr));
			
			byte[] retBytes = port.echoStringAsBinary(testString);
			//Assert.assertTrue("Not equal", testString.equals(new String(retBytes)));
			System.out.println("return Str="+retBytes);
			
			final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "com/alibaba/csb/sdk/ws/mtom/MtomSDKTest.class");
			String upFile = "/tmp/a.class";
			// file upload
			port.fileUpload(upFile,  new DataHandler(new FileDataSource(f)));
			// file download
			upFile = "/tmp/b.class";
			DataHandler dh = port.fileDownload(upFile);
			OutputStream os = new FileOutputStream(new File(upFile));
			dh.writeTo(os);
			/**/
		}
		
		public static void main(String[] args) throws Exception {
			Main main = new Main();
			main.prepareUrl();
			main.testWithProxy();
		}
}
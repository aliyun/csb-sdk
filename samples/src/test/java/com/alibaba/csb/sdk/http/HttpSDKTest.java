package com.alibaba.csb.sdk.http;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.csb.sdk.ContentBody;
import com.alibaba.csb.sdk.HttpCaller;
import com.alibaba.csb.sdk.HttpCallerException;

/**
 * 一个Http SDK编程示例，如何使用HttpCaller向服务端发送POST/GET的请求
 * 
 * @author liaotian.wq
 *
 */
public class HttpSDKTest {
	private String url;
	private static final String apiName = "PING";
	private static final String version = "vcsb";
	private static final String versionWS = "vcsb.ws";
	
	private static final String arg0 = "testa";
	
	//security related params
	private static final String ak = "ak";
	private static final String sk = "sk";
	
	@org.junit.Before
	public void prepareUrl() {
		String bhost = System.getProperty("bhost");	
		
		if (bhost==null) {
			Assert.fail("please define the sysetm param bhost, e.g. mvn test -Dbhost=10.125.60.151");
		}else if(bhost.indexOf(":")<=0) {
			bhost += ":8086";
		}
		
		url = String.format("http://%s/test", bhost);
		System.out.println("invoke broker address=" + url);
	}
	
	/**
	 * for http2http case, backend service is a http/restful service
	 * @throws HttpCallerException
	 */
	@Test
	public void callWithHttpSDK() throws HttpCallerException {
		System.out.println("testJson request url:" + url);
        System.out.println("apiName:" + apiName);
        System.out.println("ak:" + ak);
        System.out.println("sk:" + sk);
        System.out.println("arg0:" + arg0+ "  urlEncode="+URLEncoder.encode("http://abc:port/wiseking?aaa=bbb&cc=d&ddd"));
        
        // Prepare the reuqest params
        Map<String, String> params = new HashMap<String, String>();
        String req = arg0+"&abc!!bb%ddd";
        System.out.println("encode string:" +URLEncoder.encode(req)+" " + URLEncoder.encode(URLEncoder.encode(req)));
        params.put("times", URLEncoder.encode(req)); // 普通的串对象
        String ret = HttpCaller.doGet(url, apiName, version, params, ak, sk);
        System.out.println("retStr = " + ret);
        //Assert.assertTrue("Not correct response", ret != null && ret.startsWith("Hi "+req+", greeting from CSB broker"));
        params.put("times", URLEncoder.encode(req));
        String ret2 = HttpCaller.doPost(url, apiName, version, params, ak, sk);
        System.out.println("retStr2 = " + ret2);
        
        System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret));
	}
	
	/**
	 * for http2ws case, backend service is a webservice service
	 * @throws HttpCallerException
	 */
	@Test
	public void callWithHttpSDK4HTTP2WS() throws HttpCallerException {
		System.out.println("testJson request url:" + url);
        System.out.println("apiName:" + apiName);
        System.out.println("ak:" + ak);
        System.out.println("sk:" + sk);
        System.out.println("arg0:" + arg0);
        
        // Prepare the reuqest params
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", arg0); // 普通的串对象

        String ret = HttpCaller.doGet(url, apiName, versionWS, params, ak, sk);
        System.out.println("retStr = " + ret);
        
        System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret));
	}
	
	//-------- 其他调用参考
	
	//@Test
	public void httpSDKJsonString() throws HttpCallerException {
		String url = "http://11.239.187.178:8086/test?name=a&age=12&title=test";
		String apiName = "httpjsonbody";
		System.out.println("testJson request url:" + url);
        System.out.println("apiName:" + apiName);
        System.out.println("ak:" + ak);
        System.out.println("sk:" + sk);
        
        String ret = HttpCaller.doPost(url, apiName, version, new ContentBody("{\"a\":\"csb云服务总线\"}"), ak, sk);
        System.out.println("retStr = " + ret);
        
        System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret));
	}
	
	//@Test
	public void httpSDKBytes() throws HttpCallerException {
		String url = "http://11.239.187.178:8086/test?fileName=abc.png&filePath=/home/admin/";
		String apiName = "httpfile";
		System.out.println("testJson request url:" + url);
        System.out.println("apiName:" + apiName);
        System.out.println("ak:" + ak);
        System.out.println("sk:" + sk);
        
        

        String ret = HttpCaller.doPost(url, apiName, version,  new ContentBody(HttpCaller.readFileAsByteArray("/ltwork/abc.png")), ak, sk);
        System.out.println("retStr = " + ret);
        
        System.out.println("retStr with charset = " + HttpCaller.changeCharset(ret));
	}
	
	//@Test
	public void testJson()  {
		String requestURL = "http://10.125.50.237:8086/test?name=a&age=12&title=test";
		String apiName = "httpjson_target2";
		String version = "1.0.0";
		String ak ="ak";
		String sk = "sk";
		
		requestURL = "http://localhost:8086/service";
		apiName =  "getxmltoken_gdic154"; 
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("key","af514f8170be43ea93ea70f9d43ca6f2");
		paramsMap.put("signature","4O4xAxEh8RSCuyq6U3Yb9HwhCIE=");
		paramsMap.put("secret","gdjrb888");
        ak="2fba3c68960944089d1d61a3929474f1";
        sk="OeCjAkzI2DVwqKSb2yxpVVrGyKU=";
		Map<String, String> paramMap = new HashMap<String, String>();
		try {
			String result = HttpCaller.doPost(requestURL, apiName, version, paramMap, ak, sk);
			System.out.println(result);
		} catch (HttpCallerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package com.alibaba.csb.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;

public class BuilderTest {
	@Before
	public void before() {
		System.setProperty("http.caller.DEBUG", "true");
		SimpleDateFormat tf=new SimpleDateFormat("HH-mm:ss:ms"); 
		System.out.println("warmup begin ... ctime=" + tf.format(new Date()));
		HttpCaller.warmup();
		System.out.println("warmup done dtime=" + tf.format(new Date()));
		HttpCaller.warmup();
		System.out.println("2nd warmup done dtime=" + tf.format(new Date()));
	}

	@Test
	public void testBuilder() {
		HttpParameters.Builder builder = new HttpParameters.Builder();

		builder.requestURL("http://localhost:8086?arg0=123") // 设置请求的URL
		.api("PING") // 设置服务名
		.version("vcsb") // 设置版本号
		.method("get") // 设置调用方式, get/post
		.accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

		// 设置请求参数
		builder.putParamsMap("key1", "value1")
		.putParamsMap("name", "{\"a\":value1}"); // json format value
		builder.contentBody(new
				ContentBody("{\"a\":\"csb云服务总线\"}"));
		builder.method("post");
		try {
			String ret = HttpCaller.invoke(builder.build());
			System.out.println("------- ret="+ret);
		} catch (HttpCallerException e) {
			// error process
			e.printStackTrace(System.out);
		}

		try {
			// 重启设置请求参数
			builder.clearParamsMap();
			builder.putParamsMap("key1", "value1---new")
			.putParamsMap("key2", "{\"a\":\"value1-new\"}");

			// 使用post方式调用
			builder.method("post");
			HttpCaller.invoke(builder.build());
		} catch (HttpCallerException e) {
			// error process
		}
	}
	
	@Test
	public void testhttpJson()  {
		Map<Long,Long> map = new HashMap<Long, Long>();
		map.put(1l, 1l);
		map.put(2l, 2l);
		map.put(3l, 3l);
		map.put(4l, 4l);
		String mapStr = JSON.toJSONString(map);
		String requestURL = "http://localhost:8086/test?name2=a&map="+URLEncoder.encode(mapStr);
		String apiName = "PING";
		String version = "vcsb";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("key1", "v1");
		paramsMap.put("name", "v2");
		try {
			
			String result = HttpCaller.doPost(requestURL, apiName, version, paramsMap, "fbb03107e2cd42b29773a5faa55a9d99", "9i6L6NQGtba2PqnvP//KbnyCLkc=");
			System.out.println(result.length() + result);
		} catch (HttpCallerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testJson()  {
		String requestURL = "http://10.125.50.237:8086/test?name=a&age=12&title=test";
		String apiName = "dubbotest";
		String version = "1.0.0";
		Map<String, String> paramMap = new HashMap<String, String>();
		try {
			String result = HttpCaller.doPost(requestURL, apiName, version, paramMap, "ak", "sk");
			System.out.println(result);
		} catch (HttpCallerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPostJsonStr()  {
		String requestURL = "http://10.125.60.151:8086/test?arg0=a&arg1=12&title=test";
		String apiName = "demo-http2http";
		String version = "1.0.0";
		try {
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("a", "b");
			String result = HttpCaller.doPost(requestURL, apiName, version, new ContentBody("{\"a\":\"csb云服务总线\"}"), "ak", "sk");
			System.out.println(result);
		} catch (HttpCallerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testfile(){
		try {
			byte[] bytes = HttpCaller.readFileAsByteArray("C:/Users/Public/Pictures/Sample Pictures/test.txt");
			File f = new File("C:/Users/Public/Pictures/Sample Pictures/test2.txt");
			FileOutputStream out = new FileOutputStream(f);
			
			try {
				out.write(bytes, 0, bytes.length);
				out.flush();
			} finally {
				out.close();
			}
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void testPostBytes()  {
		
		String requestURL = "http://10.125.60.151:8086/test?fileName=result.txt&filePath=/home/admin/";
		String apiName = "httpfile";
		String version = "1.0.0";
		try {
			String result = HttpCaller.doPost(requestURL, apiName, version, new ContentBody(HttpCaller.readFileAsByteArray("/tmp/abc.log")), "ak", "sk");
			System.out.println(result);
		} catch (HttpCallerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testPostFile() throws HttpCallerException {
		String requestURL = "http://localhost:8088/api/uploadjar/Upload";
		String apiName = "abc";
		String version = "1.0.0";
		//String file = "/ltwork/depot/camel/assembly/target/csb-broker-1.0.4.1-SNAPSHOT.tar.gz";
		String file = "/ltwork/csb-install/httpsdk1.7.jar";
		byte[] fc = HttpCaller.readFileAsByteArray(file); 
		HttpCaller.doPost(requestURL, apiName, version, new ContentBody(fc), "ak", "sk");
	}
	
	@Test
	public void testJsonMap(){
		String json = "{\"@type\":\"java.util.HashMap\",\"test\":{\"@type\":\"com.alibaba.csb.ws.def.ParamA\",\"accounts\":[\"aaaa\",\"bbbb\"],\"age\":1,\"name\":\"test\",\"sons\":[\"cccc\",\"ddd\"]}}";
		Map result = JSON.parseObject(json, new TypeReference<Map>(){}, new Feature[]{});
		
		System.out.println(result);
		
	}
}

package com.alibaba.csb.sdk;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csb.sdk.security.ParamNode;
import com.alibaba.csb.sdk.security.SpasSigner;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;

public class OtherTest {
	@Before
	public void before() {
		System.setProperty("http.caller.DEBUG1", "true");
	}
	
	@Test 
	public void  testMap2JsonString() {
		 //如果构造一个复杂的对象有难度，则可以使用全map的方式设置json串，举例，对于json串
		 // {"f1":{"f11":"v11", "f12":["v121","v122"]}, "f2":"wiseking"}
		 // 它是有如下的方式进行转换而来
		 Map<String,Object> map = new HashMap<String,Object>();
		 
		 Map<String,Object> mapF1 = new HashMap<String,Object>();
		 mapF1.put("f11", "v11");
		 mapF1.put("f12", Arrays.asList("v121","v122"));
		 map.put("f1", mapF1);
		 
		 map.put("f2", "wiseking");
		 
		 String jsonData = JSON.toJSONString(map);
		 
		 System.out.println("jsonStr = " + jsonData);
		 
		 map.clear();
		 map.put("f1", "wiseking");
		 map.put("f2", "love");
		 jsonData = JSON.toJSONString(map);
		 System.out.println("jsonStr string = " + jsonData);
		 
		 map.clear();
		 jsonData = JSON.toJSONString(map);
		 System.out.println("jsonStr empty = " + jsonData);
	}
	
	@Test
	public void testJsonMap(){
		String json = "{\"@type\":\"java.util.HashMap\",\"test\":{\"@type\":\"com.alibaba.csb.ws.def.ParamA\",\"accounts\":[\"aaaa\",\"bbbb\"],\"age\":1,\"name\":\"test\",\"sons\":[\"cccc\",\"ddd\"]}}";
		Map result = JSON.parseObject(json, new TypeReference<Map>(){}, new Feature[]{});
		
		System.out.println(result);
		
	}
	
	@Test
	public void testSignTime() {
		ArrayList<ParamNode> paramNodeList = new ArrayList<ParamNode>();
		SpasSigner.sign(paramNodeList, "secretKey");
		long iniT = System.currentTimeMillis();
		for(int i=0; i<3; i++) {
			long startT = System.currentTimeMillis();
			paramNodeList = new ArrayList<ParamNode>();
			paramNodeList.add(new ParamNode("aaaa", "vvvvvvvvvvvvvvvvv" + i));
			String secretKey = "1vOEQGgjjUwHsjObDhFbsln3rTM4CW";
			String sv = SpasSigner.sign(paramNodeList, secretKey);

			System.out.println("sv=" + sv + " costs=" + (System.currentTimeMillis() - startT) + "ms");
		}
		System.out.println("avg="+(System.currentTimeMillis()-iniT)/10000);
	}

	@Test
	public void testLarge() {
		try {
			HttpParameters.Builder hp = HttpParameters.newBuilder();
			hp.api("lt-http2ws").requestURL("http://118.31.48.251:8086/CSB");
			hp.version("1.0.0");
			hp.method("post");
			/*
			//hp.putParamsMap("arg0", "{\"itemName\":\"wiseking\", \"items\":[{\"p1\":\"love\", \"p2\":\"test\"}]}");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("itemName", "wiseking");
			List<Map<String,String>> items = new ArrayList<Map<String, String>>();
			Map<String,String> map = new HashMap<String, String>();
			for (int i=0; i<300000; i++) {
				map = new HashMap<String, String>();
				map.put("p1", "testvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvalue"+i);
				map.put("p2", "testvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvaluetestvalue"+i);
				items.add(map);
			}
			jsonObject.put("items", items);
			String req = jsonObject.toJSONString();
			System.out.println("reqSize=="+req.length());
			*/
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<400000; i++) {
				sb.append("wisekingtesttest");
			}
			hp.putParamsMap("arg0", sb.toString());
			hp.putParamsMap("arg1", "10");
			System.out.println("reqSize=="+sb.length());
			String ret = null;
			ret = HttpCaller.invoke(hp.build());
			if (ret!=null && ret.length()>30000) {
				System.out.println("ret=big big ret");
			}else {
				System.out.println("ret=" + ret);
			}
		} catch (HttpCallerException e) {
			e.printStackTrace();
		}
	}
}

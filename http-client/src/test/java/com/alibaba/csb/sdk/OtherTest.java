package com.alibaba.csb.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
		System.setProperty("http.caller.DEBUG", "true");
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
}

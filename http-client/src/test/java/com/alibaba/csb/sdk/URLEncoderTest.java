package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.ParamNode;
import com.alibaba.csb.sdk.security.SpasSigner;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import org.junit.Before;
import org.junit.Test;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class URLEncoderTest {
	@Test
	public void test() throws Exception{
		//-Dfile.encoding 中文签名验签失败问题 是由于客户端的与服务端 -Dfile.encoding不同导致
		//特别是doGet方法 包含中文时有验签失败的问题
		System.setProperty("file.encoding", "GBK");
		String test = "测试";
		String test1 = URLDecoder.decode(test);
		System.out.println(test1);
		String test2 = URLEncoder.encode(test1);
		System.out.println(test2);
		test2 = URLDecoder.decode(test2, "UTF-8");
		System.out.println(test2);
	}

}

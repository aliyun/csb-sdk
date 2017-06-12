package com.alibaba.csb.sdk;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import static com.alibaba.csb.sdk.ParamJSONHelper.*;

public class ParamJSONHelperTest {
	@Test
	public void testA() {
		Map<String, Object> orderItemElement = toMap(toKVPair("skey1", "foo"), toKVPair("skey2", "bar"));
		Map<String, Object> orderItem = toMap(toKVPair("orderItem", orderItemElement));
		
		Map<String, Object> itemElement = toMap(toKVPair("key1", "love"), toKVPair("key2", "story"),
				toKVPair("orderItems", Arrays.asList(orderItem)));
		Map<String, Object> item = toMap(toKVPair("item", itemElement));
		
		String poItems = JSON.toJSONString(Arrays.asList(item), true);

		System.out.println("poItems=\n" + poItems);
	}
}

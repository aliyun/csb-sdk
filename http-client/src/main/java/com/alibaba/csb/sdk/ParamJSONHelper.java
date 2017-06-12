package com.alibaba.csb.sdk;

import java.util.LinkedHashMap;
import java.util.Map;


//import com.alibaba.fastjson.JSON;

/**
 * 一个辅助工具类Java对象到JSON串的泛化转换，在不定义复杂对象类的情况下，把HTTP参数转换为Json串
 * 
 * 
 * <pre>
 * 用法：
 * 
 * import static com.alibaba.csb.sdk.ParamJSONHelper.*;
 * 
 *  ...
 *  Map<String, Object> orderItemElement = toMap(
 *    toKVPair("skey1", "foo"), 
 *    toKVPair("skey2", "bar"));
 *  Map<String, Object> orderItem = toMap(toKVPair("orderItem", orderItemElement));
 *  Map<String, Object> itemElement = toMap(
 *    toKVPair("key1", "love"), 
 *    toKVPair("key2", "story"),
 *    toKVPair("orderItems", Arrays.asList(orderItem)));
 *  Map<String, Object> item = toMap(toKVPair("item", itemElement));
 *  // 调用fastjson类 将Map所代表的对象内容装换为json串
 *  String poItems = JSON.toJSONString(Arrays.asList(item), true);
 *      
 *  System.out.println("poItems=\n" + poItems);
 * 
 * 打印出的JSON串为:
 * 
 * [
 *	{
 *		"item":{
 *			"key1":"love",
 *			"key2":"story",
 *			"orderItems":[
 *				{
 *					"orderItem":{
 *						"skey1":"foo",
 *						"skey2":"bar"
 *					}
 *				}
 *			]
 *		}
 *	}
 * ]
 * 
 * </pre>
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq@alibaba-inc.com
 * @since 2017.3 
 * @version 1.0.4.4+
 *
 */
public class ParamJSONHelper {
	/**
	 * 内部静态类 存储key -- value对象
	 *
	 */
	public static class KVPair {
		private String key;
		private Object value;
	}
	
	/**
	 * 将一对儿key,value转换为KVPair实例对象
	 * @param key
	 * @param value
	 * @return
	 */
	public static KVPair toKVPair(String key, Object value) {
		KVPair rtn = new KVPair();
		rtn.key = key;
		rtn.value = value;
		return rtn;
	}
	
	/**
	 * 将多个KVPair转换为一个Map对象
	 * @param kvp
	 * @return
	 */
	public static Map<String,Object> toMap(KVPair... kvp) {
		//使用LinkedHashMap保证key的顺序与参数添加时候一致
		Map<String,Object> rtn = new LinkedHashMap<String,Object>();
		for(KVPair kv:kvp) {
			rtn.put(kv.key, kv.value);
		}
		
		return rtn;
	}
	
	/**
	 * 将一个对象转换为jsonString
	 * @param obj 任何对象，包括String, Map, Array, List或者一个复杂对象
	 * @param prettyFormat 是否转换为规范的JSON显示格式
	 * @return
	 */
//	public static String toJSONString(Object obj, boolean prettyFormat) {
//		return JSON.toJSONString(obj, prettyFormat);
//	}
}

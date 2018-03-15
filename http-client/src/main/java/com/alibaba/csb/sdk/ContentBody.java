package com.alibaba.csb.sdk;

/**
 * 设置HTTP传输的body内容，可以是Json String或者是byte[]格式
 * 
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq 
 * @since 2016
 */
public class ContentBody {
	// Json内容串 使用的key，会把json内容串作为 key=value 方式作为签名的一部分
	/*packaged*/ static final String CONTENT_BODY_SIGN_KEY = System.getProperty("csb.sdk.jsonbody.sign.key", "_jsonbody_sign_key_");
	/**
	 * 指定ContentBody的类型
	 *
	 */
	public static enum Type {
		JSON("application/json"), BINARY("application/octet-stream");
		
		private String ct;
		
		private Type(String ct) {
			this.ct = ct;
		}
		
		public String getContentType() {
			return ct;
		}
	}
	
	private String jsonBody ;
	private byte[] bytesBody ;
	private Type type;
	
	/**
	 * 使用Json串构造ContentBody
	 * @param jsonStr
	 */
	public ContentBody(String jsonStr) {
		this.jsonBody = jsonStr;
		type = Type.JSON;
	}
	
	/**
	 * 使用byte数组构造ContentBody
	 * @param bytes
	 */
	public ContentBody(byte[] bytes) {
		this.bytesBody = bytes;
		type = Type.BINARY;
	}
	
	public Type getContentType() {
		return this.type;
	}
	
	public Object getContentBody() {
		if (type ==  Type.BINARY)
			return bytesBody;
		else
			return jsonBody;
	}
}

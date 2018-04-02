package com.alibaba.csb.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static com.alibaba.csb.sdk.internel.HttpClientHelper.trimWhiteSpaces;

/**
 * Http Parameters 参数构造器，使用(Builder)模式构造http调用的所有参数
 * 
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq 
 * 
 * @since 2016
 *
 */
public class HttpParameters {
	private static final Random random = new Random(System.currentTimeMillis());
	private Builder builder;

	String getApi() {
		return builder.api;
	}

	String getVersion() {
		return builder.version;
	}

	String getAccessKey() {
		return builder.ak;
	}

	String getSecretkey() {
		return builder.sk;
	}

	String getMethod() {
		return builder.method;
	}

	String getRequestUrl() {
		return builder.requestUrl;
	}

	ContentBody getContentBody() {
		return builder.contentBody;
	}

	String getRestfulProtocolVersion() {
		return builder.restfulProtocolVersion;
	}
	
	Map<String, String> getParamsMap() {
		return builder.paramsMap;
	}
	
	Map<String, String> getHeaderParamsMap() {
		return builder.headerParamsMap;
	}

	boolean isNonce() {
		return builder.nonce;
	}

	boolean isSignContentBody() {
		return builder.signContentBody;
	}

	boolean isTimestamp() {
		return builder.timestamp;
	}

	/**
	 * 显示所设置的各个属性值
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("requestUrl=").append(this.getRequestUrl());
		sb.append("\n api=").append(this.getApi());
		sb.append("\n version=").append(this.getVersion());
		sb.append("\n method=").append(this.getMethod());
		sb.append("\n accessKey=").append(this.getAccessKey());
		sb.append("\n secretKey=").append("*********"); // hide this secret key!
		sb.append("\n contentBody=").append(this.getContentBody());
		sb.append("\n Nonce=").append(this.isNonce());
		//sb.append("\n signContentBody=").append(this.isSignContentBody());
		sb.append("\n Timestamp=").append(this.isTimestamp());
		sb.append("\n params: \n");
		for (Entry<String, String> entry : builder.paramsMap.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
		}
		
		sb.append("\n http header params: \n");
		for (Entry<String, String> entry : builder.headerParamsMap.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
		}

		return sb.toString();
	}

	/**
	 * 内部静态类，用来设置HttpCaller调用的相关参数
	 */
	public static class Builder {
		private String api;
		private String version;
		private String ak;
		private String sk;
		private String restfulProtocolVersion;
		private String method = "GET";
		private ContentBody contentBody = null;
		private String requestUrl;
		private boolean nonce;
		private boolean timestamp = true;
		private boolean signContentBody = false;
		private Map<String, String> paramsMap = new HashMap<String, String>();
		private Map<String, String> headerParamsMap = new HashMap<String, String>();

		/**
		 * 设置服务的api名
		 * @param api
		 * @return
		 */
		public Builder api(String api) {
			this.api = api;
			return this;
		}

		/**
		 * 设置服务的版本
		 * @param version
		 * @return
		 */
		public Builder version(String version) {
			this.version = version;
			return this;
		}

		/**
		 * 设置安全参数ak
		 * @param ak
		 * @return
		 */
		public Builder accessKey(String ak) {
			this.ak = ak;
			return this;
		}

		/**
		 * 设置安全参数sk
		 * @param sk
		 * @return
		 */
		public Builder secretKey(String sk) {
			this.sk = sk;
			return this;
		}

		/**
		 * 设置open restful version，1.0 is enable restful path
		 * @param restfulProtocolVersion
		 * @return
		 */
		public Builder restfulProtocolVersion(String restfulProtocolVersion) {
			this.restfulProtocolVersion = restfulProtocolVersion;
			return this;
		}

		/**
		 * 设置调用的方式： 目前支持的取值是: get, post
		 * @param method
		 * @return
		 */
		public Builder method(String method) {
			if (!"get".equalsIgnoreCase(method) && !"post".equalsIgnoreCase(method) && 
					!"cget".equalsIgnoreCase(method) && !"cpost".equalsIgnoreCase(method)) {
				throw new IllegalArgumentException("只支持 'GET', 'CGET' or 'POST', 'CPOST' method类型");
			}
			this.method = method;
			return this;
		}


		/**
		 *
		 * @param timestampFlag, 是否生成时间戳，默认是生成的
		 * @return
		 */
		public Builder timestamp(boolean timestampFlag) {
			this.timestamp = timestampFlag;

			return this;
		}

		/**
		 *
		 * @param nonceFlag, 是否生成nonce header
		 * @return
		 */
		public Builder nonce(boolean nonceFlag) {
			this.nonce = nonceFlag;

			return this;
		}

		/**
		 * 设置HTTP请求的URL串
		 * @param url
		 * @return
		 */
		public Builder requestURL(String url) {
			this.requestUrl = url;
			return this;
		}

		/**
		 * 清除已经设置的参数对
		 * @return
		 */
		public Builder clearParamsMap() {
			this.paramsMap.clear();
			return this;
		}

		/**
		 * 设置一个参数对
		 * @param key
		 * @param value
		 * @return
		 */
		public Builder putParamsMap(String key, String value) {
			this.paramsMap.put(key, value);
			return this;
		}

		/**
		 * 设置参数对集合
		 * @param map
		 * @return
		 */
		public Builder putParamsMapAll(Map<String, String> map) {
			if (map != null) {
				this.paramsMap.putAll(map);
			} else {
				throw new IllegalArgumentException("empty map!!");
			}
			return this;
		}
		
		/**
		 * 清除所有已经设置的HTTP Header参数对
		 * @return
		 */
		public Builder clearHeaderParamsMap() {
			this.headerParamsMap.clear();
			return this;
		}
		
		/**
		 * 设置一个HTTP Header参数对
		 * @param key
		 * @param value
		 * @return
		 */
		public Builder putHeaderParamsMap(String key, String value) {
			this.headerParamsMap.put(key, value);
			return this;
		}

		/**
		 * 添加所有的Http Header参数对集合
		 * @param map
		 * @return
		 */
		public Builder putHeaderParamsMapAll(Map<String, String> map) {
			if (map != null) {
				this.headerParamsMap.putAll(map);
			} else {
				throw new IllegalArgumentException("empty map!!");
			}
			return this;
		}
		
		/**
		 * 设置contentBody
		 * @param cb
		 * @return
		 */
		public Builder contentBody(ContentBody cb) {
			this.contentBody = cb;
			return this;
		}


		/**
		 * 对ContentBody内设置的json串进行签名
		 * 注意： 对目前的任何CSB Broker版本，都还不支持对contentBody的签名验证，
		 * 所以设置该选择为true时，会导致验签失败！
		 * @param sign
		 * @return
		 */
		public Builder sginContentBody(boolean sign) {
			this.signContentBody = sign;
			return this;
		}

		/**
		 * 生成最终的参数集合
		 * @return
		 */
		public HttpParameters build() {
			return new HttpParameters(this);
		}
	}

	/**
	 * private作用域的参数构造器，防止外部调用生成该实例
	 * @param builder
	 */
	private HttpParameters(Builder builder) {
		this.builder = builder;
	}
	
	/**
	 * 构造一个参数生成器
	 * @return
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	public void validate() {
		if (this.getRequestUrl() == null)
			throw new IllegalArgumentException("Bad httpparameters: null requestUrl!");

		if (this.getApi() == null)
			throw new IllegalArgumentException("Bad httpparameters: null api!");
		
		if (this.getContentBody() != null) {
			if (!"post".equalsIgnoreCase(this.getMethod())) {
				throw new IllegalArgumentException("Bad httpparameters: method must be \"post\" when contentBody is set!");
			}
			if (this.getParamsMap() != null && this.getParamsMap().size()>0) {
				//support both contentBody and postParams
				// throw new IllegalArgumentException("Bad httpparameters: paramsMap must be empty when contentBody is set!");
			}
		}

		builder.api = trimWhiteSpaces(this.getApi());
		builder.version = trimWhiteSpaces(this.getVersion());
		builder.ak = trimWhiteSpaces(this.getAccessKey());
		builder.sk = trimWhiteSpaces(this.getSecretkey());
	}
}

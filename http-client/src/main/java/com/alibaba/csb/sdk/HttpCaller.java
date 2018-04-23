package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.internel.HttpClientFactory;
import com.alibaba.csb.sdk.internel.HttpClientHelper;
import com.alibaba.csb.sdk.security.SignUtil;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * SDK工具类，用来向服务端发送HTTP请求，请求支持POST/GET方式.
 * 如果提供了AccessKey和SecurityKey参数信息，它能够在内部将请求消息进行签名处理，然后由CSB服务端进行验证.
 * 
 * <pre>
 *  
 * {@code
 * import com.alibaba.csb.sdk.HttpCaller;
 * import com.alibaba.csb.sdk.HttpCallerException;
 * ...
 * 
 * (1) 直接调用方式 (已过期，不推荐)
 * 
 * Map<String,String> params = new HashMap<String,String>();
 *    
 * Object smd = ... // 一个具体的复杂对象
 * if (smd != null) {
 *   String data = JSON.toJSONString(smd); //转换为JSON String
 *   params.put("data", data);
 * }
 * 
 * // -- Tip: 如果调用者无法获得复杂对象参数类，则可以使用全map的方式设置json串，举例，对于json串
 * // {"f1":{"f11":"v11", "f12":["v121","v122"]}, "f2":"wiseking"}
 * // 它是可以通过如下的方式进行转换而来
 * Map<String,Object> map = new HashMap<String,Object>();
 * 
 * Map<String,Object> mapF1 = new HashMap<String,Object>();
 * mapF1.put("f11", "v11");
 * mapF1.put("f12", Arrays.asList("v121","v122"));
 * map.put("f1", mapF1);
 * 
 * map.put("f2", "wiseking");
 * String jsonData = JSON.toJSONString(map);
 * // -- Tip End
 * 
 * params.put("name", "abcd"); //普通的串对象
 * params.put("password", "abcd"); //普通的串对象
 * 
 *    
 * String requestURL = "http://gateway.abc.com:8086/CSB";
 * String API_NAME = "login_system";
 * String version = "1.0.0";
 * String ak = "xxxxxx";
 * String sk = "xxxxxx"; //用户安全校验的签名密钥对
 *    
 * try {
 *   String result = HttpCaller.doPost(requestURL, API_NAME, version, params, ak, sk);
 *    
 *   if (result != null) {
 *      //返回结果处理, 如转换为JSON对象
 *      ...
 *   }
 * } catch (HttpCallerException ie) {
 *      //print error
 * }}
 * 
 * (2) 也可以使用第二种Builder的方式构造调用参数，然后进行调用 <strong>（推荐用法）</strong>
 * import com.alibaba.csb.sdk.HttpParameters;
 * import com.alibaba.csb.sdk.HttpCaller;
 * import com.alibaba.csb.sdk.HttpCallerException;
 * 
 * HttpParameters.Builder builder = HttpParameters.newBuilder();
 *      
 * builder.requestURL("http://broker-ip:8086/CSB?arg0=123") // 设置请求的URL
 *      .api("test") // 设置服务名
 *      .version("1.0.0") // 设置版本号
 *      .method("get") // 设置调用方式, get/post
 *      .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
 *      
 *  // 设置请求参数
 *  builder.putParamsMap("key1", "value1");
 *  builder.putParamsMap("key2", "{\"a\":value1}"); // json format value
 *      
 *  //设置请求调用方式
 *  builder.method("get");
 *      
 *  //设置透传的HTTP Headers
 *  builder.putHeaderParamsMap("header1", "value1");
 *  builder.putHeaderParamsMap("header2", "value2");
 *      
 *  //进行调用 返回结果
 *  String result = null;
 *  try {
 *    result = HttpCaller.invoke(builder.build());
 *      
 *    //注：如果返回结果出现乱码(不能正常显示中文),可以使用串字符集转换方法进行转换
 *    result = HttpCaller.changeCharset(result);
 *  } catch (HttpCallerException e) {
 *    // error process
 *  }
 *      
 *  try {
 *      	// 重启设置请求参数
 *      	builder.clearParamsMap();
 *      	builder.putParamsMap("key1", "value1---new");
 *      	builder.putParamsMap("key2", "{\"a\":\"value1-new\"}");
 *      
 *      	// 使用post方式调用
 *      	builder.method("post");
 *      	HttpCaller.invoke(builder.build());
 *  } catch (HttpCallerException e) {
 *      	// error process
 *  }
 *      
 * (3) 如果使用json或者bytes内容的作为http body，使用下面的方法
 *     
 *  //构造ContentBody对象
 *  ContentBody cb = new ContentBody(jsonObject.toSring());
 *  //或者
 *  cb = new ContentBody(file2bytes);
 *     
 *     //ContentBody传递，要求使用<strong>post</strong>方式进行调用
 *     //<strong>如果需要传递请求参数 可以拼接到请求URL中，或者设置paramsMap参数由SDK内部进行拼接</strong>
 *     HttpParameters.Builder builder = HttpParameters.newBuilder();      
 *     builder.requestURL("http://broker-ip:8086/CSB?arg0=123") // 设置请求的URL,可以拼接URL请求参数
 *      .api("test") // 设置服务名
 *      .version("1.0.0") // 设置版本号
 *      .method("post") // 设置调用方式, 必须为 post
 *      .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
 *     
 *     builder.contentBody(cb);
 *      
 *      //进行调用，返回结果
 *      String result = null;
 *      try {
 *      	result = HttpCaller.invoke(builder.build());
 *      } catch (HttpCallerException e) {
 *      	// error process
 *      }     
 *     
 *  
 * 高级功能
 * 1. 设置代理地址
 *    String proxyHost = "...";
 *    int proxyPort = ...;
 *    HttpCaller.setProxyHost(proxyHost, proxyPort, null); //注意：本次设置只对本线程起作用
 *    ...
 *    HttpCaller.doPost(), doGet() or invoke();
 *
 * 2. 关于连接参数的设置：
 *   a. 可以为http/https设置以下的全局性系统参数： 
 *      -Dhttp.caller.connection.max          设置连接池的最大连接数，默认是20
 *      -Dhttp.caller.connection.timeout      设置连接超时时间（毫秒），默认是-1， 永不超时
 *      -Dhttp.caller.connection.so.timeout   设置读取超时时间（毫秒），默认是-1， 永不超时
 *      -Dhttp.caller.connection.cr.timeout   设置从连接池获取连接实例的超时（毫秒），默认是-1， 永不超时
 *      -Dhttp.caller.connection.async        设置使用nio,默认fasle:同步io,true:nio
 *      -Dhttp.caller.skip.connection.pool    如何设置为true,则不使用连接池。默认行为是false,使用连接池(支持长连接)
 *   b. 也可以使用下面的方法设置以上的某一个或者多个参数：
 *      Map<String,String> sysParams = new HashMap<String,String>();
 *      sysParams.put("http.caller.connection.timeout","3000"); //设置连接超时为3秒
 *      HttpCaller.setConnectionParams(sysParams); //注意：本次设置只对本线程起作用
 *      ...
 *      HttpCaller.doPost(), doGet() or invoke();
 * </pre>
 * 
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq@alibaba-inc.com 
 * 
 * @since 2016
 *
 */
public class HttpCaller {
	private static boolean warmupFlag = false;
	private static CloseableHttpClient HTTP_CLIENT = null;
	private static PoolingHttpClientConnectionManager connMgr = null;

	private static final String RESTFUL_PATH_SIGNATURE_KEY = "csb_restful_path_signature_key";
	private static final String DEFAULT_RESTFUL_PROTOCOL_VERSION = "1.0";
	private static final String RESTFUL_PROTOCOL_VERION_KEY = "restful_protocol_version";

	public static final Boolean DEBUG = Boolean.getBoolean("http.caller.DEBUG");
	// 设置是否使用连接池的开关 -Dhttp.caller.skip.connection.pool=true 不使用连接池
	public static final Boolean SKIP_CONN_POOL = Boolean.getBoolean("http.caller.skip.connection.pool");
	// 检查连接池中不可用的连接的间隔 (单位ms, 默认 100ms)
	private static final int VALIDATE_PERIOD = Integer.getInteger("http.caller.connection.validate.span", 100);  
	// 清除连接池中过期或者长时间限制的连接的时间间隔 (单位ms, 默认 5000ms)
	private static final int CLEAN_PERIOD = Integer.getInteger("http.caller.connection.clean.span", 5000);  
	private static final int MAX_CONNECTION_TIMEOUT = -1;  
	private static final int MAX_SOCKET_TIMEOUT = -1;    
	private static final int MAX_CR_TIMEOUT = -1;  
	private static final List<String> SUPPORTED_CONNECTION_PARAMS = Arrays.asList("http.caller.connection.max",
			"http.caller.connection.timeout",
			"http.caller.connection.so.timeout",
			"http.caller.connection.cr.timeout",
			"http.caller.connection.async");

	// TODO: must set truststore for ssl
	public static final String trustCA = System.getProperty("http.caller.ssl.trustca");

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static String defaultAK = null;
	private static String defaultSK = null;
	
	private static ThreadLocal<Boolean> toCurlCmd = new ThreadLocal<Boolean>();
	private static ThreadLocal<HttpHost> proxyConfigThreadLocal = new ThreadLocal<HttpHost>();

	private static final RequestConfig.Builder requestConfigBuilder = createConnBuilder();
	private static final ThreadLocal<RequestConfig.Builder> requestConfigBuilderLocal = new ThreadLocal<RequestConfig.Builder>();

	static {
		try {
			if (!SKIP_CONN_POOL) {
				// 设置连接池
				connMgr = HttpClientFactory.createConnManager();
				// 设置连接池大小
				String maxConn = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(0));
				if (maxConn != null) {
					try {
						int imaxConn = Integer.parseInt(maxConn);
						connMgr.setMaxTotal(imaxConn);
						connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
					} catch (Exception e) {
						// log it!
						throw new HttpCallerException(String.format("[ERROR] CSB-SDK failed to create connection pool with %d connections", maxConn));
					}
				}
				connMgr.setValidateAfterInactivity(VALIDATE_PERIOD);

				HTTP_CLIENT = HttpClientFactory.createCloseableHttpClient(connMgr);

				final IdleConnectionMonitorThread clearThread = new IdleConnectionMonitorThread(connMgr);
				clearThread.setDaemon(true);
				clearThread.start();
			} else {
				if (DEBUG) {
					System.out.println("[WARNING] skip using connection pool");
				}
			}
		} catch (HttpCallerException e) {
			HTTP_CLIENT = null;
			System.out.println("[WARNING] failed to create a pooled http client with the error : " + e.getMessage());
			if (DEBUG) {
				e.printStackTrace(System.out);
			}
		}
	}
	
	public static class IdleConnectionMonitorThread extends Thread {
	    private final HttpClientConnectionManager connMgr;
	    private volatile boolean shutdown;
	    
	    public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
	        super();
	        this.connMgr = connMgr;
	    }

	    @Override
	    public void run() {
	        try {
	            while (!shutdown) {
	                synchronized (this) {
	                    wait(CLEAN_PERIOD);
	                    // Close expired connections
	                    connMgr.closeExpiredConnections();
	                    // Optionally, close connections
	                    // that have been idle longer than 30 sec
	                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
	                }
	            }
	        } catch (InterruptedException ex) {
	            // terminate
	        }
	    }
	    
	    public void shutdown() {
	        shutdown = true;
	        synchronized (this) {
	            notifyAll();
	        }
	    }
	}

	private HttpCaller() {
	}
	
	/**
	 * 加载HttpSDK所需要的类 (如，签名相关的)
	 * 
	 * 注意：这是一个高时间代价的启动方法，建议在整个JVM范围内，在使用HttpCaller调用具体的服务前，调用且只调用一次
	 */
	public static synchronized void warmup() {
		if (warmupFlag) {
			return; 
		}
		SignUtil.sign(new HashMap<String,String>(), "sk");
		warmupFlag = true;
	}
	
	private static RequestConfig.Builder createConnBuilder() {
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		String CONN_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(1));
		String SO_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(2));
		String CR_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(3));
		
		// 设置连接超时
		int  iconnTimeout = MAX_CONNECTION_TIMEOUT;
		if (CONN_TIMEOUT != null) {
			try {
				iconnTimeout = Integer.parseInt(CONN_TIMEOUT);
			} catch (Exception e) {
				// log it!
			}
		}
		configBuilder.setConnectTimeout(iconnTimeout);
		// 设置读取超时
		int  isoTimeout = MAX_SOCKET_TIMEOUT;
		if (SO_TIMEOUT != null) {
			try {
				isoTimeout = Integer.parseInt(SO_TIMEOUT);
			} catch (Exception e) {
				// log it!
			}
		}
		configBuilder.setSocketTimeout(isoTimeout);
		// 设置从连接池获取连接实例的超时
		int  icrTimeout = MAX_CR_TIMEOUT;
		if (CR_TIMEOUT != null) {
			try {
				icrTimeout = Integer.parseInt(CR_TIMEOUT);
			} catch (Exception e) {
				// log it!
			}
		}
		configBuilder.setConnectionRequestTimeout(icrTimeout);
		// 在提交请求之前 测试连接是否可用
		// 不要使用下面这个过期的方法，它的效率会很低
		//configBuilder.setStaleConnectionCheckEnabled(true);
		
		// 设置cookie ignore 
		configBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
		//DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		return  configBuilder;
	}

	/**
	 * 为接下来的调用设置代理参数。 注意：本次设置只对本线程起作用
	 * @param hostname
	 * @param port
	 * @param scheme 如果设置为null时, scheme为 "http"
	 */
	public static void setProxyHost(final String hostname, final int port, final String scheme) {
		proxyConfigThreadLocal.set(new HttpHost(hostname, port, scheme));
	}
	
	/**
	 * 为接下来的调用设置新的连接参数。 注意：本次设置只对本线程起作用
	 * @param params
	 */
	public static void setConnectionParams(Map<String,String> params) {
		if (params == null || params.size() == 0) {
			requestConfigBuilderLocal.set(requestConfigBuilder);
		} else {
			RequestConfig.Builder connBuilder = createConnBuilder();
			for (Entry<String,String> es:params.entrySet()) {
				if(!SUPPORTED_CONNECTION_PARAMS.contains(es.getKey())){
				throw new IllegalArgumentException("error connection param:" + es.getKey());
				}
				if (connMgr != null && es.getKey().equals(SUPPORTED_CONNECTION_PARAMS.get(0))) {
					connMgr.setMaxTotal(Integer.parseInt(es.getValue()));
					
				} else if (es.getKey().equals(SUPPORTED_CONNECTION_PARAMS.get(1))) {
					connBuilder.setConnectTimeout(Integer.parseInt(es.getValue()));
				} else if (es.getKey().equals(SUPPORTED_CONNECTION_PARAMS.get(2))) {
					connBuilder.setSocketTimeout(Integer.parseInt(es.getValue()));
				} else if (es.getKey().equals(SUPPORTED_CONNECTION_PARAMS.get(3))) {
					connBuilder.setConnectionRequestTimeout(Integer.parseInt(es.getValue()));
				} 
				HttpClientHelper.printDebugInfo(String.format("set %s as %s",es.getKey(),es.getValue()));
			}
			requestConfigBuilderLocal.set(connBuilder);
		}
	}
	
	private static RequestConfig getRequestConfig() {
		RequestConfig.Builder rcBuilder = null;
		if(requestConfigBuilderLocal.get() == null) {
			rcBuilder = requestConfigBuilder;
		} else {
			rcBuilder = requestConfigBuilderLocal.get();
		}

		rcBuilder.setProxy(proxyConfigThreadLocal.get());

		return rcBuilder.build();
	}
	

	/**
	 * 当参数flag设置为true时， 使当前调用doGet/doPost的线程不做真实的调用而是生成curl命令请求串返回
	 * @param flag
	 */
	public static void setCurlResponse(boolean flag) {
		toCurlCmd.set(true);
	}
	
	private static boolean isCurlResponse() {
		return toCurlCmd.get()!=null && toCurlCmd.get() == true;
	}

	/**
	 * 设置默认的AK/SK, 以后发送请求可以使用该默认值进行签名,
	 * 
	 * 注意： 这个方法设置静态的accessKey,secretKey变量到HttpCaller中，所以会影响所有的使用HttpCaller的方法，即
	 * 如果调用方法不指定AK/SK, 会使用这里本方法设置的AK/SK.
	 * 
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 */
	public static void setCredential(String accessKey, String secretKey) {
		defaultAK = accessKey;
		defaultSK = secretKey;
	}

	/**
	 * 把一个串的字符集从旧的的字符集到一个新的字符集合, 一个辅助方法，主要用于HTTP调用返回值的转换
	 * 
	 * @param result
	 *            要装换的字符串
	 * @param OldcharsetName
	 *            源编码方式
	 * @param charsetName
	 *            目标编码方式
	 * @return 返回转换后的字符串
	 * @throws HttpCallerException
	 */
	public static String changeCharset(String result, String OldcharsetName, String charsetName)
			throws HttpCallerException {
		if (result == null) {
			return result;
		}

		try {
			return new String(result.getBytes(OldcharsetName), charsetName);
		} catch (UnsupportedEncodingException e) {
			throw new HttpCallerException(e);
		}
	}

	/**
	 * 把一个串的字符集从"ISO-8859-1"改变到"UTF-8", 一个辅助方法，主要用于HTTP调用返回值的转换
	 * 
	 * @param result
	 *            要装换的字符串
	 * @return 返回转换后的字符串
	 * @throws HttpCallerException
	 */
	public static String changeCharset(String result) throws HttpCallerException {
		return changeCharset(result, "ISO-8859-1", DEFAULT_CHARSET);
	}

	/**
	 * 方法说明： 使用GET的方式发送请求服务，如果设置过默认AK/SK, 则使用它们将请求消息进行签名
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB
	 * @param apiName
	 *            API名字(服务名)
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 *             
	 * @deprecated 
	 *     1. 新版本（1.0.2.1+）的CSB服务需要定义服务版本参数
	 *     2. 推荐使用invoke()方法，并使用HttpParameters构造相关的参数            
	 * 
	 */
	public static String doGet(String requestURL, String apiName, Map<String, String> paramsMap)
			throws HttpCallerException {
		return doGet(requestURL, apiName, paramsMap, defaultAK, defaultSK);
	}

	/**
	 * 方法说明： 使用GET的方式发送请求服务，并使用指定的AK/SK将请求消息进行签名
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8080/test/abc
	 * @param apiName
	 *            API名字(服务名)
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常   
	 *              
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数 
	 */
	public static String doGet(String requestURL, String apiName, Map<String, String> paramsMap, String accessKey,
			String secretKey) throws HttpCallerException {
		return doGet(requestURL, apiName, null, paramsMap, accessKey, secretKey);
	}

	/**
	 * 使用GET的方式发送请求服务，并使用指定的AK/SK将请求消息进行签名
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
	 * @param apiName
	 *            API名字(服务名)
	 * @param version
	 *            API版本号
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值，如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理 
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 *             
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
	 *             
	 */
	public static String doGet(String requestURL, String apiName, String version, Map<String, String> paramsMap,
			String accessKey, String secretKey) throws HttpCallerException {
		HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version).putParamsMapAll(paramsMap)
				.accessKey(accessKey).secretKey(secretKey)
				.build();

		return doGet(hp, null, null);
	}

	private static String generateAsEncodeRequestUrl(String requestURL, Map<String, List<String>> urlParamsMap) {

		requestURL = HttpClientHelper.trimUrl(requestURL);

		StringBuffer params = new StringBuffer();
		for (Entry<String, List<String>> kv : urlParamsMap.entrySet()) {
			if (params.length() > 0) {
				params.append("&");
			}
			if (kv.getValue() != null) {
				List<String> vlist = kv.getValue();
				for (String v : vlist) {
					params.append(URLEncoder.encode(kv.getKey())).append("=").append(URLEncoder.encode(v));
				}
			}
		}

		String newRequestURL = requestURL;
		if (params.length() > 0)
			newRequestURL += "?" + params.toString();

		HttpClientHelper.printDebugInfo("-- requestURL=" + newRequestURL);
		return newRequestURL;
	}

	private static String doGet(HttpParameters hp, StringBuffer resHttpHeaders, Map<String, String> extSignHeadersMap) throws HttpCallerException {
		final String requestURL = hp.getRequestUrl();
		String apiName = hp.getApi();
		String version = hp.getVersion();
		Map<String, String> paramsMap = hp.getParamsMap();
		ContentBody cb = hp.getContentBody();
		String accessKey = hp.getAccessKey();
		String secretKey = hp.getSecretkey();
		Map<String, String> directParamsMap = hp.getHeaderParamsMap();
		String restfulProtocolVersion = hp.getRestfulProtocolVersion();
		boolean nonceFlag = hp.isNonce();

		long startT = System.currentTimeMillis();
		long initT = startT;
		HttpClientHelper.validateParams(apiName, accessKey, secretKey, paramsMap);

		Map<String, List<String>> urlParamsMap = HttpClientHelper.parseUrlParamsMap(requestURL, true);
		HttpClientHelper.mergeParams(urlParamsMap, paramsMap, true);
		if (DEBUG) {
			  HttpClientHelper.printDebugInfo("--+++ prepare params costs = " + (System.currentTimeMillis() - startT)+ " ms ");
			  startT = System.currentTimeMillis();
		}
		startProcessRestful(requestURL, restfulProtocolVersion, urlParamsMap);

		Map<String, String> headerParamsMap = HttpClientHelper.newParamsMap(urlParamsMap, apiName, version, accessKey,
				secretKey, hp.isTimestamp(), hp.isNonce() , extSignHeadersMap);

		endProcessRestful(restfulProtocolVersion, urlParamsMap, headerParamsMap);

		String newRequestURL = generateAsEncodeRequestUrl(requestURL, urlParamsMap);

		if (isCurlResponse()) {
			StringBuffer curl = new StringBuffer("curl ");
			curl.append(HttpClientHelper.genCurlHeaders(directParamsMap));
			curl.append(HttpClientHelper.genCurlHeaders(headerParamsMap));
			curl.append(" -k ");
			curl.append("\"").append(newRequestURL).append("\"");
			
			return curl.toString();
		}

		if (DEBUG) {
			  startT = System.currentTimeMillis();
		}	
		HttpGet httpGet = new HttpGet(newRequestURL);
		httpGet.setConfig(getRequestConfig());
		// first step to set the direct http headers
		if (directParamsMap != null)
			HttpClientHelper.setHeaders(httpGet, directParamsMap);

		// normal headers have the chance to overwrite the direct headers.
		HttpClientHelper.setHeaders(httpGet, headerParamsMap);
		if (accessKey != null && DEBUG) {
			HttpClientHelper.printDebugInfo("-- signature parameters are " + urlParamsMap);
		}
		try {
			return doHttpReq(requestURL, httpGet, resHttpHeaders);
		}finally {
			if (DEBUG) {
				HttpClientHelper.printDebugInfo("-- total = " + (System.currentTimeMillis() - initT) + " ms ");
			}
		}
	}

	private static void endProcessRestful(String restfulProtocolVersion, Map<String, List<String>> urlParamsMap, Map<String, String> headerParamsMap) {
		if(DEFAULT_RESTFUL_PROTOCOL_VERSION.equals(restfulProtocolVersion)){
			urlParamsMap.remove(RESTFUL_PATH_SIGNATURE_KEY);
			headerParamsMap.put(RESTFUL_PROTOCOL_VERION_KEY, DEFAULT_RESTFUL_PROTOCOL_VERSION);
		}
	}

	private static void startProcessRestful(String requestURL, String restfulProtocolVersion, Map<String, List<String>> urlParamsMap) throws HttpCallerException {
		if(DEFAULT_RESTFUL_PROTOCOL_VERSION.equals(restfulProtocolVersion)){
			String path = HttpClientHelper.getUrlPathInfo(requestURL);
			if(path == null){
				throw new HttpCallerException("this request is restful but the request path is　null !");
			}
			List<String> values = new ArrayList<String>();
			values.add(path);
			urlParamsMap.put(RESTFUL_PATH_SIGNATURE_KEY, values);
		}
	}

	private static boolean isSSLProtocol(String requestUrl) {
		if (requestUrl == null)
			return false;

		if (requestUrl.trim().toLowerCase().startsWith("https://")) {
			return true;
		}

		return false;
	}

	private static CloseableHttpClient createSyncHttpClient(String requestURL) throws HttpCallerException {
		CloseableHttpClient httpClient = null;
		if (isSSLProtocol(requestURL)) {
			try {
				httpClient = HttpClients.custom().setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				 	@Override
				 	public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException 
				 	{
				 	 	return true;
				 	}
				 	 	}).build()).setSSLHostnameVerifier(new org.apache.http.conn.ssl.NoopHostnameVerifier()).build();
			} catch (KeyManagementException e) {
				throw new HttpCallerException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new HttpCallerException(e);
			} catch (KeyStoreException e) {
				throw new HttpCallerException(e);
			}
		}else {
			httpClient = HttpClients.createDefault();
		}
		
		return httpClient;
	}
	private static CloseableHttpAsyncClient createAsyncHttpClient(String requestURL) throws HttpCallerException {
		CloseableHttpAsyncClient httpClient = null;
		if (isSSLProtocol(requestURL)) {
			try {

				httpClient = HttpAsyncClients.custom().setSSLHostnameVerifier(new org.apache.http.conn.ssl.NoopHostnameVerifier()).setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

					@Override
					public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
						return true;
					}
				}).build()).build();
			} catch (KeyManagementException e) {
				throw new HttpCallerException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new HttpCallerException(e);
			} catch (KeyStoreException e) {
				throw new HttpCallerException(e);
			}
		}else {
			httpClient = HttpAsyncClients.createDefault();
		}
		httpClient.start();
		return httpClient;
	}
	
	/**
	 * 以GET的方式发送URL请求
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如："http://abc.com:8086/CSB?name=test&value=123"
	 * @return 返回的JSON串
	 * @throws HttpCallerException
	 */
	public static String doGet(String requestURL) throws HttpCallerException {
		HttpGet httpGet = new HttpGet(requestURL);
		httpGet.setConfig(getRequestConfig());
		HttpClientHelper.printDebugInfo("requestURL=" + requestURL);

		return doHttpReq(requestURL,httpGet, null);
	}

	/**
	 * 使用POST方式调用HTTP服务
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
	 * @param apiName
	 *            API名字(服务名)
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 * @deprecated 
	 *     1. 新版本的CSB服务需要定义服务版本参数
	 *     2. 推荐使用invoke()方法，并使用HttpParameters构造相关的参数
	 */
	public static String doPost(String requestURL, String apiName, Map<String, String> paramsMap)
			throws HttpCallerException {
		return doPost(requestURL, apiName, null, paramsMap);
	}

	/**
	 * 使用POST方式调用HTTP服务
	 * 
	 * @param requestURL:
	 *            请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
	 * @param apiName:
	 *            API名字(服务名)
	 * @param version:
	 *            API版本号
	 * @param paramsMap:
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
	 * 
	 */
	public static String doPost(String requestURL, String apiName, String version, Map<String, String> paramsMap)
			throws HttpCallerException {
		return doPost(requestURL, apiName, version, paramsMap, defaultAK, defaultSK);
	}

	/**
	 * 使用POST方式调用HTTP服务
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
	 * @param apiName
	 *            API名字(服务名)
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 *             
	 *             
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
	 */
	public static String doPost(String requestURL, String apiName, Map<String, String> paramsMap, String accessKey,
			String secretKey) throws HttpCallerException {
		return doPost(requestURL, apiName, null, paramsMap, accessKey, secretKey);
	}

	/**
	 * 使用POST方式调用HTTP服务, 可以同时支持NameValuePair请求参数（拼接到请求URL中），并且传递contentBody
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
	 * @param apiName
	 *            API名字(服务名)
	 * @param version
	 *            API版本号
	 * @param cb
	 *            直接设置contentBody, 内容可以是json串 或者 byte[]
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常            
	 *             
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
	 * 
	 */
	public static String doPost(String requestURL, String apiName, String version, ContentBody cb, String accessKey,
			String secretKey) throws HttpCallerException {
		HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version)
				.contentBody(cb).accessKey(accessKey).secretKey(secretKey)
				.build();
		return doPost(hp, null, null);
	}

	/**
	 * 所有doPost的真正入口参数，httppost逻辑集成在这个方法中
	 * @param resHttpHeaders 是否返回http reponse headers, 如果请求参数不为空，会出现 {"_HTTP_HEADERS":[{"key":"value"}]}返回部分
	 * @return
	 * @throws HttpCallerException
	 */
	private static String doPost(HttpParameters hp, StringBuffer resHttpHeaders, Map<String, String> extSignHeadersMap) throws HttpCallerException {
		final String requestURL = hp.getRequestUrl();
		String apiName = hp.getApi();
		String version = hp.getVersion();
		Map<String, String> paramsMap = hp.getParamsMap();
		ContentBody cb = hp.getContentBody();
		String accessKey = hp.getAccessKey();
		String secretKey = hp.getSecretkey();
		Map<String, String> directHheaderParamsMap = hp.getHeaderParamsMap();
		String restfulProtocolVersion = hp.getRestfulProtocolVersion();
		boolean nonceFlag = hp.isNonce();

		long startT = System.currentTimeMillis();
		HttpClientHelper.validateParams(apiName, accessKey, secretKey, paramsMap);

		Map<String, List<String>> urlParamsMap = HttpClientHelper.parseUrlParamsMap(requestURL, true);
		String newRequestURL = generateAsEncodeRequestUrl(requestURL, urlParamsMap);
		HttpClientHelper.mergeParams(urlParamsMap, paramsMap, false);

		startProcessRestful(newRequestURL, restfulProtocolVersion, urlParamsMap);

		if (cb != null && cb.getContentType() == ContentBody.Type.JSON && hp.isSignContentBody()) {
			urlParamsMap.put(ContentBody.CONTENT_BODY_SIGN_KEY, Arrays.asList((String)cb.getContentBody()));
		}

		Map<String, String> headerParamsMap = HttpClientHelper.newParamsMap(urlParamsMap, apiName, version, accessKey,
				secretKey, true, nonceFlag, extSignHeadersMap);

		endProcessRestful(restfulProtocolVersion, urlParamsMap, headerParamsMap);

		if (isCurlResponse()) {
			return HttpClientHelper.createPostCurlString(newRequestURL, paramsMap, headerParamsMap, cb, directHheaderParamsMap);
		}

		HttpPost httpPost = HttpClientHelper.createPost(newRequestURL, paramsMap, headerParamsMap, cb);

		HttpClientHelper.setDirectHeaders(httpPost, directHheaderParamsMap);

		httpPost.setConfig(getRequestConfig());
		if (accessKey != null) {
			HttpClientHelper.printDebugInfo("signature parameters are " + urlParamsMap);
		}
		if (DEBUG) {
			HttpClientHelper.printDebugInfo("-- prepare time = " + (System.currentTimeMillis() - startT)+ " ms ");
		}

		try {
			return doHttpReq(newRequestURL, httpPost, resHttpHeaders);
		} finally {
			if (DEBUG) {
				HttpClientHelper.printDebugInfo("-- total = " + (System.currentTimeMillis() - startT) + " ms ");
			}
		}

	}

	private static String doHttpReq(String requestURL,HttpRequestBase httpRequestBase, StringBuffer sb)throws HttpCallerException {
		boolean async = isAsync();
		if(async){
			return doAsyncHttpReq(requestURL, httpRequestBase, sb);
		}else {
			return doSyncHttpReq(requestURL, httpRequestBase, sb);
		}

	}
	private static String doSyncHttpReq(String requestURL,HttpRequestBase httpRequestBase, final StringBuffer resHttpHeaders) throws HttpCallerException {
		if (DEBUG) {
			HttpClientHelper.printDebugInfo("doSyncHttpReq ");
		}
		long startT = System.currentTimeMillis();
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		if (HTTP_CLIENT != null) {
			httpClient = HTTP_CLIENT;
		} else {
			httpClient = createSyncHttpClient(requestURL);
		}
		if (DEBUG) {
			HttpClientHelper.printDebugInfo("--+++ get httpclient costs = " + (System.currentTimeMillis() - startT)+ " ms ");
			startT = System.currentTimeMillis();
		}
		try {
			try {
				response = httpClient.execute(httpRequestBase);
				fetchResHeaders(response, resHttpHeaders);
				return EntityUtils.toString(response.getEntity());
			} finally {
				if (response != null) {
					response.close();
				}
				//don't close the client for reusing
				if (HTTP_CLIENT == null) {
					httpClient.close();
				}
				if (DEBUG) {
					HttpClientHelper.printDebugInfo("-- http req & resp time = " + (System.currentTimeMillis() - startT)+ " ms ");
				}
			}
		} catch (Exception e) {
			throw new HttpCallerException(e);
		}
	}

	private static void fetchResHeaders(final HttpResponse response, final StringBuffer resHttpHeaders) {
		if (response != null && resHttpHeaders != null) {
			StringBuffer body = new StringBuffer();
			for (Header header:response.getAllHeaders()) {
				if(body.length() > 0)
					body.append(",");
				body.append(String.format("\"%s\":\"%s\"", header.getName(), header.getValue()));
			}
			
			resHttpHeaders.setLength(0);
			resHttpHeaders.append(String.format("{%s}", body.toString()));
		}
	}

	private static String doAsyncHttpReq(String requestURL,HttpRequestBase httpRequestBase, final StringBuffer resHttpHeaders) throws HttpCallerException {
		if (DEBUG) {
			HttpClientHelper.printDebugInfo("doAsyncHttpReq ");
		}

		long startT = System.currentTimeMillis();
		HttpResponse response = null;
		CloseableHttpAsyncClient httpClient = createAsyncHttpClient(requestURL);
		if (DEBUG) {
			HttpClientHelper.printDebugInfo("--+++ get httpclient costs = " + (System.currentTimeMillis() - startT)+ " ms ");
			startT = System.currentTimeMillis();
		}
		try {
			try {

				httpClient.start();
				Future<HttpResponse> asyncFuture = httpClient.execute(httpRequestBase, null);

				long waitTime = getFutureGetTimeOut();

				if (DEBUG) {
					HttpClientHelper.printDebugInfo("future waitTime :" + waitTime);
				}

				if(waitTime>0) {
					response = asyncFuture.get(waitTime, TimeUnit.MILLISECONDS);
				}else{
					response = asyncFuture.get();
				}
				fetchResHeaders(response, resHttpHeaders);
				return EntityUtils.toString(response.getEntity());
			} finally {
				httpClient.close();
				if (DEBUG) {
					HttpClientHelper.printDebugInfo("-- http req & resp time = " + (System.currentTimeMillis() - startT)+ " ms ");
				}
			}
		} catch (Exception e) {
			throw new HttpCallerException(e);
		}
	}

	/**
	 * 使用POST方式调用HTTP服务
	 * 
	 * @param requestURL
	 *            请求的服务URL, 如：http://abc.com:8086/CSB
	 * @param apiName
	 *            API名字(服务名)
	 * @param version
	 *            API版本号
	 * @param paramsMap
	 *            请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
	 * @param accessKey
	 *            访问key
	 * @param secretKey
	 *            安全key
	 * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
	 * @throws HttpCallerException
	 *             调用过程中发生的任何异常
	 *             
	 * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数          
	 */
	public static String doPost(String requestURL, String apiName, String version, Map<String, String> paramsMap,
			String accessKey, String secretKey) throws HttpCallerException {
		HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version).putParamsMapAll(paramsMap)
				.accessKey(accessKey).secretKey(secretKey)
				.build();
		return doPost(hp, null, null);
	}
	
	/**
	 * 使用invoke的方式进行http-api调用
	 * 
	 * @param hp 各种请求参数的集合类
	 * @param resHttpHeaders 当该传入参数不为空时，获取http response headers, {"key1":"value1","key2":"value2",...}
	 * @return 
	 * @throws HttpCallerException
	 */
	public static String invoke(HttpParameters hp, StringBuffer resHttpHeaders) throws HttpCallerException {
		if (hp == null)
			throw new IllegalArgumentException("null parameter!");
		HttpClientHelper.printDebugInfo("-- httpParameters=" + hp.toString());

		hp.validate();
		Map<String, String> extSignHeaders = new HashMap<String, String>();

		if ("POST".equalsIgnoreCase(hp.getMethod()) ||
				"CPOST".equalsIgnoreCase(hp.getMethod())) {
			return doPost(hp, resHttpHeaders, extSignHeaders);
		} else
			return doGet(hp,resHttpHeaders, extSignHeaders);
	}

	/**
	 * 使用invoke的方式进行http-api调用
	 * 
	 * @param hp
	 *            各种请求参数的集合类
	 * @return
	 * @throws HttpCallerException
	 */
	public static String invoke(HttpParameters hp) throws HttpCallerException {
		return invoke(hp, null);
	}

	private static final long MAX_FILE_SIZE = 10 * 1024l * 1024l; // 10M

	/**
	 * 一个便利方法，读取一个文件并把其内容转换为 byte[]
	 * 
	 * @param file
	 *            文件的全路径， 最大支持的上传文件的尺寸为10M
	 * @return
	 * @throws HttpCallerException
	 */
	//TODO: remove this unrelated method out of the class
	public static byte[] readFileAsByteArray(String file) throws HttpCallerException {
		File f = new File(file);
		if (f.exists() && f.isFile() && f.canRead()) {
			if (f.length() > MAX_FILE_SIZE)
				throw new HttpCallerException("file is too large exceed the MAX-SIZE: 10M");

			InputStream ios = null;
			ByteArrayOutputStream bos = null;
			byte[] buffer = null;
			try {
				ios = new FileInputStream(file);
				bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				int n;
				while ((n = ios.read(b)) != -1) {
					bos.write(b, 0, n);
				}
				buffer = bos.toByteArray();

			} catch (IOException e) {
				throw new HttpCallerException(e);
			} finally {
				try {
					if (ios != null)
						ios.close();
					if (bos != null)
						bos.close();
				} catch (IOException e) {
				}
			}
			return buffer;
		} else {
			throw new HttpCallerException("bad file to read:" + file);
		}
	}

	/**
	 * 把二进制数据恢复成文件
	 * 
	 * @param body
	 * @param filePath
	 * @param fileName
	 * @throws HttpCallerException
	 */
	//TODO: remove this unrelated method out of the class
	public static void recoveryFileFromBytes(byte[] body, String filePath, String fileName) throws HttpCallerException {
		try {
			String fileFullPath = filePath;
			if (fileFullPath.endsWith("/")) {
				fileFullPath += fileName;
			} else {
				fileFullPath = fileFullPath + "/" + fileName;
			}
			File f = new File(fileFullPath);
			FileOutputStream out = new FileOutputStream(f);
			try {
				out.write(body, 0, body.length);
				out.flush();
			} finally {
				if (out != null)
					out.close();
			}
		} catch (Exception e) {
			throw new HttpCallerException(e);
		}
	}

	/**
	 * 判断是否使用NIO,由系统变量http.caller.connection.async设置,默认不用
	 * @return
	 */
	public static boolean isAsync() {
		boolean async = false;
		String asyncConf = System.getProperty("http.caller.connection.async");
		if (asyncConf != null && asyncConf.length() > 0) {
			async = Boolean.valueOf(asyncConf);
		}
		return async;
	}

	/**
	 * 设置nio等待结果的时间,默认为3个等待时间总和外加0.1倍时间,0永不超时
	 * @return
	 */
	public static long getFutureGetTimeOut() {

		RequestConfig requestConfig = getRequestConfig();
		long waitTime = 0;

		int socketTimeOUt = requestConfig.getSocketTimeout();
		if (socketTimeOUt > 0) {
			waitTime = waitTime + socketTimeOUt;
		}

		int crTimeOut = requestConfig.getConnectionRequestTimeout();
		if (crTimeOut > 0) {
			waitTime = waitTime + crTimeOut;
		}

		int connTimeOut = requestConfig.getConnectTimeout();
		if (connTimeOut > 0) {
			waitTime = waitTime + connTimeOut;
		}

		return (long) (waitTime*1.1);
	}
}

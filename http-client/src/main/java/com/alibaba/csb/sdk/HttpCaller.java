package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.internel.DiagnosticHelper;
import com.alibaba.csb.sdk.internel.HttpClientConnManager;
import com.alibaba.csb.sdk.internel.HttpClientHelper;
import com.alibaba.csb.sdk.security.SignUtil;
import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.utils.IPUtils;
import com.alibaba.csb.utils.LogUtils;
import com.alibaba.csb.utils.TraceIdUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
 *      -Dhttp.caller.connection.max          设置连接池的最大连接数，默认是200
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
 * 3. 设置debug：
 *    -Dhttp.caller.DEBUG=true
 * </pre>
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq@alibaba-inc.com
 * @since 2016
 */
public class HttpCaller {
    protected static boolean warmupFlag = false;

    protected static final String RESTFUL_PATH_SIGNATURE_KEY = "csb_restful_path_signature_key";
    protected static final String DEFAULT_RESTFUL_PROTOCOL_VERSION = "1.0";
    protected static final String RESTFUL_PROTOCOL_VERION_KEY = "restful_protocol_version";


    // TODO: must set truststore for ssl
    public static final String trustCA = System.getProperty("http.caller.ssl.trustca");

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String GZIP = "gzip";

    protected static String defaultAK = null;
    protected static String defaultSK = null;

    protected static ThreadLocal<Boolean> toCurlCmd = new ThreadLocal<Boolean>();
    protected static ThreadLocal<HttpHost> proxyConfigThreadLocal = new ThreadLocal<HttpHost>();

    protected static final RequestConfig.Builder requestConfigBuilder = HttpClientConnManager.createConnBuilder();
    protected static final ThreadLocal<RequestConfig.Builder> requestConfigBuilderLocal = new ThreadLocal<RequestConfig.Builder>();

    protected static AtomicReference<String> BIZ_ID_KEY = new AtomicReference<String>();
    public static final long MAX_FILE_SIZE;

    static {
        //默认20M
        MAX_FILE_SIZE = Integer.getInteger("csb_max_file_size", 20) * 1024 * 1024;
    }

    protected HttpCaller() {
    }

    /**
     * init bizIdKey
     *
     * @param bizIdKey
     */
    public static void bizIdKey(String bizIdKey) {
        BIZ_ID_KEY.compareAndSet(null, bizIdKey);
    }

    /**
     * get bizIdkey
     *
     * @return
     */
    public static String bizIdKey() {
        String bizIdKey = BIZ_ID_KEY.get();
        return bizIdKey == null ? CsbSDKConstants.BIZID_KEY : bizIdKey;
    }

    /**
     * 加载HttpSDK所需要的类 (如，签名相关的)
     * <p>
     * 注意：这是一个高时间代价的启动方法，建议在整个JVM范围内，在使用HttpCaller调用具体的服务前，调用且只调用一次
     */
    public static synchronized void warmup() {
        if (warmupFlag) {
            return;
        }
        SignUtil.warmup();
        warmupFlag = true;
    }

    /**
     * 为接下来的调用设置代理参数。 注意：本次设置只对本线程起作用
     *
     * @param hostname
     * @param port
     * @param scheme   如果设置为null时, scheme为 "http"
     */
    public static void setProxyHost(final String hostname, final int port, final String scheme) {
        proxyConfigThreadLocal.set(new HttpHost(hostname, port, scheme));
    }

    /**
     * 为接下来的调用设置新的连接参数。 注意：本次设置只对本线程起作用
     *
     * @param params
     */
    public static void setConnectionParams(Map<String, String> params) {
        if (params == null || params.size() == 0) {
            requestConfigBuilderLocal.set(requestConfigBuilder);
        } else {
            requestConfigBuilderLocal.set(HttpClientConnManager.createConnBuilder(params));
        }
    }

    private static RequestConfig getRequestConfig() {
        RequestConfig.Builder rcBuilder = null;
        if (requestConfigBuilderLocal.get() == null) {
            rcBuilder = requestConfigBuilder;
        } else {
            rcBuilder = requestConfigBuilderLocal.get();
        }

        rcBuilder.setProxy(proxyConfigThreadLocal.get());

        return rcBuilder.build();
    }


    /**
     * 当参数flag设置为true时， 使当前调用doGet/doPost的线程不做真实的调用而是生成curl命令请求串返回
     *
     * @param flag
     */
    public static void setCurlResponse(boolean flag) {
        toCurlCmd.set(true);
    }

    private static boolean isCurlResponse() {
        return toCurlCmd.get() != null && toCurlCmd.get() == true;
    }

    /**
     * 设置默认的AK/SK, 以后发送请求可以使用该默认值进行签名,
     * <p>
     * 注意： 这个方法设置静态的accessKey,secretKey变量到HttpCaller中，所以会影响所有的使用HttpCaller的方法，即
     * 如果调用方法不指定AK/SK, 会使用这里本方法设置的AK/SK.
     *
     * @param accessKey 访问key
     * @param secretKey 安全key
     */
    public static void setCredential(String accessKey, String secretKey) {
        defaultAK = accessKey;
        defaultSK = secretKey;
    }

    /**
     * 把一个串的字符集从旧的的字符集到一个新的字符集合, 一个辅助方法，主要用于HTTP调用返回值的转换
     *
     * @param result         要装换的字符串
     * @param OldcharsetName 源编码方式
     * @param charsetName    目标编码方式
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
     * @param result 要装换的字符串
     * @return 返回转换后的字符串
     * @throws HttpCallerException
     */
    public static String changeCharset(String result) throws HttpCallerException {
        return changeCharset(result, "ISO-8859-1", DEFAULT_CHARSET);
    }

    /**
     * 方法说明： 使用GET的方式发送请求服务，如果设置过默认AK/SK, 则使用它们将请求消息进行签名
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB
     * @param apiName    API名字(服务名)
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 1. 新版本（1.0.2.1+）的CSB服务需要定义服务版本参数
     * 2. 推荐使用invoke()方法，并使用HttpParameters构造相关的参数
     */
    public static String doGet(String requestURL, String apiName, Map<String, String> paramsMap)
            throws HttpCallerException {
        return doGet(requestURL, apiName, paramsMap, defaultAK, defaultSK);
    }

    /**
     * 方法说明： 使用GET的方式发送请求服务，并使用指定的AK/SK将请求消息进行签名
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8080/test/abc
     * @param apiName    API名字(服务名)
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @param accessKey  访问key
     * @param secretKey  安全keyx
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doGet(String requestURL, String apiName, Map<String, String> paramsMap, String accessKey,
                               String secretKey) throws HttpCallerException {
        return doGet(requestURL, apiName, null, paramsMap, accessKey, secretKey);
    }

    /**
     * 使用GET的方式发送请求服务，并使用指定的AK/SK将请求消息进行签名
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName    API名字(服务名)
     * @param version    API版本号
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值，如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param accessKey  访问key
     * @param secretKey  安全key
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doGet(String requestURL, String apiName, String version, Map<String, String> paramsMap,
                               String accessKey, String secretKey) throws HttpCallerException {
        return doGet(requestURL, apiName, version, paramsMap, accessKey, secretKey, null, null);
    }

    /**
     * 使用GET的方式发送请求服务，并使用指定的AK/SK将请求消息进行签名
     *
     * @param requestURL     请求的服务URL, 如：http://abc.com:8086/CSB， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName        API名字(服务名)
     * @param version        API版本号
     * @param paramsMap      请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值，如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param accessKey      访问key
     * @param secretKey      安全key
     * @param signImpl       签名算法实现类名
     * @param verifySignImpl 验签算法实现类名
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doGet(String requestURL, String apiName, String version, Map<String, String> paramsMap,
                               String accessKey, String secretKey, String signImpl, String verifySignImpl) throws HttpCallerException {
        HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version).putParamsMapAll(paramsMap)
                .accessKey(accessKey).secretKey(secretKey).signImpl(signImpl).verifySignImpl(verifySignImpl)
                .build();

        return doGet(hp, null).response;
    }

    /**
     * 获取签名串
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName    API名字(服务名)
     * @param version    API版本号
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值，如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param accessKey  访问key
     * @param secretKey  安全key
     * @return 发送CSB请求需要增加的httpHeader，包含签名串等
     */
    public static Map<String, String> getCsbHeaders(String requestURL, String apiName, String version, Map<String, String> paramsMap, String accessKey, String secretKey) throws HttpCallerException {
        return getCsbHeaders(requestURL, apiName, version, paramsMap, accessKey, secretKey, null, null);
    }

    /**
     * 获取签名串
     *
     * @param requestURL     请求的服务URL, 如：http://abc.com:8086/CSB， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName        API名字(服务名)
     * @param version        API版本号
     * @param paramsMap      请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值，如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param accessKey      访问key
     * @param secretKey      安全key
     * @param signImpl       签名算法实现类名
     * @param verifySignImpl 验签算法实现类名
     * @return 发送CSB请求需要增加的httpHeader，包含签名串等
     */
    public static Map<String, String> getCsbHeaders(String requestURL, String apiName, String version, Map<String, String> paramsMap,
                                                    String accessKey, String secretKey, String signImpl, String verifySignImpl) throws HttpCallerException {
        Map<String, List<String>> urlParamsMap = HttpClientHelper.parseUrlParamsMap(requestURL, true);
        HttpClientHelper.mergeParams(urlParamsMap, paramsMap, true);
        return HttpClientHelper.newParamsMap(urlParamsMap, apiName, version, accessKey, secretKey,
                true, false, null, null, signImpl, verifySignImpl);
    }

    private static HttpReturn doGet(HttpParameters hp, Map<String, String> extSignHeadersMap) throws HttpCallerException {
        if (!hp.getHeaderParamsMap().containsKey(CsbSDKConstants.TRACEID_KEY)) {
            hp.getHeaderParamsMap().put(TraceData.TRACEID_KEY, TraceIdUtils.generate());
            hp.getHeaderParamsMap().put(TraceData.RPCID_KEY, TraceData.RPCID_DEFAULT);
        }
        final String requestURL = hp.getRequestUrl();
        String apiName = hp.getApi();
        String version = hp.getVersion();
        Map<String, String> paramsMap = hp.getParamsMap();
        String accessKey = hp.getAccessKey();
        String secretKey = hp.getSecretkey();
        Map<String, String> directParamsMap = hp.getHeaderParamsMap();
        String restfulProtocolVersion = hp.getRestfulProtocolVersion();

        HttpReturn ret = new HttpReturn();
        ret.diagnosticFlag = hp.isDiagnostic();
        long startT = System.currentTimeMillis();
        long initT = startT;
        DiagnosticHelper.setStartTime(ret, initT);
        HttpClientHelper.validateParams(apiName, accessKey, secretKey, paramsMap);

        Map<String, List<String>> urlParamsMap = HttpClientHelper.parseUrlParamsMap(requestURL, true);
        HttpClientHelper.mergeParams(urlParamsMap, paramsMap, true);
        if (SdkLogger.isLoggable()) {
            SdkLogger.print("--+++ prepare params costs = " + (System.currentTimeMillis() - startT) + " ms ");
        }
        startProcessRestful(requestURL, restfulProtocolVersion, urlParamsMap);

        StringBuffer signDiagnosticInfo = DiagnosticHelper.getSignDiagnosticInfo(ret);
        Map<String, String> headerParamsMap = HttpClientHelper.newParamsMap(urlParamsMap, apiName, version, accessKey,
                secretKey, hp.isTimestamp(), hp.isNonce(), extSignHeadersMap, signDiagnosticInfo, hp.getSignImpl(), hp.getVerifySignImpl());
        DiagnosticHelper.setSignDiagnosticInfo(ret, signDiagnosticInfo);

        endProcessRestful(restfulProtocolVersion, urlParamsMap, headerParamsMap);

        String newRequestURL = HttpClientHelper.generateAsEncodeRequestUrl(requestURL, urlParamsMap);


        if (isCurlResponse()) {
            StringBuffer curl = new StringBuffer("curl ");
            curl.append(HttpClientHelper.genCurlHeaders(directParamsMap));
            curl.append(HttpClientHelper.genCurlHeaders(headerParamsMap));
            curl.append(" -k ");
            curl.append("\"").append(newRequestURL).append("\"");
            ret.response = curl.toString();
            ;
            return ret;
        }

        DiagnosticHelper.calcRequestSize(ret, newRequestURL, null, null);
        HttpGet httpGet = new HttpGet(newRequestURL);
        httpGet.setConfig(getRequestConfig());
        // first step to set the direct http headers
        if (directParamsMap != null)
            HttpClientHelper.setHeaders(httpGet, directParamsMap);

        // normal headers have the chance to overwrite the direct headers.
        HttpClientHelper.setHeaders(httpGet, headerParamsMap);
        DiagnosticHelper.setRequestHeaders(ret, httpGet.getAllHeaders());

        String msg = null;
        try {
            ret = doHttpReq(requestURL, httpGet, ret);
            DiagnosticHelper.setEndTime(ret, System.currentTimeMillis());
            DiagnosticHelper.setInvokeTime(ret, System.currentTimeMillis() - initT);
            return ret;
        } catch (HttpCallerException e) {
            msg = e.getMessage();
            throw e;
        } finally {
            log(hp, startT, requestURL, ret, msg);
            if (SdkLogger.isLoggable()) {
                SdkLogger.print("-- total = " + (System.currentTimeMillis() - initT) + " ms ");
            }
        }
    }

    private static void endProcessRestful(String restfulProtocolVersion, Map<String, List<String>> urlParamsMap, Map<String, String> headerParamsMap) {
        if (DEFAULT_RESTFUL_PROTOCOL_VERSION.equals(restfulProtocolVersion)) {
            urlParamsMap.remove(RESTFUL_PATH_SIGNATURE_KEY);
            headerParamsMap.put(RESTFUL_PROTOCOL_VERION_KEY, DEFAULT_RESTFUL_PROTOCOL_VERSION);
        }
    }

    private static void startProcessRestful(String requestURL, String restfulProtocolVersion, Map<String, List<String>> urlParamsMap) throws HttpCallerException {
        if (DEFAULT_RESTFUL_PROTOCOL_VERSION.equals(restfulProtocolVersion)) {
            String path = HttpClientHelper.getUrlPathInfo(requestURL);
            if (path == null) {
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
                    public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
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
        } else {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    private static CloseableHttpAsyncClient createAsyncHttpClient(String requestURL) throws HttpCallerException {
        CloseableHttpAsyncClient httpClient = null;
        if (isSSLProtocol(requestURL)) {
            try {

                httpClient = HttpAsyncClients.custom().setSSLHostnameVerifier(new org.apache.http.conn.ssl.NoopHostnameVerifier()).setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
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
        } else {
            httpClient = HttpAsyncClients.createDefault();
        }
        httpClient.start();
        return httpClient;
    }

    /**
     * 以GET的方式发送URL请求
     *
     * @param requestURL 请求的服务URL, 如："http://abc.com:8086/CSB?name=test&value=123"
     * @return 返回的JSON串
     * @throws HttpCallerException
     */
    public static String doGet(String requestURL) throws HttpCallerException {
        HttpGet httpGet = new HttpGet(requestURL);
        httpGet.setConfig(getRequestConfig());
        HttpClientHelper.printDebugInfo("requestURL=" + requestURL);

        return doHttpReq(requestURL, httpGet, null).response;
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName    API名字(服务名)
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 1. 新版本的CSB服务需要定义服务版本参数
     * 2. 推荐使用invoke()方法，并使用HttpParameters构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, Map<String, String> paramsMap)
            throws HttpCallerException {
        return doPost(requestURL, apiName, null, paramsMap);
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL: 请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName:    API名字(服务名)
     * @param version:    API版本号
     * @param paramsMap:  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, String version, Map<String, String> paramsMap)
            throws HttpCallerException {
        return doPost(requestURL, apiName, version, paramsMap, defaultAK, defaultSK);
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName    API名字(服务名)
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @param accessKey  访问key
     * @param secretKey  安全key
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, Map<String, String> paramsMap, String accessKey,
                                String secretKey) throws HttpCallerException {
        return doPost(requestURL, apiName, paramsMap, accessKey, secretKey, null, null);
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL     请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName        API名字(服务名)
     * @param paramsMap      请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @param accessKey      访问key
     * @param secretKey      安全key
     * @param signImpl       签名算法实现类名
     * @param verifySignImpl 验签算法实现类名
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, Map<String, String> paramsMap, String accessKey,
                                String secretKey, String signImpl, String verifySignImpl) throws HttpCallerException {
        return doPost(requestURL, apiName, null, paramsMap, accessKey, secretKey, signImpl, verifySignImpl);
    }

    /**
     * 使用POST方式调用HTTP服务, 可以同时支持NameValuePair请求参数（拼接到请求URL中），并且传递contentBody
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName    API名字(服务名)
     * @param version    API版本号
     * @param cb         直接设置contentBody, 内容可以是json串 或者 byte[]
     * @param accessKey  访问key
     * @param secretKey  安全key
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, String version, ContentBody cb, String accessKey,
                                String secretKey) throws HttpCallerException {
        return doPost(requestURL, apiName, version, cb, accessKey, secretKey, null, null);
    }

    /**
     * 使用POST方式调用HTTP服务, 可以同时支持NameValuePair请求参数（拼接到请求URL中），并且传递contentBody
     *
     * @param requestURL     请求的服务URL, 如：http://abc.com:8086/CSB/abc， 如果URL里的请求参数有特殊字符(如 '&')，需要先将次值进行URL Encode处理
     * @param apiName        API名字(服务名)
     * @param version        API版本号
     * @param cb             直接设置contentBody, 内容可以是json串 或者 byte[]
     * @param accessKey      访问key
     * @param secretKey      安全key
     * @param signImpl       签名算法实现类名
     * @param verifySignImpl 验签算法实现类名
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, String version, ContentBody cb, String accessKey,
                                String secretKey, String signImpl, String verifySignImpl) throws HttpCallerException {
        HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version)
                .contentBody(cb).accessKey(accessKey).secretKey(secretKey).signImpl(signImpl).verifySignImpl(verifySignImpl)
                .build();
        return doPost(hp, null).response;
    }

    /**
     * 所有doPost的真正入口参数，httppost逻辑集成在这个方法中
     * resHttpHeaders 是否返回http reponse headers, 如果请求参数不为空，会出现 {"_HTTP_HEADERS":[{"key":"value"}]}返回部分
     *
     * @return
     * @throws HttpCallerException
     */
    private static HttpReturn doPost(HttpParameters hp, Map<String, String> extSignHeadersMap) throws HttpCallerException {
        if (!hp.getHeaderParamsMap().containsKey(CsbSDKConstants.TRACEID_KEY)) {
            hp.getHeaderParamsMap().put(TraceData.TRACEID_KEY, TraceIdUtils.generate());
            hp.getHeaderParamsMap().put(TraceData.RPCID_KEY, TraceData.RPCID_DEFAULT);
        }
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

        HttpReturn ret = new HttpReturn();
        ret.diagnosticFlag = hp.isDiagnostic();
        long startT = System.currentTimeMillis();
        DiagnosticHelper.setStartTime(ret, startT);
        HttpClientHelper.validateParams(apiName, accessKey, secretKey, paramsMap);

        Map<String, List<String>> urlParamsMap = HttpClientHelper.parseUrlParamsMap(requestURL, true);
        String newRequestURL = HttpClientHelper.generateAsEncodeRequestUrl(requestURL, urlParamsMap);
        HttpClientHelper.mergeParams(urlParamsMap, paramsMap, false);

        startProcessRestful(newRequestURL, restfulProtocolVersion, urlParamsMap);

        if (cb != null && hp.isSignContentBody()) { //判断body是否参与签名
            urlParamsMap.put(ContentBody.CONTENT_BODY_SIGN_KEY, Arrays.asList(cb.getContentBodyAsStr()));
        }

        StringBuffer signDiagnosticInfo = DiagnosticHelper.getSignDiagnosticInfo(ret);
        Map<String, String> headerParamsMap = HttpClientHelper.newParamsMap(urlParamsMap, apiName, version, accessKey,
                secretKey, true, nonceFlag, extSignHeadersMap, signDiagnosticInfo, hp.getSignImpl(), hp.getVerifySignImpl());
        DiagnosticHelper.setSignDiagnosticInfo(ret, signDiagnosticInfo);

        endProcessRestful(restfulProtocolVersion, urlParamsMap, headerParamsMap);

        if (isCurlResponse()) {
            return new HttpReturn(HttpClientHelper.createPostCurlString(newRequestURL, paramsMap, headerParamsMap, cb, directHheaderParamsMap));
        }
        DiagnosticHelper.calcRequestSize(ret, newRequestURL, paramsMap, cb);
        HttpPost httpPost = HttpClientHelper.createPost(newRequestURL, paramsMap, headerParamsMap, cb, hp.getAttachFileMap(), hp.getContentEncoding());
        DiagnosticHelper.setRequestHeaders(ret, httpPost.getAllHeaders());

        HttpClientHelper.setDirectHeaders(httpPost, directHheaderParamsMap);

        httpPost.setConfig(getRequestConfig());

        if (SdkLogger.isLoggable()) {
            SdkLogger.print("-- prepare time = " + (System.currentTimeMillis() - startT) + " ms ");
        }

        String msg = null;
        try {
            ret = doHttpReq(newRequestURL, httpPost, ret);
            DiagnosticHelper.setEndTime(ret, System.currentTimeMillis());
            DiagnosticHelper.setInvokeTime(ret, System.currentTimeMillis() - startT);
            return ret;
        } catch (HttpCallerException e) {
            msg = e.getMessage();
            throw e;
        } finally {
            log(hp, startT, requestURL, ret, msg);
            if (SdkLogger.isLoggable()) {
                SdkLogger.print("-- total = " + (System.currentTimeMillis() - startT) + " ms ");
            }
        }
    }

    private static HttpReturn doHttpReq(String requestURL, HttpRequestBase httpRequestBase, final HttpReturn ret) throws HttpCallerException {
        boolean async = isAsync();
        if (async) {
            return doAsyncHttpReq(requestURL, httpRequestBase, ret);
        } else {
            return doSyncHttpReq(requestURL, httpRequestBase, ret);
        }

    }

    private static HttpReturn doSyncHttpReq(String requestURL, HttpRequestBase httpRequestBase, final HttpReturn ret) throws HttpCallerException {
        if (SdkLogger.isLoggable()) {
            SdkLogger.print("doSyncHttpReq ");
        }
        HttpReturn rret = ret;
        if (ret == null) {
            rret = new HttpReturn();
        }

        long startT = System.currentTimeMillis();
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        if (HttpClientConnManager.HTTP_CLIENT != null) {
            httpClient = HttpClientConnManager.HTTP_CLIENT;
        } else {
            httpClient = createSyncHttpClient(requestURL);
        }
        if (SdkLogger.isLoggable()) {
            SdkLogger.print("--+++ get httpclient costs = " + (System.currentTimeMillis() - startT) + " ms ");
            startT = System.currentTimeMillis();
        }
        try {
            try {
                response = httpClient.execute(httpRequestBase);
                rret.httpCode = response.getStatusLine().getStatusCode();
                rret.responseHttpStatus = response.getStatusLine().toString();
                rret.responseHeaders = HttpClientHelper.fetchResHeaders(response);
                rret.respHttpHeaderMap = HttpClientHelper.fetchResHeaderMap(response);
                fetchResponseBody(response, rret);
                return rret;
            } finally {
                if (response != null) {
                    response.close();
                }
                //don't close the client for reusing
                if (HttpClientConnManager.HTTP_CLIENT == null) {
                    httpClient.close();
                }
                if (SdkLogger.isLoggable()) {
                    SdkLogger.print("-- http req & resp time = " + (System.currentTimeMillis() - startT) + " ms ");
                }
            }
        } catch (Exception e) {
            throw new HttpCallerException(e);
        }
    }

    private static HttpReturn doAsyncHttpReq(String requestURL, HttpRequestBase httpRequestBase, final HttpReturn ret) throws HttpCallerException {
        if (SdkLogger.isLoggable()) {
            SdkLogger.print("doAsyncHttpReq ");
        }

        HttpReturn rret = ret;
        if (ret == null) {
            rret = new HttpReturn();
        }

        long startT = System.currentTimeMillis();
        HttpResponse response = null;
        CloseableHttpAsyncClient httpClient = createAsyncHttpClient(requestURL);
        if (SdkLogger.isLoggable()) {
            SdkLogger.print("--+++ get httpclient costs = " + (System.currentTimeMillis() - startT) + " ms ");
            startT = System.currentTimeMillis();
        }
        try {
            try {

                httpClient.start();
                Future<HttpResponse> asyncFuture = httpClient.execute(httpRequestBase, null);

                long waitTime = getFutureGetTimeOut();

                if (SdkLogger.isLoggable()) {
                    SdkLogger.print("future waitTime :" + waitTime);
                }

                if (waitTime > 0) {
                    response = asyncFuture.get(waitTime, TimeUnit.MILLISECONDS);
                } else {
                    response = asyncFuture.get();
                }

                rret.httpCode = response.getStatusLine().getStatusCode();
                rret.responseHttpStatus = response.getStatusLine().toString();
                rret.responseHeaders = HttpClientHelper.fetchResHeaders(response);
                rret.respHttpHeaderMap = HttpClientHelper.fetchResHeaderMap(response);
                fetchResponseBody(response, rret);
                return rret;
            } finally {
                httpClient.close();
                if (SdkLogger.isLoggable()) {
                    SdkLogger.print("-- http req & resp time = " + (System.currentTimeMillis() - startT) + " ms ");
                }
            }
        } catch (Exception e) {
            throw new HttpCallerException(e);
        }
    }

    static private void fetchResponseBody(HttpResponse response, HttpReturn rret) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        String contentType = responseEntity.getContentType().getValue();
        if (contentType == null) {
            throw new RuntimeException("HTTP响应错误，无 Content-Type header");
        }
        contentType = contentType.toLowerCase();
        if (contentType.startsWith("text") || contentType.contains("json") || contentType.contains("xml")) {
            rret.response = EntityUtils.toString(responseEntity);
        } else {
            rret.responseBytes = EntityUtils.toByteArray(responseEntity);
        }
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL 请求的服务URL, 如：http://abc.com:8086/CSB
     * @param apiName    API名字(服务名)
     * @param version    API版本号
     * @param paramsMap  请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @param accessKey  访问key
     * @param secretKey  安全key
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, String version, Map<String, String> paramsMap,
                                String accessKey, String secretKey) throws HttpCallerException {
        return doPost(requestURL, apiName, version, paramsMap, accessKey, secretKey, null, null);
    }

    /**
     * 使用POST方式调用HTTP服务
     *
     * @param requestURL     请求的服务URL, 如：http://abc.com:8086/CSB
     * @param apiName        API名字(服务名)
     * @param version        API版本号
     * @param paramsMap      请求参数key-value参数列表，注：可以将JSON对象转换为String作为参数值
     * @param accessKey      访问key
     * @param secretKey      安全key
     * @param signImpl       签名算法实现类名
     * @param verifySignImpl 验签算法实现类名
     * @return 调用的返回值，按约定进行解析 (如 JOSN串转换成对象)
     * @throws HttpCallerException 调用过程中发生的任何异常
     * @deprecated 推荐使用<strong>invoke()</strong>方法，并使用<tt>HttpParameters</tt>构造相关的参数
     */
    public static String doPost(String requestURL, String apiName, String version, Map<String, String> paramsMap,
                                String accessKey, String secretKey, String signImpl, String verifySignImpl) throws HttpCallerException {
        HttpParameters hp = HttpParameters.newBuilder().requestURL(requestURL).api(apiName).version(version).putParamsMapAll(paramsMap)
                .accessKey(accessKey).secretKey(secretKey).signImpl(signImpl).verifySignImpl(verifySignImpl)
                .build();
        return doPost(hp, null).response;
    }

    /**
     * @param respHttpHeaderMap 当不为空时，会把所有http响应头放入此map
     */
    public static String invoke(HttpParameters hp, Map<String, String> respHttpHeaderMap) throws HttpCallerException {
        HttpReturn res = invokeReturn(hp);
        if (respHttpHeaderMap != null) {
            respHttpHeaderMap.putAll(res.respHttpHeaderMap);
        }

        return res.response;
    }

    /**
     * 使用invoke的方式进行http-api调用
     *
     * @param hp             各种请求参数的集合类
     * @param resHttpHeaders 当该传入参数不为空时，获取http response headers, {"key1":"value1","key2":"value2",...}
     * @return
     * @throws HttpCallerException
     */
    public static String invoke(HttpParameters hp, StringBuffer resHttpHeaders) throws HttpCallerException {
        HttpReturn res = invokeReturn(hp);
        if (resHttpHeaders != null && res.responseHeaders != null) {
            resHttpHeaders.setLength(0);
            resHttpHeaders.append(res.responseHeaders);
        }

        return res.response;
    }

    /**
     * 新方法，支持复杂的返回对象(包括诊断信息)
     *
     * @param hp
     * @return
     * @throws HttpCallerException
     */
    public static HttpReturn invokeReturn(HttpParameters hp) throws HttpCallerException {
        if (hp == null)
            throw new IllegalArgumentException("null parameter!");
        HttpClientHelper.printDebugInfo("-- httpParameters=" + hp.toString());

        hp.validate();
        Map<String, String> extSignHeaders = new HashMap<String, String>();

        if ("POST".equalsIgnoreCase(hp.getMethod()) ||
                "CPOST".equalsIgnoreCase(hp.getMethod())) {
            return doPost(hp, extSignHeaders);
        } else
            return doGet(hp, extSignHeaders);
    }

    /**
     * 使用invoke的方式进行http-api调用
     *
     * @param hp 各种请求参数的集合类
     * @return
     * @throws HttpCallerException
     */
    public static String invoke(HttpParameters hp) throws HttpCallerException {
        return invoke(hp, (StringBuffer) null);
    }

    /**
     * 一个便利方法，读取一个文件并把其内容转换为 byte[]
     *
     * @param file 文件的全路径， 最大支持的上传文件的尺寸为10M
     * @return
     * @throws HttpCallerException
     */
    //TODO: remove this unrelated method out of the class
    public static byte[] readFileAsByteArray(String file) throws HttpCallerException {
        return readFile(new File(file));
    }


    public static byte[] readFile(File file) {
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                return readInputStream(new FileInputStream(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("bad file to read:" + file);
        }
    }

    public static byte[] readInputStream(InputStream inputStream) {
        if (inputStream != null) {
            ByteArrayOutputStream bos = null;
            try {
                bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int n;
                while ((n = inputStream.read(b)) != -1) {
                    bos.write(b, 0, n);

                    if (bos.size() > MAX_FILE_SIZE) {
                        throw new IllegalArgumentException("attach file is too large exceed the MAX-SIZE");
                    }
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close();
                    if (bos != null)
                        bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new IllegalArgumentException("inputSteam must no null");
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
     *
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
     *
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

        return (long) (waitTime * 1.1);
    }

    private static void log(HttpParameters hp, long startTime, String requestUrl, HttpReturn httpReturn, String msg) {
        long endTime = System.currentTimeMillis();

        Map<String, String> headers = hp.getHeaderParamsMap();
        try {
            int qidx = requestUrl.indexOf("?");
            String url = qidx > -1 ? requestUrl.substring(0, qidx) : requestUrl;

            int cidx = url.indexOf(":");
            int pidx = url.indexOf(":", cidx + 3);
            if (pidx < 0) {
                pidx = url.indexOf("/", cidx + 3);
            }
            String dest = url.substring(cidx + 3, pidx);
            LogUtils.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", new Object[]{startTime, endTime, endTime - startTime
                    , "HTTP", IPUtils.getLocalHostIP(), dest
                    , headers.get(HttpCaller.bizIdKey()), headers.get(CsbSDKConstants.REQUESTID_KEY)
                    , headers.get(CsbSDKConstants.TRACEID_KEY), headers.get(CsbSDKConstants.RPCID_KEY)
                    , hp.getApi(), hp.getVersion()
                    , defaultValue(hp.getAccessKey()), defaultValue(hp.getSecretkey()), hp.getMethod()
                    , url, httpReturn.httpCode, httpReturn.responseHttpStatus, defaultValue(msg)});
        } catch (Throwable e) {
            LogUtils.exception(MessageFormat.format("csb invoke error, api:{0}, version:{1}", hp.getApi(), hp.getVersion()), e);
        }
    }

    private static String defaultValue(String val) {
        return val == null ? "" : val.trim();
    }

    public static void main(String[] args) {
        String s = "http://100.100.80.76/api/admin/ServiceRepositoryAPI";
        log(HttpParameters.newBuilder().build(), System.currentTimeMillis(), s, null, "ss");
    }
}

package com.alibaba.csb.ws.sdk;

import com.alibaba.csb.sdk.CsbSDKConstants;
import com.alibaba.csb.sdk.security.SignUtil;
import com.alibaba.csb.ws.sdk.internal.AxisStubDynamicProxyHandler;
import com.alibaba.csb.ws.sdk.internal.BindingDynamicProxyHandler;
import org.apache.axis.client.Stub;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bind accessKey, secretKey into the WS client caller (i.e. Dispath, Proxy)
 *
 * <p>
 * The tool adds the security headers (as http headers) internally when sending a soap
 * request, the headers include the following:
 *
 * <pre>
 * {@code
 * _api_name:api_name
 * _api_version:api_version
 * _api_access_key:abc   //the access key
 * _api_timestamp:1464603102715  //the current timestamp of invoker machine
 * _api_fingerprint:invoke   // the invocation web service method
 * _api_signature:FLHVBMMjaxMPUHy+2VCGPvRNcao= //the signature string calculated with ak, timestamp, fingerprint and sk
 * }
 * </pre>
 *
 * <pre>
 * {@code
 * Usage:
 *
 *   //obtain dispatch or proxy
 *   Dispatch patch = ...
 *
 *
 *   //bind the dispatch with ak/sk
 *   WSParams params = WSParams.create();
 *   params.accessKey("xxxxx");
 *   params.secretKey("xxxxx");
 *   params.api("xxxxx");
 *   params.version("xxxxx");
 *   dispatch = WSClientSDK.bind(dispatch, params);
 *
 *   //invoke the methods with the returned dispatch
 *   ret = dispatch.invoke(...);
 *
 * }
 * </pre>
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * @since 2016
 */
public class WSClientSDK {
    private static boolean warmupFlag = false;
    private static final String BOUND_HANDLER_KEY = "__DynamicProxyHandler";
    public static final boolean PRINT_SIGNINFO = Boolean.getBoolean("WSClientSDK.print.signinfo");
    private static AtomicReference<String> BIZ_ID_KEY = new AtomicReference<String>();

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
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
     * 给proxy/dispath 绑定ak/sk安全对
     *
     * @param proxy     客户端proxy或者dispatch
     * @param accessKey accessKey
     * @param secretKey secretKey
     * @return 封装了accessKey和secretKey的proxy或者dispath, 调用逻辑要使用这个返回进行WS方法调用
     * @throws WSClientException
     * @deprecated 使用 bind(T proxy, WSParams params)
     */
    public static <T> T bind(T proxy, String accessKey, String secretKey) throws WSClientException {
        return bind(proxy, accessKey, secretKey, null, null);
    }

    /**
     * 把签名和调用相关的参数绑定到ws客户端，以便调用时在SOAP请求中生成签名验证相关的http headers
     *
     * @param proxy
     * @param params
     * @param <T>
     * @return
     * @throws WSClientException
     */
    public static <T> T bind(T proxy, WSParams params) throws WSClientException {
        validateProxy(proxy);

        BindingDynamicProxyHandler handler = getHandler((BindingProvider) proxy);
        handler.setParams(params);
        return handler.bind(proxy);
    }

    public static <T extends Stub> Object bind(T proxy, WSParams params) throws WSClientException {
        AxisStubDynamicProxyHandler handler = new AxisStubDynamicProxyHandler();
        handler.setParams(params);
        return handler.bind(proxy);
    }

    /**
     * @param proxy
     * @param accessKey
     * @param secretKey
     * @param apiName
     * @param apiVersion
     * @param printHeaders
     * @param <T>
     * @return
     * @throws WSClientException
     * @deprecated 使用 bind(T proxy, WSParams params)
     */
    public static <T> T bind(T proxy, String accessKey, String secretKey, String apiName, String apiVersion, boolean printHeaders) throws WSClientException {
        validateProxy(proxy);

        BindingDynamicProxyHandler handler = getHandler((BindingProvider) proxy);
        WSParams params = WSParams.create().accessKey(accessKey).secretKey(secretKey).api(apiName).version(apiVersion).debug(printHeaders);
        handler.setParams(params);
        return handler.bind(proxy);
    }

    /**
     * 给proxy/dispath 绑定ak/sk安全对，及要调用的apiName和apiVersion
     *
     * @param proxy      客户端proxy或者dispatch
     * @param accessKey  accessKey
     * @param secretKey  secretKey
     * @param apiName    服务名
     * @param apiVersion 服务版本
     * @return 封装了accessKey和secretKey的proxy或者dispath, 调用逻辑要使用这个返回进行WS方法调用
     * @throws WSClientException
     * @deprecated 使用 bind(T proxy, WSParams params)
     */
    public static <T> T bind(T proxy, String accessKey, String secretKey, String apiName, String apiVersion) throws WSClientException {
        return bind(proxy, accessKey, secretKey, apiName, apiVersion, false);
    }

    /**
     * 设置直接从服务端返回预定义的MockResponse
     *
     * @param proxy  客户端proxy或者dispatch
     * @param isMock 是否使用mock
     * @return 封装了mock标志的proxy或者dispath
     * @throws WSClientException
     * @deprecated 使用 bind(T proxy, WSParams params) 使用WSParams来设置是否为mockResponse
     */
    public static <T> T setResponseMock(T proxy, boolean isMock) throws WSClientException {
        validateProxy(proxy);

        BindingDynamicProxyHandler handler = getHandler((BindingProvider) proxy);
        handler.setMock(isMock);
        return handler.bind(proxy);
    }

    private static BindingDynamicProxyHandler getHandler(BindingProvider bp) {
        BindingDynamicProxyHandler handler = (BindingDynamicProxyHandler) bp.getRequestContext().get(BOUND_HANDLER_KEY);
        if (handler != null) {
            return handler;
        }
        handler = new BindingDynamicProxyHandler();
        //hold the reference of the handler, make they have the same lifecycle
        bp.getRequestContext().put(BOUND_HANDLER_KEY, handler);

        return handler;
    }

    private static void validateProxy(Object proxy) throws WSClientException {
        if (proxy == null) {
            throw new WSClientException("proxy parameter is null");
        }

        if (!(proxy instanceof BindingProvider)) {
            throw new WSClientException("proxy is not a legal soap client");
        }
    }

    public static Map<String, String> genExtHeader(String fingerStr) {
        Map<String, String> extSignHeaderMap = new HashMap<String, String>();
        if (fingerStr != null) {
            extSignHeaderMap.put(CsbSDKConstants.HEADER_FINGERPRINT, fingerStr);
        }

        return extSignHeaderMap;
    }

    /**
     * 生成签名相关的HTTP所有请求头
     *
     * @param params
     * @return
     */
    public static Map<String, String> generateSignHeaders(WSParams params) {
        Map<String, String> extSignHeaderMap = genExtHeader(params.getFingerPrinter());
        Map<String, String> requestHeaders = SignUtil.newParamsMap(null, params.getApi(), params.getVersion()
                , params.getAk(), params.getSk(), params.isTimestamp(), params.isNonce(), extSignHeaderMap
                , null, params.getSignImpl(), params.getVerifySignImpl(), params.getSignAlgothrim());

        if (params.isMockRequest()) {
            requestHeaders.put(CsbSDKConstants.HEADER_MOCK, "true");
        }

        return requestHeaders;
    }
}

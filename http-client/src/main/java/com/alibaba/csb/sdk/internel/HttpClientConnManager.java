package com.alibaba.csb.sdk.internel;

import com.alibaba.csb.sdk.HttpCallerException;
import com.alibaba.csb.sdk.SdkLogger;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wiseking on 18/6/19.
 */
public class HttpClientConnManager {
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


    public static CloseableHttpClient HTTP_CLIENT = null;
    private static PoolingHttpClientConnectionManager connMgr = null;

    static {
        try {
            if (!SKIP_CONN_POOL) {
                // 设置连接池
                connMgr = HttpClientFactory.createConnManager();
                // 设置连接池大小
                String maxConnStr = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(0));
                connMgr.setMaxTotal(maxConnStr == null ? 200 : Integer.parseInt(maxConnStr));
                connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
                connMgr.setValidateAfterInactivity(VALIDATE_PERIOD);

                HTTP_CLIENT = HttpClientFactory.createCloseableHttpClient(connMgr);

                final IdleConnectionMonitorThread clearThread = new IdleConnectionMonitorThread(connMgr);
                clearThread.setDaemon(true);
                clearThread.start();
            } else {
                if (SdkLogger.isLoggable()) {
                    SdkLogger.print("[WARNING] skip using connection pool");
                }
            }
        } catch (HttpCallerException e) {
            HTTP_CLIENT = null;
            System.out.println("[WARNING] failed to create a pooled http client with the error : " + e.getMessage());
            if (SdkLogger.isLoggable()) {
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


    public static RequestConfig.Builder createConnBuilder() {
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        String CONN_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(1));
        String SO_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(2));
        String CR_TIMEOUT = System.getProperty(SUPPORTED_CONNECTION_PARAMS.get(3));

        // 设置连接超时
        int iconnTimeout = MAX_CONNECTION_TIMEOUT;
        if (CONN_TIMEOUT != null) {
            try {
                iconnTimeout = Integer.parseInt(CONN_TIMEOUT);
            } catch (Exception e) {
                // log it!
            }
        }
        configBuilder.setConnectTimeout(iconnTimeout);
        // 设置读取超时
        int isoTimeout = MAX_SOCKET_TIMEOUT;
        if (SO_TIMEOUT != null) {
            try {
                isoTimeout = Integer.parseInt(SO_TIMEOUT);
            } catch (Exception e) {
                // log it!
            }
        }
        configBuilder.setSocketTimeout(isoTimeout);
        // 设置从连接池获取连接实例的超时
        int icrTimeout = MAX_CR_TIMEOUT;
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
        return configBuilder;
    }

    public static RequestConfig.Builder createConnBuilder(Map<String, String> params) {
        RequestConfig.Builder connBuilder = createConnBuilder();
        for (Map.Entry<String, String> es : params.entrySet()) {
            if (!SUPPORTED_CONNECTION_PARAMS.contains(es.getKey())) {
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
            HttpClientHelper.printDebugInfo(String.format("set %s as %s", es.getKey(), es.getValue()));
        }

        return connBuilder;
    }
}

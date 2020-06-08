package com.alibaba.csb.sdk.internel;

import com.alibaba.csb.sdk.HttpCallerException;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class HttpClientFactory {
    public static final long MAX_KEEPALIVE_TIMEOUT = Long.getLong("http.caller.connection.keepalive.timeout", 75);

    private static ConnectionKeepAliveStrategy createKeepAliveStrategy() {
        return new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return MAX_KEEPALIVE_TIMEOUT * 1000;
            }
        };
    }

    public static CloseableHttpClient createCloseableHttpClient(PoolingHttpClientConnectionManager connManager)
            throws HttpCallerException {
        ConnectionKeepAliveStrategy myStrategy = createKeepAliveStrategy();
        CloseableHttpClient client;
        try {
            client = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setKeepAliveStrategy(myStrategy)
                    .build();
        } catch (Exception e) {
            throw new HttpCallerException("Failed to create httpclient: " + e.getMessage(), e);
        }

        return client;
    }

    /**
     * Create a connection pool which supports http and https socket
     *
     * @return
     * @throws HttpCallerException
     */
    public static PoolingHttpClientConnectionManager createConnManager() throws HttpCallerException {
        try {
            // ignore SSL certificate info with the below two setting:

            // 1. trust https server certificate always.
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws java.security.cert.CertificateException {
                    return true;
                }
            }).build();

            // 2. hostname verifier pass
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory).build();

            return new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } catch (Exception e) {
            throw new HttpCallerException("Failed to create httpclient: " + e.getMessage(), e);
        }
    }
}

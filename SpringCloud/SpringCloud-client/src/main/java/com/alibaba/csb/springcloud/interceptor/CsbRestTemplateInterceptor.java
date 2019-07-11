package com.alibaba.csb.springcloud.interceptor;

import com.alibaba.csb.springcloud.sdk.SignUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenhui on 19/7/4.
 */
public class CsbRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${csb.ak}")
    private String ak;
    @Value("${csb.sk}")
    private String sk;

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Map<String, String> formParams = null;
        Map<String, String> queryParams = null;
        Map<String, String> csbParams = null;

        HttpHeaders httpHeaders = request.getHeaders();

        csbParams = this.getCsbParams(httpHeaders);

        if (SignUtils.isCsbRequest(csbParams)) {
            MediaType mediaType = httpHeaders.getContentType();
            if (mediaType != null && MediaType.APPLICATION_FORM_URLENCODED.getSubtype().equals(mediaType.getSubtype())) {
                Charset charSet = mediaType.getCharset();
                formParams = this.getFormParams(body, charSet);
            }

            queryParams = this.getQueryParams(request);

            String signStr = SignUtils.constructSignStr(formParams, queryParams, csbParams);
            String signedStr = SignUtils.sign(signStr, this.sk);

            httpHeaders.add(SignUtils.SIGNATURE_KEY, signedStr);
            httpHeaders.add(SignUtils.TIMESTAMP_KEY, csbParams.get(SignUtils.TIMESTAMP_KEY));
            httpHeaders.add(SignUtils.ACCESS_KEY, csbParams.get(SignUtils.ACCESS_KEY));
        }
        return execution.execute(request, body);

    }

    private Map<String, String> getFormParams(byte[] body, Charset charSet) {
        Map<String, String> formParams = new HashMap<String, String>();

        List<NameValuePair> result = URLEncodedUtils.parse(new String(body), charSet);
        for (NameValuePair nameValuePair : result) {
            formParams.put(nameValuePair.getName(), nameValuePair.getValue());
        }

        return formParams;

    }

    private Map<String, String> getQueryParams(HttpRequest request) {
        Map<String, String> queryParams = new HashMap<String, String>();

        List<NameValuePair> params = URLEncodedUtils.parse(request.getURI(), Charset.defaultCharset());
        for (NameValuePair s : params) {
            queryParams.put(s.getName(), s.getValue());
        }

        return queryParams;

    }

    private Map<String, String> getCsbParams(HttpHeaders headers) {
        Map<String, String> signParams = new HashMap<String, String>();
        String[] signKeys = {SignUtils.API_NAME_KEY, SignUtils.API_VERSION_KEY};
        for (int i = 0; i < signKeys.length; i++) {
            String signKey = signKeys[i];
            Collection<String> values = headers.get(signKey);
            if (values != null && !values.isEmpty()) {
                signParams.put(signKey, values.iterator().next());
            }
        }

        signParams.put(SignUtils.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        signParams.put(SignUtils.ACCESS_KEY, this.ak);
        return signParams;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }


    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }
}

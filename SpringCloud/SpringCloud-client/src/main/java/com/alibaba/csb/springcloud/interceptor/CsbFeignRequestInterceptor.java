package com.alibaba.csb.springcloud.interceptor;

import com.alibaba.csb.springcloud.sdk.SignUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.form.ContentType;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wenhui on 19/7/3.
 */
@Configuration
public class CsbFeignRequestInterceptor implements RequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${csb.ak}")
    private String ak;
    @Value("${csb.sk}")
    private String sk;

    public void apply(RequestTemplate template) {
        Map<String, String> formParams = null;
        Map<String, String> queryParams = null;
        Map<String, String> csbParams = null;

        Map<String, Collection<String>> headers = template.headers();

        csbParams = this.getCsbParams(headers);

        if (SignUtils.isCsbRequest(csbParams)) {
            String contentTypeValue = SignUtils.getContentTypeValue(headers);
            ContentType contentType = ContentType.of(contentTypeValue);
            if (contentType != null && contentType == ContentType.URLENCODED) {
                Charset charSet = SignUtils.getCharset(contentTypeValue);
                formParams = this.getFormParams(template, charSet);
            }

            queryParams = this.getQueryParams(template);
            String signStr = SignUtils.constructSignStr(formParams, queryParams, csbParams);
            String signedStr = SignUtils.sign(signStr, this.sk);

            template.header(SignUtils.SIGNATURE_KEY, signedStr);
            template.header(SignUtils.TIMESTAMP_KEY, csbParams.get(SignUtils.TIMESTAMP_KEY));
            template.header(SignUtils.ACCESS_KEY, csbParams.get(SignUtils.ACCESS_KEY));
        }
    }

    private Map<String, String> getFormParams(RequestTemplate template, Charset charSet) {

        Map<String, String> formParams = new HashMap<String, String>();

        byte[] bodyData = template.requestBody().asBytes();
        String bodyString = bodyData != null ? new String(bodyData) : null;

        List<NameValuePair> result = URLEncodedUtils.parse(bodyString, charSet);
        for (NameValuePair nameValuePair : result) {
            formParams.put(nameValuePair.getName(), nameValuePair.getValue());
        }

        return formParams;

    }

    private Map<String, String> getQueryParams(RequestTemplate requestTemplate) {
        Map<String, String> queryParams = new HashMap<String, String>();

        Map<String, Collection<String>> quries = requestTemplate.queries();
        for (Map.Entry<String, Collection<String>> s : quries.entrySet()) {
            Collection<String> values = s.getValue();
            if (values != null && !values.isEmpty()) {
                queryParams.put(s.getKey(), values.iterator().next());
            }
        }

        return queryParams;

    }

    private Map<String, String> getCsbParams(Map<String, Collection<String>> headers) {
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
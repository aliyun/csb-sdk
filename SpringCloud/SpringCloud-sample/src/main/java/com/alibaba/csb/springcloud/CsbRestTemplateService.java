package com.alibaba.csb.springcloud;

import com.alibaba.csb.springcloud.sdk.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenhui on 19/7/4.
 */
@Component
public class CsbRestTemplateService {
    @Value("${csb.broker.url}")
    private String csbURL;
    @Autowired
    private RestTemplate restTemplate;

    public String csbGetHttp2http1(String name, String times) {

        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2http1","1.0.0",
                null,
                data);
        String result =restTemplate.exchange(this.csbURL+"?name={name}&times={times}", HttpMethod.GET,request,String.class,data).getBody();

        return result;
    }
    public String csbPostHttp2http1(String name, String times) {

        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2http1","1.0.0",
                MediaType.APPLICATION_FORM_URLENCODED,
                data);
        return restTemplate.postForObject(this.csbURL, request, String.class);
    }
    public String csbHttp2http3(String name, String times) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2http3","1.0.0",
                MediaType.APPLICATION_FORM_URLENCODED,
                data);

        return restTemplate.postForObject(this.csbURL, request, String.class);
    }


    public String csbHttp2hsf1(String name, String times) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("arg0", name);
        data.add("arg1", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                "http2hsf1","1.0.0",
                MediaType.APPLICATION_FORM_URLENCODED,
                data);

        return restTemplate.postForObject(this.csbURL, request, String.class);
    }


    public String csbGetHttp2x(String apiName,String apiVersion,String name, String times) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", name);
        data.put("times", times);


        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                apiName,apiVersion,
                MediaType.APPLICATION_FORM_URLENCODED,
                null);
        String result =restTemplate.exchange(this.csbURL+"?name={name}&times={times}", HttpMethod.GET,request,String.class,data).getBody();
        return result;
    }


    public String csbPostHttp2x(String apiName,String apiVersion,String name, String times) {
        MultiValueMap<String, String> data = new LinkedMultiValueMap();
        data.add("name", name);
        data.add("times", times);

        HttpEntity<MultiValueMap<String, String>> request = this.createRequest(
                apiName,apiVersion,
                MediaType.APPLICATION_FORM_URLENCODED,
                data);

        return restTemplate.postForObject(this.csbURL, request, String.class);
    }

    private HttpEntity<MultiValueMap<String, String>> createRequest(
            String apiName, String apiVersion,
            MediaType mediaType,
            MultiValueMap<String, String> body

    ) {

        HttpHeaders httpHeaders = new HttpHeaders();
        if(mediaType!=null) {
            httpHeaders.setContentType(mediaType);
        }
        httpHeaders.set(SignUtils.API_NAME_KEY, apiName);
        httpHeaders.set(SignUtils.API_VERSION_KEY, apiVersion);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(body, httpHeaders);
        return request;
    }
}

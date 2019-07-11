package com.alibaba.csb.springcloud;

import com.alibaba.csb.springcloud.interceptor.CsbRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by wenhui on 19/7/4.
 */
@Component
public class CsbRestTemplateConfigration {
    @Value("${csb.ak}")
    private String ak;
    @Value("${csb.sk}")
    private String sk;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        CsbRestTemplateInterceptor csbInterceptor = new CsbRestTemplateInterceptor();
        csbInterceptor.setAk(this.ak);
        csbInterceptor.setSk(this.sk);


        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(csbInterceptor);

        return restTemplate;
    }
}

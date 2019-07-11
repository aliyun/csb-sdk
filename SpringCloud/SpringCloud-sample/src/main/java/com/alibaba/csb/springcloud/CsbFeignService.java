package com.alibaba.csb.springcloud;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Created by wenhui on 19/6/11.
 */
@FeignClient(value = "csb-aliyun-cn-shanghai-sufan",configuration = CsbFeignConfiguration.class)
public interface CsbFeignService {
    @RequestLine("GET /csb?name={name}&times={times}")
    @Headers({"_api_name: http2http1", "_api_version: 1.0.0"})
    String csbGetHttp2http1(@Param("name") String name, @Param("times") String times);
    @RequestLine("POST /csb")
    @Headers({"_api_name: http2http1", "_api_version: 1.0.0","Content-Type: application/json;charset=UTF-8"})
    String csbPostHttp2http1(UserInfo userInfo);
    @RequestLine("GET /csb?arg0={name}&arg1={times}")
    @Headers({"_api_name: http2hsf1", "_api_version: 1.0.0","Content-Type: application/x-www-form-urlencoded"})
    String csbGetHttp2hsf1(@Param("name") String name, @Param("times") String times);
    @RequestLine("GET /nginx_status")
    String csbNginxStatus();
}
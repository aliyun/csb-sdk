package com.alibaba.csb.springcloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
@RestController
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/checkhelth")
    public String checkHealth() {
        return "success";
    }

    @Autowired
    private CsbFeignService csbFeignService;

    @RequestMapping("/broker/nginxStatus")
    public String brokerNginxStatus() {
        return this.csbFeignService.csbNginxStatus();
    }

    @RequestMapping("/broker/feign/get/http2http1")
    public String feignGetHttp2http1(
            @RequestParam("name") String name,
            @RequestParam("times") String times) {
        return this.csbFeignService.csbGetHttp2http1(name, times);
    }
    @RequestMapping("/broker/feign/post/http2http1")
    public String feignPostHttp2http1(
            @RequestParam("name") String name,
            @RequestParam("times") String times) {
        UserInfo userInfo = new UserInfo();
        userInfo.setName(name);
        userInfo.setTimes(times);
        return this.csbFeignService.csbPostHttp2http1(userInfo);
    }

    @RequestMapping("/broker/feign/get/http2hsf1")
    public String feignGetHttp2hsf1(
            @RequestParam("name") String name,
            @RequestParam("times") String times) {
        return this.csbFeignService.csbGetHttp2hsf1(name, times);
    }

    @Autowired
    private CsbRestTemplateService csbRestTemplateService;

    @RequestMapping("/broke/resttemplate/get/http2http1")
    public String brokeRestGetTemplateHttp2http1(
            @RequestParam("name") String name,
            @RequestParam("times") String times) {
        return this.csbRestTemplateService.csbGetHttp2http1(name, times);
    }

    @RequestMapping("/broke/resttemplate/post/http2http1")
    public String brokeRestPostTemplateHttp2http1(
            @RequestParam("name") String name,
            @RequestParam("times") String times) {
        return this.csbRestTemplateService.csbPostHttp2http1(name, times);
    }
}

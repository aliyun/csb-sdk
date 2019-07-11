package com.alibaba.csb.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
@RestController
@SpringBootApplication
@EnableEurekaClient
public class ProviderApp {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class, args);
    }

    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return "Hello," + name;
    }
    @RequestMapping("/csb")
    public String csb() {
        return "success";
    }
}

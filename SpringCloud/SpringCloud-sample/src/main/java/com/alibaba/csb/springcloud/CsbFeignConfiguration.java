package com.alibaba.csb.springcloud;

import feign.Contract;
import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * Created by wenhui on 19/7/4.
 */
public class CsbFeignConfiguration {
    // 开启Feign的日志
    @Bean
    public Logger.Level logger() {
        return Logger.Level.FULL;
    }

    @Bean
    public Contract feignContract() {
        return new feign.Contract.Default();
    }

}

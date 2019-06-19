package com.alibaba.csb.sdk;

import org.junit.Test;

import java.io.File;

public class FileTest {
    @Test
    public void testPostFile() throws HttpCallerException {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:8086/CSB") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        // 设置请求参数
        builder.contentBody(new ContentBody(new File("D:\\tmp\\user-extend.jar")));
        try {
            String ret = HttpCaller.invoke(builder.build());
            System.out.println("------- ret=" + ret);
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }

    }

}

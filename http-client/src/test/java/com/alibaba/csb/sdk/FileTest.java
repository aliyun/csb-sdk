package com.alibaba.csb.sdk;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.File;

public class FileTest {

    /**
     * 使用body直接发送文件，响应body文件
     */
    @Test
    public void testPostBodyFile() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:8086/jsontest.jsp") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        try {
            // 设置请求参数
            builder.putParamsMap("name", "name1").putParamsMap("times", "3");
            builder.contentBody(new ContentBody(new File("D:\\tmp\\user-extend.jar")));

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

    /**
     * 使用body直接form请求，附件发送文件。响应json，附件是文件
     */
    @Test
    public void testPostFormFile() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:8086/jsontest.jsp") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        // 设置form请求参数
        builder.putParamsMap("times", "2").putParamsMap("name", "we中文we");
        builder.addAttachFile("file1", new File("D:\\tmp\\user-extend.jar"));
        builder.addAttachFile("file2", new File("D:\\tmp\\AuthenticationMapper.xml"));
        try {
            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

}

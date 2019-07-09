package com.alibaba.csb.sdk;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class FileZipTest {

    /**
     * 使用body发送json
     */
    @Test
    public void testPostForm() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:18086/http2http1") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        try {
            builder.setContentEncoding(ContentEncoding.gzip);

            // 设置请求参数
            builder.putParamsMap("name", "name1中文sdfs sdlkfsadfksdkfds").putParamsMap("times", "3");
            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

    /**
     * 使用body发送json
     */
    @Test
    public void testPostBodyJson() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:18086/http2http1") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        try {
            builder.setContentEncoding(ContentEncoding.gzip);

            // 设置请求参数
            builder.putParamsMap("name", "name1中文sdfs sdlkfsadfksdkfds").putParamsMap("times", "3");
            Map<String, String> kvMap = new HashMap<String, String>();
            for (int i = 0; i < 100; ++i) {
                kvMap.put(String.valueOf(i), "abc中文佛挡杀佛顶替枯lksd" + i);
            }
            builder.contentBody(new ContentBody(JSON.toJSONString(kvMap)));

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

    /**
     * 使用body直接发送文件，响应body文件
     */
    @Test
    public void testPostBodyFile() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:18086/CSB") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
        builder.setContentEncoding(ContentEncoding.gzip);

        try {
            // 设置请求参数
            builder.putParamsMap("name", "name中文1").putParamsMap("times", "3");
            builder.contentBody(new ContentBody(new File("D:\\tmp\\AuthenticationMapper.xml")));

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
    public void testPostFormFiles() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:18086/CSB") // 设置请求的URL
//        builder.requestURL("http://localhost:7001/jsontest.jsp") // 设置请求的URL
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
        builder.setContentEncoding(ContentEncoding.gzip);

        try {
            // 设置form请求参数
            builder.putParamsMap("times", "2").putParamsMap("name", "we中文wesdsfsfdsasdefds");
            builder.addAttachFile("file1", new File("D:\\tmp\\user-extend.jar"));
            builder.addAttachFile("file2", "fileName2", new FileInputStream(new File("D:\\tmp\\AuthenticationMapper.xml")), ContentEncoding.none); //对文件进行压缩传输

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
        } catch (Exception e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

}

package com.alibaba.csb.sdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileZipTest {

    /**
     * 使用body发送json
     */
    @Test
    public void testPostForm() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://localhost:18086/CSB") // 设置请求的URL
                .api("cas-1-1_csb_cas") // 设置服务名
//                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        try {
            builder.setContentEncoding(ContentEncoding.gzip);

            // 设置请求参数
            builder.putParamsMap("name", "name1中文sdfs sdlkfsadfksdkfds").putParamsMap("times", "3")
                    .putParamsMap("str2", "31", "32", "33").putParamsMap("str3", Arrays.asList("aa", "bb", "cc"));
            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret));
            System.out.println("------- ret=" + ret.getResponseStr());
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
        builder.requestURL("http://localhost:18086/CSB") // 设置请求的URL
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
                .api("cas-file-2-1_csb_cas") // 设置服务名
//                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
//        builder.setContentEncoding(ContentEncoding.gzip);

        try {
            // 设置请求参数
            builder.putParamsMap("name", "name中文1").putParamsMap("times", "3");
            builder.contentBody(new ContentBody(new File("D:\\temp\\csb-dev.jar")));

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret, SerializerFeature.PrettyFormat));
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
        builder.requestURL("http://localhost:8086/CSB") // 设置请求的URL
//        builder.requestURL("http://localhost:18086/jsontest.jsp") // 设置请求的URL
//                .api("cas-file-2-1_csb_cas") // 设置服务名
                .api("http2http1") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey
//        builder.setContentEncoding(ContentEncoding.gzip);

        try {
            // 设置form请求参数
            builder.putParamsMap("times", "2").putParamsMap("name", "we中文wesdsfsfdsasdefds");
//            builder.addAttachFile("file1", new File("D:\\temp\\csb-dev.jar"));
            builder.addAttachFile("file2", "fileName2", new FileInputStream(new File("D:\\temp\\fileTest.xml")), ContentEncoding.none); //对文件进行压缩传输

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret, SerializerFeature.PrettyFormat));
        } catch (Exception e) {
            // error process
            e.printStackTrace(System.out);
        }
    }

    /**
     * 使用body直接form请求，附件发送文件。响应json，附件是文件
     * 接入服务是 spring cloud
     */
    @Test
    public void testPostFormFilesSpringCloud() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
//        builder.requestURL("http://localhost:8086/1.0.0/http2nacos/postFoo/abc123?name=3") // 设置请求的URL
        builder.requestURL("http://localhost:18086/csb") // 设置请求的URL
                .api("http2http11") // 设置服务名
                .version("1.0.0") // 设置版本号
                .method("post") // 设置调用方式, get/post
                .contentType("text/plain;charset=GBK") //设置请求content-type
                .accessKey("8d3608fa4f2a45f496c74e928ee633d1").secretKey("FtyFQUcRz90ngqZ1JMWADAMirGg="); // 设置accessKey 和 设置secretKey
        ;
//        builder.setContentEncoding(ContentEncoding.gzip);

        try {
            // 设置form请求参数
            builder.putParamsMap("value1", "中文1");
            builder.putParamsMap("value2", "中文2");
            builder.putParamsMap("value3", "中文3");
            builder.putParamsMap("value4", "中文4");
            builder.putParamsMap("value5", "中文5");
            builder.putParamsMap("value6", "中文6");
            builder.addAttachFile("中文key2", "中文名2", new FileInputStream(new File("D:\\temp\\pom.xml")), ContentEncoding.none); //对文件进行压缩传输
            builder.addAttachFile("中文key1", "中文名1", new FileInputStream(new File("D:\\temp\\pom.xml"))); //对文件进行压缩传输
            builder.addAttachFile("中文key3", "中文名3", new FileInputStream(new File("D:\\temp\\pom.xml"))); //对文件进行压缩传输
            builder.addAttachFile("中文key4", "中文名4", new FileInputStream(new File("D:\\temp\\pom.xml"))); //对文件进行压缩传输
            builder.addAttachFile("中文key5", "中文名5", new FileInputStream(new File("D:\\temp\\pom.xml"))); //对文件进行压缩传输

            HttpReturn ret = HttpCaller.invokeReturn(builder.build());
            System.out.println("------- ret=" + JSON.toJSONString(ret, SerializerFeature.PrettyFormat));
        } catch (Exception e) {
            // error process
            e.printStackTrace(System.out);
        }
    }


}

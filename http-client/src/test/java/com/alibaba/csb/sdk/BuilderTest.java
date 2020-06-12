package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.SampleSignImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BuilderTest {
    @Before
    public void before() {
        System.setProperty("http.caller.DEBUG", "true");
        SimpleDateFormat tf = new SimpleDateFormat("HH-mm:ss:ms");
        System.out.println("warmup begin ... ctime=" + tf.format(new Date()));
        HttpCaller.warmup();
        System.out.println("warmup done dtime=" + tf.format(new Date()));
        HttpCaller.warmup();
        System.out.println("2nd warmup done dtime=" + tf.format(new Date()));
    }

    @Test
    public void testBuilder() {
        HttpParameters.Builder builder = new HttpParameters.Builder();

        builder.requestURL("http://localhost:8086?arg0=123") // 设置请求的URL
                .api("PING") // 设置服务名
                .version("vcsb") // 设置版本号
                .method("get") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置accessKey 和 设置secretKey

        builder.putHeaderParamsMap("name", "value");

        // 设置请求参数
        builder.putParamsMap("key1", "value1")
                .putParamsMap("name", "{\"a\":value1}"); // json format value
        builder.contentBody(new
                ContentBody("{\"a\":\"csb云服务总线\"}"));
        builder.method("post");
        try {
            String ret = HttpCaller.invoke(builder.build());
            System.out.println("------- ret=" + ret);
        } catch (HttpCallerException e) {
            // error process
            e.printStackTrace(System.out);
        }

        try {
            // 重启设置请求参数
            builder.clearParamsMap();
            builder.putParamsMap("key1", "value1---new")
                    .putParamsMap("key2", "{\"a\":\"value1-new\"}");

            // 使用post方式调用
            builder.method("post");
            HttpCaller.invoke(builder.build());
        } catch (HttpCallerException e) {
            // error process
        }
    }

    @Test
    public void testHttp2SpringCloud() {
        HttpParameters.Builder builder = new HttpParameters.Builder();
        builder.requestURL("http://11.162.130.197:8086/1.0.0/http2nacos2/postFoo/abc")
                .api("http2nacos2")
                .version("1.0.0")
                .method("post") // 设置调用方式, get/post
                .accessKey("ak").secretKey("sk"); // 设置AccessKeyID和AccessKeySecret

        // 设置HTTP FORM表单请求参数
        builder.putParamsMap("name", "name1").putParamsMap("value", "123");
        try {
            String ret = HttpCaller.invoke(builder.build());
            System.out.println(ret);
        } catch (HttpCallerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testhttpJson() {
        //URI uri = new URI("http://11.239.187.178:8086/test?arg1=1&arg0=<ApproveDataInfo><TableName>sp_shenqin</TableName><UseSJBBH>false</UseSJBBH><ZZJGDM>006939801</ZZJGDM><SXBM>10281300100693980112440000</SXBM></ApproveDataInfo>");

        Map<Long, Long> map = new HashMap<Long, Long>();
        map.put(1l, 1l);
        map.put(2l, 2l);
        map.put(3l, 3l);
        map.put(4l, 4l);
        String mapStr = JSON.toJSONString(map);
        String requestURL = "http://11.239.187.178:8086/test";
        String apiName = "ci-http2http";
        String version = "1.0.0";
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("arg0", "<ApproveDataInfo><TableName>sp_shenqin</TableName><UseSJBBH>false</UseSJBBH><ZZJGDM>006939801</ZZJGDM><SXBM>10281300100693980112440000</SXBM></ApproveDataInfo>");
        paramsMap.put("access", "1");
        try {

            String result = HttpCaller.doPost(requestURL, apiName, version, paramsMap);
            System.out.println(result.length() + result);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Test
    public void testJson() {
        String requestURL = "http://localhost:8086/CSB?name=wewe&times=2";
        String apiName = "http2http1";
        String version = "1.0.0";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("sleepSeconds", "1");
        try {
            String result = HttpCaller.doPost(requestURL, apiName, version, paramMap, "025dbf21f9a5406eb86a5991187e3868", "sk");
            System.out.println(result);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testURLWithSign() {
        String requestURL = "http://localhost:8086/CSB?name=wewe&times=2";
        String apiName = "http2http1";
        String version = "1.0.0";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("sleepSeconds", "1");
        try {
            Map<String, String> csbHeaders = HttpCaller.getCsbHeaders(requestURL, apiName, version, paramMap, "025dbf21f9a5406eb86a5991187e3868", "sk",
                    SampleSignImpl.class.getCanonicalName(), "com.alibaba.aosp.extension.security.SampleVerifySignImpl");
            System.out.println(csbHeaders);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 自定义签名算法示例
     */
    @Test
    public void testJsonWithSign() {
        String requestURL = "http://localhost:8086/CSB?name=wewe&times=2";
        String apiName = "http2http1";
        String version = "1.0.0";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("sleepSeconds", "1");
        try {
            String result = HttpCaller.doPost(requestURL, apiName, version, paramMap, "ak", "sk",
                    SampleSignImpl.class.getCanonicalName(), "com.alibaba.aosp.extension.security.SampleVerifySignImpl"); //与普通调用一样，除了增加 signImpl和verifySignImpl 两个参数。
            System.out.println(result);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testPostJsonStr() {
        String requestURL = "http://localhost:8086/CSB";
        String apiName = "http2hsfDto1";
        String version = "1.0.0";
        try {
            JSONObject request = new JSONObject();
            HashMap dto = new HashMap();
            dto.put("name", "name1");
            dto.put("age", 2);
            request.put("demoDTO", dto);
            request.put("count", 12);
            String result = HttpCaller.doPost(requestURL, apiName, version, new ContentBody(request.toJSONString()), "ak", "sk");
            System.out.println(result);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testfile() {
        try {
            byte[] bytes = HttpCaller.readFileAsByteArray("C:/Users/Public/Pictures/Sample Pictures/test.txt");
            File f = new File("C:/Users/Public/Pictures/Sample Pictures/test2.txt");
            FileOutputStream out = new FileOutputStream(f);

            try {
                out.write(bytes, 0, bytes.length);
                out.flush();
            } finally {
                out.close();
            }
        } catch (Exception e) {

        }
    }

    @Test
    public void testPostBytes() {

        String requestURL = "http://10.125.60.151:8086/test?fileName=result.txt&filePath=/home/admin/";
        String apiName = "httpfile";
        String version = "1.0.0";
        try {
            String result = HttpCaller.doPost(requestURL, apiName, version, new ContentBody(HttpCaller.readFileAsByteArray("/tmp/abc.log")), "ak", "sk");
            System.out.println(result);
        } catch (HttpCallerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //@Test
    public void testPostFile() throws HttpCallerException {
        String requestURL = "http://localhost:8088/api/uploadjar/Upload";
        String apiName = "abc";
        String version = "1.0.0";
        //String file = "/ltwork/depot/camel/assembly/target/csb-broker-1.0.4.1-SNAPSHOT.tar.gz";
        String file = "/ltwork/csb-install/httpsdk1.7.jar";
        byte[] fc = HttpCaller.readFileAsByteArray(file);
        HttpCaller.doPost(requestURL, apiName, version, new ContentBody(fc), "ak", "sk");
    }

    @Test
    public void testJsonMap() {
        String json = "{\"@type\":\"java.util.HashMap\",\"test\":{\"@type\":\"com.alibaba.csb.ws.def.ParamA\",\"accounts\":[\"aaaa\",\"bbbb\"],\"age\":1,\"name\":\"test\",\"sons\":[\"cccc\",\"ddd\"]}}";
        Map result = JSON.parseObject(json, new TypeReference<Map>() {
        }, new Feature[]{});

        System.out.println(result);

    }
}

package com.alibaba.csb;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tingbin.ctb
 * 2020/7/8-12:16.
 */
public class CsbSignature {
    public static final String HMACSHA1 = "HmacSHA1";
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 本方法生成http请求的csb签名值。
     * 调用csb服务时，需要在httpHeader中增加以下几个头信息：
     * _api_name: csb服务名
     * _api_version: csb服务版本号
     * _api_access_key: csb上的凭证ak
     * _api_timestamp: 时间戳
     * _api_signature: 本方法返回的签名串
     *
     * @param apiName      csb服务名
     * @param apiVersion   csb服务版本号
     * @param timeStamp    时间戳
     * @param accessKey    csb上的凭证ak
     * @param secretKey    csb上凭证的sk
     * @param formParamMap form表单提交的参数列表(各参数值是还未urlEncoding编码的原始业务参数值)。如果是form提交，请使用 Content-Type= application/x-www-form-urlencoded
     * @param body         非form表单方式提交的请求内容，目前没有参与签名计算
     * @return 签名串
     */
    public static String sign(String apiName, String apiVersion, long timeStamp, String accessKey, String secretKey, Map<String, Object[]> formParamMap, Object body) {
        TreeMap<String, Object[]> sortedParamMap = new TreeMap<String, Object[]>();
        if (formParamMap != null) {
            sortedParamMap.putAll(formParamMap);
        }

        //设置csb要求的头参数
        sortedParamMap.put("_api_name", new String[]{apiName});
        sortedParamMap.put("_api_version", new String[]{apiVersion});
        sortedParamMap.put("_api_access_key", new String[]{accessKey});
        sortedParamMap.put("_api_timestamp", new Object[]{timeStamp});

        //对所有参数进行排序
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object[]> entry : sortedParamMap.entrySet()) {
            for (Object value : entry.getValue()) {
                builder.append(entry.getKey()).append('=').append(value).append("&");
            }
        }

        String str = builder.toString();
        if (str.endsWith("&")) {
            str = str.substring(0, str.length() - 1); //去掉最后一个多余的 & 符号
        }
        try {
            Mac mac = Mac.getInstance(HMACSHA1);
            mac.init(new SecretKeySpec(secretKey.getBytes(CHARSET_UTF8), HMACSHA1));
            return Base64.encode(mac.doFinal(str.getBytes(CHARSET_UTF8))).trim();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithm: " + HMACSHA1, e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("secretKey invalidate", e);
        }
    }

    public static void main(String args[]) {
        Map<String, Object[]> formParamMap = new HashMap<String, Object[]>();
        formParamMap.put("name", new String[]{"中文name1"});
        formParamMap.put("times", new Object[]{123});
        formParamMap.put("multiValues", new Object[]{"abc", "efg"});
        long timeStamp = System.currentTimeMillis();
//        long timeStamp = 1594191880023L;

        //form表单提交的签名串生成示例
        String signature = sign("http2http11", "1.0.0", timeStamp, "ak", "sk", formParamMap, null);
        System.out.println("form signature::::: " + signature);

        //json或xml文本提交的签名串生成示例
        signature = sign("http2http11", "1.0.0", timeStamp, "ak", "sk", null, "{\"name\":\"中文name1\", \"times\":\"123\" }");
        System.out.println("json signature::::: " + signature);
    }
}

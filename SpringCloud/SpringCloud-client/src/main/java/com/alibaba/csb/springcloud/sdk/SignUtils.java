package com.alibaba.csb.springcloud.sdk;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static feign.form.util.CharsetUtil.UTF_8;

/**
 * Created by wenhui on 19/7/4.
 */
public class SignUtils {
    public static final String SIGN_ALGRISM_NAME = "HmacSHA1";

    public static final String CONTENT_TYPE_HEADER = "Content-Type";

    public static final Pattern CHARSET_PATTERN = Pattern.compile("(?<=charset=)([\\w\\-]+)");
    public static final String API_NAME_KEY = "_api_name";
    public static final String API_VERSION_KEY = "_api_version";
    public static final String ACCESS_KEY = "_api_access_key";
    public static final String SECRET_KEY = "_api_secret_key";
    public static final String SIGNATURE_KEY = "_api_signature";
    public static final String TIMESTAMP_KEY = "_api_timestamp";

    public static String getContentTypeValue(Map<String, Collection<String>> headers) {
        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
                continue;
            }
            for (String contentTypeValue : entry.getValue()) {
                if (contentTypeValue == null) {
                    continue;
                }
                return contentTypeValue;
            }
        }
        return null;
    }

    public static Charset getCharset(String contentTypeValue) {
        Matcher matcher = CHARSET_PATTERN.matcher(contentTypeValue);
        return matcher.find()
                ? Charset.forName(matcher.group(1))
                : UTF_8;
    }



    public static String constructSignStr(Map<String, String> formParams, Map<String, String> queryParams, Map<String, String> csbParams) {
        SortedMap<String, String> params = new TreeMap<String, String>();
        if(formParams!=null) {
            params.putAll(formParams);
        }
        if(queryParams!=null) {
            params.putAll(queryParams);
        }
        if(csbParams!=null) {
            params.putAll(csbParams);
        }
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() != 0) {
                builder.append("&");
            }
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
        }

        return builder.toString();
    }

    public static String sign(String content, String sk) {

        byte[] signedData = null;
        try {
            byte[] skBytes = sk.getBytes();

            SecretKey secretKey = new SecretKeySpec(skBytes, SignUtils.SIGN_ALGRISM_NAME);
            Mac mac = Mac.getInstance(SignUtils.SIGN_ALGRISM_NAME);
            //用给定密钥初始化 Mac 对象

            mac.init(secretKey);


            byte[] contentBytes = content.getBytes();
            //完成 Mac 操作
            signedData = mac.doFinal(contentBytes);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(signedData);

    }

    public static boolean isCsbRequest(Map<String, String> csbParams) {
        boolean result = false;
        if (csbParams != null && !csbParams.isEmpty()) {
            String apiName = csbParams.get(API_NAME_KEY);
            String apiVersion = csbParams.get(API_VERSION_KEY);
            result = isNotBlank(apiName) || isNotBlank(apiVersion);
        }
        return result;
    }


    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}

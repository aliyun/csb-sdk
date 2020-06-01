package com.alibaba.csb.sdk.internel;

import com.alibaba.csb.sdk.*;
import com.alibaba.csb.sdk.security.SignUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.Map.Entry;

import static com.alibaba.csb.sdk.HttpCaller.GZIP;
//import com.alibaba.fastjson.JSONObject;

/**
 * HttpClient Helper Class
 */
public class HttpClientHelper {
    public static void printDebugInfo(String msg) {
        if (SdkLogger.isLoggable())
            SdkLogger.print(msg);
    }

    public static Map<String, List<String>> convertStrMap2ListStrMap(Map<String, String> paramsMap) {
        if (paramsMap == null) {
            return null;
        }
        Map<String, List<String>> stringListMap = new HashMap<String, List<String>>((int) (paramsMap.size() * 1.5));
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            stringListMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return stringListMap;
    }

    public static void mergeParams(Map<String, List<String>> urlParamsMap, Map<String, String> paramsMap, boolean decodeFlag) throws HttpCallerException {
        mergeParamsList(urlParamsMap, convertStrMap2ListStrMap(paramsMap), decodeFlag);
    }

    public static void mergeParamsList(Map<String, List<String>> urlParamsMap, Map<String, List<String>> paramsMap, boolean decodeFlag) throws HttpCallerException {
        if (paramsMap != null) {
            //decode all params first, due to it will be encode to construct the request URL later
            for (Entry<String, List<String>> kv : paramsMap.entrySet()) {
                for (ListIterator<String> iter = kv.getValue().listIterator(); iter.hasNext(); ) {
                    iter.set(decodeValue(kv.getKey(), iter.next(), decodeFlag));
                }

                urlParamsMap.put(kv.getKey(), kv.getValue());
            }
        }
    }

    /**
     * 根据输入的参数，关键值和扩展签名头列表 生成签名并返回最终的签名头列表
     *
     * @param paramsMap
     * @param apiName
     * @param version
     * @param accessKey
     * @param securityKey
     * @param extSignHeaders 放在extSignHeaders里的kv都参与签名
     * @return
     */
    public static Map<String, String> newParamsMap(Map<String, List<String>> paramsMap, String apiName, String version,
                                                   String accessKey, String securityKey, boolean timestampFlag, boolean nonceFlag, Map<String, String> extSignHeaders, final StringBuffer signDiagnosticInfo,
                                                   String signImpl, String verifySignImpl) {
        return SignUtil.newParamsMap(paramsMap, apiName, version, accessKey, securityKey, timestampFlag, nonceFlag, extSignHeaders, signDiagnosticInfo, signImpl, verifySignImpl);
    }

    public static String trimWhiteSpaces(String value) {
        if (value == null) return value;

        return value.trim();
    }

    public static String trimUrl(String requestURL) {
        int pos = requestURL.indexOf("?");
        String ret = requestURL;

        if (pos >= 0) {
            ret = requestURL.substring(0, pos);
        }

        return ret;
    }

    public static void validateParams(String apiName, String accessKey, String securityKey, Map<String, List<String>> paramsMap) throws HttpCallerException {
        if (apiName == null)
            throw new HttpCallerException(new InvalidParameterException("param apiName can not be null!"));

        if (accessKey != null && securityKey == null)
            throw new HttpCallerException(
                    new InvalidParameterException("param securityKey can not be null for a given accessKey!"));

        if (paramsMap != null) {
            for (Entry<String, List<String>> kv : paramsMap.entrySet()) {
                if (kv.getValue() == null) {
                    throw new HttpCallerException(new InvalidParameterException(
                            String.format("bad parasMap, the value for key [ %s ] is null, please remove the key or set its value, e.g. \"\"!", kv.getKey())));
                }
            }
        }

    }

    private static String decodeValue(String key, String value, boolean decodeFlag) throws HttpCallerException {
        if (decodeFlag) {
            if (value == null) {
                throw new HttpCallerException("bad params, the value for key {" + key + "} is null!");
            }
            return urlDecoding(value, HTTP.UTF_8);
        }

        return value;
    }

    /**
     * Parse URL parameters to Map, url-decode all values
     *
     * @param requestURL
     * @return
     * @throws HttpCallerException
     */
    public static Map<String, List<String>> parseUrlParamsMap(String requestURL, boolean decodeFlag) throws HttpCallerException {
        boolean questionMarkFlag = requestURL.contains("?");
        Map<String, List<String>> urlParamsMap = new HashMap<String, List<String>>();
        String key;
        String value;
        if (questionMarkFlag) {
            // parse params
            int pos = requestURL.indexOf("?");
            String paramStr = requestURL.substring(pos + 1);
            // requestURL = requestURL.substring(0, pos);
            // The caller needs to ensure the url-encode for a parameter value!!
            String[] params = paramStr.split("&");
            for (String param : params) {
                pos = param.indexOf("=");
                if (pos <= 0) {
                    throw new HttpCallerException("bad request URL, url params error:" + requestURL);
                }
                key = decodeValue("", param.substring(0, pos), decodeFlag);
                value = param.substring(pos + 1);
                List<String> values = urlParamsMap.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                }
                values.add(decodeValue(key, value, decodeFlag));
                urlParamsMap.put(key, values);
            }
        }

        return urlParamsMap;
    }
//
//	public static StringEntity jsonProcess(Map<String, String> params) {
//		JSONObject jsonParam = new JSONObject();
//		for (Entry<String, String> entry : params.entrySet())
//			jsonParam.put(entry.getKey(), entry.getValue());
//
//		StringEntity entity = new StringEntity(jsonParam.toString(), HTTP.UTF_8);// 解决中文乱码问题
//		entity.setContentEncoding(HTTP.UTF_8);
//		entity.setContentType("application/json");
//		return entity;
//	}

    private static void setHeaders(HttpPost httpPost, Map<String, String> newParamsMap) {
        if (newParamsMap != null) {
            for (Entry<String, String> kv : newParamsMap.entrySet())
                httpPost.addHeader(kv.getKey(), kv.getValue());
        }
    }


    public static void setHeaders(HttpGet httpGet, Map<String, String> newParamsMap) {
        if (newParamsMap != null) {
            for (Entry<String, String> kv : newParamsMap.entrySet())
                httpGet.addHeader(kv.getKey(), kv.getValue());
        }
    }

    public static String genCurlHeaders(Map<String, String> newParamsMap) {
        if (newParamsMap != null) {
            StringBuffer sb = new StringBuffer();
            for (Entry<String, String> kv : newParamsMap.entrySet())
                sb.append("-H \"").append(kv.getKey()).append(":").append(kv.getValue()).append("\"  ");

            return sb.toString();
        } else
            return "";
    }

    public static String createPostCurlString(String url, Map<String, List<String>> params, Map<String, String> headerParams, ContentBody cb, Map<String, String> directHheaderParamsMap) {
        StringBuffer sb = new StringBuffer("curl ");

        //透传的http headers
        sb.append(genCurlHeaders(directHheaderParamsMap));

        sb.append(genCurlHeaders(headerParams));

        if (params != null) {
            StringBuffer postSB = new StringBuffer();
            for (Entry<String, List<String>> e : params.entrySet()) {
                if (postSB.length() > 0) {
                    postSB.append("&");
                }
                for (String value : e.getValue()) {
                    postSB.append(e.getKey()).append("=").append(urlEncoding(value, HTTP.UTF_8));
                }
            }
            if (postSB.length() > 0) {
                sb.append(" -d \"");
                postSB.append("\"");
                sb.append(postSB.toString());
            } else {
                sb.append("--data ''");
            }
        } else {
            // set params as
            //FIXME need this ??
            sb.append("--data '");
            sb.append(urlEncodedString(toNVP(params), HTTP.UTF_8));
            sb.append("'");
        }

        sb.append(" --insecure ");
        sb.append("\"");
        sb.append(url);
        sb.append("\"");
        return sb.toString();
    }

    public static String urlEncoding(String str, String encoding) {
        try {
            return URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static String urlDecoding(String str, String encoding) {
        try {
            return URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static String urlEncodedString(List<NameValuePair> parameters, String charset) {
        return URLEncodedUtils.format(parameters,
                charset != null ? charset : HTTP.DEF_CONTENT_CHARSET.name());
    }

    /**
     * 只能有以下组合：
     * 1. paramsMap: paramsMap以form表单方式提交
     * 2. contentbody: 以json或二进制的 body 方式提交
     * 3. paramsMap + contentBody:  paramsMap以query方式提交，contentBody通过httpBody提交
     * 4. paramsMap + attatchFileMap: multi part的 form 方式提交
     * 5. contentbody + attatchFileMap: 暂不支持，因为http协议需要给contentBody命名
     * 6. paramsMap+ contentbody + attatchFileMap: 暂不支持，因为http协议需要给contentBody命名
     *
     * @return
     */
    public static HttpPost createPost(final String url, Map<String, List<String>> urlParams, Map<String, String> headerParams, ContentBody cb, Map<String, HttpParameters.AttachFile> fileMap, ContentEncoding contentEncoding) {
        //set both cb and urlParams
        String newUrl = url;
        List<NameValuePair> nvps = toNVP(urlParams);
        if (cb != null && urlParams != null) {
            String newParamStr = urlEncodedString(nvps, HTTP.UTF_8);
            if ("".equals(newParamStr) == false) { //避免出现最后多一个&： http://ip:port/x?y=1&
                if (!url.contains("?")) {
                    newUrl = String.format("%s?%s", url, newParamStr);
                } else {
                    newUrl = String.format("%s&%s", url, newParamStr);
                }
            }
        }
        HttpPost httpost = new HttpPost(newUrl);
        setHeaders(httpost, headerParams);

        HttpEntity entity;
        try {
            if (fileMap != null && fileMap.isEmpty() == false) { //有附件，则使用 form+附件 提交
                MultipartEntityBuilder multiBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532); //http头 始终使用utf-8，解决附件文件名中文乱码
                for (NameValuePair nvp : nvps) {
                    String name = urlEncoding(nvp.getName(), HTTP.UTF_8);
                    String value = urlEncoding(nvp.getValue(), HTTP.UTF_8);
                    if (ContentEncoding.gzip.equals(contentEncoding)) {
                        byte[] bytes = GZipUtils.gzipBytes(value.getBytes(HttpCaller.DEFAULT_CHARSET));
                        org.apache.http.entity.mime.content.ContentBody body = new ByteArrayBody(bytes, ContentType.APPLICATION_FORM_URLENCODED.withCharset(HttpCaller.DEFAULT_CHARSET), null);
                        FormBodyPartBuilder partBuilder = FormBodyPartBuilder.create(name, body);
                        partBuilder.setField(HTTP.CONTENT_ENCODING, GZIP);
                        multiBuilder.addPart(partBuilder.build());
                    } else {
                        multiBuilder.addTextBody(name, value, ContentType.APPLICATION_FORM_URLENCODED.withCharset(HttpCaller.DEFAULT_CHARSET));
                    }
                }

                for (Entry<String, HttpParameters.AttachFile> fileEntry : fileMap.entrySet()) {
                    HttpParameters.AttachFile file = fileEntry.getValue();
                    if (ContentEncoding.gzip.equals(file.getContentEncoding())) { //对附件进行压缩
                        FormBodyPartBuilder partBuilder = FormBodyPartBuilder.create(fileEntry.getKey(), new ByteArrayBody(GZipUtils.gzipBytes(file.getFileBytes()), file.getFileName()));
                        partBuilder.setField(HTTP.CONTENT_ENCODING, GZIP);
                        multiBuilder.addPart(partBuilder.build());
                    } else {
                        multiBuilder.addBinaryBody(fileEntry.getKey(), file.getFileBytes(), ContentType.DEFAULT_BINARY, file.getFileName());
                    }
                }
                entity = multiBuilder.build();
            } else if (cb == null) { //无附件，无body内容，则使用form提交
                entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                if (ContentEncoding.gzip.equals(contentEncoding)) {
                    entity = new GzipCompressingEntity(entity);
                    httpost.setHeader(HTTP.CONTENT_ENCODING, GZIP);
                }
            } else {
                if (ContentEncoding.gzip.equals(contentEncoding)) {
                    httpost.setHeader(HTTP.CONTENT_ENCODING, GZIP); //不参与签名，因为服务端需要先解析这个头，然后才参获得实际内容。同时兼容历史版本
                }

                if (cb.getContentType().equals(ContentType.APPLICATION_JSON)) {  //无附件，有json body内容，则 application/json 方式提交
                    StringEntity strEntity = new StringEntity(cb.getStrContentBody(), HttpCaller.DEFAULT_CHARSET);// 解决中文乱码问题
                    strEntity.setContentType(ContentType.APPLICATION_JSON.toString());
                    entity = strEntity;
                    if (ContentEncoding.gzip.equals(contentEncoding)) {
                        entity = new GzipCompressingEntity(entity);
                    }
                } else {  //无附件，有二进制body内容，则 APPLICATION_OCTET_STREAM 方式提交
                    entity = new ByteArrayEntity(cb.getBytesContentBody(), cb.getContentType());
                    if (ContentEncoding.gzip.equals(contentEncoding)) {
                        entity = new GzipCompressingEntity(entity);
                    }
                }
            }

            httpost.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return httpost;
    }

    private static List<NameValuePair> toNVP(Map<String, List<String>> urlParams) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        //fix NPE
        if (urlParams != null) {
            Set<String> keySet = urlParams.keySet();
            for (String key : keySet) {
                for (String value : urlParams.get(key)) {
                    nvps.add(new BasicNameValuePair(key, value));
                }
            }
        }
        return nvps;
    }

    public static void setDirectHeaders(HttpPost httpPost, Map<String, String> directHheaderParamsMap) {
        if (directHheaderParamsMap == null) {
            //do nothing
            return;
        } else {
            for (Entry<String, String> kv : directHheaderParamsMap.entrySet()) {
                if (kv.getKey() == null) {
                    //log.info("ignore empty key");
                } else {
                    if (HTTP.CONTENT_TYPE.equals(kv.getKey()) || !httpPost.containsHeader(kv.getKey())) {
                        // direct header has no chance to overwrite the normal headers, except it is the content-type
                        httpPost.addHeader(kv.getKey(), kv.getValue());
                    }
                }
            }
        }
    }

    public static String getUrlPathInfo(String url) throws HttpCallerException {
        URL urlStr = null;
        try {
            urlStr = new URL(url);
        } catch (Exception e) {
            throw new HttpCallerException("url is unformat, url is " + url);
        }
        String path = urlStr.getPath();
        return path;
    }

    public static Map<String, String> fetchResHeaderMap(final HttpResponse response) {
        Map<String, String> headerMap = new HashMap<String, String>();
        if (response != null) {
            headerMap.put("HTTP-STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
            for (Header header : response.getAllHeaders()) {
                headerMap.put(header.getName(), header.getValue());
            }
        }

        return headerMap;
    }

    public static String fetchResHeaders(final HttpResponse response) {
        if (response != null) {
            StringBuffer body = new StringBuffer();
            //add response http status
            body.append(String.format("\"%s\":\"%s\"", "HTTP-STATUS", response.getStatusLine()));
            for (Header header : response.getAllHeaders()) {
                if (body.length() > 0)
                    body.append(",");
                body.append(String.format("\"%s\":\"%s\"", header.getName(), header.getValue()));
            }
            return String.format("{%s}", body.toString());
        }

        return null;
    }


    public static String generateAsEncodeRequestUrl(String requestURL, Map<String, List<String>> urlParamsMap) {
        requestURL = HttpClientHelper.trimUrl(requestURL);

        StringBuffer params = new StringBuffer();
        for (Entry<String, List<String>> kv : urlParamsMap.entrySet()) {
            if (params.length() > 0) {
                params.append("&");
            }
            if (kv.getValue() != null) {
                List<String> vlist = kv.getValue();
                for (String v : vlist) {
                    params.append(urlEncoding(kv.getKey(), HTTP.UTF_8)).append("=").append(urlEncoding(v, HTTP.UTF_8));
                }
            }
        }

        String newRequestURL = requestURL;
        if (params.length() > 0)
            newRequestURL += "?" + params.toString();

        HttpClientHelper.printDebugInfo("-- requestURL=" + newRequestURL);
        return newRequestURL;
    }

    public static String getParamsUrlEncodingStr(Map<String, List<String>> params) {
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            for (Entry<String, List<String>> e : params.entrySet()) {
                for (String value : e.getValue()) {
                    sb.append("&").append(urlEncoding(e.getKey(), HTTP.UTF_8)).append("=").append(urlEncoding(value, HTTP.UTF_8));
                }
            }
        }
        if (sb.length() > 0) {
            return sb.toString().substring(1); //去掉最前面的 &
        } else {
            return "";
        }
    }
}

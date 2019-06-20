package com.alibaba.csb.sdk;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;

/**
 * 设置HTTP传输的body内容，可以是Json String或者是byte[]格式
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * @since 2016
 */
public class ContentBody {
    // body内容串 使用的key，会把body内容串作为 key=value 方式作为签名的一部分
    /*packaged*/ static final String CONTENT_BODY_SIGN_KEY = System.getProperty("csb.sdk.body.sign.key", "_api_body_sign_key_");

    private String jsonBody;
    private byte[] bytesBody;
    private ContentType type;

    /**
     * 使用Json串构造ContentBody
     *
     * @param jsonStr
     */
    public ContentBody(String jsonStr) {
        this.jsonBody = jsonStr;
        type = ContentType.APPLICATION_JSON;
    }

    /**
     * 使用byte数组构造ContentBody
     *
     * @param bytes
     */
    public ContentBody(byte[] bytes) {
        this.bytesBody = bytes;
        type = ContentType.APPLICATION_OCTET_STREAM;
    }

    /**
     * 传输文件
     */
    public ContentBody(File file) {
        this.bytesBody = HttpCaller.readFile(file);
        type = ContentType.APPLICATION_OCTET_STREAM;
    }

    public ContentType getContentType() {
        return this.type;
    }

    public String getStrContentBody() {
        return jsonBody;
    }

    public byte[] getBytesContentBody() {
        return bytesBody;
    }

    public String getContentBodyAsStr() {
        if (type == ContentType.APPLICATION_OCTET_STREAM)
            try {
                return new String(bytesBody, HttpCaller.DEFAULT_CHARSET);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        else
            return jsonBody;
    }
}

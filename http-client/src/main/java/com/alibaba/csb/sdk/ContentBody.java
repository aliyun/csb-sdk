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
    public static final int AUTO_GZIP_BODY_SIZE; //默认当body大于此值时，就自动设置请求gzip

    static {
        //默认10K 单位（string：unicode， byte：字节）
        AUTO_GZIP_BODY_SIZE = Integer.valueOf(System.getProperty("csb_auto_gzip_body_size", String.valueOf(10 * 1024)));
    }

    private String jsonBody;
    private byte[] bytesBody;
    private ContentType type;
    private Boolean needZip; //是否需要压缩。默认不设置，自动根据body大小来配置

    /**
     * 使用Json串构造ContentBody
     *
     * @param jsonStr
     */
    public ContentBody(String jsonStr) {
        this.jsonBody = jsonStr;
        type = ContentType.APPLICATION_JSON;
        needZip = false;
    }

    /**
     * 使用byte数组构造ContentBody
     *
     * @param bytes
     */
    public ContentBody(byte[] bytes) {
        this(bytes, null);
    }

    /**
     * 使用byte数组构造ContentBody
     *
     * @param needZip 是否需要压缩传输
     * @param bytes
     */
    public ContentBody(byte[] bytes, Boolean needZip) {
        this.bytesBody = bytes;
        type = ContentType.APPLICATION_OCTET_STREAM;
        this.needZip = needZip;
    }

    /**
     * 传输文件，文件未经过压缩
     */
    public ContentBody(File file) throws HttpCallerException {
        this(file, null);
    }

    /**
     * 传输文件
     *
     * @param needZip 文件是否需要压缩传输
     */
    public ContentBody(File file, Boolean needZip) throws HttpCallerException {
        this.bytesBody = HttpCaller.readFile(file);
        type = ContentType.APPLICATION_OCTET_STREAM;
        this.needZip = needZip;
    }

    /**
     * 请求是否需要压缩：
     * 1. 如果用户明确不需要，则不压缩
     * 2. 如果用户未指定，则自动判断（大于nK单位，则压缩）
     */
    public boolean isNeedZip() {
        if (needZip != null) {
            return needZip;
        }

        if (type == ContentType.APPLICATION_OCTET_STREAM) {
            if (bytesBody.length > AUTO_GZIP_BODY_SIZE) { //byte数据，大于n个字节
                return true;
            }
        }
        return false;
    }

    public ContentType getContentType() {
        return this.type;
    }

    public Object getContentBody() {
        if (type == ContentType.APPLICATION_OCTET_STREAM)
            return bytesBody;
        else
            return jsonBody;
    }

    public String getContentBodyAsStr() {
        if (type == ContentType.APPLICATION_OCTET_STREAM)
            try {
                return new String(bytesBody, HttpCaller.DEFAULT_CHARSET);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        else
            return jsonBody;
    }
}

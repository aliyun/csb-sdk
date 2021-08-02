package com.alibaba.csb.sdk;

import com.alibaba.csb.sdk.security.SpasSigner;
import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.trace.TraceFactory;
import com.alibaba.csb.utils.LogUtils;
import com.alibaba.csb.utils.TraceIdUtils;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.entity.ContentType;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

import static com.alibaba.csb.sdk.HttpCaller.TOTAL_FILE_SIZE;
import static com.alibaba.csb.sdk.internel.HttpClientHelper.trimWhiteSpaces;

/**
 * Http Parameters 参数构造器，使用(Builder)模式构造http调用的所有参数
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * @since 2016
 */
public class HttpParameters {
    public static final long MAX_FILE_AMOUNT;

    static {
        MAX_FILE_AMOUNT = Integer.getInteger("csb_httpAttachmentMaxAmount", 5); //一次最多上传5个附件
    }

    private Builder builder;

    String getApi() {
        return builder.api;
    }

    String getVersion() {
        return builder.version;
    }

    String getAccessKey() {
        return builder.ak;
    }

    String getSecretkey() {
        return builder.sk;
    }

    String getMethod() {
        return builder.method;
    }

    String getRequestUrl() {
        return builder.requestUrl;
    }

    ContentBody getContentBody() {
        return builder.contentBody;
    }

    ContentType getContentType() {
        return builder.contentType;
    }

    Map<String, AttachFile> getAttachFileMap() {
        return builder.attatchFileMap;
    }

    String getRestfulProtocolVersion() {
        return builder.restfulProtocolVersion;
    }

    public Map<String, List<String>> getParamsMap() {
        return builder.paramsMap;
    }

    ContentEncoding getContentEncoding() {
        return builder.contentEncoding;
    }

    public Map<String, String> getHeaderParamsMap() {
        return builder.headerParamsMap;
    }

    boolean isNonce() {
        return builder.nonce;
    }

    boolean isSignContentBody() {
        return builder.signContentBody;
    }

    boolean isTimestamp() {
        return builder.timestamp;
    }

    boolean isDiagnostic() {
        return builder.diagnostic;
    }

    public String getSignImpl() {
        return builder.signImpl;
    }

    public String getVerifySignImpl() {
        return builder.verifySignImpl;
    }

    public String getSignAlgorithm(){
        return builder.getSignAlgorithm();
    }

    /**
     * 显示所设置的各个属性值
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("requestUrl=").append(this.getRequestUrl());
        sb.append("\n api=").append(this.getApi());
        sb.append("\n version=").append(this.getVersion());
        sb.append("\n method=").append(this.getMethod());
        sb.append("\n accessKey=").append(this.getAccessKey());
        sb.append("\n secretKey=").append("*********"); // hide this secret key!
        sb.append("\n contentBody=").append(this.getContentBody());
        sb.append("\n Nonce=").append(this.isNonce());
        //sb.append("\n signContentBody=").append(this.isSignContentBody());
        sb.append("\n Timestamp=").append(this.isTimestamp());
        sb.append("\n signImpl=").append(this.getSignImpl());
        sb.append("\n verifySignImpl=").append(this.getVerifySignImpl());
        sb.append("\n isDiagnostic=").append(this.isDiagnostic());
        sb.append("\n params: \n");
        for (Entry<String, List<String>> entry : builder.paramsMap.entrySet()) {
            sb.append(entry.getKey()).append("=");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append(value);
            } else if (value instanceof String[]) {
                sb.append(Arrays.toString((String[]) value));
            } else if (value instanceof List) {
                sb.append(value);
            }
            sb.append("\n");
        }

        sb.append("\n http header params: \n");
        for (Entry<String, String> entry : builder.headerParamsMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    public static class AttachFile {
        @Setter
        private HttpParameters httpParameters;
        @Getter
        private String fileName;
        @Getter
        private byte[] fileBytes;
        private ContentEncoding contentEncoding;

        public AttachFile(String fileName, byte[] fileBytes, ContentEncoding contentEncoding) {
            this.fileName = fileName;
            this.fileBytes = fileBytes;
            this.contentEncoding = contentEncoding;
        }

        /**
         * 请求是否需要压缩：
         * 1. 如果用户明确不需要，则不压缩
         */
        public ContentEncoding getContentEncoding() {
            if (contentEncoding == null) {
                return httpParameters != null ? httpParameters.getContentEncoding() : null;
            } else {
                return contentEncoding;
            }
        }
    }

    /**
     * 内部静态类，用来设置HttpCaller调用的相关参数
     * 只能有以下组合：
     * 1. paramsMap: paramsMap以form表单方式提交
     * 2. contentbody: 以json或二进制的 body 方式提交
     * 3. paramsMap + contentBody:  paramsMap以query方式提交，contentBody通过httpBody提交
     * 4. paramsMap + attatchFileMap: multi part的 form 方式提交
     * 5. contentbody + attatchFileMap: 暂不支持，因为http协议需要给contentBody命名
     * 6. paramsMap+ contentbody + attatchFileMap: 暂不支持，因为http协议需要给contentBody命名
     */
    public static class Builder {
        private String api;
        private String version;
        private String ak;
        private String sk;
        private String restfulProtocolVersion;
        private String method = "POST";
        private ContentType contentType;
        private ContentBody contentBody = null;
        private Map<String, AttachFile> attatchFileMap;
        private String requestUrl;
        private String signImpl;
        private String verifySignImpl;
        private boolean nonce;
        private boolean timestamp = true;
        private boolean signContentBody;
        private Map<String, List<String>> paramsMap = new HashMap<String, List<String>>();
        private ContentEncoding contentEncoding;
        private Map<String, String> headerParamsMap = new HashMap<String, String>();
        private boolean diagnostic = false;
        private HttpServletRequest request;
        private boolean overrideBizId = false;

        public Builder() {
            signContentBody = Boolean.parseBoolean(System.getProperty("csb_sign_content_body", "false")); //默认body参考签名，为了兼容历史版本，暂不参与签名
            headerParamsMap.put("Accept-Encoding", HttpCaller.GZIP);//默认设置接受gzip
        }

        public Builder copy() {
            HttpParameters.Builder builder = HttpParameters.newBuilder();
            builder.api = this.api;
            builder.version = this.version;
            builder.ak = this.ak;
            builder.sk = this.sk;
            builder.restfulProtocolVersion = this.restfulProtocolVersion;
            builder.method = this.method;
            builder.contentType = this.contentType;
            builder.contentBody = this.contentBody;
            builder.attatchFileMap = this.attatchFileMap;
            builder.requestUrl = this.requestUrl;
            builder.signImpl = this.signImpl;
            builder.verifySignImpl = this.verifySignImpl;
            builder.nonce = this.nonce;
            builder.timestamp = this.timestamp;
            builder.signContentBody = this.signContentBody;
            builder.paramsMap = new HashMap<String, List<String>>(this.paramsMap);
            builder.contentEncoding = this.contentEncoding;
            builder.headerParamsMap = new HashMap<String, String>(this.headerParamsMap);
            builder.diagnostic = this.diagnostic;
            builder.request = this.request;
            builder.overrideBizId = this.overrideBizId;
            return builder;
        }

        /**
         * 设置是否对 paramsMap和contentBody 进行压缩
         */
        public Builder setContentEncoding(ContentEncoding contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }

        /**
         * 增加附件
         */
        public Builder addAttachFile(String key, String fileName, InputStream inputStream) {
            return addAttachFile(key, fileName, inputStream, null);
        }

        /**
         * 增加附件
         */
        public Builder addAttachFile(String key, File file) {
            return addAttachFile(key, file, null);
        }

        /**
         * 增加附件
         */
        public Builder addAttachFile(String key, File file, ContentEncoding contentEncoding) {
            if (file == null) {
                throw new IllegalArgumentException("file不允许为空");
            }
            return addAttachFile(key, file.getName(), HttpCaller.readFile(file), contentEncoding);
        }

        /**
         * 增加附件
         */
        public Builder addAttachFile(String key, String fileName, InputStream inputStream, ContentEncoding contentEncoding) {
            if (inputStream == null) {
                throw new IllegalArgumentException("file内容不允许为空");
            }
            return addAttachFile(key, fileName, HttpCaller.readInputStream(inputStream), contentEncoding);
        }

        private Builder addAttachFile(String key, String fileName, byte[] bytes, ContentEncoding contentEncoding) {
            if (method.equalsIgnoreCase("POST") == false) {
                throw new IllegalArgumentException("发送附件必须使用POST");
            }
            if (contentBody != null) {
                throw new IllegalArgumentException("无法同时发送 contentBody 和 文件");
            }
            if (bytes == null) {
                throw new IllegalArgumentException("file内容不允许为空");
            }
            if (key == null) {
                throw new IllegalArgumentException("key不允许为空");
            }
            if (fileName == null) {
                throw new IllegalArgumentException("fileName不允许为空");
            }

            if (attatchFileMap == null) {
                attatchFileMap = new HashMap<String, AttachFile>();
            }

            if (attatchFileMap.size() >= MAX_FILE_AMOUNT) {
                throw new IllegalArgumentException("附件数量超过限制");
            }


            if (getTotalFileSize() + bytes.length > TOTAL_FILE_SIZE) {
                throw new IllegalArgumentException("attach file is too large exceed the MAX-SIZE");
            }

            attatchFileMap.put(key, new AttachFile(fileName, bytes, contentEncoding));
            return this;
        }

        public int getTotalFileSize() {
            int totalSize = 0;
            if (attatchFileMap != null) {
                for (Entry<String, AttachFile> stringAttachFileEntry : attatchFileMap.entrySet()) {
                    totalSize += stringAttachFileEntry.getValue().fileBytes.length;
                }
            }
            return totalSize;
        }

        /**
         * 设置服务的api名
         *
         * @param api
         * @return
         */
        public Builder api(String api) {
            this.api = api;
            return this;
        }

        /**
         * 设置服务的版本
         *
         * @param version
         * @return
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * 设置安全参数ak
         *
         * @param ak
         * @return
         */
        public Builder accessKey(String ak) {
            this.ak = ak;
            return this;
        }

        /**
         * 设置安全参数sk
         *
         * @param sk
         * @return
         */
        public Builder secretKey(String sk) {
            this.sk = sk;
            return this;
        }

        /**
         * bizId不存在时设置, 适用于中间环节
         *
         * @param bizId
         * @return
         */
        public Builder bizId(String bizId) {
            if (bizId == null) {
                return this;
            }
            if (!this.headerParamsMap.containsKey(HttpCaller.bizIdKey())) {
                this.putHeaderParamsMap(HttpCaller.bizIdKey(), bizId);
            }
            return this;
        }

        /**
         * 设置bizId, 若已存在则覆盖
         *
         * @param bizId
         * @return
         */
        public Builder setBizId(String bizId) {
            this.putHeaderParamsMap(HttpCaller.bizIdKey(), bizId);
            overrideBizId = true;
            return this;
        }

        public Builder requestId(String requestId) {
            this.putHeaderParamsMap(CsbSDKConstants.REQUESTID_KEY, requestId);
            return this;
        }

        /**
         * 签名算法
         *
         * @param signAlgorithm
         * @return
         */
        public Builder signAlgorithm(SpasSigner.SigningAlgorithm signAlgorithm) {
            this.putHeaderParamsMap(CsbSDKConstants.SIGNATURE_ALGORITHM_KEY, signAlgorithm.name());
            return this;
        }

        /**
         * 设置Http Request，用于trace()前
         *
         * @param request
         * @return
         */
        public Builder setRequest(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        /**
         * 启用trace，未引入TraceFilter时调用
         *
         * @param request
         * @return
         */
        public Builder trace(HttpServletRequest request) {
            return this.setRequest(request).trace();
        }

        /**
         * 启用trace，需先设置request，未引入TraceFilter时调用
         *
         * @return
         */
        public Builder trace() {
            if (TraceFactory.getTraceData() != null) {
                LogUtils.warn("you have turned on filter mode without call the trace method");
                return this;
            }
            if (this.request == null) {
                LogUtils.error("to enable tracing, you need to call the setRequest method or turn on the filter mode.");
                throw new RuntimeException("to enable trace, you need to call setRequest method or turned on filter mode.");
            }

            String traceId = request.getHeader(TraceData.TRACEID_KEY);
            String rpcId = request.getHeader(TraceData.RPCID_KEY);
            String bizId = request.getHeader(HttpCaller.bizIdKey());

            this.putHeaderParamsMap(TraceData.TRACEID_KEY, traceId != null ? traceId : TraceIdUtils.generate());
            this.putHeaderParamsMap(TraceData.RPCID_KEY, rpcId != null ? rpcId : TraceData.RPCID_DEFAULT);
            if (!overrideBizId && bizId != null && !bizId.trim().equals("")) {
                this.putHeaderParamsMap(HttpCaller.bizIdKey(), bizId);
            }
            return this;
        }

        /**
         * 添加trace header
         *
         * @return
         */
        public Builder addTraceHeader() {
            requestId(TraceIdUtils.generate());
            TraceData traceData = TraceFactory.getTraceData();
            if (traceData == null) {
                return this;
            }
            addTraceHeader(traceData);
            return this;
        }

        /**
         * 添加trace header
         *
         * @param traceData
         */
        private void addTraceHeader(TraceData traceData) {
            this.putHeaderParamsMap(TraceData.TRACEID_KEY, traceData.getTraceId() != null ? traceData.getTraceId() : TraceIdUtils.generate());
            this.putHeaderParamsMap(TraceData.RPCID_KEY, traceData.getRpcId() != null ? traceData.getRpcId() : TraceData.RPCID_DEFAULT);
            bizId(traceData.getBizId());
        }

        /**
         * 设置open restful version，1.0 is enable restful path
         *
         * @param restfulProtocolVersion
         * @return
         */
        public Builder restfulProtocolVersion(String restfulProtocolVersion) {
            this.restfulProtocolVersion = restfulProtocolVersion;
            return this;
        }

        /**
         * 设置调用的方式： 目前支持的取值是: get, post
         *
         * @param method
         * @return
         */
        public Builder method(String method) {
            if (!"get".equalsIgnoreCase(method) && !"post".equalsIgnoreCase(method) &&
                    !"cget".equalsIgnoreCase(method) && !"cpost".equalsIgnoreCase(method)) {
                throw new IllegalArgumentException("只支持 'GET', 'CGET' or 'POST', 'CPOST' method类型");
            }
            this.method = method;
            return this;
        }


        /**
         * @param timestampFlag, 是否生成时间戳，默认是生成的
         * @return
         */
        public Builder timestamp(boolean timestampFlag) {
            this.timestamp = timestampFlag;

            return this;
        }

        /**
         * @param nonceFlag, 是否生成nonce header
         * @return
         */
        public Builder nonce(boolean nonceFlag) {
            this.nonce = nonceFlag;

            return this;
        }

        /**
         * 设置HTTP请求的URL串
         *
         * @param url
         * @return
         */
        public Builder requestURL(String url) {
            this.requestUrl = url;
            return this;
        }

        /**
         * 清除已经设置的参数对
         *
         * @return
         */
        public Builder clearParamsMap() {
            this.paramsMap.clear();
            return this;
        }

        /**
         * 设置一个参数对列表
         *
         * @param key
         * @param valueList
         * @return
         */
        public Builder putParamsMap(String key, List<String> valueList) {
            if (valueList == null) {
                throw new IllegalArgumentException("valueList is not allow null.");
            }
            if (valueList instanceof ArrayList) {
                this.paramsMap.put(key, valueList);

            } else { //保证是数组list
                this.paramsMap.put(key, Arrays.asList(valueList.toArray(new String[valueList.size()])));
            }
            return this;
        }

        /**
         * 设置一个参数对列表
         *
         * @param key
         * @param value
         * @return
         */
        public Builder putParamsMap(String key, String... value) {
            if (value == null) {
                throw new IllegalArgumentException("value is not allow null.");
            }
            this.paramsMap.put(key, Arrays.asList(value));
            return this;
        }

        /**
         * 设置参数对集合
         *
         * @param map
         * @return
         */
        public Builder putParamsMapAll(Map<String, String> map) {
            if (map != null) {
                for (Entry<String, String> entry : map.entrySet()) {
                    this.paramsMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            } else {
                throw new IllegalArgumentException("empty map!!");
            }
            return this;
        }

        /**
         * 设置参数对集合
         *
         * @param map
         * @return
         */
        public Builder putParamsMap(Map<String, List<String>> map) {
            if (map == null) {
                throw new IllegalArgumentException("empty map!!");
            }
            for (Entry<String, List<String>> entry : map.entrySet()) {
                this.paramsMap.put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * 设置参数对集合
         *
         * @param map
         * @return
         */
        public Builder putParamsMapListAll(Map<String, List<String>> map) {
            if (map != null) {
                this.paramsMap.putAll(map);
            } else {
                throw new IllegalArgumentException("empty map!!");
            }
            return this;
        }

        /**
         * 清除所有已经设置的HTTP Header参数对
         *
         * @return
         */
        public Builder clearHeaderParamsMap() {
            this.headerParamsMap.clear();
            return this;
        }

        /**
         * 设置一个HTTP Header参数对
         *
         * @param key
         * @param value
         * @return
         */
        public Builder putHeaderParamsMap(String key, String value) {
            this.headerParamsMap.put(key, value);
            return this;
        }

        /**
         * 添加所有的Http Header参数对集合
         *
         * @param map
         * @return
         */
        public Builder putHeaderParamsMapAll(Map<String, String> map) {
            if (map != null) {
                this.headerParamsMap.putAll(map);
            } else {
                throw new IllegalArgumentException("empty map!!");
            }
            return this;
        }

        /**
         * 设置contentType
         *
         * @param contentTypeStr
         * @return
         */
        public Builder contentType(String contentTypeStr) {
            this.contentType = org.apache.http.entity.ContentType.parse(contentTypeStr);
            return this;
        }

        /**
         * 设置contentBody
         *
         * @param cb
         * @return
         */
        public Builder contentBody(ContentBody cb) {
            if (method.equalsIgnoreCase("POST") == false) {
                throw new IllegalArgumentException("发送contentBody必须使用POST");
            }
            if (attatchFileMap != null && attatchFileMap.isEmpty() == false) {
                throw new IllegalArgumentException("无法同时发送 contentBody 和 文件");
            }

            this.contentBody = cb;
            return this;
        }


        /**
         * 对ContentBody内设置的json串进行签名
         * 注意： 对目前的任何CSB Broker版本，都还不支持对contentBody的签名验证，
         * 所以设置该选择为true时，会导致验签失败！
         *
         * @param sign
         * @return
         */
        public Builder sginContentBody(boolean sign) {
            this.signContentBody = sign;
            return this;
        }

        /**
         * 设置SPI签名实现类, 不使用默认的签名实现方法，而是使用自定义的并且与CSB-Broker协商过的实现
         *
         * @param signImpl
         * @return
         */
        public Builder signImpl(String signImpl) {
            this.signImpl = signImpl;
            return this;
        }

        /**
         * 设置SPI验签实现类, 不使用默认的验签实现方法，而是使用自定义的并且与CSB-Broker协商过的实现
         *
         * @param verifySignImpl
         * @return
         */
        public Builder verifySignImpl(String verifySignImpl) {
            this.verifySignImpl = verifySignImpl;
            return this;
        }

        /**
         * 是否返回diagnostic信息
         *
         * @param diagnostic
         * @return
         */
        public Builder diagnostic(boolean diagnostic) {
            this.diagnostic = diagnostic;
            return this;
        }

        /**
         * 生成最终的参数集合
         *
         * @return
         */
        public HttpParameters build() {
            HttpParameters httpParameters = new HttpParameters(this);
            if (attatchFileMap != null) {
                for (AttachFile attachFile : attatchFileMap.values()) {
                    attachFile.setHttpParameters(httpParameters);
                }
            }
            return httpParameters;
        }

        /**
         * 获取TraceId
         *
         * @return
         */
        public String getTraceId() {
            return this.headerParamsMap.get(TraceData.TRACEID_KEY);
        }

        /**
         * 获取RpcId
         *
         * @return
         */
        public String getRpcId() {
            return this.headerParamsMap.get(TraceData.RPCID_KEY);
        }

        /**
         * 获取bizId
         *
         * @return
         */
        public String getBizId() {
            return this.headerParamsMap.get(HttpCaller.bizIdKey());
        }

        /**
         * 获取requestId
         *
         * @return
         */
        public String getRequestId() {
            return this.headerParamsMap.get(TraceData.REQUESTID_KEY);
        }

        /**
         * 获取签名算法
         *
         * @return
         */
        public String getSignAlgorithm() {
            return this.headerParamsMap.get(CsbSDKConstants.SIGNATURE_ALGORITHM_KEY);
        }

        public String api() {
            return this.api;
        }

        public String version() {
            return this.version;
        }

        public String accessKey() {
            return this.ak;
        }

        public String secretKey() {
            return this.sk;
        }

        public String method() {
            return this.method;
        }

        public String url() {
            return this.requestUrl;
        }

        public Map<String, List<String>> paramsMap() {
            return this.paramsMap;
        }

        public Map<String, String> headerMap() {
            return this.headerParamsMap;
        }

        public ContentBody contentBody() {
            return this.contentBody;
        }

        public ContentType contentType() {
            return this.contentType;
        }

        public ContentEncoding contentEncoding() {
            return this.contentEncoding;
        }
    }

    /**
     * private作用域的参数构造器，防止外部调用生成该实例
     *
     * @param builder
     */
    private HttpParameters(Builder builder) {
        this.builder = builder;
    }

    /**
     * 构造一个参数生成器
     *
     * @return
     */
    public static Builder newBuilder() {
        return new Builder().addTraceHeader();
    }

    public void validate() {
        if (this.getRequestUrl() == null)
            throw new IllegalArgumentException("Bad httpparameters: null requestUrl!");

        String api = this.getApi(), version = this.getVersion();
        if (api == null) {
            try {
                URI uri = new URI(this.getRequestUrl().matches("https?://.+") ? this.getRequestUrl() : "http://" + this.getRequestUrl());
                String query = uri.getQuery();
                if (query != null && !"".equals(query.trim())) {
                    String[] params = query.split("&");
                    for (String p : params) {
                        if (p.startsWith("_api_name=")) {
                            api = p.substring(10);
                        }
                        if (p.startsWith("_api_version=")) {
                            version = p.substring(13);
                        }
                    }
                }
                if (api == null || "".equals(api.trim())) {
                    String[] path = uri.getPath().split("/");
                    if (path.length >= 3) {
                        api = path[2];
                        version = path[1];
                    }
                }
            } catch (URISyntaxException e) {
            }
        }
        if (api == null || "".equals(api.trim())) {
            throw new IllegalArgumentException("Bad httpparameters: null api!");
        }
        if (version == null || "".equals(version.trim())) {
            throw new IllegalArgumentException("Bad httpparameters: null version!");
        }

        if (this.getContentBody() != null) {
            if (!"post".equalsIgnoreCase(this.getMethod())) {
                throw new IllegalArgumentException("Bad httpparameters: method must be \"post\" when contentBody is set!");
            }
            if (this.getParamsMap() != null && this.getParamsMap().size() > 0) {
                //support both contentBody and postParams
                // throw new IllegalArgumentException("Bad httpparameters: paramsMap must be empty when contentBody is set!");
            }
        }

        builder.api = trimWhiteSpaces(api);
        builder.version = trimWhiteSpaces(version);
        builder.ak = trimWhiteSpaces(this.getAccessKey());
        builder.sk = trimWhiteSpaces(this.getSecretkey());
    }
}

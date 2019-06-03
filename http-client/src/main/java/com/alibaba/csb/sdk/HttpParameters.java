package com.alibaba.csb.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.trace.TraceFactory;
import com.alibaba.csb.utils.LogUtils;
import com.alibaba.csb.utils.TraceIdUtils;

import static com.alibaba.csb.sdk.internel.HttpClientHelper.trimWhiteSpaces;

/**
 * Http Parameters 参数构造器，使用(Builder)模式构造http调用的所有参数
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 * @since 2016
 */
public class HttpParameters {
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

    String getRestfulProtocolVersion() {
        return builder.restfulProtocolVersion;
    }

    Map<String, String> getParamsMap() {
        return builder.paramsMap;
    }

    Map<String, String> getHeaderParamsMap() {
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
        for (Entry<String, String> entry : builder.paramsMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        sb.append("\n http header params: \n");
        for (Entry<String, String> entry : builder.headerParamsMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 内部静态类，用来设置HttpCaller调用的相关参数
     */
    public static class Builder {
        private String api;
        private String version;
        private String ak;
        private String sk;
        private String restfulProtocolVersion;
        private String method = "GET";
        private ContentBody contentBody = null;
        private String requestUrl;
        private String signImpl;
        private String verifySignImpl;
        private boolean nonce;
        private boolean timestamp = true;
        private boolean signContentBody = false;
        private Map<String, String> paramsMap = new HashMap<String, String>();
        private Map<String, String> headerParamsMap = new HashMap<String, String>();
        private boolean diagnostic = false;
        private HttpServletRequest request;
        private boolean overrideBizId = false;

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
         * 设置一个参数对
         *
         * @param key
         * @param value
         * @return
         */
        public Builder putParamsMap(String key, String value) {
            this.paramsMap.put(key, value);
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
         * 设置contentBody
         *
         * @param cb
         * @return
         */
        public Builder contentBody(ContentBody cb) {
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
            return new HttpParameters(this);
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

        if (this.getApi() == null)
            throw new IllegalArgumentException("Bad httpparameters: null api!");

        if (this.getContentBody() != null) {
            if (!"post".equalsIgnoreCase(this.getMethod())) {
                throw new IllegalArgumentException("Bad httpparameters: method must be \"post\" when contentBody is set!");
            }
            if (this.getParamsMap() != null && this.getParamsMap().size() > 0) {
                //support both contentBody and postParams
                // throw new IllegalArgumentException("Bad httpparameters: paramsMap must be empty when contentBody is set!");
            }
        }

        builder.api = trimWhiteSpaces(this.getApi());
        builder.version = trimWhiteSpaces(this.getVersion());
        builder.ak = trimWhiteSpaces(this.getAccessKey());
        builder.sk = trimWhiteSpaces(this.getSecretkey());
    }
}

package com.alibaba.csb.ws.sdk;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csb.sdk.security.SpasSigner;
import com.alibaba.csb.trace.TraceData;
import com.alibaba.csb.trace.TraceFactory;
import com.alibaba.csb.utils.LogUtils;
import com.alibaba.csb.utils.TraceIdUtils;

import lombok.Getter;

/**
 * WebService 相关的调用参数设置
 * Created by wiseking on 18/1/4.
 */
@Getter
public class WSParams {
    private String api;           //api-name
    private String version;       //api-version
    private String ak;            //accessKey
    private String sk;            //secretKey
    private String fingerPrinter; //指纹
    private boolean mockRequest;  //是否为mock请求
    private boolean timestamp = true; //是否生成时间戳http-header
    private boolean nonce;            //是否成成Nonce防重放http-header
    private boolean debug;            //是否打印调试信息
    private String signImpl;          //设置spi签名实现类
    private String signAlgothrim = SpasSigner.SigningAlgorithm.HmacSHA1.name();
    private String verifySignImpl;    //设置spi验签实现类

    private String traceId;
    private String rpcId;
    private String requestId;
    private String bizId;
    private HttpServletRequest request;
    private boolean overrideBizId = false;


    public static WSParams create() {
        return new WSParams().addTrace();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("api=").append(api);
        sb.append("version=").append(version);
        sb.append("ak=").append(ak);
        sb.append("sk=").append(sk);
        sb.append("signAlgothrim=").append(signAlgothrim);
        sb.append("traceId=").append(traceId);
        sb.append("rpcId=").append(rpcId);
        sb.append("requestId=").append(requestId);
        sb.append(WSClientSDK.bizIdKey() + "=").append(bizId);
        sb.append("mockRequest=").append(mockRequest);
        sb.append("timestamp=").append(timestamp);
        sb.append("nonce=").append(nonce);
        sb.append("debug=").append(debug);

        return sb.toString();
    }

    /**
     * 设置服务的api名
     *
     * @param api
     * @return
     */
    public WSParams api(String api) {
        this.api = api;
        return this;
    }

    /**
     * 设置服务的版本
     *
     * @param version
     * @return
     */
    public WSParams version(String version) {
        this.version = version;
        return this;
    }

    /**
     * 设置安全参数ak
     *
     * @param ak
     * @return
     */
    public WSParams accessKey(String ak) {
        this.ak = ak;
        return this;
    }

    /**
     * 设置安全参数sk
     *
     * @param sk
     * @return
     */
    public WSParams secretKey(String sk) {
        this.sk = sk;
        return this;
    }

    /**
     * 设置bizId，不覆盖
     *
     * @param bizId
     * @return
     */
    public WSParams bizId(String bizId) {
        if (this.bizId == null) {
            this.bizId = bizId;
        }
        return this;
    }

    /**
     * 设置requestId
     *
     * @param traceId
     * @return
     */
    public WSParams traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置requestId
     *
     * @param rpcId
     * @return
     */
    public WSParams rpcId(String rpcId) {
        this.rpcId = rpcId;
        return this;
    }

    /**
     * 设置requestId
     *
     * @param requestId
     * @return
     */
    public WSParams requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 设置bizId，覆盖
     *
     * @param bizId
     * @return
     */
    public WSParams setBizId(String bizId) {
        this.bizId = bizId;
        overrideBizId = true;
        return this;
    }

    /**
     * 是否设置时间戳，默认是true
     *
     * @param timestamp
     * @return
     */
    public WSParams timestamp(boolean timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * 设置防重放号，是否开启nonce设置
     *
     * @param nonce
     * @return
     */
    public WSParams nonce(boolean nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * 设置指纹值
     *
     * @param fingerPrinter
     * @return
     */
    public WSParams fingerPrinter(String fingerPrinter) {
        this.fingerPrinter = fingerPrinter;
        return this;
    }

    /**
     * 是否打印调试信息
     *
     * @param debug
     * @return
     */
    public WSParams debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 是否为mockRequest, 如果是则直接有broker返回mock结果，而不是调用后端的真正接入服务
     *
     * @param mockRequest
     * @return
     */
    public WSParams mockRequest(boolean mockRequest) {
        this.mockRequest = mockRequest;
        return this;
    }

    /**
     * 设置其它的签名方法实现类
     *
     * @param signImpl
     * @return
     */
    public WSParams signImpl(String signImpl) {
        this.signImpl = signImpl;
        return this;
    }

    /**
     * 设置签名算法
     *
     * @param signAlgothrim
     * @return
     */
    public WSParams signAlgothrim(String signAlgothrim) {
        this.signAlgothrim = signAlgothrim;
        return this;
    }

    /**
     * 设置其它的验证方法实现类
     *
     * @param verifySignImpl
     * @return
     */
    public WSParams verifySignImpl(String verifySignImpl) {
        this.verifySignImpl = verifySignImpl;
        return this;
    }

    /**
     * 添加trace header
     *
     * @return
     */
    private WSParams addTrace() {
        requestId(TraceIdUtils.generate());
        TraceData traceData = TraceFactory.getTraceData();
        if (traceData != null) {
            this.traceId = traceData.getTraceId();
            this.rpcId = traceData.getRpcId();
            bizId(traceData.getBizId());
        }
        return this;
    }

    /**
     * 设置Http Request，用于trace()前
     *
     * @param request
     * @return
     */
    public WSParams setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    /**
     * 手动启用trace，未引入TraceFilter时调用
     *
     * @param request
     * @return
     */
    public WSParams trace(HttpServletRequest request) {
        return this.setRequest(request).trace();
    }

    /**
     * 手动启用trace，需先设置request，未引入TraceFilter时调用
     *
     * @return
     */
    public WSParams trace() {
        if (TraceFactory.getTraceData() != null) {
            LogUtils.info("you have turned on filter mode without call the trace method");
            return this;
        }
        if (this.request == null) {
            LogUtils.error("to enable tracing, you need to call the setRequest method or turn on the filter mode.");
            throw new RuntimeException("to enable trace, you need to call setRequest method or turned on filter mode.");
        }

        String traceId = request.getHeader(TraceData.TRACEID_KEY);
        String rpcId = request.getHeader(TraceData.RPCID_KEY);
        String bizId = request.getHeader(WSClientSDK.bizIdKey());

        this.traceId = traceId;//发送请求前如果为空会生成
        this.rpcId = rpcId;//发送请求前如果为空会生成
        if (!overrideBizId && bizId != null && !bizId.trim().equals("")) {//不覆盖header数据
            this.bizId = bizId;
        }
        return this;
    }
}

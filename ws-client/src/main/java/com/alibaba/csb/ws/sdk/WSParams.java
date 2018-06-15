package com.alibaba.csb.ws.sdk;

import lombok.Getter;

import java.util.Random;

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

  public static WSParams create() {
    return new WSParams();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("api=").append(api);
    sb.append("version=").append(version);
    sb.append("ak=").append(ak);
    sb.append("sk=").append(sk);
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
   * @param mockRequest
   * @return
   */
  public WSParams mockRequest(boolean mockRequest) {
    this.mockRequest = mockRequest;
    return this;
  }

  /**
   * 设置其它的签名方法实现类
   * @param signImpl
   * @return
   */
  public WSParams signImpl(String signImpl) {
    this.signImpl = signImpl;
    return this;
  }
}

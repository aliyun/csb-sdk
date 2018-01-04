package com.alibaba.csb.ws.sdk;

import lombok.Getter;

import java.util.Random;

/**
 * WebService 相关的调用参数设置
 * Created by wiseking on 18/1/4.
 */
@Getter
public class WSParams {
  private String api;
  private String version;
  private String ak;
  private String sk;
  private boolean mockRequest;
  private boolean nonce;
  private boolean debug;

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
}

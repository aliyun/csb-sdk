package com.alibaba.csb.sdk;

/**
 * Http Return 对象 包含调用的返回结果，并且包含一些诊断相关的信息包括：
 * 1. response                调用的返回值
 * 2. responseHeaders         返回的http headers
 * 3. responseHttpStatus      返回的http状态
 *
 * 4. diagnosticInfo {
 *       requestHeaders          请求headers
 *       signParams              参与签名的字段
 *       startTime               调用的起始时间
 *       endTime                 调用的结束时间
 *       totalInvokeTime         本地调用的用时(ms)
 *       requestSize             本次请求的大小
 *       responseSize            本次响应的大小
 *       brokerIp                处理本次请求的BrokerIP地址
 * }
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 *
 * @since 2018
 *
 */
public class HttpReturn {
  public String responseHttpStatus;
  public String response;
  public String responseHeaders;
  public String diagnosticInfo; //json格式串，方便增减新的诊断项
  public boolean diagnosticFlag;

  public HttpReturn(){}

  public HttpReturn(String response) {
    this.response = response;
  }
}

package com.alibaba.csb.sdk;

/**
 * Http Return 对象 包含调用的返回结果，可一些诊断相关的信息包括：
 * 1. http-request headers
 * 2. http-response headers
 * 3. http-request body (trim if too big)
 * 4. DiagnosticInfo {
 *       startTime
 *       endTime
 *       totalInvokeTime
 *       requestSize
 *       responseSize
 *       brokerIp
 * }
 *
 * @author Alibaba Middleware CSB Team
 * @author liaotian.wq
 *
 * @since 2018
 *
 */
public class HttpReturn {
  public String response;
  public String reqeustHeaders;
  public String responseHeaders;
  public String diagnosticInfo;
}

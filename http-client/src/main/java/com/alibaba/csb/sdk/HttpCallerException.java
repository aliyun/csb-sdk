package com.alibaba.csb.sdk;

/**
 * 调用HttpCaller产生的异常
 * 
 * @author Alibaba Middleware CSB Team
 * @since 2016
 *
 */
public class HttpCallerException extends Exception {
	public HttpCallerException(Exception e) {
		super(e);
	}
	
	public HttpCallerException(String msg, Exception e) {
		super(msg, e);
	}
	
	public HttpCallerException(String msg) {
		super(msg);
	}
}

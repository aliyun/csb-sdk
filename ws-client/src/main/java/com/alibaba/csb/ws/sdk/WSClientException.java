package com.alibaba.csb.ws.sdk;

/**
 * Exception class when using the SDK.
 * @author liaotian.wq
 *
 */
public class WSClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public WSClientException(String errMsg) {
		super(errMsg);
	}
	
	public WSClientException(String errMsg, Throwable thr) {
		super(errMsg, thr);
	}
}

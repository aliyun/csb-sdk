package com.alibaba.csb.sentinel;

/**
 * Created by yaolan.lt on 16/1/4.
 */
public class LimitExceedException extends RuntimeException {
    public LimitExceedException(String message) {
        super(message);
    }
}

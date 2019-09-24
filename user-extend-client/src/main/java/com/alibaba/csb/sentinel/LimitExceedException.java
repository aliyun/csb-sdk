package com.alibaba.csb.sentinel;

/**
 */
public class LimitExceedException extends RuntimeException {
    public LimitExceedException(String message) {
        super(message);
    }
}

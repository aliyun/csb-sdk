package com.alibaba.csb.SelfDefProcess;

import lombok.Data;

/**
 * 自定义处理逻辑异常
 */
@Data
public class SelfDefProcessException extends RuntimeException {
    private String errorCode;

    public SelfDefProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

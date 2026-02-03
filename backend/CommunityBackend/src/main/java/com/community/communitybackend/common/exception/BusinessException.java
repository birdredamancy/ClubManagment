package com.community.communitybackend.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于在Service层抛出可预期的业务错误，由全局异常处理器统一返回给前端
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

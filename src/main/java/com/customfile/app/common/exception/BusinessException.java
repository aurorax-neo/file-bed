package com.customfile.app.common.exception;


import com.customfile.app.common.response.StateCode;

import java.io.Serial;

/**
 * 业务异常
 *
 * @author YCJ
 * @date 2023/01/08
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 429445491781348218L;
    /**
     * 状态码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(StateCode stateCode) {
        super(stateCode.getMessage());
        this.code = stateCode.getCode();
    }

    public BusinessException(StateCode stateCode, String message) {
        super(message);
        this.code = stateCode.getCode();
    }

    public int getCode() {
        return code;
    }
}

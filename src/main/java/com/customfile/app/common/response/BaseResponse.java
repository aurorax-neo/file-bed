package com.customfile.app.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 基响应
 *
 * @author YCJ
 * @date 2022/12/18
 */
@Data
public class BaseResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 7600162751688412365L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;


    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(StateCode stateCode) {
        this(stateCode.getCode(), stateCode.getMessage(), null);
    }
}

package com.customfile.app.common.response;


/**
 * 结果跑龙套
 *
 * @author YCJ
 * @date 2022/12/18
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @return {@link BaseResponse}<{@link T}>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(StateCode.SUCCESS.getCode(), StateCode.SUCCESS.getMessage(), data);
    }


    /**
     * 错误
     *
     * @param stateCode 错误代码
     * @return {@link BaseResponse}
     */
    public static <T> BaseResponse<T> error(StateCode stateCode) {
        return new BaseResponse<>(stateCode);
    }


    /**
     * 错误
     *
     * @param code    代码
     * @param message 消息
     * @return {@link BaseResponse}
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }


    /**
     * 错误
     *
     * @param stateCode 错误代码
     * @param message   消息
     * @return {@link BaseResponse}
     */
    public static <T> BaseResponse<T> error(StateCode stateCode, String message) {
        return new BaseResponse<>(stateCode.getCode(), message, null);
    }
}

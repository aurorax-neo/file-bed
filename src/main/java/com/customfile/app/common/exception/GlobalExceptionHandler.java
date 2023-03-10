package com.customfile.app.common.exception;

import com.customfile.app.common.response.BaseResponse;
import com.customfile.app.common.response.ResultUtils;
import com.customfile.app.common.response.StateCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理程序
 *
 * @author YCJ
 * @date 2022/12/18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理程序
     *
     * @param e e
     * @return {@link BaseResponse}<{@link ?}>
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Enum<StateCode>> businessExceptionHandler(BusinessException e) {
//        log.error("\nstart: \nClass->GlobalExceptionHandler: \nBusinessException: " + e.getMessage() + "\nend.");
        return ResultUtils.error(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
//        log.error("\nstart: \nClass->GlobalExceptionHandler: \nRuntimeException: " + e.getMessage() + "\nend.");
        return ResultUtils.error(StateCode.SYSTEM_ERROR);
    }

    /**
     * http请求方法不支持异常
     *
     * @return {@link BaseResponse}<{@link ?}>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<Enum<StateCode>> httpRequestMethodNotSupportedException() {
        return ResultUtils.error(StateCode.OPERATION_ERROR, "method 方法不支持");
    }

    /**
     * http媒体类型不支持
     *
     * @return {@link BaseResponse}<{@link ?}>
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public BaseResponse<Enum<StateCode>> httpMediaTypeNotSupportedException() {
        return ResultUtils.error(StateCode.PARAMS_ERROR, "不支持媒体类型");
    }
}

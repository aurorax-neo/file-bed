package com.customfile.app.common.response;


/**
 * 错误代码
 *
 * @author YCJ
 * @date 2022/12/18
 */
public enum StateCode {

    /**
     * 成功
     */
    SUCCESS(0, "ok"),
    /**
     * 参数错误
     */
    PARAMS_ERROR(40000, "请求参数错误"),
    /**
     * 未登录错误
     */
    NOT_LOGIN_ERROR(40100, "未登录"),
    /**
     * 没有身份验证错误
     */
    NO_AUTH_ERROR(40200, "无权限"),
    /**
     * 禁止错误
     */
    FORBIDDEN_ERROR(40300, "禁止访问"),
    /**
     * 没有发现错误
     */
    NOT_FOUND_ERROR(40400, "请求数据不存在"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(50000, "系统内部异常"),
    /**
     * 操作错误
     */
    OPERATION_ERROR(50100, "操作失败"),
    /**
     * 存在错误
     */
    EXISTED_ERROR(10000, "已存在");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 消息
     */
    private final String message;

    StateCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package com.yx.demo.enums;

/**
 * @author yinxing
 * @date 2019/11/21 11:12
 * @description
 */
public enum ExceptionEnum {

    SUCCESS("200", "成功"),
    FAIL("201", "失败"),
    EXCEPTION("202", "网络异常"),
    UN_LOGIN("203", "未登录"),
    ES_SEARCH_FAIL("204","ES查询错误");

    private String code;

    private String message;

    ExceptionEnum(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

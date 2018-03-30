package com.yyy.TourWeb.tools;

public class Result<T> {
    private static final String DEFAULT_SUCCESS_MSG = "成功";
    private static final String DEFAULT_ERROR_MSG = "失败";

    protected Integer code;

    protected Boolean success;

    protected String msg;

    protected T data;

    public Result() {
    }

    public Result(Integer code, Boolean success, String msg, T data) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(String msg, T t) {
        return new Result<>(0, true, msg, t);
    }

    public static <T> Result<T> ok(T t) {
        return Result.ok(DEFAULT_SUCCESS_MSG, t);
    }

    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    public static <T> Result<T> error(Integer code, String msg, T t) {
        return new Result<>(code, false, msg, t);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return Result.error(code, msg, null);
    }

    public static <T> Result<T> error(Integer code) {
        return Result.error(code, DEFAULT_ERROR_MSG, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
       return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

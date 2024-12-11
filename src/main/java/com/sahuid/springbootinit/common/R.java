package com.sahuid.springbootinit.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private int code;
    private T value;
    private String msg;


    public static <T> R<T> ok() {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setValue(data);
        result.setMsg("success");
        return result;
    }

    public static <T> R<T> ok(T data, String msg) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setValue(data);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> ok(String msg) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}

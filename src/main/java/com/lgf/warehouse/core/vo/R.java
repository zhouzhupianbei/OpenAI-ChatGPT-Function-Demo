package com.lgf.warehouse.core.vo;

import lombok.Data;

@Data
public class R {

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final int UNAUTHORIZED = 401;
    private int code;
    private String msg;
    private Object data;

    public R() {
    }

    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(Object data) {
        this.code = 200;
        this.msg = "success";
        this.data = data;
    }

    public R(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static R ok() {
        return new R(200, "success");
    }

    public static R ok(Object data) {
        return new R(data);
    }

    public static R error() {
        return new R(500, "error");
    }

    public static R error(String msg) {
        return new R(500, msg);
    }

    public static R error(int code, String msg) {
        return new R(code, msg);
    }
}

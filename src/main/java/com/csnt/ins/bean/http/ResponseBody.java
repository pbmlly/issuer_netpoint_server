package com.csnt.ins.bean.http;

/**
 * @ClassName ResponseBody
 * @Description 响应对象
 * @Author duwanjiang
 * @Date 2019/6/22 13:52
 * Version 1.0
 **/
public class ResponseBody {
    int code;
    String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"msg\":\"" + msg + "\"}";
    }
}

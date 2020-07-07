package com.csnt.ins.bean.result;

import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.StringUtil;

import java.io.Serializable;

/**
 * @author luoxiaojian
 * @Description:
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class Result<T> implements Serializable {
    Boolean success = true;
    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Result() {
    }

    public Result(Integer code, String msg, T data, Boolean success) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.setSuccess(success);
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        if (200 == code) {
            success = true;
        } else {
            success = false;
        }
    }

    /**
     * 处理成功
     *
     * @param data 数据
     * @return 处理成功
     */
    public static Result success(Object data) {
        return new Result(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), data, true);
    }

    /**
     * 处理成功
     *
     * @param data 数据
     * @return 处理成功
     */
    public static Result success(Object data, String msg) {
        return new Result(ResponseStatusEnum.SUCCESS.getCode(), StringUtil.isEmpty(msg) ?
                ResponseStatusEnum.SUCCESS.getMsg() : msg, data, true);
    }

    /**
     * 鉴权失败
     *
     * @return 鉴权失败
     */
    public static Result authFail() {
        return new Result(ResponseStatusEnum.SYS_AUTHENTICATION_FAILURE.getCode(), ResponseStatusEnum.SYS_AUTHENTICATION_FAILURE.getMsg());
    }

    /**
     * 鉴权失败
     *
     * @return 鉴权失败
     */
    public static Result authFail(String msg) {
        return new Result(ResponseStatusEnum.SYS_AUTHENTICATION_FAILURE.getCode(), StringUtil.isEmpty(msg) ? ResponseStatusEnum.SYS_AUTHENTICATION_FAILURE.getMsg() : msg);
    }

    /**
     * 登录失败
     *
     * @return 登录失败
     */
    public static Result loginFail() {
        return new Result(ResponseStatusEnum.SYS_LOGIN_ERROR.getCode(), ResponseStatusEnum.SYS_LOGIN_ERROR.getMsg());
    }

    /**
     * 系统异常
     *
     * @return 系统异常
     */
    public static Result sysError(String msg) {
        return new Result(ResponseStatusEnum.SYS_INTERVAL_ERROR.getCode(), StringUtil.isEmpty(msg) ? ResponseStatusEnum.SYS_INTERVAL_ERROR.getMsg() : msg);
    }

    /**
     * 返回参数异常
     *
     * @return 参数异常
     */
    public static Result paramError() {
        return byEnum(ResponseStatusEnum.SYS_INVALID_PARAM);
    }

    /**
     * 返回参数异常
     *
     * @return 参数异常
     */
    public static Result paramNotNullError(String... infos) {
        Result result = paramError();
        StringBuilder sb = new StringBuilder();
        for (String info : infos) {
            sb.append(info).append(",");
        }
        result.setMsg(result.getMsg() + ".以下值不能为空:" + sb.toString());
        return result;
    }

    /**
     * 根据枚举返回状态
     *
     * @return 状态
     */
    public static Result byEnum(ResponseStatusEnum statusEnum) {
        return new Result(statusEnum.getCode(), statusEnum.getMsg());
    }

    /**
     * 根据枚举返回状态
     *
     * @return 状态
     */
    public static Result byEnum(ResponseStatusEnum statusEnum, String msg) {
        return new Result(statusEnum.getCode(), StringUtil.isEmpty(msg) ? statusEnum.getMsg() : msg);
    }

    /**
     * 根据业务异常返回状态
     *
     * @return 状态
     */
    public static Result bizError(int code, String msg) {
        return new Result(code, msg);
    }

    /**
     * 车牌唯一性检查
     *
     * @return 参数异常
     */
    public static Result vehCheckError(String msg) {
        return new Result(ResponseStatusEnum.SYS_CHECK_VEH_ERROR.getCode(), StringUtil.isEmpty(msg) ? ResponseStatusEnum.SYS_CHECK_VEH_ERROR.getMsg() : msg);
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

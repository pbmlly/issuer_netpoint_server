package com.csnt.ins.enumobj;

/**
 * @ClassName ResponseStatusEnmu
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/22 13:59
 * Version 1.0
 **/
public enum ResponseStatusEnum {

    SUCCESS(200,"接收成功"),
    SYS_INTERVAL_ERROR(501,"服务器遇到错误，无法完成请求"),
    //------------------系统错误 700-799---------------------
    SYS_AUTHENTICATION_FAILURE(700,"请重新登录"),
    SYS_INVALID_PARAM(701,"参数解析异常"),
    SYS_REQUEST_DATA_ERROR(702,"请求数据格式错误"),
    SYS_DB_SAVE_ERROR(703,"数据入库异常"),
    SYS_INVALID_MD5(704,"md5校验失败"),
    SYS_API_UNAUTHORIZED(705,"接口未授权"),
    SYS_LOGIN_ERROR(706,"登录失败，用户名或密码错误"),
    SYS_API_ERROR(707,"业务类型不存在"),
    SYS_CHECK_VEH_ERROR(708,"验证不通过，车牌唯一性验证失败；"),
    SYS_DB_PRIMARY_ERROR(709,"数据入库主键冲突"),
    //------------------业务错误 800-899--------------------
    NO_DATA_ERROR(800,"暂无数据"),
    BIZ_ORDER_PROCESSED(801,"订单已处理完成"),
    BIZ_UPLOAD_ERROR(802,"上传数据异常"),
    BIZ_ISSUER_ERROR(803,"发行异常");

    private int code;
    private String msg;

    private ResponseStatusEnum(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

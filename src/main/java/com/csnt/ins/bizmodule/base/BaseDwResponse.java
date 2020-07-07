package com.csnt.ins.bizmodule.base;

import com.csnt.ap.ct.bean.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共上传对象
 *
 * @author cloud
 */
public  class BaseDwResponse<T>  extends BaseResponse {
    private static Logger logger = LoggerFactory.getLogger(BaseDwResponse.class);
    private T data;
    private String info;
    private String receiveTime;
    private String result;
    private String vehicleCompare;
    @JsonIgnore
    private String msg;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getVehicleCompare() {
        return vehicleCompare;
    }

    public void setVehicleCompare(String vehicleCompare) {
        this.vehicleCompare = vehicleCompare;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @JsonIgnore
    private int count;
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}

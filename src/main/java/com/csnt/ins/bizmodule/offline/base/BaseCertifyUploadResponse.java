package com.csnt.ins.bizmodule.offline.base;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseResponse;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * 公共上传对象
 *
 * @author cloud
 */
public  class BaseCertifyUploadResponse<T>  extends BaseResponse {
    private static Logger logger = LoggerFactory.getLogger(BaseCertifyUploadResponse.class);
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

package com.csnt.ins.model.json;

import com.csnt.ins.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SupOrderCancelRequest<M extends SupOrderCancelRequest<M>> extends Model<M> implements IBean {
    private String accountId;
    private String orderId;
    private Integer orderType;
    private Integer startType;
    @JsonFormat(pattern = DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS, timezone = DateUtil.FORMAT_GMT8)
    private String startTime;
    private String msg;
    private Integer isSendBack;
    private Integer isPay;

    public SupOrderCancelRequest() {
    }

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String var1) {
        this.accountId = var1;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String var1) {
        this.orderId = var1;
    }

    public Integer getOrderType() {
        return this.orderType;
    }

    public void setOrderType(Integer var1) {
        this.orderType = var1;
    }

    public Integer getStartType() {
        return this.startType;
    }

    public void setStartType(Integer var1) {
        this.startType = var1;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String var1) {
        this.startTime = var1;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String var1) {
        this.msg = var1;
    }

    public Integer getIsSendBack() {
        return this.isSendBack;
    }

    public void setIsSendBack(Integer var1) {
        this.isSendBack = var1;
    }

    public Integer getIsPay() {
        return this.isPay;
    }

    public void setIsPay(Integer var1) {
        this.isPay = var1;
    }

}

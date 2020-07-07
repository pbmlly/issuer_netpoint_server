package com.csnt.ins.model.offline;

import java.util.List;

public class SupBankRefundRequest {
    private String issuerId;
    private Long messageId;
    private String bankId;
    private Integer count;
    private Long amount;
    private String applyTime;
    private String sign;
    private List<SupBankRefundModel> transaction;
    private String notifyUrl;

    public SupBankRefundRequest() {
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String var1) {
        this.issuerId = var1;
    }

    public Long getMessageId() {
        return this.messageId;
    }

    public void setMessageId(Long var1) {
        this.messageId = var1;
    }

    public String getBankId() {
        return this.bankId;
    }

    public void setBankId(String var1) {
        this.bankId = var1;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer var1) {
        this.count = var1;
    }

    public Long getAmount() {
        return this.amount;
    }

    public void setAmount(Long var1) {
        this.amount = var1;
    }

    public String getApplyTime() {
        return this.applyTime;
    }

    public void setApplyTime(String var1) {
        this.applyTime = var1;
    }

    public List<SupBankRefundModel> getTransaction() {
        return this.transaction;
    }

    public void setTransaction(List<SupBankRefundModel> var1) {
        this.transaction = var1;
    }

    public String getNotifyUrl() {
        return this.notifyUrl;
    }

    public void setNotifyUrl(String var1) {
        this.notifyUrl = var1;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String var1) {
        this.sign = var1;
    }

}
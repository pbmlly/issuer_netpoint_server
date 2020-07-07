package com.csnt.ins.model.json;

public class SupPayQueryRequest {
    private String issuerId;
    private Long messageId;
    private String bankId;
    private String sign;

    public SupPayQueryRequest() {
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

    public String getSign() {
        return this.sign;
    }

    public void setSign(String var1) {
        this.sign = var1;
    }
}

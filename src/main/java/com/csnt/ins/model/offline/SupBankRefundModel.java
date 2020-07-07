package com.csnt.ins.model.offline;

public class SupBankRefundModel {
    private String unpayId;
    private String payId;
    private Long fee;
    private String title;

    public SupBankRefundModel() {
    }

    public String getUnpayId() {
        return this.unpayId;
    }

    public void setUnpayId(String var1) {
        this.unpayId = var1;
    }

    public String getPayId() {
        return this.payId;
    }

    public void setPayId(String var1) {
        this.payId = var1;
    }

    public Long getFee() {
        return this.fee;
    }

    public void setFee(Long var1) {
        this.fee = var1;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String var1) {
        this.title = var1;
    }
}
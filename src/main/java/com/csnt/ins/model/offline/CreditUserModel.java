package com.csnt.ins.model.offline;

public class CreditUserModel {
    private String id;
    private String name;
    private Integer userIdType;
    private String positiveImageStr;
    private String negativeImageStr;
    private String phone;
    private String address;
    private Integer registeredType;
    private String issueChannelId;

    public CreditUserModel() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String var1) {
        this.id = var1;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public Integer getUserIdType() {
        return this.userIdType;
    }

    public void setUserIdType(Integer var1) {
        this.userIdType = var1;
    }

    public String getPositiveImageStr() {
        return this.positiveImageStr;
    }

    public void setPositiveImageStr(String var1) {
        this.positiveImageStr = var1;
    }

    public String getNegativeImageStr() {
        return this.negativeImageStr;
    }

    public void setNegativeImageStr(String var1) {
        this.negativeImageStr = var1;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String var1) {
        this.phone = var1;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String var1) {
        this.address = var1;
    }

    public Integer getRegisteredType() {
        return this.registeredType;
    }

    public void setRegisteredType(Integer var1) {
        this.registeredType = var1;
    }

    public String getIssueChannelId() {
        return this.issueChannelId;
    }

    public void setIssueChannelId(String var1) {
        this.issueChannelId = var1;
    }
}

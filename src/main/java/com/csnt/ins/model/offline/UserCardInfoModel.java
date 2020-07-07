package com.csnt.ins.model.offline;

public class UserCardInfoModel  {
    private String issuerId;
    private String vehicleId;
    private String cardId;
    private String enableTime;
    private String expireTime;
    private String model;
    private String plateNum;
    private String issueChannelId;
    private Integer plateColor;
    private Integer cardType;
    private Integer brand;
    private Integer issueChannelType;

    public UserCardInfoModel() {
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String var1) {
        this.issuerId = var1;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String var1) {
        this.vehicleId = var1;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String var1) {
        this.cardId = var1;
    }

    public String getEnableTime() {
        return this.enableTime;
    }

    public void setEnableTime(String var1) {
        this.enableTime = var1;
    }

    public String getExpireTime() {
        return this.expireTime;
    }

    public void setExpireTime(String var1) {
        this.expireTime = var1;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String var1) {
        this.model = var1;
    }

    public String getPlateNum() {
        return this.plateNum;
    }

    public void setPlateNum(String var1) {
        this.plateNum = var1;
    }

    public String getIssueChannelId() {
        return this.issueChannelId;
    }

    public void setIssueChannelId(String var1) {
        this.issueChannelId = var1;
    }

    public Integer getPlateColor() {
        return this.plateColor;
    }

    public void setPlateColor(Integer var1) {
        this.plateColor = var1;
    }

    public Integer getCardType() {
        return this.cardType;
    }

    public void setCardType(Integer var1) {
        this.cardType = var1;
    }

    public Integer getBrand() {
        return this.brand;
    }

    public void setBrand(Integer var1) {
        this.brand = var1;
    }

    public Integer getIssueChannelType() {
        return this.issueChannelType;
    }

    public void setIssueChannelType(Integer var1) {
        this.issueChannelType = var1;
    }
}
package com.csnt.ins.model.offline;

public class UserSignoflRequest extends AbstractIssAccountRequest{
    private String vehicleId;
    private String plateNum;
    private Integer plateColor;
    private Integer signType;
    private String issueChannelId;
    private String channelType;
    private Integer cardType;
    private String account;
    private String enableTime;
    private String closeTime;
    private String info;
    private Integer status;

    public UserSignoflRequest() {
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String var1) {
        this.vehicleId = var1;
    }

    public String getPlateNum() {
        return this.plateNum;
    }

    public void setPlateNum(String var1) {
        this.plateNum = var1;
    }

    public Integer getPlateColor() {
        return this.plateColor;
    }

    public void setPlateColor(Integer var1) {
        this.plateColor = var1;
    }

    public Integer getSignType() {
        return this.signType;
    }

    public void setSignType(Integer var1) {
        this.signType = var1;
    }

    public String getIssueChannelId() {
        return this.issueChannelId;
    }

    public void setIssueChannelId(String var1) {
        this.issueChannelId = var1;
    }

    public String getChannelType() {
        return this.channelType;
    }

    public void setChannelType(String var1) {
        this.channelType = var1;
    }

    public Integer getCardType() {
        return this.cardType;
    }

    public void setCardType(Integer var1) {
        this.cardType = var1;
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String var1) {
        this.account = var1;
    }

    public String getEnableTime() {
        return this.enableTime;
    }

    public void setEnableTime(String var1) {
        this.enableTime = var1;
    }

    public String getCloseTime() {
        return this.closeTime;
    }

    public void setCloseTime(String var1) {
        this.closeTime = var1;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String var1) {
        this.info = var1;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer var1) {
        this.status = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.SIGNOFL;
//    }
}
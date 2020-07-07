package com.csnt.ins.model.offline;

public class TouchTokenRequest  {
    private String appId;
    private String accessToken;
    private String openId;
    private String sign;

    public TouchTokenRequest() {
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String var1) {
        this.appId = var1;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String var1) {
        this.accessToken = var1;
    }

    public String getOpenId() {
        return this.openId;
    }

    public void setOpenId(String var1) {
        this.openId = var1;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String var1) {
        this.sign = var1;
    }

}

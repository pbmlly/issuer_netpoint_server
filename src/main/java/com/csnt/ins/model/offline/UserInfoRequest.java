package com.csnt.ins.model.offline;

public class UserInfoRequest  {
    public UserInfoRequest() {
    }

    private String accessToken;
    private String openId;

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
}
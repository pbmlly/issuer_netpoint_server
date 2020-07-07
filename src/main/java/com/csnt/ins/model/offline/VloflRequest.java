package com.csnt.ins.model.offline;

public class VloflRequest  {
    private String accessToken;
    private String openId;
    private String accountId;
    private String sign;
    private String encryptedData;
    private Integer type;

    public VloflRequest() {
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer var1) {
        this.type = var1;
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

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String var1) {
        this.accountId = var1;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String var1) {
        this.sign = var1;
    }

    public String getEncryptedData() {
        return this.encryptedData;
    }

    public void setEncryptedData(String var1) {
        this.encryptedData = var1;
    }
}

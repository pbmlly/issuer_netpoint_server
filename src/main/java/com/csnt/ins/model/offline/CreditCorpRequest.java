package com.csnt.ins.model.offline;

public class CreditCorpRequest {
    private String appId;
    private String appSecret;
    private String sign;
    private String encryptedData;

    public CreditCorpRequest() {
    }

    public String getAppId() {
        return this.appId;
    }

    public void setAppId(String var1) {
        this.appId = var1;
    }

    public String getAppSecret() {
        return this.appSecret;
    }

    public void setAppSecret(String var1) {
        this.appSecret = var1;
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

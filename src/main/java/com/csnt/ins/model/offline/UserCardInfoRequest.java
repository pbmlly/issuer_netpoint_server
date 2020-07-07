package com.csnt.ins.model.offline;

public class UserCardInfoRequest extends AbstractIssAccountRequest  {
    private Integer type;
    private String encryptedData;
    private String sign;

    public UserCardInfoRequest() {
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.CARDINFO;
//    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String var1) {
        this.sign = var1;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer var1) {
        this.type = var1;
    }

    public String getEncryptedData() {
        return this.encryptedData;
    }

    public void setEncryptedData(String var1) {
        this.encryptedData = var1;
    }
}
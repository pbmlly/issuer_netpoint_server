package com.csnt.ins.model.offline;

public class UserObuInfoRequest extends AbstractIssAccountRequest  {
    private Integer type;
    private String sign;
    private String encryptedData;

    public UserObuInfoRequest() {
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.OBUINFO;
//    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer var1) {
        this.type = var1;
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

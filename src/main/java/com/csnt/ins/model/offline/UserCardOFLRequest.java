package com.csnt.ins.model.offline;

public class UserCardOFLRequest extends AbstractIssAccountRequest {
    private String obuId;

    public UserCardOFLRequest() {
    }

    public String getObuId() {
        return this.obuId;
    }

    public void setObuId(String var1) {
        this.obuId = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.CARDOFL;
//    }
}
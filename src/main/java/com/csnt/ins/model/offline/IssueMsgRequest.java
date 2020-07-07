package com.csnt.ins.model.offline;

public class IssueMsgRequest extends AbstractIssTokenRequest{
    private String mobile;

    public String getMobile()
    {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
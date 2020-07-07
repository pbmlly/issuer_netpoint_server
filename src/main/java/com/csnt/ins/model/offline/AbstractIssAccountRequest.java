package com.csnt.ins.model.offline;

public class AbstractIssAccountRequest extends AbstractIssTokenRequest {
    private String accountId;

    public AbstractIssAccountRequest() {
    }

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String var1) {
        this.accountId = var1;
    }


}

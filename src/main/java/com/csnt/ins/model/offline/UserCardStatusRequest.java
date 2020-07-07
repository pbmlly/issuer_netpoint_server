package com.csnt.ins.model.offline;

public class UserCardStatusRequest extends AbstractIssAccountRequest{
    private String cardId;
    private Integer type;
    private Integer status;

    public UserCardStatusRequest() {
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.CARDSTATUS;
//    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String var1) {
        this.cardId = var1;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer var1) {
        this.type = var1;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer var1) {
        this.status = var1;
    }
}

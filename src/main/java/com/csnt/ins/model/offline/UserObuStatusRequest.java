package com.csnt.ins.model.offline;

public class UserObuStatusRequest  extends AbstractIssAccountRequest {
    private Integer type;
    private String obuId;
    private Integer status;

    public UserObuStatusRequest() {
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.OBUSTATUS;
//    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer var1) {
        this.type = var1;
    }

    public String getObuId() {
        return this.obuId;
    }

    public void setObuId(String var1) {
        this.obuId = var1;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer var1) {
        this.status = var1;
    }
}

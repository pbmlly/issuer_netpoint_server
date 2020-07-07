package com.csnt.ins.model.offline;

public class UserVPOFLRequest extends AbstractIssAccountRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    public UserVPOFLRequest() {
    }

    public Integer getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(Integer var1) {
        this.pageNo = var1;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer var1) {
        this.pageSize = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.VPOFL;
//    }
}

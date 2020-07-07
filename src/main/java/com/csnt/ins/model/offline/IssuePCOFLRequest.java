package com.csnt.ins.model.offline;

public class IssuePCOFLRequest extends AbstractIssAccountRequest {
    private String plateNum;
    private Integer plateColor;

    public IssuePCOFLRequest() {
    }

    public String getPlateNum() {
        return this.plateNum;
    }

    public void setPlateNum(String var1) {
        this.plateNum = var1;
    }

    public Integer getPlateColor() {
        return this.plateColor;
    }

    public void setPlateColor(Integer var1) {
        this.plateColor = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return IssueServiceCmd.PCOFL;
//    }
}
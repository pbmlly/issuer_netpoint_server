package com.csnt.ins.model.offline;

public class IssueVertifyOFLRequest  {
    private String plateNum;
    private Integer plateColor;
    private Integer type;
    private String sign;
    private String encryptedData;

    public IssueVertifyOFLRequest() {
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

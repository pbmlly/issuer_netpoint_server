package com.csnt.ins.model.offline;

public class OwnerOflModel  {
    private String driverId;
    private String driverName;
    private Integer driverIdType;
    private String positiveImageStr;
    private String negativeImageStr;
    private String driverPhone;
    private String driverAddr;

    public OwnerOflModel() {
    }

    public String getDriverId() {
        return this.driverId;
    }

    public void setDriverId(String var1) {
        this.driverId = var1;
    }

    public String getDriverName() {
        return this.driverName;
    }

    public void setDriverName(String var1) {
        this.driverName = var1;
    }

    public Integer getDriverIdType() {
        return this.driverIdType;
    }

    public void setDriverIdType(Integer var1) {
        this.driverIdType = var1;
    }

    public String getPositiveImageStr() {
        return this.positiveImageStr;
    }

    public void setPositiveImageStr(String var1) {
        this.positiveImageStr = var1;
    }

    public String getNegativeImageStr() {
        return this.negativeImageStr;
    }

    public void setNegativeImageStr(String var1) {
        this.negativeImageStr = var1;
    }

    public String getDriverPhone() {
        return this.driverPhone;
    }

    public void setDriverPhone(String var1) {
        this.driverPhone = var1;
    }

    public String getDriverAddr() {
        return this.driverAddr;
    }

    public void setDriverAddr(String var1) {
        this.driverAddr = var1;
    }
}
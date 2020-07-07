package com.csnt.ins.model.offline;

public class UserObuOFLRequest extends AbstractIssAccountRequest {
    private String vehicleId;

    public UserObuOFLRequest() {
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String var1) {
        this.vehicleId = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.OBUOFL;
//    }
}
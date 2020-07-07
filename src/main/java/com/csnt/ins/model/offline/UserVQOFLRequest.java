package com.csnt.ins.model.offline;

public class UserVQOFLRequest extends AbstractIssAccountRequest {
    private String vehicleId;

    public UserVQOFLRequest() {
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String var1) {
        this.vehicleId = var1;
    }

//    protected ServiceSubTypeAware getCmd() {
//        return UserCmd.VQOFL;
//    }
}

package com.csnt.ins.model.issuer.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseEtcVehicleinfoHistory<M extends BaseEtcVehicleinfoHistory<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setType(java.lang.Integer type) {
		set("type", type);
	}
	
	public java.lang.Integer getType() {
		return getInt("type");
	}

	public void setUserId(java.lang.String userId) {
		set("userId", userId);
	}
	
	public java.lang.String getUserId() {
		return getStr("userId");
	}

	public void setOwnerName(java.lang.String ownerName) {
		set("ownerName", ownerName);
	}
	
	public java.lang.String getOwnerName() {
		return getStr("ownerName");
	}

	public void setOwnerIdType(java.lang.Integer ownerIdType) {
		set("ownerIdType", ownerIdType);
	}
	
	public java.lang.Integer getOwnerIdType() {
		return getInt("ownerIdType");
	}

	public void setOwnerIdNum(java.lang.String ownerIdNum) {
		set("ownerIdNum", ownerIdNum);
	}
	
	public java.lang.String getOwnerIdNum() {
		return getStr("ownerIdNum");
	}

	public void setOwnerTel(java.lang.String ownerTel) {
		set("ownerTel", ownerTel);
	}
	
	public java.lang.String getOwnerTel() {
		return getStr("ownerTel");
	}

	public void setAddress(java.lang.String address) {
		set("address", address);
	}
	
	public java.lang.String getAddress() {
		return getStr("address");
	}

	public void setContact(java.lang.String contact) {
		set("contact", contact);
	}
	
	public java.lang.String getContact() {
		return getStr("contact");
	}

	public void setRegisteredType(java.lang.Integer registeredType) {
		set("registeredType", registeredType);
	}
	
	public java.lang.Integer getRegisteredType() {
		return getInt("registeredType");
	}

	public void setChannelId(java.lang.String channelId) {
		set("channelId", channelId);
	}
	
	public java.lang.String getChannelId() {
		return getStr("channelId");
	}

	public void setRegisteredTime(java.util.Date registeredTime) {
		set("registeredTime", registeredTime);
	}
	
	public java.util.Date getRegisteredTime() {
		return get("registeredTime");
	}

	public void setVehicleType(java.lang.String vehicleType) {
		set("vehicleType", vehicleType);
	}
	
	public java.lang.String getVehicleType() {
		return getStr("vehicleType");
	}

	public void setVehicleModel(java.lang.String vehicleModel) {
		set("vehicleModel", vehicleModel);
	}
	
	public java.lang.String getVehicleModel() {
		return getStr("vehicleModel");
	}

	public void setUseCharacter(java.lang.Integer useCharacter) {
		set("useCharacter", useCharacter);
	}
	
	public java.lang.Integer getUseCharacter() {
		return getInt("useCharacter");
	}

	public void setVIN(java.lang.String VIN) {
		set("VIN", VIN);
	}
	
	public java.lang.String getVIN() {
		return getStr("VIN");
	}

	public void setEngineNum(java.lang.String engineNum) {
		set("engineNum", engineNum);
	}
	
	public java.lang.String getEngineNum() {
		return getStr("engineNum");
	}

	public void setRegisterDate(java.util.Date registerDate) {
		set("registerDate", registerDate);
	}
	
	public java.util.Date getRegisterDate() {
		return get("registerDate");
	}

	public void setIssueDate(java.util.Date issueDate) {
		set("issueDate", issueDate);
	}
	
	public java.util.Date getIssueDate() {
		return get("issueDate");
	}

	public void setFileNum(java.lang.String fileNum) {
		set("fileNum", fileNum);
	}
	
	public java.lang.String getFileNum() {
		return getStr("fileNum");
	}

	public void setApprovedCount(java.lang.Integer approvedCount) {
		set("approvedCount", approvedCount);
	}
	
	public java.lang.Integer getApprovedCount() {
		return getInt("approvedCount");
	}

	public void setTotalMass(java.lang.Integer totalMass) {
		set("totalMass", totalMass);
	}
	
	public java.lang.Integer getTotalMass() {
		return getInt("totalMass");
	}

	public void setMaintenanceMass(java.lang.Integer maintenanceMass) {
		set("maintenanceMass", maintenanceMass);
	}
	
	public java.lang.Integer getMaintenanceMass() {
		return getInt("maintenanceMass");
	}

	public void setPermittedWeight(java.lang.Integer permittedWeight) {
		set("permittedWeight", permittedWeight);
	}
	
	public java.lang.Integer getPermittedWeight() {
		return getInt("permittedWeight");
	}

	public void setOutsideDimensions(java.lang.String outsideDimensions) {
		set("outsideDimensions", outsideDimensions);
	}
	
	public java.lang.String getOutsideDimensions() {
		return getStr("outsideDimensions");
	}

	public void setPermittedTowWeight(java.lang.Integer permittedTowWeight) {
		set("permittedTowWeight", permittedTowWeight);
	}
	
	public java.lang.Integer getPermittedTowWeight() {
		return getInt("permittedTowWeight");
	}

	public void setTestRecord(java.lang.String testRecord) {
		set("testRecord", testRecord);
	}
	
	public java.lang.String getTestRecord() {
		return getStr("testRecord");
	}

	public void setWheelCount(java.lang.Integer wheelCount) {
		set("wheelCount", wheelCount);
	}
	
	public java.lang.Integer getWheelCount() {
		return getInt("wheelCount");
	}

	public void setAxleCount(java.lang.Integer axleCount) {
		set("axleCount", axleCount);
	}
	
	public java.lang.Integer getAxleCount() {
		return getInt("axleCount");
	}

	public void setAxleDistance(java.lang.Integer axleDistance) {
		set("axleDistance", axleDistance);
	}
	
	public java.lang.Integer getAxleDistance() {
		return getInt("axleDistance");
	}

	public void setAxisType(java.lang.String axisType) {
		set("axisType", axisType);
	}
	
	public java.lang.String getAxisType() {
		return getStr("axisType");
	}

	public void setVehicleFeatureVersion(java.lang.String vehicleFeatureVersion) {
		set("vehicleFeatureVersion", vehicleFeatureVersion);
	}
	
	public java.lang.String getVehicleFeatureVersion() {
		return getStr("vehicleFeatureVersion");
	}

	public void setVehicleFeatureCode(java.lang.String vehicleFeatureCode) {
		set("vehicleFeatureCode", vehicleFeatureCode);
	}
	
	public java.lang.String getVehicleFeatureCode() {
		return getStr("vehicleFeatureCode");
	}

	public void setPayAccountNum(java.lang.String payAccountNum) {
		set("payAccountNum", payAccountNum);
	}
	
	public java.lang.String getPayAccountNum() {
		return getStr("payAccountNum");
	}

	public void setOperation(java.lang.Integer operation) {
		set("operation", operation);
	}
	
	public java.lang.Integer getOperation() {
		return getInt("operation");
	}

	public void setChannelType(java.lang.String channelType) {
		set("channelType", channelType);
	}
	
	public java.lang.String getChannelType() {
		return getStr("channelType");
	}

	public void setOrgId(java.lang.String orgId) {
		set("orgId", orgId);
	}
	
	public java.lang.String getOrgId() {
		return getStr("orgId");
	}

	public void setOperatorId(java.lang.String operatorId) {
		set("operatorId", operatorId);
	}
	
	public java.lang.String getOperatorId() {
		return getStr("operatorId");
	}

	public void setOpTime(java.util.Date opTime) {
		set("opTime", opTime);
	}
	
	public java.util.Date getOpTime() {
		return get("opTime");
	}

	public void setUploadResult(java.lang.String uploadResult) {
		set("uploadResult", uploadResult);
	}
	
	public java.lang.String getUploadResult() {
		return getStr("uploadResult");
	}

	public void setUploadResultMsg(java.lang.String uploadResultMsg) {
		set("uploadResultMsg", uploadResultMsg);
	}
	
	public java.lang.String getUploadResultMsg() {
		return getStr("uploadResultMsg");
	}

	public void setUploadTime(java.util.Date uploadTime) {
		set("uploadTime", uploadTime);
	}
	
	public java.util.Date getUploadTime() {
		return get("uploadTime");
	}

	public void setResponseTime(java.util.Date responseTime) {
		set("responseTime", responseTime);
	}
	
	public java.util.Date getResponseTime() {
		return get("responseTime");
	}

	public void setIsuploadBank(java.lang.Integer isuploadBank) {
		set("isuploadBank", isuploadBank);
	}
	
	public java.lang.Integer getIsuploadBank() {
		return getInt("isuploadBank");
	}

	public void setUploadBankResult(java.lang.String uploadBankResult) {
		set("uploadBankResult", uploadBankResult);
	}
	
	public java.lang.String getUploadBankResult() {
		return getStr("uploadBankResult");
	}

	public void setUploadBankResultMsg(java.lang.String uploadBankResultMsg) {
		set("uploadBankResultMsg", uploadBankResultMsg);
	}
	
	public java.lang.String getUploadBankResultMsg() {
		return getStr("uploadBankResultMsg");
	}

	public void setUploadBankTime(java.util.Date uploadBankTime) {
		set("uploadBankTime", uploadBankTime);
	}
	
	public java.util.Date getUploadBankTime() {
		return get("uploadBankTime");
	}

	public void setUploadStatus(java.lang.Integer uploadStatus) {
		set("uploadStatus", uploadStatus);
	}
	
	public java.lang.Integer getUploadStatus() {
		return getInt("uploadStatus");
	}

	public void setCreateTime(java.util.Date createTime) {
		set("createTime", createTime);
	}
	
	public java.util.Date getCreateTime() {
		return get("createTime");
	}

	public void setUpdateTime(java.util.Date updateTime) {
		set("updateTime", updateTime);
	}
	
	public java.util.Date getUpdateTime() {
		return get("updateTime");
	}

	public void setBackup1(java.lang.Integer backup1) {
		set("backup1", backup1);
	}
	
	public java.lang.Integer getBackup1() {
		return getInt("backup1");
	}

	public void setBackup2(java.lang.Integer backup2) {
		set("backup2", backup2);
	}
	
	public java.lang.Integer getBackup2() {
		return getInt("backup2");
	}

	public void setBackup3(java.lang.Integer backup3) {
		set("backup3", backup3);
	}
	
	public java.lang.Integer getBackup3() {
		return getInt("backup3");
	}

	public void setBackup4(java.lang.String backup4) {
		set("backup4", backup4);
	}
	
	public java.lang.String getBackup4() {
		return getStr("backup4");
	}

	public void setBackup5(java.lang.String backup5) {
		set("backup5", backup5);
	}
	
	public java.lang.String getBackup5() {
		return getStr("backup5");
	}

}

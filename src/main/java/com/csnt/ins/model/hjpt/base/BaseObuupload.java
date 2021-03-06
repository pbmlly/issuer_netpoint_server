package com.csnt.ins.model.hjpt.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseObuupload<M extends BaseObuupload<M>> extends Model<M> implements IBean {

	public void setCsntID(java.lang.String csntID) {
		set("csntID", csntID);
	}
	
	public java.lang.String getCsntID() {
		return getStr("csntID");
	}

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setBrand(java.lang.Integer brand) {
		set("brand", brand);
	}
	
	public java.lang.Integer getBrand() {
		return getInt("brand");
	}

	public void setModel(java.lang.String model) {
		set("model", model);
	}
	
	public java.lang.String getModel() {
		return getStr("model");
	}

	public void setUserId(java.lang.String userId) {
		set("userId", userId);
	}
	
	public java.lang.String getUserId() {
		return getStr("userId");
	}

	public void setVehicleId(java.lang.String vehicleId) {
		set("vehicleId", vehicleId);
	}
	
	public java.lang.String getVehicleId() {
		return getStr("vehicleId");
	}

	public void setEnableTime(java.lang.String enableTime) {
		set("enableTime", enableTime);
	}
	
	public java.lang.String getEnableTime() {
		return getStr("enableTime");
	}

	public void setExpireTime(java.lang.String expireTime) {
		set("expireTime", expireTime);
	}
	
	public java.lang.String getExpireTime() {
		return getStr("expireTime");
	}

	public void setRegisteredType(java.lang.Integer registeredType) {
		set("registeredType", registeredType);
	}
	
	public java.lang.Integer getRegisteredType() {
		return getInt("registeredType");
	}

	public void setRegisteredChannelId(java.lang.String registeredChannelId) {
		set("registeredChannelId", registeredChannelId);
	}
	
	public java.lang.String getRegisteredChannelId() {
		return getStr("registeredChannelId");
	}

	public void setRegisteredTime(java.lang.String registeredTime) {
		set("registeredTime", registeredTime);
	}
	
	public java.lang.String getRegisteredTime() {
		return getStr("registeredTime");
	}

	public void setInstallType(java.lang.Integer installType) {
		set("installType", installType);
	}
	
	public java.lang.Integer getInstallType() {
		return getInt("installType");
	}

	public void setInstallChannelId(java.lang.String installChannelId) {
		set("installChannelId", installChannelId);
	}
	
	public java.lang.String getInstallChannelId() {
		return getStr("installChannelId");
	}

	public void setInstallTime(java.lang.String installTime) {
		set("installTime", installTime);
	}
	
	public java.lang.String getInstallTime() {
		return getStr("installTime");
	}

	public void setStatus(java.lang.Integer status) {
		set("status", status);
	}
	
	public java.lang.Integer getStatus() {
		return getInt("status");
	}

	public void setStatusChangeTime(java.lang.String statusChangeTime) {
		set("statusChangeTime", statusChangeTime);
	}
	
	public java.lang.String getStatusChangeTime() {
		return getStr("statusChangeTime");
	}

	public void setOperation(java.lang.Integer operation) {
		set("operation", operation);
	}
	
	public java.lang.Integer getOperation() {
		return getInt("operation");
	}

	public void setCsntCreateTime(java.util.Date csntCreateTime) {
		set("csntCreateTime", csntCreateTime);
	}
	
	public java.util.Date getCsntCreateTime() {
		return get("csntCreateTime");
	}

	public void setCsntfileName(java.lang.String csntfileName) {
		set("csntfileName", csntfileName);
	}
	
	public java.lang.String getCsntfileName() {
		return getStr("csntfileName");
	}

	public void setBackup(java.lang.String backup) {
		set("backup", backup);
	}
	
	public java.lang.String getBackup() {
		return getStr("backup");
	}

	public void setIssucess(java.lang.String issucess) {
		set("issucess", issucess);
	}
	
	public java.lang.String getIssucess() {
		return getStr("issucess");
	}

}

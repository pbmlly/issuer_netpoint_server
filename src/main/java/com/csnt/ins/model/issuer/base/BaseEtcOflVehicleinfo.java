package com.csnt.ins.model.issuer.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseEtcOflVehicleinfo<M extends BaseEtcOflVehicleinfo<M>> extends Model<M> implements IBean {

	public void setVehicleId(java.lang.String vehicleId) {
		set("vehicleId", vehicleId);
	}
	
	public java.lang.String getVehicleId() {
		return getStr("vehicleId");
	}

	public void setUserId(java.lang.String userId) {
		set("userId", userId);
	}
	
	public java.lang.String getUserId() {
		return getStr("userId");
	}

	public void setDepUserId(java.lang.String depUserId) {
		set("depUserId", depUserId);
	}
	
	public java.lang.String getDepUserId() {
		return getStr("depUserId");
	}

	public void setDepVehicleId(java.lang.String depVehicleId) {
		set("depVehicleId", depVehicleId);
	}
	
	public java.lang.String getDepVehicleId() {
		return getStr("depVehicleId");
	}

	public void setAccountId(java.lang.String accountId) {
		set("accountId", accountId);
	}
	
	public java.lang.String getAccountId() {
		return getStr("accountId");
	}

	public void setUserType(java.lang.Integer userType) {
		set("userType", userType);
	}

	public java.lang.Integer getUserType() {
		return getInt("userType");
	}

	public void setLinkMobile(java.lang.String linkMobile) {
		set("linkMobile", linkMobile);
	}
	
	public java.lang.String getLinkMobile() {
		return getStr("linkMobile");
	}

	public void setBankUserName(java.lang.String bankUserName) {
		set("bankUserName", bankUserName);
	}
	
	public java.lang.String getBankUserName() {
		return getStr("bankUserName");
	}

	public void setCertsn(java.lang.String certsn) {
		set("certsn", certsn);
	}
	
	public java.lang.String getCertsn() {
		return getStr("certsn");
	}

	public void setProtocolNumber(java.lang.String protocolNumber) {
		set("protocolNumber", protocolNumber);
	}
	
	public java.lang.String getProtocolNumber() {
		return getStr("protocolNumber");
	}

	public void setPosId(java.lang.String posId) {
		set("posId", posId);
	}
	
	public java.lang.String getPosId() {
		return getStr("posId");
	}

	public void setGenTime(java.util.Date genTime) {
		set("genTime", genTime);
	}
	
	public java.util.Date getGenTime() {
		return get("genTime");
	}

	public void setTrxSerno(java.lang.String trxSerno) {
		set("trx_serno", trxSerno);
	}
	
	public java.lang.String getTrxSerno() {
		return getStr("trx_serno");
	}

	public void setEmployeeId(java.lang.String employeeId) {
		set("employeeId", employeeId);
	}
	
	public java.lang.String getEmployeeId() {
		return getStr("employeeId");
	}

	public void setOrgTrxSerno(java.lang.String orgTrxSerno) {
		set("org_trx_serno", orgTrxSerno);
	}
	
	public java.lang.String getOrgTrxSerno() {
		return getStr("org_trx_serno");
	}

	public void setCardType(java.lang.Integer cardType) {
		set("cardType", cardType);
	}
	
	public java.lang.Integer getCardType() {
		return getInt("cardType");
	}

	public void setAccType(java.lang.Integer accType) {
		set("acc_type", accType);
	}

	public java.lang.Integer getAccType() {
		return getInt("acc_type");
	}

	public void setBankPost(java.lang.String bankPost) {
		set("bankPost", bankPost);
	}
	
	public java.lang.String getBankPost() {
		return getStr("bankPost");
	}

	public void setIssuerChannelId(java.lang.String issuerChannelId) {
		set("issuerChannelId", issuerChannelId);
	}

	public java.lang.String getIssuerChannelId() {
		return getStr("issuerChannelId");
	}
	public void setChannelType(java.lang.String channelType) {
		set("channelType", channelType);
	}

	public java.lang.String getChannelType() {
		return getStr("channelType");
	}

	public void setBindStatus(java.lang.Integer bindStatus) {
		set("bindStatus", bindStatus);
	}

	public java.lang.Integer getBindStatus() {
		return getInt("bindStatus");
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

	public void setBackup1(java.lang.String backup1) {
		set("backup1", backup1);
	}
	
	public java.lang.String getBackup1() {
		return getStr("backup1");
	}

	public void setBackup2(java.lang.String backup2) {
		set("backup2", backup2);
	}
	
	public java.lang.String getBackup2() {
		return getStr("backup2");
	}

	public void setBackup3(java.lang.String backup3) {
		set("backup3", backup3);
	}
	
	public java.lang.String getBackup3() {
		return getStr("backup3");
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

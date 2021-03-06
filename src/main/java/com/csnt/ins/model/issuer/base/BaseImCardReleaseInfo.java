package com.csnt.ins.model.issuer.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseImCardReleaseInfo<M extends BaseImCardReleaseInfo<M>> extends Model<M> implements IBean {

	public void setAccountid(java.lang.String accountid) {
		set("accountid", accountid);
	}
	
	public java.lang.String getAccountid() {
		return getStr("accountid");
	}

	public void setOrderid(java.lang.String orderid) {
		set("orderid", orderid);
	}

	public java.lang.String getOrderid() {
		return getStr("orderid");
	}

	public void setVehicleid(java.lang.String vehicleid) {
		set("vehicleid", vehicleid);
	}
	
	public java.lang.String getVehicleid() {
		return getStr("vehicleid");
	}

	public void setCardid(java.lang.String cardid) {
		set("cardid", cardid);
	}
	
	public java.lang.String getCardid() {
		return getStr("cardid");
	}

	public void setCardtype(java.lang.Integer cardtype) {
		set("cardtype", cardtype);
	}
	
	public java.lang.Integer getCardtype() {
		return getInt("cardtype");
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

	public void setAgencyid(java.lang.String agencyid) {
		set("agencyid", agencyid);
	}
	
	public java.lang.String getAgencyid() {
		return getStr("agencyid");
	}

	public void setPlatenum(java.lang.String platenum) {
		set("platenum", platenum);
	}
	
	public java.lang.String getPlatenum() {
		return getStr("platenum");
	}

	public void setPlatecolor(java.lang.Integer platecolor) {
		set("platecolor", platecolor);
	}
	
	public java.lang.Integer getPlatecolor() {
		return getInt("platecolor");
	}

	public void setEnabletime(java.lang.String enabletime) {
		set("enabletime", enabletime);
	}
	
	public java.lang.String getEnabletime() {
		return getStr("enabletime");
	}

	public void setExpiretime(java.lang.String expiretime) {
		set("expiretime", expiretime);
	}
	
	public java.lang.String getExpiretime() {
		return getStr("expiretime");
	}

	public void setIssuedtype(java.lang.Integer issuedtype) {
		set("issuedtype", issuedtype);
	}
	
	public java.lang.Integer getIssuedtype() {
		return getInt("issuedtype");
	}

	public void setChannelid(java.lang.String channelid) {
		set("channelid", channelid);
	}
	
	public java.lang.String getChannelid() {
		return getStr("channelid");
	}

	public void setIssuedtime(java.lang.String issuedtime) {
		set("issuedtime", issuedtime);
	}
	
	public java.lang.String getIssuedtime() {
		return getStr("issuedtime");
	}

	public void setSign(java.lang.String sign) {
		set("sign", sign);
	}
	
	public java.lang.String getSign() {
		return getStr("sign");
	}

	public void setSynstatus(java.lang.String synstatus) {
		set("synstatus", synstatus);
	}
	
	public java.lang.String getSynstatus() {
		return getStr("synstatus");
	}

	public void setSyntime(java.util.Date syntime) {
		set("syntime", syntime);
	}
	
	public java.util.Date getSyntime() {
		return get("syntime");
	}

	public void setExpcol1(java.lang.String expcol1) {
		set("expcol1", expcol1);
	}
	
	public java.lang.String getExpcol1() {
		return getStr("expcol1");
	}

	public void setExpcol2(java.lang.String expcol2) {
		set("expcol2", expcol2);
	}
	
	public java.lang.String getExpcol2() {
		return getStr("expcol2");
	}

	public void setExpcol3(java.lang.String expcol3) {
		set("expcol3", expcol3);
	}
	
	public java.lang.String getExpcol3() {
		return getStr("expcol3");
	}

	public void setExpcol4(java.lang.String expcol4) {
		set("expcol4", expcol4);
	}
	
	public java.lang.String getExpcol4() {
		return getStr("expcol4");
	}

	public void setExpcol5(java.lang.String expcol5) {
		set("expcol5", expcol5);
	}
	
	public java.lang.String getExpcol5() {
		return getStr("expcol5");
	}

	public void setCreatedatetime(java.util.Date createdatetime) {
		set("createdatetime", createdatetime);
	}
	
	public java.util.Date getCreatedatetime() {
		return get("createdatetime");
	}

	public void setUpdatedatetime(java.util.Date updatedatetime) {
		set("updatedatetime", updatedatetime);
	}
	
	public java.util.Date getUpdatedatetime() {
		return get("updatedatetime");
	}

	public void setResponsestatus(java.lang.String responsestatus) {
		set("responsestatus", responsestatus);
	}
	
	public java.lang.String getResponsestatus() {
		return getStr("responsestatus");
	}

	public void setResponsemsg(java.lang.String responsemsg) {
		set("responsemsg", responsemsg);
	}
	
	public java.lang.String getResponsemsg() {
		return getStr("responsemsg");
	}

	public void setResponsetime(java.util.Date responsetime) {
		set("responsetime", responsetime);
	}
	
	public java.util.Date getResponsetime() {
		return get("responsetime");
	}

	public void setReqfilename(java.lang.String reqfilename) {
		set("reqfilename", reqfilename);
	}
	
	public java.lang.String getReqfilename() {
		return getStr("reqfilename");
	}

	public void setIssueid(java.lang.String issueid) {
		set("issueid", issueid);
	}
	
	public java.lang.String getIssueid() {
		return getStr("issueid");
	}

}

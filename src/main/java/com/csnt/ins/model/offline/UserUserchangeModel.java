package com.csnt.ins.model.offline;

public class UserUserchangeModel {
    private String accessToken;
    private String openId;

    private String accountId;
    private String code;
    private String name;
    private String positiveImageStr;
    private String negativeImageStr;
    private String phone;
    private String address;

    public UserUserchangeModel() {
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPositiveImageStr() {
        return positiveImageStr;
    }

    public void setPositiveImageStr(String positiveImageStr) {
        this.positiveImageStr = positiveImageStr;
    }

    public String getNegativeImageStr() {
        return negativeImageStr;
    }

    public void setNegativeImageStr(String negativeImageStr) {
        this.negativeImageStr = negativeImageStr;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



}

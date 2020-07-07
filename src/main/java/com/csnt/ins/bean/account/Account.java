package com.csnt.ins.bean.account;

/**
 * @ClassName Account
 * @Description 用户账号对象
 * @Author duwanjiang
 * @Date 2019/6/22 14:20
 * Version 1.0
 **/
public class Account {
    String account;
    String password;

    public Account(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

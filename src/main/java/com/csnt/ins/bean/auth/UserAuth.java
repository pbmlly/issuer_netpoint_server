package com.csnt.ins.bean.auth;

import com.csnt.ins.jwttoken.Bean.IJwtAble;

/**
 * @author luoxiaojian
 * @Description:
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class UserAuth<T extends IJwtAble> {
    private T user;
    private String token;
    private Long expiresIn;

    public UserAuth() {
    }

    public UserAuth(T user, String token, Long expiresIn) {
        this.user = user;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "UserAuth{" +
                "user=" + user +
                ", token='" + token + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}

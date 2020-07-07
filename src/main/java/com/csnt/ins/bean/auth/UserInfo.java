package com.csnt.ins.bean.auth;

import com.csnt.ins.jwttoken.Bean.IJwtAble;

import java.util.Date;
import java.util.List;

/**
 * @author luoxiaojian
 * @Description: 网点信息节点扩展信息
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class UserInfo implements IJwtAble {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户密码
     */
    private String userPwd;
    /**
     * 网点编号
     */
    private String posId;
    /**
     * 网点名称
     */
    private String posName;
    /**
     * 用户类型
     */
    private Integer type;
    /**
     * 渠道类型
     */
    private String channelType;
    /**
     * 是否激活
     */
    private Integer isActive;
    /**
     * 验证码
     */
    private String tocken;
    /**
     * 最后修改时间
     */
    private Date lastModifyPasswordTime;

    private String host;

    private String client;

    private Double longitude;

    private Double latitude;

    private List<Role> rolesList;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getAgencyId() {
        //客服合作机构编码是 网点编码的前11位
        return posId.length() >= 11 ? posId.substring(0, 11) : "";
    }


    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getTocken() {
        return tocken;
    }

    public void setTocken(String tocken) {
        this.tocken = tocken;
    }

    @Override
    public List<Role> getRoles() {
        return rolesList;
    }

    public void setRoles(List<Role> roles) {
        this.rolesList = roles;
    }

    @Override
    public List<String> getForces() {
        return null;
    }

    @Override
    public Date getLastModifyPasswordTime() {
        return lastModifyPasswordTime;
    }

    public void setLastModifyPasswordTime(Date lastModifyPasswordTime) {
        this.lastModifyPasswordTime = lastModifyPasswordTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userName='" + userName + '\'' +
                ", userPwd='" + userPwd + '\'' +
                ", posId='" + posId + '\'' +
                ", posName='" + posName + '\'' +
                ", type=" + type +
                ", channelType='" + channelType + '\'' +
                ", isActive=" + isActive +
                ", tocken='" + tocken + '\'' +
                ", lastModifyPasswordTime=" + lastModifyPasswordTime +
                ", host='" + host + '\'' +
                ", client='" + client + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", rolesList=" + rolesList +
                '}';
    }
}

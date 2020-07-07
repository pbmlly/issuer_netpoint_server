package com.csnt.ins.bean.netty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author source
 */
//msgLen-msgType;;userId;;authorization;;msgMd5;;jsonObj ({…}|[…])
public class DataInfo implements Serializable {
    /**
     * 是否是错误消息
     */
    private boolean error = false;
    /**
     * 访问ip
     */
    private String clientIp;
    /**
     * 访问端口
     */
    private int clientPort;
    /**
     * 消息类型
     */
    private String msgType="";
    /**
     * 用户名
     */
    private String userId="";
    /**
     * 鉴权码
     */
    private String authorization="";
    /**
     * 请求json的MD5
     */
    private String requestJsonMd5="";
    /**
     * 请求json
     */
    private String requestJson="";
    /**
     * 响应json的MD5
     */
    private String responseJsonMd5="";
    /**
    /**
     * 响应json
     */
    private String responseJson="";


    public DataInfo(String clientIp, int clientPort) {
        this.clientIp = clientIp;
        this.clientPort = clientPort;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getRequestJsonMd5() {
        return requestJsonMd5;
    }

    public void setRequestJsonMd5(String requestJsonMd5) {
        this.requestJsonMd5 = requestJsonMd5;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String getResponseJsonMd5() {
        return responseJsonMd5;
    }

    public void setResponseJsonMd5(String responseJsonMd5) {
        this.responseJsonMd5 = responseJsonMd5;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }
}

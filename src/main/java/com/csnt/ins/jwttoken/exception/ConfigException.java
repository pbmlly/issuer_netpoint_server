package com.csnt.ins.jwttoken.exception;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian
 * FOR : 配置项目产生的异常
 */
public class ConfigException extends RuntimeException {

    private static String messagePrefix = "Token 请求中的 对应的 ";

    private static String messageEnd = "不可以为 ";

    /**
     * 组合异常产生原因
     *
     * @param keyWord
     * @param status
     */
    public ConfigException(String keyWord, String status) {
        super(messagePrefix + keyWord + messageEnd + status);
    }
}

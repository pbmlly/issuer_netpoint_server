package com.csnt.ins.jwttoken.exception;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian
 * FOR : IJwtAble对象构建异常
 */
public class JwtAbleCreatedException extends RuntimeException {

    private static String messagePrefix = "构建 IJwtAble 的对象中的 对应的 ";

    private static String messageEnd = "属性不可以为 ";

    /**
     * 组合异常产生原因
     *
     * @param keyWord
     * @param status
     */
    public JwtAbleCreatedException(String keyWord, String status) {
        super(messagePrefix + keyWord + messageEnd + status);
    }
}

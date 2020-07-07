package com.csnt.ins.utils;

import java.math.BigDecimal;

/**
 * @author source
 * @version 2.0
 * @timestamp 2018/11/16
 */
public class MathUtil {

    /**
     * 字符串转Byte类型
     */
    public static Byte asByte(Object object) {
        return new BigDecimal(object.toString()).byteValueExact();
    }

    /**
     * 字符串转Short类型
     */
    public static Short asShort(Object object) {
        return new BigDecimal(object.toString()).shortValueExact();
    }


    /**
     * 字符串转Integer类型
     */
    public static Integer asInteger(Object object) {
        return new BigDecimal(object.toString()).intValueExact();
    }

    /**
     * 字符串转Long类型
     */
    public static Long asLong(Object object) {
        return new BigDecimal(object.toString()).longValueExact();
    }

    /**
     * 字符串转Double类型
     */
    public static Double asDouble(Object object) {
        return new BigDecimal(object.toString()).doubleValue();
    }

    /**
     * 字符串转Float类型
     */
    public static Float asFloat(Object object) {
        return new BigDecimal(object.toString()).floatValue();
    }

    /**
     * 字符串转BigDecimal类型
     */
    public static BigDecimal asBigDecimal(Object object) {
        return new BigDecimal(object.toString());
    }


    /**
     * 字符串转BigDecimal类型
     */
    public static Boolean asBoolean(Object object) {
        return 1==asInteger(object);
    }

    /**
     * int[]数组求和
     */
    public static int sumResultCount(int[] result) {
        int resultSum = 0;
        for (int temp : result) {
            resultSum += temp;
        }
        return resultSum;
    }
}

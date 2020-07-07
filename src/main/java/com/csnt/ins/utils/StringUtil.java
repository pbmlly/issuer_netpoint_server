package com.csnt.ins.utils;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Record;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * 字符串工具类
 *
 * @author source
 */
public class StringUtil {

    /**
     * 请求数据格式 split标识
     */
    public static final String RECEIVE_MSG_SPLIT = ";;";

    public static final Charset GBK = Charset.forName("GBK");

    public static final String UTF8_STR = "UTF-8";
    public static final Charset UTF8 = Charset.forName(UTF8_STR);

    /**
     * base64字符的前缀
     */
    public static final String BASE64_PREFIX_REGEX = "(data:)(.*)(;base64,)";


    /**
     * 空对象判断 如果多个对象其中有一个为空，那么返回true
     *
     * @param objs 所有的对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object... objs) {
        if (objs == null) {
            return true;
        }
        for (Object o : objs) {
            if (o == null || "".equals(o.toString().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 非空对象判断
     *
     * @param obj
     * @return
     */
    public static boolean isNotEmpty(Object... obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断是否包含元素
     *
     * @param array
     * @param str
     * @return
     */
    public static boolean isContained(String[] array, String str) {
        if (isEmpty(str)) {
            return false;
        }
        if (array == null) {
            return false;
        }
        for (String temp : array) {
            if (temp.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static String readString(ByteBuf in, int length) {
        return in.readCharSequence(length, UTF8).toString();
    }

    public static String readString(DataInputStream in, int length) throws IOException {
        byte[] data = new byte[length];
        in.read(data, 0, data.length);
        return new String(data, UTF8);
    }


    /**
     * 左填充字符
     *
     * @param src
     * @param len
     * @param ch
     * @return
     */
    public static String padLeft(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }
        char[] charr = new char[len];
        System.arraycopy(src.toCharArray(), 0, charr, diff, src.length());
        for (int i = 0; i < diff; i++) {
            charr[i] = ch;
        }
        return new String(charr);
    }

    /**
     * 判断json字符串是否有效
     *
     * @param jsonString 需验证字符串
     * @return 源字符串
     */
    public static String validJson(String jsonString) {
        if (JSON.isValid(jsonString)) {
            return jsonString;
        } else {
            throw new RuntimeException("解析报文体json文本失败");
        }

    }

    /**
     * 将二进制数据转为base64
     *
     * @param bytes 二进制数组
     * @return 源字符串
     */
    public static String getBase64StrByByteArr(byte[] bytes) {
        return new String(encodeBase64(bytes));

    }

    /**
     * 判断非空参数是否为空
     *
     * @param paramRecord
     * @param paramNames
     * @return
     */
    public static boolean isEmptyArg(Record paramRecord, String paramNames) {
        if (StringUtil.isEmpty(paramNames)) {
            return true;
        }
        String[] parsms = paramNames.split(",");
        for (String paramName : parsms) {
            if (StringUtil.isEmpty(paramRecord.getObject(paramName))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取base64图片的大小
     *
     * @param base64ImgStr
     * @return
     */
    public static int getBase64ImgSize(String base64ImgStr) {
        // 1.需要计算文件流大小，首先把头部的data:image/png;base64,（注意有逗号）去掉。
        String str = base64ImgStr.replaceFirst(BASE64_PREFIX_REGEX, "");
        //2.找到等号，把等号也去掉
        Integer equalIndex = str.indexOf("=");
        if (str.indexOf("=") > 0) {
            str = str.substring(0, equalIndex);
        }
        //3.原来的字符流大小，单位为字节
        Integer strLength = str.length();
        //4.计算后得到的文件流大小，单位为字节
        Integer size = strLength - (strLength / 8) * 2;
        return size;
    }

    /**
     * 获取uuid
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}

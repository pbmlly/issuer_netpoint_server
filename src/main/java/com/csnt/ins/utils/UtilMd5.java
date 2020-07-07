package com.csnt.ins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Created by huangxiang on 2017/6/7.
 */
public class UtilMd5 {
    private static final Logger logger = LoggerFactory.getLogger(UtilMd5.class);

    public static boolean checkMd5(String encodeStr, String md5Str) {
        return EncoderByMd5(encodeStr).equalsIgnoreCase(md5Str);
    }

    public static boolean checkMd5(byte[] encodeBytes, String md5Str) {
        return EncoderByMd5(encodeBytes).equalsIgnoreCase(md5Str);
    }

    public static boolean checkMd5(File file, String md5Str) {
        return EncoderByMd5(file).equalsIgnoreCase(md5Str);
    }


    public static String EncoderByMd5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(bytes);
            return byteArrayToHex(array);
        } catch (Exception e) {
            logger.error("md5 encode error!  str " + bytes.toString(), e);
        }
        return null;
    }

    private static String byteArrayToHex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static String EncoderByMd5(String str) {
        return EncoderByMd5(str.getBytes(Charset.forName("UTF-8")));
    }

    public static String EncoderByMd5Gbk(String str) {
        return EncoderByMd5(str.getBytes(Charset.forName("GBK")));
    }

    public static String EncoderByMd5(File file) {
        InputStream ins = null;
        DigestInputStream digestInputStream = null;
        try {
            ins = new FileInputStream(file);
            byte[] buffer = null;
            if (file.length() > 1024) {
                buffer = new byte[1024];
            } else {
                buffer = new byte[(int) file.length()];
            }

            MessageDigest md5 = MessageDigest.getInstance("MD5");

            digestInputStream = new DigestInputStream(ins, md5);

            while (digestInputStream.read(buffer) > 0) {
                ;
            }

            // 获取最终的MessageDigest
            md5 = digestInputStream.getMessageDigest();

            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = md5.digest();

            // 同样，把字节数组转换成字符串
            return byteArrayToHex(resultByteArray);
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } finally {
            try {
                digestInputStream.close();

            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
            try {
                ins.close();
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
        return "";
    }
}

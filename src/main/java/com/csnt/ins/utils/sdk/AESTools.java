package com.csnt.ins.utils.sdk;

import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESTools  {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_PASS_WORD = "com.all-in-data#";

    public AESTools() {
    }

    public static String decrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, getSecretKey(password));
            byte[] result = cipher.doFinal(Base64Tools.decodeFromString(content));
            return new String(result, "utf-8");
        } catch (Exception var4) {
            LoggerFactory.getLogger(AESTools.class).error("", var4);
            throw new RuntimeException("AES解密失败:" + content);
        }
    }

    public static String encrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(1, getSecretKey(password));
            byte[] result = cipher.doFinal(byteContent);
            return Base64Tools.encodeToString(result);
        } catch (Exception var5) {
            LoggerFactory.getLogger(AESTools.class).error("", var5);
            throw new RuntimeException("AES加密失败:" + content);
        }
    }

    private static SecretKeySpec getSecretKey(String password) {
        try {
            return new SecretKeySpec(password.getBytes("UTF-8"), "AES");
        } catch (Exception var2) {
            LoggerFactory.getLogger(AESTools.class).error("", var2);
            throw new RuntimeException("生成加密秘钥");
        }
    }
}

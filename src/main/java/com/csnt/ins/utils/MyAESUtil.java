package com.csnt.ins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author cml
 * @date 2019/11/19
 */
public class MyAESUtil {
    private static final Logger logger = LoggerFactory.getLogger(MyAESUtil.class);

    /**
     * java使用AES加密解密 AES-128-ECB加密
     * 与mysql数据库aes加密算法通用
     * 数据库aes加密解密
     * -- 加密
     *    SELECT to_base64(AES_ENCRYPT('www.gowhere.so','jkl;POIU1234++=='));
     *    -- 解密
     *    SELECT AES_DECRYPT(from_base64('Oa1NPBSarXrPH8wqSRhh3g=='),'jkl;POIU1234++==');
     * @author 836508
     *
     */
    /**
     *  加密
     * @param sSrc
     * @return
     * @throws Exception
     */
    public static String Encrypt(String sSrc) throws Exception {
        if (StringUtil.isEmpty(sSrc)) {
            return null;
        }
        String sKey = SysConfig.getAesKey();
        if (sKey == null) {
            logger.info("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            logger.info("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    /**
     *  解密
     * @param sSrc
     * @return
     * @throws Exception
     */
    public static String Decrypt(String sSrc) throws Exception {
        if (StringUtil.isEmpty(sSrc)) {
            return null;
        }
        String sKey = SysConfig.getAesKey();
        try {
            // 判断Key是否正确
            if (sKey == null) {
                logger.info("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                logger.info("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
}

package com.csnt.ins.utils.sdk;

import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SignatureTools {
    private static final Charset DEFAULT_CHARSET;

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }

    public SignatureTools() {
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
    private static SecretKeySpec getSecretKey(String password) {
        try {
            return new SecretKeySpec(password.getBytes("UTF-8"), "AES");
        } catch (Exception var2) {
            LoggerFactory.getLogger(AESTools.class).error("", var2);
            throw new RuntimeException("生成加密秘钥");
        }
    }

    public static String rsa256Sign(String content, String priKeyBase64) {
        byte[] signed = sign(SignAlgorithm.SHA256withRSA, content.getBytes(DEFAULT_CHARSET), Base64Tools.decodeFromString(priKeyBase64));
        return Base64Tools.encodeToString(signed);
    }

    public static boolean rsa256Verify(String content, String pubKeyBase64, String signBase64) {
        return verify(SignAlgorithm.SHA256withRSA, content.getBytes(DEFAULT_CHARSET), Base64Tools.decodeFromString(signBase64), Base64Tools.decodeFromString(pubKeyBase64));
    }

    public static byte[] sign(SignAlgorithm algorithm, byte[] content, byte[] key) {
        try {
            PrivateKey priKey = generatePrivateKey(algorithm, key);
            Signature signature = Signature.getInstance(algorithm.getValue());
            signature.initSign(priKey);
            signature.update(content);
            byte[] signed = signature.sign();
            return signed;
        } catch (Exception var6) {
            throw new RuntimeException(var6);
        }
    }

    public static boolean verify(SignAlgorithm algorithm, byte[] content, byte[] sign, byte[] key) {
        try {
            Signature signature = Signature.getInstance(algorithm.getValue());
            PublicKey publicKey = generatePublicKey(algorithm, key);
            signature.initVerify(publicKey);
            signature.update(content);
            return signature.verify(sign);
        } catch (Exception var6) {
            var6.printStackTrace();
            throw new RuntimeException(var6);
        }
    }

    public static PrivateKey generatePrivateKey(SignAlgorithm algorithmType, byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return generatePrivateKey(algorithmType, (KeySpec)(new PKCS8EncodedKeySpec(key)));
    }

    public static PrivateKey generatePrivateKey(SignAlgorithm algorithmType, KeySpec keySpec) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return KeyFactory.getInstance(algorithmType.getType()).generatePrivate(keySpec);
    }

    public static PublicKey generatePublicKey(SignAlgorithm algorithm, byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return generatePublicKey(algorithm, (KeySpec)(new X509EncodedKeySpec(key)));
    }

    public static PublicKey generatePublicKey(SignAlgorithm algorithm, KeySpec keySpec) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return KeyFactory.getInstance(algorithm.getType()).generatePublic(keySpec);
    }
}

package com.csnt.ins.utils.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class SecurityTools {
    protected static Logger logger = LoggerFactory.getLogger(SecurityTools.class);

    private static char[] digit = new char[]{'C', 'h', 'e', 'n', 'P', 'e', 'i', 'A', 'n', '8', '1', '0', '2', '1', '6', 'C'};
    private static char[] commonDigit = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static String DEFAULT_KEY = "MyBeautyCyc20111121";

    public SecurityTools() {
    }

    public static String encryptStr(String str, HashType type, boolean useCommonDigit) {
        byte[] b = hash(str.getBytes(Charset.forName("UTF-8")), type);
        return toHexString(b, useCommonDigit);
    }

    public static byte[] hash(byte[] data, HashType type) {
        MessageDigest md = type.getDigest();
        md.update(data);
        return md.digest();
    }

    private static String toHexString(byte[] b, boolean useCommonDigit) {
        StringBuilder sb = new StringBuilder(b.length * 2);

        for (int i = 0; i < b.length; ++i) {
            if (useCommonDigit) {
                sb.append(commonDigit[(b[i] & 240) >>> 4]);
                sb.append(commonDigit[b[i] & 15]);
            } else {
                sb.append(digit[(b[i] & 240) >>> 4]);
                sb.append(digit[b[i] & 15]);
            }
        }

        return sb.toString();
    }

    public static enum HashType {
        MD5("MD5") {
        },
        SHA_1("SHA-1") {
        },
        SHA_256("SHA-256") {
        },
        SHA_384("SHA-384") {
        },
        SHA_512("SHA-512") {
        };

        private String value;

        private HashType(String value) {
            this.value = value;
        }

        public MessageDigest getDigest() {
            try {
                return MessageDigest.getInstance(this.getValue());
            } catch (NoSuchAlgorithmException var2) {
                throw new RuntimeException("没有这种算法:" + this.value);
            }
        }

        public String getValue() {
            return this.value;
        }
    }
}

package com.csnt.ins.utils.sdk;

public enum SignAlgorithm {
    NONEwithRSA("NONEwithRSA", "RSA"),
    MD2withRSA("MD2withRSA", "RSA"),
    MD5withRSA("MD5withRSA", "RSA"),
    SHA1withRSA("SHA1withRSA", "RSA"),
    SHA256withRSA("SHA256withRSA", "RSA"),
    SHA384withRSA("SHA384withRSA", "RSA"),
    SHA512withRSA("SHA512withRSA", "RSA"),
    NONEwithDSA("NONEwithDSA", "DSA"),
    SHA1withDSA("SHA1withDSA", "DSA"),
    NONEwithECDSA("NONEwithECDSA", "EC"),
    SHA1withECDSA("SHA1withECDSA", "EC"),
    SHA256withECDSA("SHA256withECDSA", "EC"),
    SHA384withECDSA("SHA384withECDSA", "EC"),
    SHA512withECDSA("SHA512withECDSA", "EC");

    private String value;
    private String type;

    private SignAlgorithm(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

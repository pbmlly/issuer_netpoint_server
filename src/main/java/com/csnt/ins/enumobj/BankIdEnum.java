package com.csnt.ins.enumobj;

/**
 * 中国工商银行	63010102001
 * 中国建设银行	63010102002
 * 中国银行青海省分行	63010102003
 * 中国农业银行	63010102004
 * 中国邮政储蓄银行	63010102006
 * 青海银行	63010102033
 * 微信ETC小程序	63010188001
 * 客服中心	63010199999
 *
 * @ClassName OrderStatusEnum
 * @Description 银行编码
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum BankIdEnum {
    /**
     * 工商银行
     */
    ICBC_BANK("63010102001"),
    /**
     * 中国建设银行
     */
    CCB_BANK("63010102002"),
    /**
     * 中国银行青海省分行
     */
    QH_BRANCH_BANK("63010102003"),
    /**
     * 中国农业银行
     */
    ABC_BANK("63010102004"),
    /**
     * 中国邮政储蓄银行
     */
    PSBC_BANK("63010102006"),
    /**
     * 青海银行
     */
    QH_BANK("63010102033"),
    /**
     * 微信ETC小程序
     */
    WX_ETC("63010188001"),
    /**
     * 客服中心
     */
    CSC_CENTER("63010199999");

    private BankIdEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    /**
     * 获取部中心签约渠道
     *
     * @return
     */
    public static String getDepSignChannel(String bankPost) {
        String signChannel = "";
        switch (bankPost) {
            //中国工商银行
            case "63010102001":
                signChannel = "001";
                break;
            //中国建设银行
            case "63010102002":
                signChannel = "002";
                break;
            //中国银行青海省分行
            case "63010102003":
                signChannel = "003";
                break;
            //中国农业银行
            case "63010102004":
                signChannel = "004";
                break;
            //中国邮政储蓄银行
            case "63010102006":
                signChannel = "006";
                break;
            //青海银行
            case "63010102033":
                signChannel = "028";
                break;
            //微信ETC小程序
            case "63010188001":
                signChannel = "102";
                break;
            //微信ETC小程序
            case "63010188003":
                signChannel = "102";
                break;
            default:
                signChannel = "103";
        }
        return signChannel;
    }
}

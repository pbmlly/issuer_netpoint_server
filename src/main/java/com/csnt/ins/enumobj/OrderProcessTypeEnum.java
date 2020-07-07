package com.csnt.ins.enumobj;

/**
 * 订单处理类型
 */
public enum OrderProcessTypeEnum {
    /**
     * 手持机
     */
    ORDER_HANDSET(1),
    /**
     * 快发
     */
    ORDER_FAST(2),
    /**
     * 通用（快发、手持）
     */
    ORDER_NORMAL(3),
    /**
     * 4-微信小程序二发
     */
    ORDER_WX2(4),
    /**
     * 5-微信小程序
     */
    ORDER_WX(5),
    /**
     * 6-支付宝小程序
     */
    ORDER_ZFB(6),
    /**
     * 7-云闪付小程序
     */
    ORDER_YSF(7),

    /**
     * 8-世纪恒通
     */
    ORDER_VETC(8);

    int value;

    private OrderProcessTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean equals(int value) {
        return this.value == value;
    }
}

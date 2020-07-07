package com.csnt.ins.enumobj;

/**
 * @ClassName OrderStatusEnum
 * @Description 订单状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum OrderStatusEnum {
    /**
     * 2- 未支付
     */
    UNPAY(2),
    /**
     * 3- 已支付
     */
    PAYED(3),
    /**
     * 4- 已撤单
     */
    CANCELED(4),
    /**
     * 5- 退货中
     */
    INRETURN(5),
    /**
     * 6- 已退货
     */
    RETURNED(6);

    private OrderStatusEnum(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public boolean equals(int value) {
        return this.value == value;
    }
}

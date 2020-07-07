package com.csnt.ins.enumobj;

/**
 * @ClassName OrderStatusEnum
 * @Description 订单处理状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum OrderProcessStatusEnum {
    /**
     * 未处理
     */
    UNPROCESS(0),
    /**
     * 处理中
     */
    PROCESSING(1),
    /**
     * 处理完成
     */
    PROCESSED(2);

    private OrderProcessStatusEnum(int value) {
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

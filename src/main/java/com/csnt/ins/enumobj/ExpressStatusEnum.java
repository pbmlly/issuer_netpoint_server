package com.csnt.ins.enumobj;

/**
 * @ClassName OrderStatusEnum
 * @Description 邮寄状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ExpressStatusEnum {
    /**
     * 1- 未发货
     */
    UNEXPRESS(1),
    /**
     * 2- 已发货
     */
    EXPRESSED(2),
    /**
     * 3- 已激活
     */
    ACTIVATED(3),
    /**
     * 4- 已评价
     */
    EVALUATED(4);

    private ExpressStatusEnum(int value) {
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

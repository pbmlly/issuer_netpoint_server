package com.csnt.ins.enumobj;

/**
 * @ClassName ServiceTypeEnum
 * @Description 服务类型
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ServiceTypeEnum {
    /**
     * 1-撤单
     */
    CANCEL(1),
    /**
     * 2-退货
     */
    RETURN(2),
    /**
     * 3-换签
     */
    CHANGE_OBU(3),
    /**
     * 4-换卡
     */
    CHANGE_CARD(4),
    /**
     * 5- 换卡签全套
     */
    CHANGE_ALL(5);

    private ServiceTypeEnum(int value) {
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

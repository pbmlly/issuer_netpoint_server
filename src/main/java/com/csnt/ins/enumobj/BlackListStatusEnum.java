package com.csnt.ins.enumobj;

/**
 * @ClassName OrderStatusEnum
 * @Description 黑名单状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum BlackListStatusEnum {
    /**
     * 1- 进入名单
     */
    CREATE(1),
    /**
     * 2- 解除名单
     */
    DELETE(2);

    private BlackListStatusEnum(int value) {
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

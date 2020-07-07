package com.csnt.ins.enumobj;

/**
 * @ClassName OperationEnum
 * @Description 操作类型
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum OperationEnum {
    /**
     * 1-新增
     * 2-变更
     * 3-删除
     */
    ADD(1),
    /**
     * 变更
     */
    UPDATE(2),
    /**
     * 删除
     */
    DELETE(3);

    private OperationEnum(int value) {
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

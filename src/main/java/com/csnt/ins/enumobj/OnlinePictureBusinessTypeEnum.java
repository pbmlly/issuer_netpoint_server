package com.csnt.ins.enumobj;

/**
 * 1-互联网订单图片
 * 2-线下挂起图片
 * 3-激活OBU图片
 *
 * @ClassName OrderStatusEnum
 * @Description 图片表业务类型
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum OnlinePictureBusinessTypeEnum {
    /**
     * 1-互联网订单图片
     */
    INTERNET(1, "互联网订单图片"),
    /**
     * 2-线下挂起图片
     */
    OFL_HANG(2, "线下挂起图片"),
    /**
     * 3-激活OBU图片
     */
    OBU_ACITVATE(3, "激活OBU图片");

    private OnlinePictureBusinessTypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private int value;
    private String name;

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}

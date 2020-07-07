package com.csnt.ins.enumobj;

/**
 * @ClassName OrderStatusEnum
 * @Description 订单类型
 * 1-新办
 * 2- 换签
 * 3- 换卡
 * 4- 换卡签全套
 * 5- 签补办
 * 6- 卡补办
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum OrderTypeEnum {
    /**
     * 1- 新办
     */
    NEW(1, "新办"),
    /**
     * 2- 换签
     */
    CHANGE_OBU(2, "换签"),
    /**
     * 3- 换卡
     */
    CHANGE_CARD(3, "换卡"),
    /**
     * 4- 换卡签全套
     */
    CHANGE_ALL(4, "换卡签全套"),
    /**
     * 5- 签补办
     */
    REISSUE_OBU(5, "签补办"),
    /**
     * 6- 卡补办
     */
    REISSUE_CARD(6, "卡补办");

    private OrderTypeEnum(int value, String name) {
        this.name = name;
        this.value = value;
    }

    private int value;
    private String name;

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    /**
     * 判断是否和当前值相等
     *
     * @param type
     * @return
     */
    public boolean equals(int type) {
        return value == type;
    }

    /**
     * 返回当前业务类型的名称
     *
     * @param type
     * @return
     */
    public static String getName(int type) {
        for (OrderTypeEnum orderTypeEnum : OrderTypeEnum.values()) {
            if (orderTypeEnum.equals(type)) {
                return orderTypeEnum.getName();
            }
        }
        return null;
    }
}

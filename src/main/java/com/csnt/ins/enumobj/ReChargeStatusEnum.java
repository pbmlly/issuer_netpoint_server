package com.csnt.ins.enumobj;

/**
 * @ClassName OfflineBusinessTypeEnum
 * @Description 充值状态
 * 0-未确认
 * 1-已确认
 * 2-已抵充
 * @Author duwanjiang
 * @Date 2019/8/11 17:12
 * Version 1.0
 **/
public enum ReChargeStatusEnum {
    /**
     * 0- 待确认
     */
    UNCONFIRM(0, "待确认"),
    /**
     * 1- 已确认
     */
    CONFIRMED(1, "已确认"),
    /**
     * 2- 已抵充
     */
    OFFSET(2, "已抵充");

    private ReChargeStatusEnum(int value, String name) {
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
     * 判断是否和当前值相等
     *
     * @param type
     * @return
     */
    public boolean equals(Integer type) {
        return value == type;
    }


    /**
     * 获取业务类型的名称
     *
     * @param type
     * @return
     */
    public static String getName(int type) {
        for (ReChargeStatusEnum offlineBusinessTypeEnum : ReChargeStatusEnum.values()) {
            if (offlineBusinessTypeEnum.equals(type)) {
                return offlineBusinessTypeEnum.getName();
            }
        }
        return null;
    }
}

package com.csnt.ins.enumobj;

/**
 * @ClassName OfflineBusinessTypeEnum
 * @Description 充值业务类型
 * 1-充值操作
 * 2-抵充操作
 * 3-冲正操作
 * 4-冲正抵充操作
 * @Author duwanjiang
 * @Date 2019/8/11 17:12
 * Version 1.0
 **/
public enum ReChargeBusinessTypeEnum {
    /**
     * 1- 充值操作
     */
    RECHARGE(1, "充值操作"),
    /**
     * 2- 抵充操作
     */
    RECHARGE_OFFSET(2, "抵充操作"),
    /**
     * 3- 冲正操作
     */
    CHARGE_BACK(3, "冲正操作"),
    /**
     * 4- 冲正抵充操作
     */
    CHARGE_BACK_OFFSET(4, "冲正抵充操作");

    private ReChargeBusinessTypeEnum(int value, String name) {
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
        for (ReChargeBusinessTypeEnum offlineBusinessTypeEnum : ReChargeBusinessTypeEnum.values()) {
            if (offlineBusinessTypeEnum.equals(type)) {
                return offlineBusinessTypeEnum.getName();
            }
        }
        return null;
    }
}

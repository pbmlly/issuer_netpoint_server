package com.csnt.ins.enumobj;

/**
 * @ClassName OfflineBusinessTypeEnum
 * @Description 线下业务类型
 * 1-新办
 * 2- 换签
 * 3- 换卡
 * 4- 换卡签全套
 * 5- 签补办
 * 6- 卡补办
 * 7- 挂起
 * 8- 解挂起
 * 9- 核销
 * 10-挂失
 * 11-解挂失
 * 20-卡挂起
 * 21-签挂起
 * 22-卡注销
 * 23-签注销
 * 24-卡解挂
 * 25-签解挂
 * 26-卡挂失
 * 27-签挂失
 * 28-卡解挂失
 * 29-签解挂失
 * @Author duwanjiang
 * @Date 2019/8/11 17:12
 * Version 1.0
 **/
public enum OfflineBusinessTypeEnum {
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
    REISSUE_CARD(6, "卡补办"),
    /**
     * 7- 挂起
     */
    HANG_UP(7, "挂起"),
    /**
     * 8- 解挂起
     */
    UNHANG(8, "解挂起"),
    /**
     * 9- 核销
     */
    CANCEL(9, "核销"),
    /**
     * 10-挂失
     */
    LOST(10, "挂失"),
    /**
     * 11-解挂失
     */
    UNLOST(11, "解挂失"),
    /**
     * 20-卡挂起
     */
    HANG_UP_CARD(20, "卡挂起"),
    /**
     * 21-签挂起
     */
    HANG_UP_OBU(21, "签挂起"),
    /**
     * 22-卡注销
     */
    CANCEL_CARD(22, "卡注销"),
    /**
     * 23-签注销
     */
    CANCEL_OBU(23, "签注销"),
    /**
     * 24-卡解挂
     */
    UNHANG_CARD(24, "卡解挂"),
    /**
     * 25-签解挂
     */
    UNHANG_OBU(25, "签解挂"),
    /**
     * 26-卡挂失
     */
    LOSS_OF_CARD(26, "卡挂失"),
    /**
     * 27-签挂失
     */
    LOSS_OF_OBU(27, "签挂失"),
    /**
     * 28-卡解挂失
     */
    UNLOSS_OF_CARD(28, "卡解挂失"),
    /**
     * 29-签解挂失
     */
    UNLOSS_OF_OBU(29, "签解挂失");

    private OfflineBusinessTypeEnum(int value, String name) {
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
     * 2- 换签
     * 3- 换卡
     * 4- 换卡签全套
     * 5- 签补办
     * 6- 卡补办
     * 7- 挂起
     * 8- 解挂起
     * 9- 核销
     * 20-卡挂起
     * 21-签挂起
     * 22-卡注销
     * 23-签注销
     * 24-卡解挂
     * 25-签解挂
     * 26-卡挂失
     * 27-签挂失
     * 28-卡解挂失
     * 29-签解挂失
     *
     * @param type
     * @return
     */
    public static int changeBusinessType(int type) {
        if (type == 20) {
            return 7;
        } else if (type == 21) {
            return 7;
        } else if (type == 22) {
            return 9;
        } else if (type == 23) {
            return 9;
        } else if (type == 24) {
            return 8;
        } else if (type == 25) {
            return 8;
        } else if (type == 26) {
            return 10;
        } else if (type == 27) {
            return 10;
        } else if (type == 28) {
            return 11;
        } else if (type == 29) {
            return 11;
        }
        return type;
    }

    /**
     * 获取业务类型的名称
     *
     * @param type
     * @return
     */
    public static String getName(int type) {
        for (OfflineBusinessTypeEnum offlineBusinessTypeEnum : OfflineBusinessTypeEnum.values()) {
            if (offlineBusinessTypeEnum.equals(type)) {
                return offlineBusinessTypeEnum.getName();
            }
        }
        return null;
    }
}

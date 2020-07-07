package com.csnt.ins.enumobj;

/**
 * 1-  正常
 * 2-  有卡挂起
 * 3-  无卡挂起
 * 4-  有卡注销
 * 5-  无卡注销
 * 6-  卡挂失
 *
 * @ClassName OrderStatusEnum
 * @Description 卡线下渠道状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum CardOflStatusEnum {
    /**
     * 1-  正常
     */
    NORMAL(1),
    /**
     * 2-  有卡挂起
     */
    HANG_UP_WITH_CARD(2),
    /**
     * 3-  无卡挂起
     */
    HANG_UP_WITHOUT_CARD(3),
    /**
     * 4-  有卡注销
     */
    CANCEL_WITH_CARD(4),
    /**
     * 5-  无卡注销
     */
    CANCEL_WITHOUT_CARD(5),
    /**
     * 6-  卡挂失
     */
    LOSS_OF_CARD(6);

    private CardOflStatusEnum(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public boolean equals(int value) {
        return this.value == value;
    }
    /**
     * 卡的状态
     * 1-	正常
     * 2-	有卡挂起
     * 3-	无卡挂起
     * 4-	有卡注销
     * 5-	无卡注销
     * 6-	卡挂失
     * <p>
     * 业务类型：
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
     *
     * @param type
     * @return
     */
    public static int getStatusByBusinessType(int type) {
        //3- 换卡
        //4- 换卡签全套
        //6- 卡补办
        //7- 挂起
        //8- 解挂起
        if (OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)) {
            //更换
            return CANCEL_WITH_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //补办
            return CANCEL_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //9- 核销
            return CANCEL_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)) {
            //7- 挂起
            return HANG_UP_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            //8- 解挂起
            return NORMAL.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)) {
            //挂失
            return LOSS_OF_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            //解挂失
            return NORMAL.getValue();
        }
        return NORMAL.getValue();
    }
}

package com.csnt.ins.enumobj;

/**
 * 1-  卡挂失
 * 2-  无卡挂起
 * 3-  无卡注销
 * 4-  账户透支
 * 5-  合作机构黑名单
 * 6-  车型不符
 *
 * @ClassName OrderStatusEnum
 * @Description 黑名单类型
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum CardBlackListTypeEnum {
    /**
     * 1-  卡挂失
     */
    REPORT_LOSS(1),
    /**
     * 2-  无卡挂起
     */
    HANG_UP_WITHOUT_CARD(2),
    /**
     * 3-  无卡注销
     */
    CANCEL_WITHOUT_CARD(3),
    /**
     * 4-  账户透支
     */
    ACCOUNT_OVERDRAFT(4),
    /**
     * 5-  合作机构黑名单
     */
    GIZ(5),
    /**
     * 6-  车型不符
     */
    TYPE_DISCREPANCY(6);

    private CardBlackListTypeEnum(int value) {
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
    public static int getBlacklistTypeByBusinessType(int type) {
        //1-	卡挂失
        //2-	无卡挂起
        //3-	无卡注销
        //4-	账户透支
        //5-	合作机构黑名单
        //6-	车型不符
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //更换/补办
            return CANCEL_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)) {
            //挂起/解挂
            return HANG_UP_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //9- 核销
            return CANCEL_WITHOUT_CARD.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)) {
            //挂失
            return REPORT_LOSS.getValue();
        } else {
            return REPORT_LOSS.getValue();
        }
    }
}

package com.csnt.ins.enumobj;

/**
 * 101-核销
 * 102-挂起
 * 103-未关联扣款渠道
 * 104-账户透支
 * 105-支付机构限制使用
 * <p>
 * 老版本：
 * 1-标签挂失
 * 2-无签挂起
 * 3-无签注销
 * 4-车型不符
 *
 * @ClassName OrderStatusEnum
 * @Description 黑名单类型
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ObuBlackListTypeEnum {
    /**
     * 101-核销
     */
    NEW_CANCEL(101),
    /**
     * 102-挂起
     */
    NEW_HANG_UP(102),
    /**
     * 103-未关联扣款渠道
     */
    NEW_WITHOUT_CHANNEL(103),
    /**
     * 104-账户透支
     */
    NEW_OVERDRAFT(104),
    /**
     * 105-支付机构限制使用
     */
    NEW_RESTRICT_USE(5),
    /**
     * 1-标签挂失
     */
    OLD_SIGN_MISSING(1),
    /**
     * 2-无签挂起
     */
    OLD_HANG_UP_WITHOUT_SIGN(2),
    /**
     * 3-无签注销
     */
    OLD_CANCEL_WITHOUT_SIGN(3),
    /**
     * 4-车型不符
     */
    OLD_TYPE_DISCREPANCY(4);

    private ObuBlackListTypeEnum(int value) {
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
        //1-标签挂失
        //2-无签挂起
        //3-无签注销
        //4-车型不符
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //更换/补办
            return OLD_CANCEL_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            //挂起/解挂
            return OLD_HANG_UP_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //9- 核销
            return OLD_CANCEL_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)) {
            //挂起/解挂
            return OLD_SIGN_MISSING.getValue();
        }
        return OLD_SIGN_MISSING.getValue();
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
    public static int getNewBlacklistTypeByBusinessType(int type) {
        //1-标签挂失
        //2-无签挂起
        //3-无签注销
        //4-车型不符
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //更换/补办
            return NEW_CANCEL.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)) {
            //挂起/解挂
            return NEW_HANG_UP.getValue();
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //9- 核销
            return NEW_CANCEL.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)) {
            //挂起/解挂
            return NEW_HANG_UP.getValue();
        }
        return NEW_CANCEL.getValue();
    }
}

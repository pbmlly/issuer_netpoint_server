package com.csnt.ins.enumobj;

/**
 * 1- 正常
 * 6- 标签挂失
 * 101 核销
 * 102 挂起
 * 103 未关联扣款渠道
 * 104 账户透支
 * 105 支付机构限制
 *
 * @ClassName OrderStatusEnum
 * @Description OBU线下渠道状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ObuOflStatusEnum {
    /**
     * 1-  正常
     */
    NORMAL(1),
    /**
     * 6- 标签挂失
     */
    LOST_OF_OBU(6),
    /**
     * 101 核销
     */
    CANCEL(101),
    /**
     * 102 挂起
     */
    HANG_UP(102),
    /**
     * 103 未关联扣款渠道
     */
    WITHOUT_CHANNEL(103),
    /**
     * 104 账户透支
     */
    OVERDRAFT(104),
    /**
     * 105 支付机构限制
     */
    RESTRICT_USE(105);

    private ObuOflStatusEnum(int value) {
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
     * 1- 正常
     * 6- 标签挂失
     * 101 核销
     * 102 挂起
     * 103 未关联扣款渠道
     * 104 账户透支
     * 105 支付机构限制
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
        //2- 换签
        //4- 换卡签全套
        //5- 签补办
        //9- 核销
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //更换/核销
            return CANCEL.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)) {
            //7- 挂起
            return HANG_UP.getValue();
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            //8- 解挂起
            return NORMAL.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)) {
            //挂失
            return LOST_OF_OBU.getValue();
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            //解挂失
            return NORMAL.getValue();
        }
        return NORMAL.getValue();
    }
}

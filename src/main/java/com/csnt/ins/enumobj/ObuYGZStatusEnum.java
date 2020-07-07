package com.csnt.ins.enumobj;

/**
 * 新版本：
 * 1- 正常
 * 101 核销
 * 102 挂起
 * 103 未关联扣款渠道
 * 104 账户透支
 * 105 支付机构限制
 * <p>
 * 老版本：
 * 1-	正常
 * 2-	有签挂起
 * 3-	无签挂起
 * 4-	有签注销
 * 5-	无签注销
 * 6-	标签挂失
 * 7-	已过户
 * 8-	维修中
 *
 * @ClassName OrderStatusEnum
 * @Description OBU营改增状态
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ObuYGZStatusEnum {
    /**
     * 1-  正常
     */
    NEW_NORMAL(1, "正常"),
    /**
     * 101 核销
     */
    NEW_CANCEL(101, "核销"),
    /**
     * 102 挂起
     */
    NEW_HANG_UP(102, "挂起"),
    /**
     * 103 未关联扣款渠道
     */
    NEW_WITHOUT_CHANNEL(103, "未关联扣款渠道"),
    /**
     * 104 账户透支
     */
    NEW_OVERDRAFT(104, "账户透支"),
    /**
     * 105 支付机构限制
     */
    NEW_RESTRICT_USE(105, "支付机构限制"),
    /**
     * 1-	正常
     */
    OLD_NORMAL(1, "正常"),
    /**
     * 2-  有签挂起
     */
    OLD_HANG_UP_WITH_SIGN(2, "有签挂起"),
    /**
     * 3-  无签挂起
     */
    OLD_HANG_UP_WITHOUT_SIGN(3, "无签挂起"),
    /**
     * 4-  有签注销
     */
    OLD_CANCEL_WITH_SIGN(4, "有签注销"),
    /**
     * 5-  无签注销
     */
    OLD_CANCEL_WITHOUT_SIGN(5, "无签注销"),
    /**
     * 6-  标签挂失
     */
    OLD_LOSS_OF_SIGN(6, "标签挂失"),
    /**
     * 7-  已过户
     */
    OLD_TRANSFERRED(7, "已过户"),
    /**
     * 8-	维修中
     */
    OLD_REPAIRING(8, "维修中");

    private ObuYGZStatusEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    private int value;
    private String name;

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public boolean equals(int value) {
        return this.value == value;
    }

    /**
     * 1- 正常
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
    public static int getNewStatusByBusinessType(int type) {
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
            return NEW_CANCEL.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)) {
            //7- 挂起
            return NEW_HANG_UP.getValue();
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            //8- 解挂起
            return NEW_NORMAL.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)) {
            //挂失
            return NEW_CANCEL.getValue();
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            //解挂失
            return NEW_NORMAL.getValue();
        }
        return NEW_NORMAL.getValue();
    }

    /**
     * 1-	正常
     * 2-	有签挂起
     * 3-	无签挂起
     * 4-	有签注销
     * 5-	无签注销
     * 6-	标签挂失
     * 7-	已过户
     * 8-	维修中
     *
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
    public static int getOldStatusByBusinessType(int type) {
        //2- 换签
        //4- 换卡签全套
        //5- 签补办
        //9- 核销
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)) {
            //更换
            return OLD_CANCEL_WITH_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //补办
            return OLD_CANCEL_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //注销
            return OLD_CANCEL_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)) {
            //7- 挂起
            return OLD_HANG_UP_WITHOUT_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            //8- 解挂起
            return OLD_NORMAL.getValue();
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)) {
            //挂失
            return OLD_LOSS_OF_SIGN.getValue();
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            return OLD_NORMAL.getValue();
        }
        return OLD_NORMAL.getValue();
    }

    /**
     * 获取状态名称
     *
     * @param status
     * @param isOld
     * @return
     */
    public static String getName(int status, boolean isOld) {
        for (ObuYGZStatusEnum obuYGZStatusEnum : ObuYGZStatusEnum.values()) {
            if (isOld && obuYGZStatusEnum.name().contains("NEW")) {
                continue;
            }
            if (!isOld && obuYGZStatusEnum.name().contains("OLD")) {
                continue;
            }
            if (obuYGZStatusEnum.equals(status)) {
                return obuYGZStatusEnum.getName();
            }
        }
        return null;
    }
}

package com.csnt.ins.enumobj;

/**
 * 0-未审核
 * 1-审核通过
 * 2-审核拒绝
 * 3-obu已激活
 *
 * @ClassName OrderStatusEnum
 * @Description obu二次激活申请单结果
 * @Author duwanjiang
 * @Date 2019/7/8 17:12
 * Version 1.0
 **/
public enum ObuActivateApplyResultEnum {
    /**
     * 0-未审核
     */
    UNCHECK(0, "未处理"),
    /**
     * 1-审核通过
     */
    PASSED(1, "审核通过"),
    /**
     * 2-审核拒绝
     */
    REFUSED(2, "审核拒绝"),
    /**
     * 3-obu已激活
     */
    ACTIVATED(3, "obu已激活");

    private ObuActivateApplyResultEnum(int value, String name) {
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

    public boolean equals(int value){
        return this.value == value;
    }

    public boolean equals(Integer value){
        return this.value == value;
    }

    /**
     * 获取业务类型的名称
     *
     * @param value
     * @return
     */
    public static String getName(int value) {
        for (ObuActivateApplyResultEnum obuActivateApplyResultEnum : ObuActivateApplyResultEnum.values()) {
            if (obuActivateApplyResultEnum.equals(value)) {
                return obuActivateApplyResultEnum.getName();
            }
        }
        return null;
    }
}

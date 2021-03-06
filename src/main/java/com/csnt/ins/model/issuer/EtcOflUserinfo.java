package com.csnt.ins.model.issuer;

import com.csnt.ins.model.issuer.base.BaseEtcOflUserinfo;
import com.csnt.ins.utils.DbUtil;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class EtcOflUserinfo extends BaseEtcOflUserinfo<EtcOflUserinfo> {
    public static final EtcOflUserinfo dao = new EtcOflUserinfo().dao();

    /**
     * 根据用户证件号和证件类型查询线下渠道用户
     *
     * @param userIdNum
     * @param userIdType
     * @return
     */
    public static EtcOflUserinfo findFirstByUserIdNumAndType(String userIdNum, Integer userIdType) {
        return dao.findFirst(DbUtil.getSql("queryEtcOflUserByUserIdNumAndType"), userIdNum, userIdType);
    }
    /**
     * 根据客户编号查询线下渠道用户
     *
     * @param userId
     * @return
     */
    public static EtcOflUserinfo findOflUserFirstByUserId(String userId) {
        return dao.findFirst(DbUtil.getSql("queryEtcOflUserByUserId"), userId);
    }
    /**
     * 根据车辆编码查询线下渠道的用户信息
     *
     * @param vehicleId
     * @return
     */
    public static EtcOflUserinfo findFirstEtcOflUserInfoByVehicleId(String vehicleId) {
        return dao.findFirst(DbUtil.getSql("queryEtcOflUserInfoByVehicleId"), vehicleId);
    }
}

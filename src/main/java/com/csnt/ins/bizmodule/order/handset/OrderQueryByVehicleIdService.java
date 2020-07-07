package com.csnt.ins.bizmodule.order.handset;


import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 8906车牌查询订单接口
 *
 * @author source
 */
public class OrderQueryByVehicleIdService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(OrderQueryByVehicleIdService.class);

    private String serviceName = "[8906车牌查询订单接口]";

    @Override
    public Result entry(Map dataMap) {
        List<Record> datas;
        try {
            Record vehicleInfo = new Record().setColumns(dataMap);
            String plateNum = vehicleInfo.get("plateNum");
            String plateColor = vehicleInfo.get("plateColor");
            String posId = vehicleInfo.get("posId");
            String userId = vehicleInfo.get("userId");
            Integer userIdType = vehicleInfo.getInt("userIdType");
            String userIdNum = vehicleInfo.get("userIdNum");
            String type = vehicleInfo.getStr("type");
            if (StringUtil.isEmpty(plateNum, plateColor, posId, userId, userIdType, userIdNum, type)) {
                logger.error("{}传入参数plateNum,plateColor,posId,userId,userIdType,userIdNum,type不能为空", serviceName);
                return Result.paramNotNullError("plateNum,plateColor,posId,userId,userIdType,userIdNum,type");
            }

            logger.info("{}接收参数:{}", serviceName, vehicleInfo);

            if (SysConfig.getEncryptionFlag()) {
                userIdNum = MyAESUtil.Encrypt(userIdNum);
            }

            datas = Db.find(DbUtil.getSql("queryHandSetOrderInfo"), posId, userId, plateNum, plateColor, userIdType, userIdNum, type);
            if (datas.size() == 0) {
                return Result.sysError("未检查到可操作订单！");
            }
            logger.info("{}[{}]查询到的订单数据有{}条", serviceName, vehicleInfo, datas.size());
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        Record order = datas.get(0);
        if (SysConfig.getEncryptionFlag()) {
            try {
                order.set("accountName", MyAESUtil.Decrypt( order.getStr("accountName")));
                order.set("mobile", MyAESUtil.Decrypt( order.getStr("mobile")));
                order.set("userIdNum", MyAESUtil.Decrypt( order.getStr("userIdNum")));
                order.set("postName", MyAESUtil.Decrypt( order.getStr("postName")));
                order.set("postPhone", MyAESUtil.Decrypt( order.getStr("postPhone")));
                order.set("postAddr", MyAESUtil.Decrypt( order.getStr("postAddr")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success(order);
    }


}

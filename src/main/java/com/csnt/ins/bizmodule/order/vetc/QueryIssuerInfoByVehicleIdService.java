package com.csnt.ins.bizmodule.order.vetc;


import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 8935根据车牌编号查询发行信息接口
 *
 * @author source
 */
public class QueryIssuerInfoByVehicleIdService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(QueryIssuerInfoByVehicleIdService.class);

    private String serviceName = "[8935根据车牌编号查询发行信息接口]";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record vehicleInfo = new Record().setColumns(dataMap);
            String vehicleCode = vehicleInfo.get("vehicleCode");
            logger.info("{}接收车牌查询申请信息接口:{}", serviceName, vehicleInfo);

            if (StringUtil.isEmpty(vehicleCode)) {
                logger.error("{}参数vehicleCode不能为空", serviceName);
                return Result.paramNotNullError("vehicleCode");
            }

            List<Record> datas;
            try {

                datas = Db.find(DbUtil.getSql("queryIssuerInfoByVehicleCode"), vehicleCode,vehicleCode,vehicleCode);
                if (datas.size() == 0) {
                    logger.error("{}未检查到该车牌信息信息", serviceName);
                    return Result.sysError("未检查到该车牌信息！");
                }
             } catch (Exception e) {
                logger.error("{}[vehicleCode={}]数据查询异常:{}", serviceName, vehicleCode, e.toString(), e);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "查询数据异常");
            }

            Record rsMap = (Record) datas.get(0);
            logger.info("{}查询车辆发行信息成功:{}", serviceName, rsMap);

            if (SysConfig.getEncryptionFlag()) {
                try {
                    rsMap.set("userName", MyAESUtil.Decrypt( rsMap.getStr("userName")));
                    rsMap.set("mobile", MyAESUtil.Decrypt( rsMap.getStr("mobile")));
                    rsMap.set("userIdNum", MyAESUtil.Decrypt( rsMap.getStr("userIdNum")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 预注销查询
            // 2、查询该车辆是否已经进入待注销状态
            Record checkRc = Db.findFirst(DbUtil.getSql("queryCardCancelByVeh"),  rsMap.getStr("id"));
            if (checkRc != null) {
                rsMap.set("cardStatus",12);
                rsMap.set("cardStatusDesc","预注销");

            }


            return Result.success(rsMap);
        } catch (ClassCastException e) {
            logger.error("{}传入参数类型异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "传入参数类型异常");
        } catch (Exception e) {
            logger.error("{}查询车辆发行信息异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询车辆发行信息异常");
        }
    }
}

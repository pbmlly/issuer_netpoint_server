package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1003车辆信息查询
 *
 * @author source
 **/
public class QueryVehicleService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryVehicleService.class);

    private final String serverName = "[1003车辆信息查询]";

    /**
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            String vehicleId = record.getStr("vehicleId");
//            Record vehicleRecord = findYgVehcileInfo(vehicleId);
//            if (vehicleRecord == null) {
            Record vehicleRecord = findIssueVehcileInfo(vehicleId);
                if (vehicleRecord == null) {
                    vehicleRecord = findHJPTVehcileInfo(vehicleId);
                    if (vehicleRecord == null) {
                        return Result.success(null, "未查询到车辆信息");
                    }
                }
//            }

            //增加isNew的判断
            vehicleRecord = addIsNew(vehicleRecord);
            return Result.success(getVehicleResultRecord(vehicleRecord));
        } catch (Throwable t) {
            logger.error("{}车牌信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    private Record getVehicleResultRecord(Record record) {
        Record vehicleResultRecord = new Record();
        vehicleResultRecord.set("id", record.get("id"));
        vehicleResultRecord.set("type", record.get("type"));
        vehicleResultRecord.set("userId", record.get("userId"));
        vehicleResultRecord.set("ownerName", record.get("ownerName"));
        vehicleResultRecord.set("ownerIdType", record.get("ownerIdType"));
        vehicleResultRecord.set("ownerIdNum", record.get("ownerIdNum"));
        vehicleResultRecord.set("ownerTel", record.get("ownerTel"));
        vehicleResultRecord.set("address", record.get("address"));
        vehicleResultRecord.set("contact", record.get("contact"));
        vehicleResultRecord.set("registeredType", record.get("registeredType"));
        vehicleResultRecord.set("channelId", record.get("channelId"));
        vehicleResultRecord.set("registeredTime", record.get("registeredTime"));
        vehicleResultRecord.set("vehicleType", record.get("vehicleType"));
        vehicleResultRecord.set("vehicleModel", record.get("vehicleModel"));
        vehicleResultRecord.set("useCharacter", record.get("useCharacter"));
        vehicleResultRecord.set("VIN", record.get("VIN"));
        vehicleResultRecord.set("engineNum", record.get("engineNum"));
        vehicleResultRecord.set("registerDate", record.get("registerDate"));
        vehicleResultRecord.set("issueDate", record.get("issueDate"));
        vehicleResultRecord.set("fileNum", record.get("fileNum"));
        vehicleResultRecord.set("approvedCount", record.get("approvedCount"));
        vehicleResultRecord.set("totalMass", record.get("totalMass"));
        vehicleResultRecord.set("maintenanceMass", record.get("maintenanceMass"));
        vehicleResultRecord.set("permittedWeight", record.get("permittedWeight"));
        vehicleResultRecord.set("outsideDimensions", record.get("outsideDimensions"));
        vehicleResultRecord.set("permittedTowWeight", record.get("permittedTowWeight"));
        vehicleResultRecord.set("testRecord", record.get("testRecord"));
        vehicleResultRecord.set("wheelCount", record.get("wheelCount"));
        vehicleResultRecord.set("axleCount", record.get("axleCount"));
        vehicleResultRecord.set("axleDistance", record.get("axleDistance"));
        vehicleResultRecord.set("axisType", record.get("axisType"));
        vehicleResultRecord.set("vehicleFeatureVersion", record.get("vehicleFeatureVersion"));
        vehicleResultRecord.set("vehicleFeatureCode", record.get("vehicleFeatureCode"));
        vehicleResultRecord.set("payAccountNum", record.get("payAccountNum"));
        vehicleResultRecord.set("operation", record.get("operation"));
        vehicleResultRecord.set("isNew", record.get("isNew"));

        if (SysConfig.getEncryptionFlag()) {
            try {
                vehicleResultRecord.set("ownerName", MyAESUtil.Decrypt( record.getStr("ownerName")));
                vehicleResultRecord.set("ownerIdNum",MyAESUtil.Decrypt( record.getStr("ownerIdNum")));
                vehicleResultRecord.set("ownerTel", MyAESUtil.Decrypt( record.getStr("ownerTel")));
                vehicleResultRecord.set("address", MyAESUtil.Decrypt( record.getStr("address")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return vehicleResultRecord;
    }

    /**
     * 查询易构车辆信息表
     *
     * @param vehicleId
     * @return
     */
    private Record findYgVehcileInfo(String vehicleId) {
        Kv kv = new Kv().set("id", vehicleId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findYGVehicleInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    /**
     * 查询汇聚平台车辆信息表
     *
     * @param vehicleId
     * @return
     */
    private Record findHJPTVehcileInfo(String vehicleId) {
        Kv kv = new Kv().set("id", vehicleId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findHJPTVehicleInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }

    /**
     * 查询发行车辆信息表
     *
     * @param vehicleId
     * @return
     */
    private Record findIssueVehcileInfo(String vehicleId) {
        Kv kv = new Kv().set("id", vehicleId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findIssueVehicleInfoById", kv);
        return Db.use().findFirst(sqlPara);
    }

    /**
     * 根据车辆id查询当前车辆是否含有正常、挂失状态的卡、签(operation < 3)，
     * <p>
     * 1. 如果存在，则isNew=0
     * 2. 如果不存在，判断当前车辆在etc_ofl_vehicleInfo(accountId is not null)中是否存在
     * 1. 如果存在，则isNew=0
     * 2. 如果不存在，则isNew=1
     *
     * @param vehicleRecord
     * @return
     */
    private Record addIsNew(Record vehicleRecord) {
        String vehicleId = vehicleRecord.get("id");
        //1、 根据车辆id查询当前车辆是否含有正常、挂失状态的卡、签(operation < 3)
        Record vehicleRec = Db.findFirst(DbUtil.getSql("queryEtcVehicleByVehicleId"), vehicleId);
        if (vehicleRec == null
                || (StringUtil.isEmpty(vehicleRec.getStr("cardId")) && StringUtil.isEmpty(vehicleRec.getStr("obuId")))) {
            logger.info("{}当前车辆[vehicleId={}]不含有正常、挂失状态的卡、签", serverName, vehicleId);
            // 判断当前车辆在etc_ofl_vehicleInfo(accountId is not null)中是否存在
            vehicleRec = Db.findFirst(DbUtil.getSql("queryEtcOflVehicleCountById"), vehicleId);
            if (vehicleRec == null || vehicleRec.getInt("num") == 0) {
                vehicleRecord.set("isNew", 1);
            } else {
                vehicleRecord.set("isNew", 0);
            }
        } else {
            vehicleRecord.set("isNew", 0);
        }
        return vehicleRecord;
    }
}

package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1005OBU信息查询
 * @author source
 **/
public class QueryObuService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryObuService.class);

    private final String serverName = "[1005OBU信息查询]";

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
            String obuId = record.getStr("obuId");
            Record obuRecord = findYgObuInfo(obuId);
            if (obuRecord == null) {
                obuRecord = Db.findFirst(DbUtil.getSql("findCenterObuInfoById"), obuId);
                if (obuRecord == null) {
                    obuRecord = findHJPTObuInfo(obuId);
                    if (obuRecord == null) {
                        return Result.success(null, "未查询到OBU信息");
                    }
                }

            }
            return Result.success(getObuResultRecord(obuRecord));
        } catch (Throwable t) {
            logger.error("{}OBU信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    private Record getObuResultRecord(Record record) {
        Record obuResultRecord = new Record();
        obuResultRecord.set("id", record.get("id"));
        obuResultRecord.set("brand", record.get("brand"));
        obuResultRecord.set("model", record.get("model"));
        obuResultRecord.set("obuSign", record.get("obuSign"));
        obuResultRecord.set("userId", record.get("userId"));
        obuResultRecord.set("vehicleId", record.get("vehicleId"));
        obuResultRecord.set("enableTime", record.get("enableTime"));
        obuResultRecord.set("expireTime", record.get("expireTime"));
        obuResultRecord.set("registeredType", record.get("registeredType"));
        obuResultRecord.set("registeredChannelId", record.get("registeredChannelId"));
        obuResultRecord.set("registeredTime", record.get("registeredTime"));
        obuResultRecord.set("installType", record.get("installType"));
        obuResultRecord.set("installChannelId", record.get("installChannelId"));
        obuResultRecord.set("installTime", record.get("installTime"));
        obuResultRecord.set("status", record.get("status"));
        obuResultRecord.set("statusChangeTime", record.get("statusChangeTime"));
        obuResultRecord.set("operation", record.get("operation"));
        obuResultRecord.set("isActive", record.get("isActive"));
        obuResultRecord.set("activeTime", record.get("activeTime"));
        obuResultRecord.set("activeType", record.get("activeType"));
        obuResultRecord.set("activeChannel", record.get("activeChannel"));
        obuResultRecord.set("userIdNum", record.get("userIdNum"));
        if (SysConfig.getEncryptionFlag()) {
            //解密
            try {
                obuResultRecord.set("userIdNum", MyAESUtil.Decrypt( record.getStr("userIdNum")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        obuResultRecord.set("userIdType", record.get("userIdType"));
        return obuResultRecord;
    }

    /**
     * 查询易构车辆信息表
     *
     * @param obuId
     * @return
     */
    private Record findYgObuInfo(String obuId) {
        Kv kv = new Kv().set("id", obuId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findYGObuInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    /**
     * 查询易构车辆信息表
     *
     * @param obuId
     * @return
     */
    private Record findHJPTObuInfo(String obuId) {
        Kv kv = new Kv().set("id", obuId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findHJPTObuInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
}

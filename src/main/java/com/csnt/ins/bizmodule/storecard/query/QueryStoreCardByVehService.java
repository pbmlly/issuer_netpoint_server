package com.csnt.ins.bizmodule.storecard.query;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.CardYGZStatusEnum;
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
 * 1004卡信息查询
 *
 * @author source
 **/
public class QueryStoreCardByVehService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryStoreCardByVehService.class);

    private final String serverName = "[8708储蓄卡信息查询]";

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
            String cardId = record.getStr("cardId");
            String veh = record.getStr("vehplate");
            String opId = record.get("opId") == null ? null : record.get("opId").toString();

            if (StringUtil.isEmpty(cardId) && StringUtil.isEmpty(veh)) {
                Result.bizError(766,"车辆编号、卡必须输其一");
            }

            String cd = "1 =1 ";

            if (StringUtil.isNotEmpty(cardId)) {
                cd = cd + " and c.id = '" + cardId + "' \n";
            }
            if (StringUtil.isNotEmpty(veh)) {
                cd = cd + " and ( left(c.vehicleId,POSITION('_' IN c.vehicleId) -1) = '" + veh + "'" +
                        "  or c.vehicleId = '" + veh+ "'  ) \n";
            }
            //sql构造
            Kv kv = new Kv();
            kv.set("condition", cd);
            //查询数据库数据
            SqlPara sqlPara = DbUtil.getSqlPara("queryCenterStoreCardInfoByVeh", kv);

            Record cardRecord = Db.findFirst(sqlPara);
            if (cardRecord == null) {
                return Result.success(null, "未查询到卡信息");
            }
            if (cardRecord != null && (cardRecord.getInt("status") ==4 || cardRecord.getInt("status") ==5) ) {
                return Result.success(null, "该储蓄卡已经注销");
            }


            if (opId != null) {
                Record cdRc = Db.findFirst(DbUtil.getSql("queryUserIdBankPost"), opId);
                if (cdRc != null && cdRc.get("bankpost") != null) {

                    if (!cdRc.get("bankpost").toString().equals(
                            cardRecord.getStr("bankPost").length()>11?cardRecord.getStr("bankPost").substring(0,11):cardRecord.getStr("bankPost") )) {
                        return Result.bizError(889, "卡所属银行编号非该员工所属银行编号。");
                    }
                }
            }


            return Result.success(getCardResultRecord(cardRecord));
        } catch (Throwable t) {
            logger.error("{}卡信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    private Record getCardResultRecord(Record record) {
        Record cardResultRecord = new Record();
        cardResultRecord.set("id", record.get("id"));
        cardResultRecord.set("cardType", record.get("cardType"));
        cardResultRecord.set("brand", record.get("brand"));
        cardResultRecord.set("model", record.get("model"));
        cardResultRecord.set("agencyId", record.get("agencyId"));
        cardResultRecord.set("userId", record.get("userId"));
        cardResultRecord.set("vehicleId", record.get("vehicleId"));
        cardResultRecord.set("enableTime", record.get("enableTime"));
        cardResultRecord.set("expireTime", record.get("expireTime"));
        cardResultRecord.set("issuedType", record.get("issuedType"));
        cardResultRecord.set("channelId", record.get("channelId"));
        cardResultRecord.set("issuedTime", record.get("issuedTime"));
        cardResultRecord.set("status", record.get("status"));
        cardResultRecord.set("statusChangeTime", record.get("statusChangeTime"));
        cardResultRecord.set("operation", record.get("operation"));
        cardResultRecord.set("userIdNum", record.get("userIdNum"));
        if ( SysConfig.getEncryptionFlag()) {
            try {
                cardResultRecord.set("userIdNum", MyAESUtil.Decrypt( record.get("userIdNum")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cardResultRecord.set("userIdType", record.get("userIdType"));
        return cardResultRecord;
    }
}

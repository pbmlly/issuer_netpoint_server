package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.CardYGZStatusEnum;
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
 * 1004卡信息查询
 *
 * @author source
 **/
public class QueryCardService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryCardService.class);

    private final String serverName = "[1004卡信息查询]";

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
//            Record cardRecord = findYgCardInfo(cardId);
//            if (cardRecord == null) {
                Record cardRecord = Db.findFirst(DbUtil.getSql("queryCenterCardInfoById"), cardId);
                if (cardRecord == null) {
//                    cardRecord = findHJPTCardInfo(cardId);
//                    if (cardRecord == null) {
                        return Result.success(null, "未查询到卡信息");
//                    }
                }
//            }
            // 判断银行编号
            // 登录员工编号
            String opId = record.get("opId") == null ? null : record.get("opId").toString();
            if (opId != null) {
                Record cdRc = Db.findFirst(DbUtil.getSql("queryUserIdBankPost"), opId);
                if (cdRc != null && cdRc.get("bankpost") != null) {

                    if (!cdRc.get("bankpost").toString().equals(
                            cardRecord.getStr("bankPost").length()> 11?cardRecord.getStr("bankPost").substring(0,11):cardRecord.getStr("bankPost"))) {
                        return Result.bizError(889, "卡所属银行编号非该员工所属银行编号。");
                    }
                 }
            }

            //判断当前卡是否已经进入待注销
            Record cardCancelConfirmCount = Db.findFirst(DbUtil.getSql("queryCountCardCancelConfirmById"), cardId);
            if (cardCancelConfirmCount != null
                    && cardCancelConfirmCount.getInt("num") > 0) {
                cardRecord.set("status", CardYGZStatusEnum.PRE_LOG_OFF_STATUS.getValue());
            }

            return Result.success(getCardResultRecord(cardRecord));
        } catch (Throwable t) {
            logger.error("{}OBU信息查询失败:{}", serverName, t.getMessage(), t);
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
        if (SysConfig.getEncryptionFlag()) {
            //解密
            try {
                cardResultRecord.set("userIdNum", MyAESUtil.Decrypt( record.getStr("userIdNum")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        cardResultRecord.set("userIdType", record.get("userIdType"));
        return cardResultRecord;
    }

    /**
     * 查询易构车辆信息表
     *
     * @param cardId
     * @return
     */
    private Record findYgCardInfo(String cardId) {
        Kv kv = new Kv().set("id", cardId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findYGCardInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    /**
     * 查询易构车辆信息表
     *
     * @param cardId
     * @return
     */
    private Record findHJPTCardInfo(String cardId) {
        Kv kv = new Kv().set("id", cardId);
        SqlPara sqlPara = Db.getSqlPara("mysql.findHJPTCardInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
}

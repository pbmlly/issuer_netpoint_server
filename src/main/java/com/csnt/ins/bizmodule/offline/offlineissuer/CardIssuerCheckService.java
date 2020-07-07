package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 8805卡发行校验
 * @author source
 **/
public class CardIssuerCheckService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(CardIssuerCheckService.class);

    private final String serverName = "[8805卡发行校验]";

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
            String veh = record.getStr("plateNum") + "_" + record.getStr("plateColor");
            // 判断卡是否存在
//            Record cardRecord = findYgCardInfo(cardId);
//            if (cardRecord == null) {
            Record  cardRecord = Db.findFirst(DbUtil.getSql("CheckCenterCardInfoById"), cardId);
//                if (cardRecord == null) {
//                    cardRecord = findHJPTCardInfo(cardId);
//                }
//            }
            if (cardRecord != null) {
                return  new Result(704, "该卡已经发行");
            }
            // 判断车辆信息是否绑卡判断车辆信息是否绑卡,现在迁移的储蓄卡注销，就会报错
            Record vehRecord = findYgCardByVeh(veh);
            if (vehRecord == null) {
                 vehRecord = Db.findFirst(DbUtil.getSql("CheckCenterCardInfoByVeh"), veh);
                if (vehRecord == null) {
                    //由于易构注销车辆方式，会导致汇聚平台中的卡签异常，无法为该车发行新卡签
//                    vehRecord = findHJPTCardByVeh(veh);
                }
            }
            if (vehRecord != null) {
                return  new Result(704, "该车已经发卡");
            }

            return Result.success(null,"可发行");
        } catch (Throwable t) {
            logger.error("{}card信息检查失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 查询易构card信息表
     *
     * @param card
     * @return
     */
    private Record findYgCardInfo(String card) {
        Kv kv = new Kv().set("id", card);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGCardInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    private Record findYgCardByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGCardInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }
    /**
     * 查询汇聚平台卡信息表
     *
     * @param card
     * @return
     */
    private Record findHJPTCardInfo(String card) {
        Kv kv = new Kv().set("id", card);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTCardInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
    private Record findHJPTCardByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTCardInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
}

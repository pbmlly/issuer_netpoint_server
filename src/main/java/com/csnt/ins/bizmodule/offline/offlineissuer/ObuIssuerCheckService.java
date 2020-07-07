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
 * 8807OBU发行校验
 * @author source
 **/
public class ObuIssuerCheckService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(ObuIssuerCheckService.class);

    private final String serverName = "[8807OBU发行校验]";

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
            String veh = record.getStr("plateNum") + "_" + record.getStr("plateColor");

            // 判断OBU是否存在
//            Record obuRecord = findYgObuInfo(obuId);
//            if (obuRecord == null) {
            Record obuRecord = Db.findFirst(DbUtil.getSql("CheckCenterObuInfoById"), obuId);
//                if (obuRecord == null) {
//                    obuRecord = findHJPTObuInfo(obuId);
//                }
//            }
            if (obuRecord != null) {
                return  new Result(704, "该OBU已经发行");
            }
            // 判断车辆信息是否绑卡,现在迁移的储蓄卡注销，就会报错
//            Record vehRecord = findYgObuByVeh(veh);
//            if (vehRecord == null) {
                Record  vehRecord = Db.findFirst(DbUtil.getSql("CheckCenterObuInfoByVeh"), veh);
                if (vehRecord == null) {
                    //由于易构注销车辆方式，会导致汇聚平台中的卡签异常，无法为该车发行新卡签
//                    vehRecord = findHJPTObuByVeh(veh);
                }
//            }
            if (vehRecord != null) {
                return  new Result(704, "该车已经发obu");
            }

            return Result.success(null,"可发行");
        } catch (Throwable t) {
            logger.error("{}OBU信息检查失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 查询易构obu信息表
     *
     * @param obuId
     * @return
     */
    private Record findYgObuInfo(String obuId) {
        Kv kv = new Kv().set("id", obuId);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGObuInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    private Record findYgObuByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGObuInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }
    /**
     * 查询汇聚平台obu信息表
     *
     * @param obuId
     * @return
     */
    private Record findHJPTObuInfo(String obuId) {
        Kv kv = new Kv().set("id", obuId);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTObuInfoById", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
    private Record findHJPTObuByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTObuInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }
}

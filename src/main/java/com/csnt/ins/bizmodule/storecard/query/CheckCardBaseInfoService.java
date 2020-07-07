package com.csnt.ins.bizmodule.storecard.query;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 8713储蓄卡基础信息检查
 *
 * @author source
 **/
public class CheckCardBaseInfoService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(CheckCardBaseInfoService.class);

    private final String serverName = "[8713储蓄卡基础信息检查]";
    private final String CHECK_NOMAIL = "1";
    private final String CHECK_NONNOMAIL = "2";

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

            if (StringUtil.isEmpty(cardId) ) {
                Result.bizError(766,"卡信息必须传入");
            }

            //读取卡信息
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}未找到卡记录,cardid:{}", serverName, cardId);
                return Result.bizError(751, "未找到该卡信息。");
            }

            Record userInfo = Db.findFirst(DbUtil.getSql("queryCenterEtcUserById"), cardInfo.getStr("userId"));
            if (userInfo == null) {
                return Result.success(CHECK_NONNOMAIL);
            }
            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), cardInfo.getStr("vehicleId"));
            if (vehicleInfo == null) {
                return Result.success(CHECK_NONNOMAIL);
            }

            return Result.success(CHECK_NOMAIL);
        } catch (Throwable t) {
            logger.error("{}卡信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }
}

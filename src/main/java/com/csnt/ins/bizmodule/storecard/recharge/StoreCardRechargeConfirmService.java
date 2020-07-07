package com.csnt.ins.bizmodule.storecard.recharge;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.enumobj.ReChargeStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName StoreCardRechargeRequestService
 * @Description 8702储值卡充值确认接口
 * @Author duwanjiang
 * @Date 2019/9/6 10:26
 * Version 1.0
 **/
public class StoreCardRechargeConfirmService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(StoreCardRechargeConfirmService.class);

    private final String serviceName = "[8702储值卡充值确认接口]";

    private final String TABLE_ETCTS_STORECARD_RECHARGE_LIST = "etcts_storecard_recharge_list";

    private final String TRANSACTION_RECHARGEUPLOAD_REQ = "TRANSACTION_RECHARGEUPLOAD_REQ_";

    /**
     * 1、判断流水号是否存
     * 2、判断当前流水的状态是否为待确认
     * 3、上传部中心
     * 4、更新流水状态为确认完成
     * 5、响应给客户端
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        String msg = null;
        try {
            Record record = new Record().setColumns(dataMap);
            String paramStr = "id,opTime,orgId,operatorId,channelType";
            if (StringUtil.isEmptyArg(record, paramStr)) {
                logger.error("{}请求的{}不能为空", serviceName, paramStr);
                return Result.paramNotNullError(paramStr);
            }

            //1、判断流水号是否存在
            String id = record.getStr("id");
            Record reChargeRecord = Db.findFirst(DbUtil.getSql("queryReChargeListById"), id);
            if (reChargeRecord == null) {
                logger.error("{}充值确认流水时,未找到对应的流水[id={}]", serviceName, id);
                return Result.sysError("充值确认流水时,未找到对应的流水");
            }

            //2、判断流水的状态是否为待确认
            int reChargeStatus = reChargeRecord.getInt("status");
            if (ReChargeStatusEnum.OFFSET.equals(reChargeStatus)) {
                logger.error("{}当前流水为[{}]状态,非[待确认]状态,不能进行确认[id={}]",
                        serviceName, ReChargeStatusEnum.getName(reChargeStatus), id);
                return Result.sysError(String.format("当前流水为[%s]状态,非[待确认]状态,不能进行确认", ReChargeStatusEnum.getName(reChargeStatus)));
            } else if (ReChargeStatusEnum.CONFIRMED.equals(reChargeStatus)) {
                logger.info("{}当前流水[已确认],无需重复确认:[id={}]", serviceName, id);
                return Result.success(null, "当前流水[已确认],无需重复确认");
            }

            Record offsetRecord = new Record();
            //3、上传部中心
            Map sendMsg = convertSendMsg(reChargeRecord);
            BaseUploadResponse response = uploadYGZ(sendMsg, TRANSACTION_RECHARGEUPLOAD_REQ);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains("重复")) {
                // 部中心业务错误保存
                 if (response.getStateCode() >= 700 &&
                         response.getStateCode() < 1000) {
                     offsetRecord.set("uploadCenterMsg",response.getErrorMsg());
                     offsetRecord.set("uploadCenterCode",response.getStateCode());
                     msg = "上传部中心失败，无法打印票根";

                 } else {
                     logger.error("{}上传部中心充值交易上传失败:{}", serviceName, response.getErrorMsg());
                     return Result.sysError(String.format("上传部中心充值交易失败:%s", response.getErrorMsg()));
                 }

            }

            //4、更新流水的状态为已确认

            offsetRecord.set("id", id);
            //设置为已抵充状态
            offsetRecord.set("status", ReChargeStatusEnum.CONFIRMED.getValue());
            offsetRecord.set("updateTime", new Date());
            if (Db.update(TABLE_ETCTS_STORECARD_RECHARGE_LIST, offsetRecord)) {
                logger.info("{}充值确认成功", serviceName);
            } else {
                logger.error("{}充值确认入库失败", serviceName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "充值确认入库失败");
            }

        } catch (Exception e) {
            logger.error("{}储值卡充值确认异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "储值卡充值确认异常");
        }
        //5、响应给客户端
        return Result.success(msg);
    }

    /**
     * 转换发送数据到部中心
     *
     * @param reChargeRecord
     * @return
     */
    private Map convertSendMsg(Record reChargeRecord) {
        Map map = new HashMap();
        map.put("id", reChargeRecord.getStr("id"));
        map.put("paidAmount", reChargeRecord.get("paidAmount"));
        map.put("giftAmount", reChargeRecord.get("giftAmount"));
        map.put("rechargeAmount", reChargeRecord.get("rechargeAmount"));
        map.put("cardId", reChargeRecord.get("cardId"));
//        map.put("accountId", reChargeRecord.get("cardId"));
//        map.put("channelType", reChargeRecord.get("channelType"));
//        map.put("channelId", reChargeRecord.get("channelId"));
        return map;
    }
}

package com.csnt.ins.bizmodule.storecard.cancel;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName StoreCardCancelService
 * @Description 8710储蓄卡注销
 * @Author cml
 * @Date 2019/9/6 10:26
 * Version 1.0
 **/
public class StoreCardCancelService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(StoreCardCancelService.class);

    private final String serviceName = "[8710储值卡注销接口]";

    private final String TABLE_CARDINFO = "etc_cardinfo";
    private final String TABLE_CARDINFO_HISTORY = "etc_cardinfo_history";
    private final String TABLE_ETC_STORECARD_REFUND_LIST = "etc_storecard_refund_list";
    private final String TABLE_CARDBLACKLIST = "etc_cardblacklist";

    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";
    private final String BASIC_CARDBLACKLISTUPLOAD_REQ = "BASIC_CARDBLACKLISTUPLOAD_REQ_";
    private final String REPEAT_MSG = "注销状态";
    private final String REPEAT_MSG1 = "对应黑名单重复上传";

    private final String CARD_TYPE_DEP = "2";
    private final int CONFIRMSTATUS_INIT = 0;

    /**
     * 1、参数判断
     * 2、判断卡是否为储蓄卡
     * 3、上传部中心，黑名单
     * 4、更新相关记录
     * 5、响应给客户端
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            Record record = new Record().setColumns(dataMap);
            String cardId = record.getStr("cardId");
            int cancelType = record.getInt("cancelType");
            Long readCardBalance = record.getLong("readCardBalance");
            String channelType = record.getStr("channelType");
            String orgId = record.getStr("orgId");
            String operatorId = record.getStr("operatorId");
            if (StringUtil.isEmpty(cardId, cancelType, operatorId,channelType,orgId)) {
                logger.error("{}请求的cardId, cancelType, operatorId,channelType,orgId不能为空", serviceName);
                return Result.paramNotNullError("cardId, cancelType, operatorId,channelType,orgId");
            }
            if (cancelType !=4 && cancelType !=5) {
                logger.error("{}注销类型有误,cardid:{}", serviceName, cardId);
                return Result.bizError(859, "注销类型有误。");
            }
            if (cancelType == CardYGZStatusEnum.CANCEL_WITH_CARD.getValue()
                    && readCardBalance == null) {
                logger.error("{}为有卡注销时，卡内余额不能为空,cardid:{}", serviceName, cardId);
                return Result.bizError(750, "为有卡注销时，卡内余额不能为空。");
            }

            //读取卡信息
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}未找到卡记录,cardid:{}", serviceName, cardId);
                return Result.bizError(751, "未找到该卡信息。");
            }

            int  status = cardInfo.getInt("status");
            if (status == 4 || status == 5) {
                logger.error("{}该卡已经注销,cardid:{}", serviceName, cardId);
                return Result.bizError(758, "该卡已经注销。");
            }

            // 判断卡是否为储蓄卡
            String cardType = cardInfo.getStr("cardType");
            if (!CARD_TYPE_DEP.equals(cardType.substring(0,1))) {
                logger.error("{}该卡不是储蓄卡,cardid:{}", serviceName, cardId);
                return Result.bizError(752, "该卡不是储蓄卡。");
            }

            // 判断该卡是否已经注销


            //设置卡信息的状态
            cardInfo.set("status", cancelType);
            cardInfo.set("operation", 2);
            cardInfo.set("statusChangeTime", new Date());

            //7、营改增平台上传卡信息上传及变更
            BaseUploadResponse response = uploadBasicCardInfo(cardInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains(REPEAT_MSG)) {
                logger.error("{}上传卡营改增信息失败:{}", serviceName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }

            //8、卡黑名单上传
            Record cardBlacklistRecord = new Record();
            response = uploadCardBlacklist(cardId, cardBlacklistRecord);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains(REPEAT_MSG1)     ) {
                logger.error("{}上传黑名单信息失败:{}", serviceName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }

            //添加卡属性
            Date currentDate = new Date();
            cardInfo.set("channelType", channelType);
            cardInfo.set("opTime", currentDate);
            cardInfo.set("updateTime", currentDate);
            cardBlacklistRecord.set("id", StringUtil.getUUID());
            cardBlacklistRecord.set("insertTime", currentDate);
            //上传状态 0-未上传 1-已上传
            cardBlacklistRecord.set("uploadStatus", 1);
            cardBlacklistRecord.set("uploadTime", currentDate);

            //9、记录发行记录
            EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
            etcIssuedRecord.setUuid(StringUtil.getUUID());
            etcIssuedRecord.setVehicleId(cardInfo.getStr("vehicleId"));
            etcIssuedRecord.setUserId(cardInfo.get("userId"));
            etcIssuedRecord.setCardId(cardId);
            etcIssuedRecord.setCardStatus(cancelType);
            etcIssuedRecord.setBusinessType(22);
            etcIssuedRecord.setReason("储蓄卡注销");
            etcIssuedRecord.setOpTime(currentDate);
            etcIssuedRecord.setCreateTime(currentDate);
            etcIssuedRecord.setUpdateTime(currentDate);

            // 增加etc_storecard_refund_list，注销储蓄卡表
            Record scRefundRc = new Record();
            scRefundRc.set("id",StringUtil.getUUID());
            scRefundRc.set("cardId",cardId);
            scRefundRc.set("bankPost",cardInfo.get("bankPost"));
//            scRefundRc.set("channelType",channelType);
            scRefundRc.set("orgId",orgId);
            scRefundRc.set("operatorId",operatorId);
            scRefundRc.set("status",cancelType);
            scRefundRc.set("readCardBalance",readCardBalance !=null?readCardBalance.longValue():0);
            scRefundRc.set("readCardTime",new Date());
            scRefundRc.set("confirmStatus",CONFIRMSTATUS_INIT);
            scRefundRc.set("createTime",new Date());
            scRefundRc.set("updateTime",new Date());


            // 更新绑卡信息
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(cardInfo.getStr("vehicleId"));

            if (etcOflVehicleinfo !=null) {
                etcOflVehicleinfo.setBankPost(null);
                etcOflVehicleinfo.setUpdateTime(new Date());
            }
            //10、存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                if (!Db.update(TABLE_CARDINFO, cardInfo)) {
                    logger.error("{}更新TABLE_CARDINFO表失败", serviceName);
                    return false;
                }
                cardInfo.set("createTime", currentDate);
                if (!Db.save(TABLE_CARDINFO_HISTORY, ids, cardInfo)) {
                    logger.error("{}插入TABLE_CARDINFO_HISTORY表失败", serviceName);
                    return false;
                }

                if (!Db.save(TABLE_CARDBLACKLIST, cardBlacklistRecord)) {
                    logger.error("{}插入TABLE_CARDBLACKLIST表失败", serviceName);
                    return false;
                }
                if (!etcIssuedRecord.save()) {
                    logger.error("{}插入etcIssuedRecord表失败", serviceName);
                    return false;
                }

                if (!Db.save(TABLE_ETC_STORECARD_REFUND_LIST, scRefundRc)) {
                    logger.error("{}插入TABLE_ETC_STORECARD_REFUND_LIST表失败", serviceName);
                    return false;
                }

                if (etcOflVehicleinfo !=null) {
                    etcOflVehicleinfo.update();
                }


                return true;
            });
            if (flag) {
                logger.info("{}注销成功，cardid={}", serviceName,cardId);
                return Result.success(null, "卡状态变更成功");
            } else {
                logger.error("{}卡注销失败,入库失败，cardid={}", serviceName,cardId);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "卡注销失败");
            }

        } catch (Exception e) {
            logger.error("{}储值卡注销异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "储值卡注销异常");
        }
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

        return map;
    }

    /**
     * 上传卡信息到部中心
     *
     * @param cardInfo
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record cardInfo) {
        //installType=1时 installChannelId =0
//        if (1 == MathUtil.asInteger(cardInfo.get("installType"))) {
//            cardInfo.set("installChannelId", 0);
//        }
        cardInfo.set("operation", OperationEnum.UPDATE.getValue());

        EtcCardinfoJson etcCardinfoJson = new EtcCardinfoJson();
        etcCardinfoJson._setOrPut(cardInfo.getColumns());
        // 时间需要转换为字符串
        etcCardinfoJson.setEnableTime(DateUtil.formatDate(cardInfo.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setExpireTime(DateUtil.formatDate(cardInfo.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setIssuedTime(DateUtil.formatDate(cardInfo.get("issuedTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setStatusChangeTime(DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));

        logger.info("{}上传卡的内容为:{}", serviceName, etcCardinfoJson);
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传卡响应信息:{}", serviceName, response);
        return response;
    }

    /**
     * 上传卡黑名单
     *
     * @param cardId
     * @param cardBlacklistRecord
     * @return
     */
    private BaseUploadResponse uploadCardBlacklist(String cardId, Record cardBlacklistRecord) {
        BaseUploadResponse response = new BaseUploadResponse();
        cardBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();

        cardBlacklistRecord.set("cardId", cardId);
        cardBlacklistRecord.set("type", CardBlackListTypeEnum.CANCEL_WITHOUT_CARD.getValue());
        cardBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());
        // 状态
        sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        //进入黑名单时间
        cardBlacklistRecord.set("creationTime", new Date());
        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("cardId", cardId);
        // 类型
        sedMsg.put("type", CardBlackListTypeEnum.CANCEL_WITHOUT_CARD.getValue());

        response = uploadYGZ(sedMsg, BASIC_CARDBLACKLISTUPLOAD_REQ);
        logger.info("{}上传卡黑名单响应信息:{}", serviceName, response);
        return response;
    }

}

package com.csnt.ins.bizmodule.order.obuactivatenotice;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ObuActivateApplyResultEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.enumobj.ServiceTypeEnum;
import com.csnt.ins.model.json.SupCardEnableRequest;
import com.csnt.ins.model.json.SupObuEnableRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.csnt.ins.utils.sdk.SignatureManager;
import com.csnt.ins.utils.sdk.SignatureTools;
import com.jfinal.json.Jackson;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 1101OBU状态通知接口
 *
 * @ClassName CheckObuStatusService
 * @Description TODO
 * @Author tanxing
 * @Date 2019/7/01 14:55
 * Version 1.0
 **/
public class ObuActivateNoticeService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(ObuActivateNoticeService.class);

    private final String serverName = "[1101OBU状态通知接口]";

    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();

    private final String TABLE_ETC_OBUINFO_HISTORY = "etc_obuinfo_history";
    private final String TABLE_ONLINE_ORDERS = "online_orders";
    private final String TABLE_ETC_OBUACTIVATE_APPLY = "etc_obuactivate_apply";

    private final String CHANNEL_CENTER = "041";

    private final String ISSBS_OBUENABLE_REQ = "ISSBS_OBUENABLE_REQ_";
    private final String ISSBS_CARDENABLE_REQ = "ISSBS_CARDENABLE_REQ_";

    private final String ISSBS_OBUENABLE_REPEAT_MSG = "obu已存在";
    private final String ISSBS_CARDENABLE_REPEAT_MSG = "该卡号已存在";

    /**
     * 1、判断此OBU是否在发行系统中存在
     * 2、校验订单是否已撤单
     * 3、检验OBU二次激活申请是否审核通过
     * 4、上传部中心OBU发行数据
     * 5、上传部中心卡发行信息
     * 6、更新数据库
     *
     * @param dataMap json数据
     * @return
     */
    @Override
    public Result entry(Map dataMap) {
        boolean flag = false, obuIssuerFlag = true, cardIssuerFlag = true;
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            logger.info("{}接收到参数:{}", serverName, record);
            String obuId = record.getStr("obuId");

            Integer isActive = record.getInt("isActive");
            Integer activeType = record.getInt("activeType");
            Integer activeChannel = record.getInt("activeChannel");
            String activeTimeStr = record.getStr("activeTime");
            if (StringUtil.isEmpty(obuId, isActive, activeChannel, activeType, activeTimeStr)) {
                logger.error("{}传入参数不能为空", serverName);
                return Result.paramNotNullError("obuId,isActive,activeChannel,activeType,activeTime");
            }

            //1、判断此OBU是否在发行系统中存在
            Date activeTime = DateUtil.parseDate(activeTimeStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
            Record obuInfo = Db.findFirst(DbUtil.getSql("queryProcessedOrderCardOBUInfoList"), obuId);
            if (obuInfo == null) {
                logger.error("{}[obuId={}]此OBU不存在", serverName, obuId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "此OBU不存在");
            }
            String orderId = obuInfo.getStr("orderId");
            if (StringUtil.isEmpty(obuInfo)) {
                logger.error("{}[obuId={}]此OBU不存在", serverName, obuId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "此OBU不存在");
            }

            //2、校验订单是否已撤单
            Integer serviceType = obuInfo.getInt("serviceType");
            if (serviceType != null
                    && (serviceType == ServiceTypeEnum.CANCEL.getValue()
                    || serviceType == ServiceTypeEnum.RETURN.getValue())) {
                logger.error("{}当前订单已撤单或退货,无法进行激活", serviceType);
                return Result.bizError(860, "当前订单已撤单或退货,无法进行激活");
            }

            //3、检验OBU二次激活申请是否审核通过
            Record applyRecord = Db.findFirst(DbUtil.getSql("findActivateApplyByObuId"), obuId);
            if (applyRecord != null && !ObuActivateApplyResultEnum.PASSED.equals(applyRecord.getInt("result"))) {
                int result = applyRecord.getInt("result");
                logger.error("{}[obuId={}]此OBU二次激活申请审核结果为[{}]，暂不能二次激活",
                        serverName, obuId, ObuActivateApplyResultEnum.getName(result));
                return Result.bizError(855, String.format("此OBU二次激活申请审核结果为[%s]，暂不能二次激活",
                        ObuActivateApplyResultEnum.getName(result)));
            }

            //设置激活信息
            if (activeTime == null) {
                activeTime = new Date();
            }
            obuInfo.set("isActive", isActive);
            obuInfo.set("activeTime", activeTime == null ? new Date() : activeTime);
            obuInfo.set("activeType", activeType);
            obuInfo.set("activeChannel", activeChannel);

            String channelType = obuInfo.get("channelType");
            //4、上传部中心OBU发行数据
            if (CHANNEL_CENTER.equals(channelType)) {
                BaseUploadResponse response = uploadObuIssueInfo(obuInfo);
                if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !response.getErrorMsg().contains(ISSBS_OBUENABLE_REPEAT_MSG)) {
                    logger.error("{}上传OBU发行数据失败:{}", serviceType, response);
                    obuIssuerFlag = false;
                    return Result.bizError(867, "上传OBU发行数据失败");
                }

                //5、上传部中心卡发行信息
                response = uploadCardIssueInfo(obuInfo);
                if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !response.getErrorMsg().contains(ISSBS_CARDENABLE_REPEAT_MSG)) {
                    logger.error("{}上传卡发行数据失败:{}", serviceType, response);
                    cardIssuerFlag = false;
                    return Result.bizError(868, "上传卡发行数据失败");
                }
            }


            //查询OBUinfo的信息
            Record obuHistoryRecord = dataToObuHisitoryRecord(obuId, isActive, activeType, activeChannel, activeTime);

            //更新OBU激活状态
            Kv kv = new Kv().set("id", obuId).set("isActive", isActive)
                    .set("activeTime", activeTime).set("activeType", activeType)
                    .set("activeChannel", activeChannel).set("updatetime", new Date());
            SqlPara sqlPara = DbUtil.getSqlPara("updateObuInfoById", kv);

            //设置obu二次激活申请的结果为已激活
            if (applyRecord != null) {
                applyRecord.set("result", ObuActivateApplyResultEnum.ACTIVATED.getValue());
            }

            //6、更新数据库
            boolean[] issuerFlagArr = {obuIssuerFlag, cardIssuerFlag};
            //更新订单表中的同步OBU发行数据标识
            Record onlineOrders = dataToOnlineOrders(issuerFlagArr, orderId);

            flag = Db.tx(() -> {
                int i = Db.update(sqlPara);
                if (i > 0) {
                    //向历史表插入一条激活数据
                    if (!Db.save(TABLE_ETC_OBUINFO_HISTORY, obuHistoryRecord)) {
                        logger.error("{}保存OBU激活信息失败:obuId={}", serverName, obuId);
                        return false;
                    }
                    //更新订单表中的同步OBU发行数据标识
                    if (!Db.update(TABLE_ONLINE_ORDERS, "orderId", onlineOrders)) {
                        logger.error("{}更新TABLE_ONLINE_ORDERS表失败", serverName);
                        return false;
                    }

                    //更新obu二次激活申请表
                    if (applyRecord != null) {
                        if (!Db.update(TABLE_ETC_OBUACTIVATE_APPLY, "id", applyRecord)) {
                            logger.error("{}更新TABLE_ETC_OBUACTIVATE_APPLY表失败", serverName);
                            return false;
                        }
                    }

                } else {
                    logger.error("{}未更新到OBU信息:obuId={}", serverName, obuId);
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}激活OBU成功:{}", serverName, obuInfo);
            } else {
                logger.error("{}激活OBU失败,未更新到OBU信息", serverName);
                return Result.bizError(869, "激活OBU失败,未更新到OBU信息");
            }
        } catch (ClassCastException c) {
            logger.error("{}参数类型异常:{}", serverName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Exception e) {
            logger.error("{}保存OBU激活信息异常:{}", serverName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "保存OBU激活信息异常");
        }

        return Result.success(null);
    }

    /**
     * 获取obuhistoryRecord对象
     *
     * @param obuId
     * @param isActive
     * @param activeType
     * @param activeChannel
     * @param activeTime
     * @return
     */
    private Record dataToObuHisitoryRecord(String obuId, Integer isActive, Integer activeType, Integer activeChannel, Date activeTime) {

        //查询OBUinfo的信息
        Record obuHistoryRecord = Db.findFirst(DbUtil.getSql("findAllObuInfoById"), obuId);
        //设置激活信息
        Date now = new Date();
        obuHistoryRecord.set("opTime", now);
        obuHistoryRecord.set("createTime", now);
        obuHistoryRecord.set("updateTime", now);
        obuHistoryRecord.set("isActive", isActive);
        obuHistoryRecord.set("activeTime", activeTime);
        obuHistoryRecord.set("activeType", activeType);
        obuHistoryRecord.set("activeChannel", activeChannel);
        return obuHistoryRecord;
    }

    /**
     * 获取订单record对象
     *
     * @param issuerFlagArr
     * @param orderId
     * @return
     */
    private Record dataToOnlineOrders(boolean[] issuerFlagArr, String orderId) {
        //更新订单表中的同步OBU发行数据标识
        Record onlineOrders = new Record();
        onlineOrders.set("orderId", orderId);
        //OBU发行是否成功
        if (issuerFlagArr[0]) {
            //更改订单同步标识 1-已同步 2-已上传部中心
            onlineOrders.set("syncObuStatus", 2);
            onlineOrders.set("syncObuTime", new Date());
        }
        //卡发行是否成功
        if (issuerFlagArr[1]) {
            //同步状态 同步卡状态 1-已同步 2-已上传部中心
            onlineOrders.set("syncStatus", 2);
            //同步时间 同步卡状态时间
            onlineOrders.set("syncTime", new Date());
        }
        return onlineOrders;
    }


    /**
     * 上传OBU发行信息
     *
     * @param obu
     * @return
     */
    private BaseUploadResponse uploadObuIssueInfo(Record obu) {
        String fileName = ISSBS_OBUENABLE_REQ + CommonAttribute.ISSUER_ISS_SENDER + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";

        SupObuEnableRequest supObuEnableRequest = new SupObuEnableRequest();
        supObuEnableRequest.setAccountId(obu.get("accountId"));
        supObuEnableRequest.setIssueId(CommonAttribute.ISSUER_CODE);
        supObuEnableRequest.setVehicleId(obu.get("vehicleId"));
        supObuEnableRequest.setObuId(obu.get("obuId"));
        supObuEnableRequest.setBrand(obu.get("obuBrand"));
        supObuEnableRequest.setModel(obu.get("obuModel"));
        supObuEnableRequest.setObuSign(obu.get("obuSign"));
        supObuEnableRequest.setPlateNum(obu.get("plateNum"));
        supObuEnableRequest.setPlateColor(obu.get("plateColor"));
        supObuEnableRequest.setEnableTime(DateUtil.formatDate(obu.getDate("obuEnableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supObuEnableRequest.setExpireTime(DateUtil.formatDate(obu.getDate("obuExpireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supObuEnableRequest.setActiveTime(DateUtil.formatDate(obu.getDate("activeTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supObuEnableRequest.setActiveType(obu.get("activeType"));
        supObuEnableRequest.setActiveChannel(obu.get("activeChannel"));
        supObuEnableRequest.setOrderId(obu.get("orderId"));

        String content = SignatureManager.getSignContent(UtilJson.toJson(supObuEnableRequest), fileName);
        supObuEnableRequest.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));
        String json = Jackson.getJson().toJson(supObuEnableRequest);
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        logger.info("{}上传OBU发行响应信息:{}", serverName, response);
        return response;
    }

    /**
     * 上传卡发行信息
     *
     * @param card
     * @return
     */
    private BaseUploadResponse uploadCardIssueInfo(Record card) {
        String fileName = ISSBS_CARDENABLE_REQ + CommonAttribute.ISSUER_ISS_SENDER + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";

        SupCardEnableRequest supCardEnableRequest = new SupCardEnableRequest();
        supCardEnableRequest.setAccountId(card.get("accountId"));
        supCardEnableRequest.setIssueId(CommonAttribute.ISSUER_CODE);
        supCardEnableRequest.setVehicleId(card.get("vehicleId"));
        supCardEnableRequest.setCardId(card.get("cardId"));
        supCardEnableRequest.setCardType(card.get("cardType"));
        supCardEnableRequest.setBrand(card.get("cardBrand"));
        supCardEnableRequest.setModel(card.get("cardModel"));
        supCardEnableRequest.setAgencyId(card.get("agencyId"));
        supCardEnableRequest.setPlateNum(card.get("plateNum"));
        supCardEnableRequest.setPlateColor(card.get("plateColor"));
        supCardEnableRequest.setEnableTime(DateUtil.formatDate(card.getDate("cardEnableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supCardEnableRequest.setExpireTime(DateUtil.formatDate(card.getDate("cardExpireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supCardEnableRequest.setIssuedType(card.get("issuedType"));
        supCardEnableRequest.setChannelId(card.get("channelId"));
        supCardEnableRequest.setIssuedTime(DateUtil.formatDate(card.getDate("issuedTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        supCardEnableRequest.setOrderId(card.get("orderId"));

        String content = SignatureManager.getSignContent(UtilJson.toJson(supCardEnableRequest), fileName);
        supCardEnableRequest.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));
        String json = Jackson.getJson().toJson(supCardEnableRequest);
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        logger.info("{}上传卡发行响应信息:{}", serverName, response);
        return response;
    }
}
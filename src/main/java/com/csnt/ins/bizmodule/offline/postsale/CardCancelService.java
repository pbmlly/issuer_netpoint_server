package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.AuthTouchService;
import com.csnt.ins.bizmodule.offline.service.UserCardStatusUploadService;
import com.csnt.ins.bizmodule.offline.service.UserSignoflUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 8813 卡注销接口
 * 无需修改加减密
 * @author duwanjiang
 **/
public class CardCancelService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(CardCancelService.class);

    private final String serverName = "[8813 卡注销接口]";

    /**
     * 用户凭证刷新服务
     */
    AuthTouchService authTouchService = new AuthTouchService();
    /**
     * 卡状态变更通知
     */
    UserCardStatusUploadService userCardStatusUploadService = new UserCardStatusUploadService();
    /**
     * 4.1  车辆支付渠道绑定/ 解绑通知
     */
    UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();


    private final String TABLE_CARDBLACKLIST = "etc_cardblacklist";
    private final String TABLE_CARD_CANCEL_CONFIRM = "etc_card_cancel_confirm";

    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";
    private final String BASIC_CARDBLACKLISTUPLOAD_REQ = "BASIC_CARDBLACKLISTUPLOAD_REQ_";
    private final String REPEAT_MSG = "黑名单重复上传";
    private final String REPEATSING_MSG = "已经";
    private final String CARD_TYPE_DEP = "2";
    private final String CHANNEL_CENTER = "041";
    private final String CHANNEL_VETC = "0403";

    /**
     * 1、检查卡号是否存在
     * 2、判断该车辆信息已经进入了待注销状态
     * 3、判断当前卡状态是否为正常状态,非正常不能进行注销
     * 4、判断当前车辆的obu是否已注销，如果未注销则提示先注销OBU
     * 5、上传黑名单信息
     * 6、记录发行记录
     * 7、保存卡注销待确认记录和修改卡状态
     * 8、响应客户端
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);

            //证件类型
            Integer userIdType = record.get("userIdType");
            //证件号码
            String userIdNum = record.get("userIdNum");
            //车牌号码
            String vehiclePlate = record.get("plateNum");
            //车牌颜色
            Integer vehicleColor = record.get("plateColor");
            //卡序号编码
            String cardId = record.get("cardId");
            //业务状态
            Integer status = record.get("status");
            //变更原因
            String reason = record.get("reason");
            //渠道类型
            String channelType = record.get("channelType");
            //操作员ID
            String operatorId = record.getStr("operatorId");
            if (StringUtil.isEmpty(userIdType, userIdNum, vehiclePlate, vehicleColor, status, cardId, channelType, reason)) {
                logger.error("{}参数userIdType, userIdNum, vehiclePlate, vehicleColor, status, cardId, channelType, reason不能为空", serverName);
                return Result.paramNotNullError("userIdType, userIdNum, vehiclePlate, vehicleColor, status, cardId, channelType, reason");
            }



            //核销
            int businessType = OfflineBusinessTypeEnum.CANCEL.getValue();

            String vehicleId = vehiclePlate + "_" + vehicleColor;

            //region 注释部中心接口
            // 检查客户是否在部中心线下渠道开户
            /*EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, userIdType);
            if (etcOflUserinfo != null) {
                //调用凭证刷新接口
                Result result = authTouchService.entry(Kv.by("openId", etcOflUserinfo.getOpenId())
                        .set("accessToken", etcOflUserinfo.getAccessToken()));
                if (result.getCode() == ResponseStatusEnum.SUCCESS.getCode()) {
                    if (result.getData() != null) {
                        Map responsDataMap = (Map) result.getData();
                        Object accessToken = responsDataMap.get("accessToken");
                        Object expiresIn = responsDataMap.get("expiresIn");
                        if (StringUtil.isNotEmpty(accessToken, expiresIn)) {
                            etcOflUserinfo.setAccessToken((String) accessToken);
                            etcOflUserinfo.setExpiresIn((Integer) expiresIn);
                            //保存新的token到数据表中
                            etcOflUserinfo.update();
                        }
                    }
                } else {
                    logger.error("{}调用凭证刷新接口失败:{}", serverName, result);
                    return result;
                }

                EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
                if (etcOflVehicleinfo == null) {
                    logger.error("{}未查询到车辆绑定银行卡信息", serverName);
                    return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆绑定银行卡信息");
                }
                //4.1  车辆支付渠道绑定/ 解绑通知
                result = userSignoflUploadService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                        .set("plateNum", vehiclePlate)
                        .set("plateColor", vehicleColor)
                        //签约方式 1-线上 2-线下
                        .set("signType", 2)
                        .set("issueChannelId", etcOflVehicleinfo.getPosId())
                        .set("channelType", BankIdEnum.getDepSignChannel(etcOflVehicleinfo.getBankPost()))
                        //绑定的卡类型 1-信用卡 2-借记卡
                        .set("cardType", etcOflVehicleinfo.getCardType())
                        .set("account", etcOflVehicleinfo.getAccountId())
                        .set("enableTime", DateUtil.formatDate(etcOflVehicleinfo.getGenTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                        .set("closeTime", getCloseTime(etcOflVehicleinfo.getGenTime()))
                        .set("info", reason)
                        //绑定状态 1:绑定 2:解绑
                        .set("status", 2));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !result.getMsg().contains(REPEATSING_MSG)) {
                    logger.error("{}解绑银行卡失败:{}", serverName, result);
                    return result;
                }

                //调用4.5 卡状态变更通知
                result = userCardStatusUploadService.entry(Kv.by("cardId", cardId)
                        .set("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("type", OfflineBusinessTypeEnum.changeBusinessType(businessType))
                        .set("status", CardOflStatusEnum.getStatusByBusinessType(businessType)));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    logger.error("{}调用卡状态变更通知接口失败:{}", serverName, result);
                    return result;
                }
            }*/
            //endregion

            // 1、检查卡号是否存在
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}发行系统未找到当前卡:{}", serverName, cardId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前卡信息");
            }
            // 判断卡的车辆编号与上送的车辆编号是否一致
            if (!vehicleId.equals(cardInfo.getStr("vehicleId"))) {
                logger.error("{}上送车辆编号与卡的车辆编号不一致,vehicleId:{}", serviceName, cardInfo.getStr("vehicleId"));
                return Result.bizError(752, "上送车辆编号与卡的车辆编号不一致。");
            }

            // 判断卡是否为储蓄卡
            String cardType = cardInfo.getStr("cardType");
            if (CARD_TYPE_DEP.equals(cardType.substring(0,1))) {
                logger.error("{}该卡是储蓄卡,cardid:{}", serviceName, cardId);
                return Result.bizError(752, "该卡是储蓄卡，不能使用该注销接口。");
            }
            // 判断卡是否为总行发行
            String cltype = cardInfo.getStr("channelType");
            if (CHANNEL_CENTER.equals(cltype.substring(0,3)) ) {
                logger.error("{}该卡是总对总发行不能使用该接口,cardid:{}", serviceName, cardId);
                return Result.bizError(754, "该卡是总对总发行不能使用该接口。");
            }
            if(CHANNEL_VETC.equals(cltype.substring(0,4)) && !CHANNEL_VETC.equals(channelType.substring(0,4))) {
                logger.error("{}该卡是线上渠道发行不能使用该接口,cardid:{}", serviceName, cardId);
                return Result.bizError(754, "该卡是线上渠道发行，该渠道不能注销改卡。");
            }

            // 2、查询该车辆是否已经进入待注销状态
            Record checkRc = Db.findFirst(DbUtil.getSql("queryCardCancelByVeh"), vehicleId);
            if (checkRc != null) {
                return Result.sysError("该车辆信息已经进入了待注销状态");
            }


            // 3、判断当前卡是否是正常状态
            if (!CardYGZStatusEnum.NORMAL.equals((int) cardInfo.getInt("status"))) {
                logger.error("{}当前卡状态不为正常状态,不能进行注销", serverName);
                return Result.sysError("当前卡状态不为正常状态,不能进行注销");
            }

            // 判断该卡是否为黑名单卡信息
            if (Db.use("black").queryInt(Db.getSql("mysql.countBlackCardId"), cardId) > 0) {
                logger.error("{}该卡是黑名单，cardid:{}", serviceName, cardId);
                return Result.bizError(752, "该卡是黑名单，请先解除黑名单。");
            }


            //4、判断当前车辆的obu是否已注销，如果未注销则提示先注销OBU
            Record obuRecord = Db.findFirst(DbUtil.getSql("queryEtcObuByVeh"), cardInfo.getStr("vehicleId"));
            if (obuRecord != null) {
                logger.error("{}当前卡对应车辆的OBU还未注销,请先注销OBU:{}", serverName, obuRecord.getStr("id"));
                return Result.sysError("当前卡对应车辆的OBU还未注销,请先注销OBU");
            }

            //设置卡信息的状态
            cardInfo.set("status", status);
//            cardInfo.set("status", CardYGZStatusEnum.getStatusByBusinessType(businessType));
            /*//卡信息上传及变更
            BaseUploadResponse response = uploadBasicCardInfo(cardInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传卡营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }*/

            //卡黑名单上传
            Record cardBlacklistRecord = new Record();
            BaseUploadResponse response = uploadCardBlacklist(cardId, businessType, cardBlacklistRecord);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains(REPEAT_MSG)) {
                logger.error("{}上传黑名单信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }
            //添加卡属性
            Date currentDate = new Date();

            cardBlacklistRecord.set("id", StringUtil.getUUID());
            cardBlacklistRecord.set("insertTime", currentDate);
            //记录发行记录
            EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
            etcIssuedRecord.setUuid(StringUtil.getUUID());
            etcIssuedRecord.setVehicleId(vehicleId);
            etcIssuedRecord.setUserId(cardInfo.get("userId"));
            etcIssuedRecord.setCardId(cardId);
//            etcIssuedRecord.setCardStatus(CardYGZStatusEnum.getStatusByBusinessType(businessType));
            etcIssuedRecord.setCardStatus(status);
            etcIssuedRecord.setBusinessType(businessType);
            etcIssuedRecord.setReason(reason);
            etcIssuedRecord.setOpTime(currentDate);
            etcIssuedRecord.setCreateTime(currentDate);
            etcIssuedRecord.setUpdateTime(currentDate);

            Record cardCancelConfirmRecord = new Record();
            cardCancelConfirmRecord.setColumns(record);
            cardCancelConfirmRecord.set("userId", cardInfo.getStr("userId"));
            cardCancelConfirmRecord.set("vehicleId", vehicleId);
            //绑定状态 1:绑定 2:解绑
//            cardCancelConfirmRecord.set("status", 1);
            //确认状态 0-待确认 1-已确认
            cardCancelConfirmRecord.set("confirmStatus", 0);
//            cardCancelConfirmRecord.set("confirmTime", new Date());
            cardCancelConfirmRecord.set("createTime", new Date());
            cardCancelConfirmRecord.set("updateTime", new Date());

            // 操作员ID
            cardCancelConfirmRecord.set("operatorId", operatorId);

            if (SysConfig.getEncryptionFlag()) {
                //加密存储
                cardCancelConfirmRecord.set("userIdNum", MyAESUtil.Encrypt(userIdNum));
            }

            cardCancelConfirmRecord.remove("plateColor", "plateNum", "businessType");
            //存储数据
            boolean flag = Db.tx(() -> {
                if (!Db.save(TABLE_CARD_CANCEL_CONFIRM, "cardId,userId,vehicleId", cardCancelConfirmRecord)) {
                    logger.error("{}保存卡注销信息到ETC_CARD_CANCEL_CONFIRM表失败", serverName);
                    return false;
                }
                if (!Db.save(TABLE_CARDBLACKLIST, "cardId,userId,vehicleId", cardBlacklistRecord)) {
                    logger.error("{}保存卡注销信息到TABLE_CARDBLACKLIST表失败", serverName);
                    return false;
                }
                if (!etcIssuedRecord.save()) {
                    logger.error("{}保存卡注销信息到etcIssuedRecord表失败", serverName);
                    return false;
                }
                return true;
            });
            if (flag) {
                logger.info("{}卡注销成功", serverName);
                return Result.success(null, "卡注销成功");
            } else {
                logger.error("{}卡注销失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }
        } catch (Throwable t) {
            logger.error("{}卡注销异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 更加开启时间获取关闭时间
     *
     * @param genTime
     * @return
     */
    private String getCloseTime(Date genTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(genTime);
        calendar.add(Calendar.YEAR, 10);
        return DateUtil.formatDate(calendar.getTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
    }

    /**
     * 上传卡信息到部中心
     *
     * @param cardInfo
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record cardInfo) {
        //installType=1时 installChannelId =0
        if (1 == MathUtil.asInteger(cardInfo.get("installType"))) {
            cardInfo.set("installChannelId", 0);
        }
        cardInfo.set("operation", OperationEnum.ADD.getValue());

        EtcObuinfoJson etcObuinfoJson = new EtcObuinfoJson();
        etcObuinfoJson._setOrPut(cardInfo.getColumns());
        logger.info("{}上传卡的内容为:{}", serverName, etcObuinfoJson);
        BaseUploadResponse response = upload(etcObuinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传卡响应信息:{}", serverName, response);
        return response;
    }

    /**
     * 上传卡黑名单
     *
     * @param cardId
     * @param businessType
     * @param cardBlacklistRecord
     * @return
     */
    private BaseUploadResponse uploadCardBlacklist(String cardId, int businessType, Record cardBlacklistRecord) {
        cardBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();

        //进入黑名单
        cardBlacklistRecord.set("creationTime", new Date());
        cardBlacklistRecord.set("cardId", cardId);
        cardBlacklistRecord.set("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
        cardBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());

        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("cardId", cardId);
        // 类型
        sedMsg.put("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
        // 状态
        sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
//        String json = Jackson.getJson().toJson(sedMsg);
//        String fileName = BASIC_CARDBLACKLISTUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(sedMsg, BASIC_CARDBLACKLISTUPLOAD_REQ);
        logger.info("{}上传卡黑名单响应信息:{}", serverName, response);
        return response;
    }

}

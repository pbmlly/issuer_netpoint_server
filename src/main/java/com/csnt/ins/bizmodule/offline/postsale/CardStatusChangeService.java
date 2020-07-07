package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardStatusUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 8811 卡状态变更接口
 *
 * @author duwanjiang
 **/
public class CardStatusChangeService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(CardStatusChangeService.class);

    private final String serverName = "[8811 卡状态变更接口]";

    /**
     * 卡状态变更通知
     */
    UserCardStatusUploadService userCardStatusUploadService = new UserCardStatusUploadService();

    private final String TABLE_CARDINFO = "etc_cardinfo";
    private final String TABLE_CARDINFO_HISTORY = "etc_cardinfo_history";
    private final String TABLE_CARDBLACKLIST = "etc_cardblacklist";

    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";
    private final String BASIC_CARDBLACKLISTUPLOAD_REQ = "BASIC_CARDBLACKLISTUPLOAD_REQ_";
    private final String REPEAT_MSG = "当前卡状态为";
    private final String MSG_NORMAL = "无法办理此项业务";
    private final String MSG_NORMAL1 = "未查询当前账户下的该";
    private final String CHANNEL_CENTER = "041";
    private final String MSG_BLACK_NORMAL = "没有找到对应的黑名单";

    /**
     * 1、判断发行系统是否有当前卡
     * 2、判断当前卡状态是否可以做对应的售后操作
     * 3、查询该车辆是否已经进入待注销状态
     * 4、判断当前业务类型是否是注销，该接口不能注销
     * 5、检查卡的营改增状态
     * 6、检查客户是否在部中心线下渠道开户
     * 1、已开户：
     * 1、调用4.5 卡状态变更通知
     * 7、营改增平台上传卡信息上传及变更
     * 8、卡黑名单上传
     * 9、保存记录发行记录
     * 10、存储数据
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
            //业务类型
            Integer businessType = record.get("businessType");
            //状态
            Integer status = record.get("status");
            //变更原因
            String reason = record.get("reason");
            //渠道类型
            String channelType = record.get("channelType");

            if (StringUtil.isEmpty(userIdType, userIdNum, vehiclePlate, vehicleColor, cardId, businessType, status, channelType, reason)) {
                logger.error("{}参数userIdType, userIdNum, vehiclePlate, vehicleColor, cardId, businessType, status, channelType, reason不能为空", serverName);
                return Result.paramNotNullError("userIdType, userIdNum, vehiclePlate, vehicleColor, cardId, businessType, status, channelType, reason");
            }

            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( record.getStr("userIdNum"));
            }

            String vehicleId = vehiclePlate + "_" + vehicleColor;
            // 1、判断发行系统是否有当前卡
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}发行系统未找到当前卡:{}", serverName, cardId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前卡信息");
            }

            // 判断卡是否为总行发行
            String cltype = cardInfo.getStr("channelType");
            if (CHANNEL_CENTER.equals(cltype.substring(0,3))) {
                logger.error("{}该卡是总对总发行不能使用该接口,cardid:{}", serviceName, cardId);
                return Result.bizError(754, "该卡是总对总发行不能使用该接口。");
            }


            //2、判断当前卡状态是否可以做对应的售后操作
            Result checkResult = checkYGZCardStatus(businessType, cardInfo.getInt("status"));
            if (checkResult != null) {
                logger.error("{}检查卡的营改增状态错误:{}", serverName, checkResult.getMsg());
                return checkResult;
            }



            // 3、查询该车辆是否已经进入待注销状态
            Record checkRc = Db.findFirst(DbUtil.getSql("queryCardCancelByVeh"), vehicleId);
            if (checkRc != null) {
                logger.error("{}该车辆信息已经进入了待注销状态,不能更改卡状态", serverName);
                return Result.sysError("该车辆信息已经进入了待注销状态,不能更改卡状态");
            }

            // 换卡，换卡签全套，补卡检查黑名单信息
            if (OfflineBusinessTypeEnum.CHANGE_CARD.equals(businessType) ||
                    OfflineBusinessTypeEnum.CHANGE_ALL.equals(businessType) ||
                    OfflineBusinessTypeEnum.REISSUE_CARD.equals(businessType)) {
                if (Db.use("black").queryInt(Db.getSql("mysql.countBlackCardId"), cardId) > 0) {
                    logger.error("{}该卡是黑名单，cardid:{}", serviceName, cardId);
                    return Result.bizError(752, "该卡是黑名单，请先解除黑名单。");
                }
            }

            //4、判断当前业务类型是否是注销
            if (OfflineBusinessTypeEnum.changeBusinessType(businessType) == OfflineBusinessTypeEnum.CANCEL.getValue()) {
                logger.error("{}当前接口不能走注销流程", serverName);
                return Result.sysError("当前接口不能走注销流程");
            }

            //5、检查卡的营改增状态
            checkResult = checkYGZStatus(businessType, status);
            if (checkResult != null) {
                logger.error("{}检查卡的营改增状态错误:{}", serverName, checkResult.getMsg());
                return checkResult;
            }

            //6、检查客户是否在部中心线下渠道开户
            EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, userIdType);
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflUserinfo != null
                    && etcOflVehicleinfo != null
                    && etcOflVehicleinfo.getDepVehicleId() != null) {
                //刷新用户凭证
                Result result = oflAuthTouch(etcOflUserinfo);
                //判断刷新凭证是否成功，失败则直接退出
                if (!result.getSuccess()) {
                    logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                    return result;
                }

                //调用4.5 卡状态变更通知
                result = userCardStatusUploadService.entry(Kv.by("cardId", cardId)
                        .set("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("type", OfflineBusinessTypeEnum.changeBusinessType(businessType))
                        .set("status", CardOflStatusEnum.getStatusByBusinessType(businessType)));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !result.getMsg().contains(MSG_NORMAL1)
                        && !result.getMsg().contains(MSG_NORMAL + OfflineBusinessTypeEnum.changeBusinessType(businessType))) {
                    logger.error("{}调用卡状态变更通知接口失败:{}", serverName, result);
                    return result;
                }
            }


            //设置卡信息的状态
            cardInfo.set("status", status);
            //7、营改增平台上传卡信息上传及变更
            BaseUploadResponse response = uploadBasicCardInfo(cardInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传卡营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }

            //8、卡黑名单上传
            Record cardBlacklistRecord = new Record();
            response = uploadCardBlacklist(cardId, businessType, cardBlacklistRecord);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains(MSG_BLACK_NORMAL)
                    && !response.getErrorMsg().contains(REPEAT_MSG)) {
                logger.error("{}上传黑名单信息失败:{}", serverName, response);
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
            etcIssuedRecord.setCardStatus(status);
            etcIssuedRecord.setBusinessType(businessType);
            etcIssuedRecord.setReason(reason);
            etcIssuedRecord.setOpTime(currentDate);
            etcIssuedRecord.setCreateTime(currentDate);
            etcIssuedRecord.setUpdateTime(currentDate);

            //10、存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                if (!Db.update(TABLE_CARDINFO, cardInfo)) {
                    logger.error("{}更新TABLE_CARDINFO表失败", serverName);
                    return false;
                }
                cardInfo.set("createTime", currentDate);
                if (!Db.save(TABLE_CARDINFO_HISTORY, ids, cardInfo)) {
                    logger.error("{}插入TABLE_CARDINFO_HISTORY表失败", serverName);
                    return false;
                }

                if (!Db.save(TABLE_CARDBLACKLIST, cardBlacklistRecord)) {
                    logger.error("{}插入TABLE_CARDBLACKLIST表失败", serverName);
                    return false;
                }
                if (!etcIssuedRecord.save()) {
                    logger.error("{}插入etcIssuedRecord表失败", serverName);
                    return false;
                }
                return true;
            });
            if (flag) {
                logger.info("{}卡状态变更成功", serverName);
                return Result.success(null, "卡状态变更成功");
            } else {
                logger.error("{}卡状态变更失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "卡状态变更失败");
            }
        } catch (Throwable t) {
            logger.error("{}卡状态变更异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 判断当前卡状态是否可以做对应的售后操作
     *
     * @param type
     * @param cardStatus
     * @return
     */
    private Result checkYGZCardStatus(int type, int cardStatus) {
        if (OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)
                || OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOST.equals(type)
        ) {
            if (!CardYGZStatusEnum.NORMAL.equals(cardStatus)) {
                return Result.sysError(String.format("当前处理的老卡状态为[%s]状态,不为[正常]状态,不能进行[%s]操作",
                        CardYGZStatusEnum.getName(cardStatus),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }

        if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            if (!CardYGZStatusEnum.HANG_UP_WITHOUT_CARD.equals(cardStatus)
                    && !CardYGZStatusEnum.HANG_UP_WITH_CARD.equals(cardStatus)) {
                return Result.sysError(String.format("当前处理的老卡状态为[%s]状态,不为[挂起]状态,不能进行[%s]操作",
                        CardYGZStatusEnum.getName(cardStatus),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }
        if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)) {
            if (!CardYGZStatusEnum.LOSS_OF_CARD.equals(cardStatus)) {
                return Result.sysError(String.format("当前处理的老卡状态为[%s]状态,不为[挂起]状态,不能进行[%s]操作",
                        CardYGZStatusEnum.getName(cardStatus),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }
        return null;
    }

    /**
     * 检查卡营改增状态是否合法
     *
     * @param type
     * @param status
     * @return
     */
    private Result checkYGZStatus(int type, int status) {
        //3- 换卡
        //4- 换卡签全套
        //6- 卡补办
        //7- 挂起
        //8- 解挂起
        if (OfflineBusinessTypeEnum.CHANGE_CARD.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)) {
            //更换
            if (!CardYGZStatusEnum.CANCEL_WITH_CARD.equals(status)) {
                return Result.sysError("[更换]业务只能选择[有卡注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.REISSUE_CARD.equals(type)) {
            //补办
            if (!CardYGZStatusEnum.CANCEL_WITHOUT_CARD.equals(status)) {
                return Result.sysError("[补办]业务只能选择[无卡注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //注销
            if (!CardYGZStatusEnum.CANCEL_WITH_CARD.equals(status)
                    && !CardYGZStatusEnum.CANCEL_WITHOUT_CARD.equals(status)) {
                return Result.sysError("[注销]业务只能选择[注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)) {
            //7- 挂起
            if (!CardYGZStatusEnum.HANG_UP_WITHOUT_CARD.equals(status)
                    && !CardYGZStatusEnum.HANG_UP_WITH_CARD.equals(status)) {
                return Result.sysError("[挂起]业务只能选择[挂起]状态");
            }
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)) {
            //8- 解挂起
            if (!CardYGZStatusEnum.NORMAL.equals(status)) {
                return Result.sysError("[解挂起]业务只能选择[正常]状态");
            }
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)) {
            //挂失
            if (!CardYGZStatusEnum.LOSS_OF_CARD.equals(status)) {
                return Result.sysError("[挂失]业务只能选择[挂失]状态");
            }
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            if (!CardYGZStatusEnum.NORMAL.equals(status)) {
                return Result.sysError("[解挂失]业务只能选择[正常]状态");
            }
        }
        return null;
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
        etcCardinfoJson.setStatusChangeTime(DateUtil.formatDate(cardInfo.get("statusChangeTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));

        logger.info("{}上传卡的内容为:{}", serverName, etcCardinfoJson);
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
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
        BaseUploadResponse response = new BaseUploadResponse();
        cardBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();
        //判断业务类型进行黑名单类型为进入或解除
        if (businessType == OfflineBusinessTypeEnum.UNHANG.getValue()
                //卡解挂起
                || businessType == OfflineBusinessTypeEnum.UNHANG_CARD.getValue()
                //解挂失
                || businessType == OfflineBusinessTypeEnum.UNLOSS_OF_CARD.getValue()
                //解挂失
                || businessType == OfflineBusinessTypeEnum.UNLOST.getValue()
        ) {
            //8- 解挂起  为解除黑名单
            Record cardBlackRecord = Db.findFirst(DbUtil.getSql("queryEtcCardBlacklistByIdAndTypeAndStatus"),
                    cardId, CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType), BlackListStatusEnum.CREATE.getValue());

            if (cardBlackRecord == null) {
                logger.error("{}当前没有找到对应的黑名单，无法解除黑名单", serverName);
                response.setStateCode(704);
                response.setErrorMsg("没有找到对应的黑名单，无法解除黑名单");
                return response;
            }
            //解除当前黑名单
            cardBlackRecord.set("status", BlackListStatusEnum.DELETE.getValue());

            // 状态
            sedMsg.put("status", BlackListStatusEnum.DELETE.getValue());

            int relieveDiff = SysConfig.CONFIG.getInt("blacklist.relieve.diff");
            //判断黑名单是否过了冷冻时间
            long diffTime = DateUtil.getTimes(cardBlackRecord.getDate("creationTime"), new Date()) / (1000 * 60);
            if (diffTime <= relieveDiff) {
                response.setStateCode(704);
                response.setErrorMsg("当前黑名单处于冷却期,请[" + (relieveDiff - diffTime) + "]分钟后再试");
                logger.error("{}当前黑名单的creationTime与当前时间相差[{}]分钟,小于{}分钟,不能进行解除", serverName, diffTime, relieveDiff);
                return response;
            }

            //复制record内容
            cardBlacklistRecord.setColumns(cardBlackRecord);
        } else {
            //进入黑名单
            cardBlacklistRecord.set("cardId", cardId);
            cardBlacklistRecord.set("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            cardBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());
            // 状态
            sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        }
        //进入黑名单时间
        cardBlacklistRecord.set("creationTime", new Date());
        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("cardId", cardId);
        // 类型
        sedMsg.put("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));

        response = uploadYGZ(sedMsg, BASIC_CARDBLACKLISTUPLOAD_REQ);
        logger.info("{}上传卡黑名单响应信息:{}", serverName, response);
        return response;
    }

}

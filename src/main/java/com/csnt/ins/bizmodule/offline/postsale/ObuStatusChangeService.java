package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuStatusUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcObuinfoJson;
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
 * 8810 OBU状态变更接口
 *
 * @author duwanjiang
 **/
public class ObuStatusChangeService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(ObuStatusChangeService.class);

    private final String serverName = "[8810 OBU状态变更接口]";
    private final String MSG_NORMAL = "无法办理此项业务";
    private final String MSG_NORMAL1 = "未查询当前账户下的该";
    private final String MSG_NORMAL2 = "当前obu状态为(101)";
    private final String MSG_BLACK_NORMAL = "解除黑名单类型不存在";
    /**
     * OBU状态变更通知
     */
    UserObuStatusUploadService userObuStatusUploadService = new UserObuStatusUploadService();

    private final String TABLE_OBUINFO = "etc_obuinfo";
    private final String TABLE_OBUINFO_HISTORY = "etc_obuinfo_history";
    private final String TABLE_OBUBLACKLIST = "etc_obublacklist";

    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_OBUBLACKLISTUPLOAD_REQ = "BASIC_OBUBLACKLISTUPLOAD_REQ_";
    private final String REPEAT_MSG = "黑名单类型已存在";
    private final String CHANNEL_CENTER = "041";

    /**
     * 1、取obu信息，判断是否存在
     * 2、判断当前OBU状态是否可以做对应的售后操作
     * 3、查询该车辆是否已经进入待注销状态,如果卡注销了，那么obu只能进行注销操作
     * 4、检查营改增状态
     * 5、检查客户是否在部中心线下渠道开户
     * 1、已开户
     * 1、调用4.4 OBU状态变更通知
     * 6、OBU营改增信息上传及变更
     * 7、OBU黑名单上传
     * 8、记录发行记录保存
     * 9、存储数据
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
            //OBU 序号编码
            String obuId = record.get("obuId");
            //业务类型
            Integer businessType = record.get("businessType");
            //OBU 营改增状态
            Integer status = record.get("status");
            //变更原因
            String reason = record.get("reason");
            //渠道类型
            String channelType = record.get("channelType");
            //操作员ID
            String operatorId = record.getStr("operatorId");

            if (StringUtil.isEmpty(userIdType, userIdNum, vehiclePlate, vehicleColor, status, obuId, businessType, channelType, reason)) {
                logger.error("{}参数userIdType, userIdNum, vehiclePlate, vehicleColor, status, obuId, businessType, channelType, reason不能为空", serverName);
                return Result.paramNotNullError("userIdType, userIdNum, vehiclePlate, vehicleColor, status, obuId, businessType,channelType, reason");
            }
            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( record.getStr("userIdNum"));
            }


            String vehicleId = vehiclePlate + "_" + vehicleColor;

            // 1、取obu信息，判断是否存在
            Record obuInfo = Db.findFirst(DbUtil.getSql("queryEtcObuInfoById"), obuId);
            if (obuInfo == null) {
                logger.error("{}发行系统未找到当前OBU:{}", serverName, obuId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前OBU信息");
            }
            // 判断卡是否为总行发行
            String cltype = obuInfo.getStr("channelType");
            if (CHANNEL_CENTER.equals(cltype.substring(0,3))) {
                logger.error("{}该OBU是总对总发行不能使用该接口,obuId:{}", serviceName, obuId);
                return Result.bizError(754, "该OBU是总对总发行不能使用改接口。");
            }


            //2、判断当前OBU状态是否可以做对应的售后操作
            Result checkResult = checkYGZObuStatus(businessType, obuInfo.getInt("status"));
            if (checkResult != null) {
                logger.error("{}检查OBU的营改增状态错误:{}", serverName, checkResult.getMsg());
                return checkResult;
            }

            //  3、查询该车辆是否已经进入待注销状态,如果卡注销了，那么obu只能进行注销操作
            Record checkRc = Db.findFirst(DbUtil.getSql("queryCardCancelByVeh"), vehicleId);
            if (checkRc != null
                    && !OfflineBusinessTypeEnum.CANCEL.equals(businessType)
                    && !OfflineBusinessTypeEnum.CANCEL_CARD.equals(businessType)
                    && !OfflineBusinessTypeEnum.CANCEL_OBU.equals(businessType)
                    ) {
                logger.error("{}该车辆信息已经进入了待注销状态[businessType={}],不能更改OBU状态", serverName, businessType);
                return Result.sysError("该车辆信息已经进入了待注销状态,不能更改OBU状态");
            }

            //4、检查营改增状态
            checkResult = checkYGZStatus(businessType, status);
            if (checkResult != null) {
                logger.error("{}检查OBU营改增状态错误:{}", serverName, checkResult.getMsg());
                return checkResult;
            }

            // 5、检查客户是否在部中心线下渠道开户
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

                //调用4.4 OBU状态变更通知
                result = userObuStatusUploadService.entry(Kv.by("obuId", obuId)
                        .set("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("type", OfflineBusinessTypeEnum.changeBusinessType(businessType))
                        .set("status", ObuOflStatusEnum.getStatusByBusinessType(businessType)));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !(OfflineBusinessTypeEnum.changeBusinessType(businessType) == 9 && result.getMsg().contains(MSG_NORMAL2))
                        && !result.getMsg().contains(MSG_NORMAL1)
                        && !result.getMsg().contains(MSG_NORMAL + OfflineBusinessTypeEnum.changeBusinessType(businessType))) {
                    logger.error("{}调用OBU状态变更通知接口失败:{}", serverName, result);
                    return result;
                }
            }


            //设置obu信息的状态
            obuInfo.set("status", status);

            //6、OBU营改增信息上传及变更
            BaseUploadResponse response = uploadBasicObuInfo(obuInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传OBU营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }

            //7、OBU黑名单上传
            Record obuBlacklistRecord = new Record();
            response = uploadObuBlacklist(obuId, businessType, obuBlacklistRecord);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !response.getErrorMsg().contains(MSG_BLACK_NORMAL)
                    && !response.getErrorMsg().contains(REPEAT_MSG)) {
                logger.error("{}上传黑名单信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }


            //添加obu属性
            Date currentDate = new Date();
            obuInfo.set("channelType", channelType);
            obuInfo.set("opTime", currentDate);
            obuInfo.set("updateTime", currentDate);

            obuBlacklistRecord.set("id", StringUtil.getUUID());
            obuBlacklistRecord.set("insertTime", currentDate);
            //上传状态 0-未上传 1-已上传
            obuBlacklistRecord.set("uploadStatus", 1);
            obuBlacklistRecord.set("uploadTime", currentDate);

            //8、记录发行记录
            EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
            etcIssuedRecord.setUuid(StringUtil.getUUID());
            etcIssuedRecord.setVehicleId(obuInfo.getStr("vehicleId"));
            etcIssuedRecord.setUserId(obuInfo.get("userId"));
            etcIssuedRecord.setObuId(obuId);
            etcIssuedRecord.setObuStatus(status);
            etcIssuedRecord.setBusinessType(businessType);
            etcIssuedRecord.setReason(reason);
            etcIssuedRecord.setOpTime(currentDate);
            etcIssuedRecord.setCreateTime(currentDate);
            etcIssuedRecord.setUpdateTime(currentDate);

            //9、存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                if (!Db.update(TABLE_OBUINFO, obuInfo)) {
                    logger.error("{}更新TABLE_OBUINFO表失败", serverName);
                    return false;
                }

                obuInfo.set("createTime", currentDate);
                obuInfo.set("operatorId",operatorId);
                if (!Db.save(TABLE_OBUINFO_HISTORY, ids, obuInfo)) {
                    logger.error("{}插入TABLE_OBUINFO_HISTORY表失败", serverName);
                    return false;
                }

                if (!Db.save(TABLE_OBUBLACKLIST, obuBlacklistRecord)) {
                    logger.error("{}插入TABLE_OBUBLACKLIST表失败", serverName);
                    return false;
                }
                if (!etcIssuedRecord.save()) {
                    logger.error("{}插入etcIssuedRecord表失败", serverName);
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}obu状态变更成功", serverName);
                return Result.success(null, "obu状态变更成功");
            } else {
                logger.error("{}obu状态变更失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "obu状态变更失败");
            }
        } catch (Throwable t) {
            logger.error("{}obu状态变更异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 检查OBU营改增状态是否合法
     *
     * @param type
     * @param status
     * @return
     */
    private Result checkYGZStatus(int type, int status) {
        if (OfflineBusinessTypeEnum.CHANGE_OBU.equals(type)
                || OfflineBusinessTypeEnum.CHANGE_ALL.equals(type)) {
            //更换
            if (!ObuYGZStatusEnum.OLD_CANCEL_WITH_SIGN.equals(status)) {
                return Result.sysError("[更换]业务只能选择[有签注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.REISSUE_OBU.equals(type)) {
            //补办
            if (!ObuYGZStatusEnum.OLD_CANCEL_WITHOUT_SIGN.equals(status)) {
                return Result.sysError("[补办]业务只能选择[无签注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.CANCEL.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_CARD.equals(type)
                || OfflineBusinessTypeEnum.CANCEL_OBU.equals(type)) {
            //注销
            if (!ObuYGZStatusEnum.OLD_CANCEL_WITHOUT_SIGN.equals(status)
                    && !ObuYGZStatusEnum.OLD_CANCEL_WITH_SIGN.equals(status)) {
                return Result.sysError("[注销]业务只能选择[注销]状态");
            }
        } else if (OfflineBusinessTypeEnum.HANG_UP.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_OBU.equals(type)
                || OfflineBusinessTypeEnum.HANG_UP_CARD.equals(type)) {
            //7- 挂起
            if (!ObuYGZStatusEnum.OLD_HANG_UP_WITHOUT_SIGN.equals(status)
                    && !ObuYGZStatusEnum.OLD_HANG_UP_WITH_SIGN.equals(status)) {
                return Result.sysError("[挂起]业务只能选择[挂起]状态");
            }
        } else if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)) {
            //8- 解挂起
            if (!ObuYGZStatusEnum.OLD_NORMAL.equals(status)) {
                return Result.sysError("[解挂起]业务只能选择[正常]状态");
            }
        } else if (OfflineBusinessTypeEnum.LOST.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.LOSS_OF_CARD.equals(type)) {
            //挂失
            if (!ObuYGZStatusEnum.OLD_LOSS_OF_SIGN.equals(status)) {
                return Result.sysError("[挂失]业务只能选择[挂失]状态");
            }
        } else if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)) {
            if (!ObuYGZStatusEnum.OLD_NORMAL.equals(status)) {
                return Result.sysError("[解挂失]业务只能选择[正常]状态");
            }
        }
        return null;
    }

    /**
     * 判断当前OBU状态是否可以做对应的售后操作
     *
     * @param type
     * @param obuStatus
     * @return
     */
    private Result checkYGZObuStatus(int type, int obuStatus) {
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
            if (!ObuYGZStatusEnum.OLD_NORMAL.equals(obuStatus)) {
                return Result.sysError(String.format("当前处理的老OBU状态为[%s]状态,不为[正常]状态,不能进行[%s]操作",
                        ObuYGZStatusEnum.getName(obuStatus, true),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }

        if (OfflineBusinessTypeEnum.UNHANG.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_CARD.equals(type)
                || OfflineBusinessTypeEnum.UNHANG_OBU.equals(type)) {
            if (!ObuYGZStatusEnum.OLD_HANG_UP_WITHOUT_SIGN.equals(obuStatus)
                    && !ObuYGZStatusEnum.OLD_HANG_UP_WITH_SIGN.equals(obuStatus)) {
                return Result.sysError(String.format("当前处理的老OBU状态为[%s]状态,不为[挂起]状态,不能进行[%s]操作",
                        ObuYGZStatusEnum.getName(obuStatus, true),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }
        if (OfflineBusinessTypeEnum.UNLOST.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_OBU.equals(type)
                || OfflineBusinessTypeEnum.UNLOSS_OF_CARD.equals(type)) {
            if (!ObuYGZStatusEnum.OLD_LOSS_OF_SIGN.equals(obuStatus)) {
                return Result.sysError(String.format("当前处理的老OBU状态为[%s]状态,不为[挂起]状态,不能进行[%s]操作",
                        ObuYGZStatusEnum.getName(obuStatus, true),
                        OfflineBusinessTypeEnum.getName(type)));
            }
        }
        return null;
    }

    /**
     * 上传OBU信息到部中心
     *
     * @param obuInfo
     * @return
     */
    private BaseUploadResponse uploadBasicObuInfo(Record obuInfo) {
        //installType=1时 installChannelId =0
        if (1 == MathUtil.asInteger(obuInfo.get("installType"))) {
            obuInfo.set("installChannelId", 0);
        }
        obuInfo.set("operation", OperationEnum.UPDATE.getValue());

        EtcObuinfoJson etcObuinfoJson = new EtcObuinfoJson();
        etcObuinfoJson._setOrPut(obuInfo.getColumns());
        // 时间需要转换为字符串
        etcObuinfoJson.setEnableTime(DateUtil.formatDate(obuInfo.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setExpireTime(DateUtil.formatDate(obuInfo.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setRegisteredTime(DateUtil.formatDate(obuInfo.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setInstallTime(DateUtil.formatDate(obuInfo.get("installTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setStatusChangeTime(DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());

        logger.info("{}上传OBU的内容为:{}", serverName, etcObuinfoJson);
        BaseUploadResponse response = upload(etcObuinfoJson, BASIC_OBUUPLOAD_REQ);
        logger.info("{}上传OBU响应信息:{}", serverName, response);
        return response;
    }

    /**
     * 上传OBU黑名单
     *
     * @param obuId
     * @param businessType
     * @param obuBlacklistRecord
     * @return
     */
    private BaseUploadResponse uploadObuBlacklist(String obuId, int businessType, Record obuBlacklistRecord) {
        BaseUploadResponse response = new BaseUploadResponse();
        obuBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();
        //判断业务类型进行黑名单类型为进入或解除
        if (businessType == OfflineBusinessTypeEnum.UNHANG.getValue()
                //卡解挂起
                || businessType == OfflineBusinessTypeEnum.UNHANG_OBU.getValue()
                //解挂失
                || businessType == OfflineBusinessTypeEnum.UNLOSS_OF_OBU.getValue()
                //解挂失
                || businessType == OfflineBusinessTypeEnum.UNLOST.getValue()
                ) {
            //8- 解挂起  为解除黑名单
            Record obuBlackRecord = Db.findFirst(DbUtil.getSql("queryEtcObuBlacklistByIdAndTypeAndStatus"),
                    obuId, ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType), BlackListStatusEnum.CREATE.getValue());
            if (obuBlackRecord == null) {
                logger.error("{}当前没有找到对应的黑名单，无法解除黑名单", serverName);
                response.setStateCode(704);
                response.setErrorMsg("没有找到对应的黑名单，无法解除黑名单");
                return response;
            }
            //解除当前黑名单
            obuBlackRecord.set("status", BlackListStatusEnum.DELETE.getValue());

            // 状态
            sedMsg.put("status", BlackListStatusEnum.DELETE.getValue());

            int relieveDiff = SysConfig.CONFIG.getInt("blacklist.relieve.diff");
            //判断黑名单是否过了冷冻时间
            long diffTime = DateUtil.getTimes(obuBlackRecord.getDate("creationTime"), new Date()) / (1000 * 60);
            if (diffTime <= relieveDiff) {
                response.setStateCode(704);
                response.setErrorMsg("当前黑名单处于冷却期,请[" + (relieveDiff - diffTime) + "]分钟后再试");
                logger.error("{}当前黑名单的creationTime与当前时间相差[{}]分钟,小于{}分钟,不能进行解除", serverName, diffTime, relieveDiff);
                return response;
            }

            //复制record内容
            obuBlacklistRecord.setColumns(obuBlackRecord);
        } else {
            //进入黑名单
            obuBlacklistRecord.set("OBUId", obuId);
            obuBlacklistRecord.set("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            obuBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());
            // 状态
            sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        }
        //进入黑名单
        obuBlacklistRecord.set("creationTime", new Date());
        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("OBUId", obuId);
        // 类型
        sedMsg.put("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));

        response = uploadYGZ(sedMsg, BASIC_OBUBLACKLISTUPLOAD_REQ);
        logger.info("{}上传OBU黑名单响应信息:{}", serverName, response);
        return response;
    }


}

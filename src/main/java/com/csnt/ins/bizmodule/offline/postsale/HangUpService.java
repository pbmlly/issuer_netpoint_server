package com.csnt.ins.bizmodule.offline.postsale;

import com.alibaba.druid.util.Base64;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.IssueVertifyoflUploadService;
import com.csnt.ins.bizmodule.offline.service.UserAccountUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 8812 挂起接口
 *
 * @author duwanjiang
 **/
public class HangUpService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(HangUpService.class);

    private final String serverName = "[8812 挂起接口]";

    /**
     * 证件信息验证
     */
    private IssueVertifyoflUploadService issueVertifyoflUploadService = new IssueVertifyoflUploadService();
    private final Integer CUSTOMER_TYPE_COMPANY = 2;
    private final Integer CUSTOMER_TYPE_PERSONAL = 1;
    private final String TABLE_OBUINFO = "etc_obuinfo";
    private final String TABLE_OBUINFO_HISTORY = "etc_obuinfo_history";
    private final String TABLE_OBUBLACKLIST = "etc_obublacklist";
    private final String ETC_OFL_USERINFO = "etc_ofl_userinfo";
    private final String TABLE_CARDINFO = "etc_cardinfo";
    private final String TABLE_CARDINFO_HISTORY = "etc_cardinfo_history";
    private final String TABLE_CARDBLACKLIST = "etc_cardblacklist";
    private final String TABLE_ETC_OFL_VEHILCEINFO = "etc_ofl_vehicleinfo";
    private final String TABLE_ETC_VEHILCEINFO = "etc_vehicleinfo";
    private final String TABLE_VEHILCEINFO_HIS = "etc_vehicleinfo_history";
    private final String ONLINE_PICTURE = "onlinepicture";


    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

    private final String BASIC_OBUBLACKLISTUPLOAD_REQ = "BASIC_OBUBLACKLISTUPLOAD_REQ_";
    private final String BASIC_CARDBLACKLISTUPLOAD_REQ = "BASIC_CARDBLACKLISTUPLOAD_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";


    /**
     * 8831获取账户信息
     */
    UserAccountUploadService userAccountUploadService = new UserAccountUploadService();

    private final String REPEAT_MSG = "重复";

    /**
     * 1、判断车辆是否在监管平台
     * ----1、在：
     * --------1、判断用户是否开户，没有先开户，然后调用部中心线下监管平台的 车辆驾驶信息认证，让部中心挂起
     * ---2、不在：
     * --------1、判断当前车辆是否在本省存在
     * ------------1、不存在：程序退出，返回当前车辆未在本省开户
     * 2、判断图片大小是否符合标准
     * 3、上传营改增平台卡的状态为挂起
     * 4、上传营改增平台OBU的状态为挂起
     * 5、上传营改增平台卡的黑名单
     * 6、上传营改增平台OBU的黑名单
     * 7、营改增平台删除车辆信息
     * 8、清空etc_ofl_vehicleinfo车辆的绑卡信息
     * <p>
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
            //车架号
            String vin = record.get("vin");
            //发动机号
            String engineNum = record.get("engineNum");
            //变更原因
            String reason = record.get("reason");
            //渠道类型
            String channelType = record.get("channelType");
            //发证日期 YYYY-MM-DD
            String issueDateStr = record.get("issueDate");
            //注册日期 YYYY-MM-DDTHH:mm:ss
            String registerDateStr = record.get("registerDate");
            //使用性质 1:营运 2:非营运
            Integer useCharacter = record.get("useCharacter");
            //车辆类型
            String vehicleType = record.get("vehicleType");
            //身份证正面图片
            String idPositiveImageStr = record.get("idPositiveImageStr");
            //行驶证图片
            String vehPositiveImageStr = record.get("vehPositiveImageStr");


            if (StringUtil.isEmpty(userIdType, userIdNum, vehiclePlate, vehicleColor, vin, channelType, reason, useCharacter, vehicleType, idPositiveImageStr, vehPositiveImageStr)) {
                logger.error("{}参数userIdType, userIdNum, vehiclePlate, vehicleColor, vin, channelType, reason, useCharacter,vehicleType, idPositiveImageStr, vehPositiveImageStr不能为空", serverName);
                return Result.paramNotNullError("userIdType, userIdNum, vehiclePlate, vehicleColor, vin, channelType, reason, useCharacter, vehicleType, idPositiveImageStr, vehPositiveImageStr");
            }
            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( record.getStr("userIdNum"));
            }

            String vehicleId = vehiclePlate + "_" + vehicleColor;

            //1、判断车辆是否在监管平台
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.findFirstRegeistedOflVehicleByVehicleId(vehicleId);
            if (etcOflVehicleinfo == null) {
                //1、判断当前车辆是否在本省存在
                Record vehicleRecord = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
                if (vehicleRecord == null) {
                    logger.error("{}当前车辆在本省发行系统中不存,无法进行车辆挂起vehicleId={}", serverName, vehicleId);
                    return Result.sysError("当前车辆在本省发行系统中不存,无法进行车辆挂起");
                }
            } else {
                // 1、检查客户是否在部中心线下渠道开户
                EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, userIdType);
                if (etcOflUserinfo == null) {
                    // 如果客户线下未开户则开户
                    if (etcOflUserinfo == null) {
                        Record userRc = Db.findFirst(DbUtil.getSql("queryEtcUserByUserId"),userIdType,userIdNum);
                        if (userRc == null) {
                            return Result.bizError(794, "未找到对应的客户信息");
                        }

                        Result result = callOffineOpenUser(userRc,userRc.getStr("id"));
                        if (!result.getSuccess()) {
                            return result;
                        }
                        //取账号返回的相关信息
                        Map tkMap = (Map) result.getData();
                        Record oflUserRc = dataToOflUser(userRc, tkMap, userRc.getStr("id"));
                        if (Db.save(ETC_OFL_USERINFO, oflUserRc)) {
                            logger.info("{}保存线下监管平台ETC_OFL_USERINFO表数据成功", serviceName);
                        } else {
                            logger.error("{}保存线下监管平台ETC_OFL_USERINFO表数据失败", serviceName);
                            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR,
                                    "保存线下监管平台ETC_OFL_USERINFO表数据失败");
                        }
                        etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(userRc.getStr("id"));

                    }
                }
                if (etcOflUserinfo != null) {
                    //刷新用户凭证
                    Result result = oflAuthTouch(etcOflUserinfo);
                    //判断刷新凭证是否成功，失败则直接退出
                    if (!result.getSuccess()) {
                        logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                        return result;
                    }

                    try {
                        //检查日期格式
                        DateUtil.parseDate(issueDateStr, DateUtil.FORMAT_YYYY_MM_DD);
                        DateUtil.parseDate(registerDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                    } catch (Exception e) {
                        logger.error("{}issueDate、registerDate转换日期格式异常:{}", serverName, e.toString(), e);
                        return Result.sysError("issueDate、registerDate日期格式异常");
                    }
                    //调用5.4 证件信息验证 userIdNum 上传部中心，不需要加密
                    result = issueVertifyoflUploadService.entry(Kv.by("openId", etcOflUserinfo.getOpenId())
                            .set("accessToken", etcOflUserinfo.getAccessToken())
                            .set("plateNum", vehiclePlate)
                            .set("plateColor", vehicleColor)
                            //验证类型 1- 被动挂起
                            .set("type", 1)
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("driverId", record.get("userIdNum"))
                            .set("driverName", etcOflUserinfo.getUserName()==null?null:MyAESUtil.Decrypt(etcOflUserinfo.getUserName()))
                            .set("driverIdType", userIdType)
                            .set("vin", vin)
                            .set("engineNum", engineNum)
                            .set("issueDate", issueDateStr)
                            .set("registerDate", registerDateStr)
                            .set("useCharacter", useCharacter)
                            .set("vehicleType", vehicleType));
                    if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                        logger.error("{}调用部中心证件信息验证接口失败:{}", serverName, result);
                        return result;
                    }
                } else {
                    logger.error("{}[userIdNum={},userIdType={}]当前用户还未开户,请先开户", serverName, userIdNum, userIdType);
                    return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "当前用户还未开户,请先开户");
                }
            }

            //2、判断图片大小是否符合标准
            if (!isLess200KCheckImgSize(idPositiveImageStr)) {
                logger.error("{}身份证正面照文件过大", serviceName);
                return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "身份证正面照文件过大,请在200K以内");
            }

            if (!isLess200KCheckImgSize(vehPositiveImageStr)) {
                logger.error("{}行驶证照文件过大", serviceName);
                return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "行驶证照文件过大,请在200K以内");
            }

            // 取obu信息
            Record obuInfo = Db.findFirst(DbUtil.getSql("queryEtcOBUInfoByVehicleIdAndUserId"), vehicleId);
//            if (obuInfo == null) {
//                logger.error("{}发行系统未找到当前OBU[vehicleId={}]", serverName, vehicleId);
//                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前OBU信息");
//            }
            // 取卡信息
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoByVehicleIdAndUserId"), vehicleId);
            if (cardInfo == null) {
                logger.error("{}发行系统未找到当前卡[vehicleId={}]", serverName, vehicleId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前卡信息");
            }


            //7- 挂起
            int businessType = OfflineBusinessTypeEnum.HANG_UP.getValue();

            int obuStatus = obuInfo == null?0: obuInfo.getInt("status");
            int cardStatus = cardInfo.getInt("status");
            Date currentDate = new Date();
            Record obuBlacklistRecord = new Record();;
            if (obuInfo != null) {

                if (obuStatus != ObuYGZStatusEnum.OLD_CANCEL_WITH_SIGN.getValue() &&
                        obuStatus != ObuYGZStatusEnum.OLD_CANCEL_WITHOUT_SIGN.getValue()) {
                    //设置obu信息的状态
                    obuInfo.set("status", ObuYGZStatusEnum.getOldStatusByBusinessType(businessType));

                    //3、OBU信息上传及变更
                    BaseUploadResponse response = uploadBasicObuInfo(obuInfo);
                    if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                        logger.error("{}上传OBU营改增信息失败:{}", serverName, response);
                        return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
                    }
                    //4、OBU黑名单上传
//                Record obuBlacklistRecord = new Record();
                    response = uploadObuBlacklist(obuInfo.getStr("id"), businessType, obuBlacklistRecord);
                    if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                            && !response.getErrorMsg().contains(REPEAT_MSG)) {
                        logger.error("{}上传OBU黑名单信息失败:{}", serverName, response);
                        return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
                    }

                    obuBlacklistRecord.set("id", StringUtil.getUUID());
                    obuBlacklistRecord.set("insertTime", currentDate);
                    //上传状态 0-未上传 1-已上传
                    obuBlacklistRecord.set("uploadStatus", 1);
                    obuBlacklistRecord.set("uploadTime", currentDate);

                }
            }

            Record cardBlacklistRecord = new Record();;
            if (cardStatus != CardYGZStatusEnum.CANCEL_WITH_CARD.getValue() &&
                    cardStatus != CardYGZStatusEnum.CANCEL_WITHOUT_CARD.getValue()) {
                //设置卡信息的状态
                cardInfo.set("status", CardYGZStatusEnum.getStatusByBusinessType(businessType));
                //5、卡信息上传及变更
                BaseUploadResponse response = uploadBasicCardInfo(cardInfo);
                if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    logger.error("{}上传卡营改增信息失败:{}", serverName, response);
                    return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
                }
                //6、卡黑名单上传
//                cardBlacklistRecord = new Record();
                response = uploadCardBlacklist(cardInfo.getStr("id"), businessType, cardBlacklistRecord);
                if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !response.getErrorMsg().contains(REPEAT_MSG)) {
                    logger.error("{}上传黑名单信息失败:{}", serverName, response);
                    return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
                }

                cardBlacklistRecord.set("id", StringUtil.getUUID());
                cardBlacklistRecord.set("insertTime", currentDate);
                //上传状态 0-未上传 1-已上传
                cardBlacklistRecord.set("uploadStatus", 1);
                cardBlacklistRecord.set("uploadTime", currentDate);
            }



            // 7、删除车辆信息
            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
            BaseUploadResponse response = uploadCenterVehInfo(vehicleInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}删除车辆信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }


            // 图片信息
            Record picRc = dataToOnlinePicture(record, cardInfo.getStr("userId"));

            //添加obu属性

            if (obuInfo != null) {
                obuInfo.set("channelType", channelType);
                obuInfo.set("opTime", currentDate);
                obuInfo.set("updateTime", currentDate);
            }




            //添加卡属性
            cardInfo.set("channelType", channelType);
            cardInfo.set("opTime", currentDate);
            cardInfo.set("updateTime", currentDate);


            //记录发行记录
            EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
            etcIssuedRecord.setUuid(StringUtil.getUUID());
            etcIssuedRecord.setVehicleId(vehicleId);
            etcIssuedRecord.setUserId(cardInfo.get("userId"));
            etcIssuedRecord.setCardId(cardInfo.getStr("id"));
            etcIssuedRecord.setCardStatus(CardYGZStatusEnum.getStatusByBusinessType(businessType));
            etcIssuedRecord.setObuId(obuInfo==null?null:obuInfo.getStr("id"));
            etcIssuedRecord.setObuStatus(ObuYGZStatusEnum.getOldStatusByBusinessType(businessType));
            etcIssuedRecord.setBusinessType(businessType);
            etcIssuedRecord.setReason(reason);
            etcIssuedRecord.setOpTime(currentDate);
            etcIssuedRecord.setCreateTime(currentDate);
            etcIssuedRecord.setUpdateTime(currentDate);

            //存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {

                if (obuInfo != null) {
                    if (obuStatus != ObuYGZStatusEnum.OLD_CANCEL_WITH_SIGN.getValue() &&
                            obuStatus != ObuYGZStatusEnum.OLD_CANCEL_WITHOUT_SIGN.getValue()) {
                        if (!Db.update(TABLE_OBUINFO, obuInfo)) {
                            logger.error("{}更新TABLE_OBUINFO表失败", serverName);
                            return false;
                        }
                        if (!Db.save(TABLE_OBUINFO_HISTORY, ids, obuInfo)) {
                            logger.error("{}更新TABLE_OBUINFO_HISTORY表失败", serverName);
                            return false;
                        }
                    }

                    if (obuBlacklistRecord.get("issuerId") != null) {
                        if (!Db.save(TABLE_OBUBLACKLIST, obuBlacklistRecord)) {
                            logger.error("{}插入TABLE_OBUBLACKLIST表失败", serverName);
                            return false;
                        }
                    }
                }


                if (cardStatus != CardYGZStatusEnum.CANCEL_WITH_CARD.getValue() &&
                        cardStatus != CardYGZStatusEnum.CANCEL_WITHOUT_CARD.getValue()) {
                    if (!Db.update(TABLE_CARDINFO, cardInfo)) {
                        logger.error("{}更新TABLE_OBUBLACKLIST表失败", serverName);
                        return false;
                    }
                    if (!Db.save(TABLE_CARDINFO_HISTORY, ids, cardInfo)) {
                        logger.error("{}插入TABLE_CARDINFO_HISTORY表失败", serverName);
                        return false;
                    }
                }

                if (cardBlacklistRecord.get("issuerId") != null) {
                    if (!Db.save(TABLE_CARDBLACKLIST, cardBlacklistRecord)) {
                        logger.error("{}插入TABLE_CARDBLACKLIST表失败", serverName);
                        return false;
                    }
                }

                if (!etcIssuedRecord.save()) {
                    logger.error("{}插入etcIssuedRecord表失败", serverName);
                    return false;
                }

                // 挂起相关车辆是需要删除车辆信息，在录入车辆信息时候可以改变所属客户信息
                Db.delete(TABLE_ETC_VEHILCEINFO, vehicleInfo);

                vehicleInfo.set("opTime", new Date());
                vehicleInfo.set("createTime", new Date());
                if (!Db.save(TABLE_VEHILCEINFO_HIS, vehicleInfo)) {
                    logger.error("{}保存车辆历史表失败", serverName);
                    return false;
                }

                //7、清空etc_ofl_vehicleInfo表的绑卡信息
                if (etcOflVehicleinfo != null) {
                    if (!etcOflVehicleinfo.delete()) {
                        logger.error("{}删除etcOflVehicleinfo失败", serverName);
                        return false;
                    }
                }

                if (picRc != null) {
                    if (!Db.save(ONLINE_PICTURE, picRc)) {
                        logger.error("{}保存ONLINE_PICTURE表数据失败", serviceName);
                        return false;
                    }
                }

                return true;
            });
            if (flag) {
                logger.info("{}挂起成功", serverName);
                return Result.success(null, "挂起成功");
            } else {
                logger.error("{}挂起失败，入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "挂起失败");
            }
        } catch (Throwable t) {
            logger.error("{}查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 存储图片信息到在线用户
     */
    private Record dataToOnlinePicture(Record vehRecord, String userId) {

        if (StringUtil.isEmpty(vehRecord.get("idPositiveImageStr"), vehRecord.get("vehPositiveImageStr"))) {
            return null;
        }
        Record record = new Record();

        // 客户编号 userId
        record.set("Id", StringUtil.getUUID());
        //1-互联网订单图片  2-线下挂起图片
        record.set("businessType", 2);
        //订单编号 或  用户编号 或 证件号
        record.set("userId", userId);
        // 车牌号
        record.set("CarNumber", vehRecord.get("plateNum"));
        // 车牌颜色
        record.set("Calcolor", vehRecord.get("plateColor"));

        //证件正面照
        record.set("ImgPositive", Base64.base64ToByteArray(vehRecord.get("idPositiveImageStr")));
        //行驶证图片
        record.set("ImgHome", Base64.base64ToByteArray(vehRecord.get("vehPositiveImageStr")));
        //入库时间
        record.set("CreateTime", new Date());

        return record;
    }

    /**
     * 检查图片的大小是否小于200K
     *
     * @param imgBase64Str
     * @return
     */
    private boolean isLess200KCheckImgSize(String imgBase64Str) {
        Integer size = StringUtil.getBase64ImgSize(imgBase64Str);
        logger.info("{}当前传入图片大小为[{}]字节", serviceName, size);
        if (size / 1024 > SysConfig.getPictureSizeLimit()) {
            return false;
        }
        return true;
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
        obuInfo.set("operation", OperationEnum.ADD.getValue());

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
        obuBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();

        //判断业务类型进行黑名单类型为进入或解除
        if (businessType == 8) {
            //8- 解挂起  为解除黑名单
            obuBlacklistRecord = Db.findFirst(DbUtil.getSql("queryEtcObuBlacklistByIdAndTypeAndStatus"),
                    obuId, ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType), BlackListStatusEnum.CREATE.getValue());
            //解除当前黑名单
            obuBlacklistRecord.set("status", BlackListStatusEnum.DELETE.getValue());

            // 发行服务机构
            sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
            // OBU黑名单生成 时间
            sedMsg.put("creationTime", DateUtil.formatDate(new Date(), "yyyy-MM-dd'T'HH:mm:ss"));
            // OBU序号编码
            sedMsg.put("OBUId", obuId);
            // 类型
            sedMsg.put("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            // 状态
            sedMsg.put("status", BlackListStatusEnum.DELETE.getValue());
        } else {
            //进入黑名单
            obuBlacklistRecord.set("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
            obuBlacklistRecord.set("OBUId", obuId);
            obuBlacklistRecord.set("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            obuBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());

            // 发行服务机构
            sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
            // OBU黑名单生成 时间
            sedMsg.put("creationTime", DateUtil.formatDate(new Date(), "yyyy-MM-dd'T'HH:mm:ss"));
            // OBU序号编码
            sedMsg.put("OBUId", obuId);
            // 类型
            sedMsg.put("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            // 状态
            sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        }
//        String json = Jackson.getJson().toJson(sedMsg);
//        String fileName = BASIC_OBUBLACKLISTUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(sedMsg, BASIC_OBUBLACKLISTUPLOAD_REQ);
        logger.info("{}上传OBU黑名单响应信息:{}", serverName, response);
        return response;
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
        cardInfo.set("operation", OperationEnum.ADD.getValue());

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
        cardBlacklistRecord.set("issuerId", CommonAttribute.ISSUER_CODE);
        Map sedMsg = new HashMap<>();

        //判断业务类型进行黑名单类型为进入或解除
        if (businessType == 8) {
            //8- 解挂起  为解除黑名单
            cardBlacklistRecord = Db.findFirst(DbUtil.getSql("queryEtcCardBlacklistByIdAndTypeAndStatus"),
                    cardId, CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType), BlackListStatusEnum.CREATE.getValue());
            //解除当前黑名单
            cardBlacklistRecord.set("status", BlackListStatusEnum.DELETE.getValue());

            // 发行服务机构
            sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
            // OBU黑名单生成 时间
            sedMsg.put("creationTime", DateUtil.formatDate(new Date(), "yyyy-MM-dd'T'HH:mm:ss"));
            // OBU序号编码
            sedMsg.put("cardId", cardId);
            // 类型
            sedMsg.put("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            // 状态
            sedMsg.put("status", BlackListStatusEnum.DELETE.getValue());
        } else {
            //进入黑名单
            cardBlacklistRecord.set("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
            cardBlacklistRecord.set("cardId", cardId);
            cardBlacklistRecord.set("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            cardBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());

            // 发行服务机构
            sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
            // OBU黑名单生成 时间
            sedMsg.put("creationTime", DateUtil.formatDate(new Date(), "yyyy-MM-dd'T'HH:mm:ss"));
            // OBU序号编码
            sedMsg.put("cardId", cardId);
            // 类型
            sedMsg.put("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
            // 状态
            sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        }
//        String json = Jackson.getJson().toJson(sedMsg);
//        String fileName = BASIC_CARDBLACKLISTUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(sedMsg, BASIC_CARDBLACKLISTUPLOAD_REQ);
        logger.info("{}上传卡黑名单响应信息:{}", serverName, response);
        return response;
    }

    /**
     * 上传车辆信息到部中心
     *
     * @param appMap
     * @return
     */
    private BaseUploadResponse uploadCenterVehInfo(Record appMap) {

        EtcVehicleinfoJson vehicleinfoJson = new EtcVehicleinfoJson();
        // 车辆编号
        vehicleinfoJson.setId(appMap.get("id"));
        // 收费车型
        vehicleinfoJson.setType(appMap.get("type"));
        // 所属客户编号
        vehicleinfoJson.setUserId(appMap.get("userId"));

        if ( SysConfig.getEncryptionFlag()) {
            //解密
            // 机动车所有人名称
            try {
                vehicleinfoJson.setOwnerName(appMap.get("ownerName")==null?null:MyAESUtil.Decrypt(appMap.get("ownerName")));
                // 机动车所有人证件号码
                vehicleinfoJson.setOwnerIdNum(appMap.get("ownerIdNum")==null?null:MyAESUtil.Decrypt(appMap.get("ownerIdNum")));
                // 所有人联系地址
                vehicleinfoJson.setAddress(appMap.get("address")==null?null:MyAESUtil.Decrypt(appMap.get("address")));
                // 所有人联系方式
                vehicleinfoJson.setOwnerTel(appMap.get("ownerTel")==null?null:MyAESUtil.Decrypt(appMap.get("ownerTel")));
                // 指定联系人姓名
                vehicleinfoJson.setContact(appMap.get("ownerTel")==null?null:MyAESUtil.Decrypt(appMap.get("contact")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 机动车所有人名称
            vehicleinfoJson.setOwnerName(appMap.get("ownerName"));
            // 机动车所有人证件号码
            vehicleinfoJson.setOwnerIdNum(appMap.get("ownerIdNum"));
            // 所有人联系地址
            vehicleinfoJson.setAddress(appMap.get("address"));
            // 所有人联系方式
            vehicleinfoJson.setOwnerTel(appMap.get("ownerTel"));
            // 指定联系人姓名
            vehicleinfoJson.setContact(appMap.get("contact"));
        }


        // 机动车所有人证件类型
        vehicleinfoJson.setOwnerIdType(appMap.get("ownerIdType"));

        // 录入方式1-线上，2-线下
        vehicleinfoJson.setRegisteredType(appMap.get("registeredType"));
        // 录入渠道编号
        vehicleinfoJson.setChannelId(appMap.get("channelId"));
        // 录入时间
        vehicleinfoJson.setRegisteredTime(appMap.get("registeredTime"));
        // 行驶证品牌型号
        vehicleinfoJson.setVehicleModel(appMap.get("vehicleModel"));
        // 车辆识别代号
        vehicleinfoJson.setVin(appMap.get("vin"));
        // 车辆发动机号 engineNum
        vehicleinfoJson.setEngineNum(appMap.get("engineNum"));
        // 核定载人数
        vehicleinfoJson.setApprovedCount(appMap.get("approvedCount"));
        // 核定载质量
        vehicleinfoJson.setPermittedTowWeight(appMap.get("permittedWeight"));
        // 外廓尺寸
        vehicleinfoJson.setOutsideDimensions(getOutsideDimensions(appMap.get("outsideDimensions")));

        // 车轴数
        vehicleinfoJson.setWheelCount(appMap.get("wheelCount"));
        // 车轮数
        vehicleinfoJson.setAxleCount(appMap.get("axleCount"));

        // 轴距
        vehicleinfoJson.setAxleDistance(appMap.get("axleDistance"));
        // 轴型
        vehicleinfoJson.setAxisType(appMap.get("axisType"));

        // 操作
        vehicleinfoJson.setOperation(OperationEnum.DELETE.getValue());


//        String json = Jackson.getJson().toJson(vehicleinfoJson);
//        String fileName = BASIC_VEHICLEUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(vehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);
        logger.info("{}上传车辆响应信息:{}", serviceName, response);
        return response;

    }

    /**
     * 获取车辆轮廓
     *
     * @param dimension
     * @return
     */
    private String getOutsideDimensions(String dimension) {
        return StringUtil.isEmpty(dimension) ? "" :
                dimension.replace("*", "X")
                        .replace("m", "")
                        .replace("x", "X")
                        .replace("×", "X");
    }

    /**
     * @return
     */
    private Result callOffineOpenUser(Record dataRc, String userId) {
        Result result = new Result();
        //客户类型嗯嗯。
        Integer userType = dataRc.get("userType");
        if (userType.equals(CUSTOMER_TYPE_COMPANY)) {
            // 调用2.2 线下渠道单位用户开户
            result = offineOpenCorpUser1(dataRc);
        } else {
            // 调用2.1 线下渠道个人用户开户
            result = offineOpenPersonalUser1(dataRc);
        }
        if (!result.getSuccess()) {
            return result;
        }
        //  获取返回的相关数据
        Map tokenMap = (Map) result.getData();
        // 调用3.2获取账户信息
        Result accountResult = getUserAccount(tokenMap);
        if (!accountResult.getSuccess()) {
            logger.error("{}[userId={}]下线监管平台查询账户信息失败{}", serviceName, userId, accountResult);
            return accountResult;
        }
        Map dataMap = getAccountId(accountResult, dataRc);
        if (dataMap == null) {
            return  new Result(788,"未取得部中心的客户Id");
        }
        tokenMap.put("accountId", dataMap.get("accountId"));
        //将accessToken、openId、expiresIn设置到encryptedData中
//        dataMap.put("accessToken", tokenMap.get("accessToken"));
//        dataMap.put("openId", tokenMap.get("openId"));
//        dataMap.put("expiresIn", tokenMap.get("expiresIn"));

        return result;
    }

    /**
     * 获取用户的accountId
     *
     * @param accountResult
     * @param record
     * @return
     */
    private Map getAccountId(Result accountResult, Record record) {
        Map daMap = (Map) accountResult.getData();
        List<Map> accountList = (List) daMap.get("encryptedData");
        int userType = record.get("userType");
        String department = record.get("department");

        Map dataMap = null;
        //查询匹配的账户
        //判断是否是单位用户 单位用户需要根据部门判断
        if (userType == CUSTOMER_TYPE_COMPANY) {
//            dataMap = accountList.get(0);
            for (Map accountMap : accountList) {
                int accountType = MathUtil.asInteger(accountMap.get("accountType"));
                if (userType == accountType && department.equals(accountMap.get("department").toString())) {
                    dataMap = accountMap;
                }
            }
        } else {
            for (Map accountMap : accountList) {
                int accountType = MathUtil.asInteger(accountMap.get("accountType"));
                if (userType == accountType) {
                    dataMap = accountMap;
                }
            }
        }
        return dataMap;
    }

    /*
     * 线下渠道个人用户开户
     */
    private Result offineOpenPersonalUser1(Record record) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);
        outMap.put("data", null);
        Result result = new Result();
        if (SysConfig.getEncryptionFlag()) {
            try {
                result = certifyCreituserService.entry(Kv.by("id", MyAESUtil.Decrypt( record.get("userIdNum")))
                        .set("name",  MyAESUtil.Decrypt( record.get("userName")))
                        .set("userIdType",record.get("userIdType"))
                        .set("positiveImageStr",null)
                        .set("negativeImageStr",null)
                        .set("phone",MyAESUtil.Decrypt( record.get("tel")))
                        .set("address", MyAESUtil.Decrypt( record.get("address")))
                        .set("registeredType",record.get("registeredType"))
                        .set("issueChannelId", record.get("channelId")));
            } catch (Exception e) {
                e.printStackTrace();
                return Result.bizError(799, "单位客户加密失败");
            }
        } else {
            result = certifyCreituserService.entry(Kv.by("id", record.get("userIdNum"))
                    .set("name", record.get("userName"))
                    .set("userIdType",record.get("userIdType"))
                    .set("positiveImageStr",null)
                    .set("negativeImageStr",null)
                    .set("phone",record.get("tel"))
                    .set("address", record.get("address"))
                    .set("registeredType",record.get("registeredType"))
                    .set("issueChannelId", record.get("channelId")));
        }


        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下渠道个人用户开户失败:{}", serviceName, result);
            result.setSuccess(false);
        }
        return result;
    }


    /**
     * 线下渠道单位用户开户
     */
    private Result offineOpenCorpUser1(Record record) {

        Result result = new Result();
        if (SysConfig.getEncryptionFlag()) {
            try {
                result = certifyCreitcorpService.entry(Kv.by("id",MyAESUtil.Decrypt( record.get("userIdNum")))
                        .set("name", MyAESUtil.Decrypt( record.get("userName")))
                        .set("corpIdType",record.get("userIdType"))
                        .set("positiveImageStr",null)
                        .set("negativeImageStr",null)
                        .set("phone",MyAESUtil.Decrypt( record.get("tel")))
                        .set("address",MyAESUtil.Decrypt( record.get("address")))
                        .set("registeredType",record.get("registeredType"))
                        .set("issueChannelId", record.get("channelId"))
                        .set("department", record.get("department"))
                        .set("agentName", MyAESUtil.Decrypt( record.get("agentName")))
                        .set("agentIdType", record.get("agentIdType"))
                        .set("agentIdNum",MyAESUtil.Decrypt( record.get("agentIdNum")))
                        .set("bank", record.get("bank"))
                        .set("bankAddr", record.get("bankAddr"))
                        .set("bankAccount",MyAESUtil.Decrypt( record.get("bankAccount")))
                        .set("taxpayerCode", record.get("taxpayerCode"))
                );
            } catch (Exception e) {
                e.printStackTrace();
                return Result.bizError(799, "单位客户加密失败");
            }
        } else {
            result = certifyCreitcorpService.entry(Kv.by("id", record.get("userIdNum"))
                    .set("name", record.get("userName"))
                    .set("corpIdType",record.get("userIdType"))
                    .set("positiveImageStr",null)
                    .set("negativeImageStr",null)
                    .set("phone",record.get("tel"))
                    .set("address", record.get("address"))
                    .set("registeredType",record.get("registeredType"))
                    .set("issueChannelId", record.get("channelId"))
                    .set("department", record.get("department"))
                    .set("agentName", record.get("agentName"))
                    .set("agentIdType", record.get("agentIdType"))
                    .set("agentIdNum", record.get("agentIdNum"))
                    .set("bank", record.get("bank"))
                    .set("bankAddr", record.get("bankAddr"))
                    .set("bankAccount", record.get("bankAccount"))
                    .set("taxpayerCode", record.get("taxpayerCode"))
            );
        }


        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下渠道单位用户开户失败:{}", serviceName, result);
            result.setSuccess(false);
        }
        return result;

    }

    /**
     * 存储etc_ofl_userinfo
     */
    private Record dataToOflUser(Record appMap, Map tokenMap, String userId) {
        Record rc = new Record();

        if (tokenMap == null) {
            return null;
        }
//        if (SysConfig.getEncryptionFlag()) {
//            //加密
//
//            try {
//                // 用户证件号
//                rc.set("userIdNum", MyAESUtil.Encrypt( appMap.get("userIdNum")));
//                // 用户名称
//                rc.set("userName", MyAESUtil.Encrypt( appMap.get("userName")));
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//
//        } else {
//            // 用户证件号
//            rc.set("userIdNum", appMap.get("userIdNum"));
//            // 用户名称
//            rc.set("userName", appMap.get("userName"));
//        }

        // 用户证件号
        rc.set("userIdNum", appMap.get("userIdNum"));
        // 用户名称
        rc.set("userName", appMap.get("userName"));
        // 客户编号
        rc.set("userId", userId);
        // 部发行认证及监管系统用户编号
        rc.set("openId", tokenMap.get("openId"));
        // 接口调用凭证
        rc.set("accessToken", tokenMap.get("accessToken"));
        // 接口调用凭证超时时间
        rc.set("expiresIn", tokenMap.get("expiresIn"));
        // 线下渠道部中心userId
        rc.set("depUserId", tokenMap.get("accountId"));

        // 用户证件类型
        rc.set("userIdType", appMap.get("userIdType"));

        // 网点编号
        rc.set("posId", appMap.get("orgId"));
        // 信息录入人工号
        rc.set("operatorId", appMap.get("operatorId"));
        // 操作时间
        rc.set("opTime", new Date());
        // 创建时间
        rc.set("createTime", new Date());
        // 更新时间
        rc.set("updateTime", new Date());
        return rc;
    }

    /**
     * 获取账户信息
     */
    private Result getUserAccount(Map map) {
        Result result = userAccountUploadService.entry(Kv.by("accessToken", map.get("accessToken"))
                .set("openId", map.get("openId")));
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用[openId={}]线下监管平台获取账户信息失败:{}", serviceName, map.get("openId"), result);
        }
        return result;
    }

}

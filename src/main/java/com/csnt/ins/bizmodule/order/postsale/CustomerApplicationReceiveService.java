package com.csnt.ins.bizmodule.order.postsale;


import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardStatusUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuStatusUploadService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcIssuedRecord;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.issuer.OnlineOrders;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.json.Jackson;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 恒通邮寄信息接收接口
 **/
public class CustomerApplicationReceiveService implements IReceiveService, BaseUploadService {
    Logger logger = LoggerFactory.getLogger(CustomerApplicationReceiveService.class);

    private String serviceName = "[8933客服申请订单接收]";

    private final String MSG_NORMAL = "无法办理此项业务";
    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_OBUBLACKLISTUPLOAD_REQ = "BASIC_OBUBLACKLISTUPLOAD_REQ_";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";
    private final String BASIC_CARDBLACKLISTUPLOAD_REQ = "BASIC_CARDBLACKLISTUPLOAD_REQ_";
    private final String REPEAT_MSG = "重复";
    private final String MSG_NORMAL1 = "未查询当前账户下的该";

    private final String TABLE_CUSTOMER_APPLICATION = "etc_customer_application_info";
    private final String TABLE_CARDINFO = "etc_cardinfo";
    private final String TABLE_CARDINFO_HISTORY = "etc_cardinfo_history";
    private final String TABLE_CARDBLACKLIST = "etc_cardblacklist";
    private final String TABLE_OBUINFO = "etc_obuinfo";
    private final String TABLE_OBUINFO_HISTORY = "etc_obuinfo_history";
    private final String TABLE_OBUBLACKLIST = "etc_obublacklist";
    private final String CARDTYPE_DEP = "2";

    /**
     * OBU状态变更通知
     */
    UserObuStatusUploadService userObuStatusUploadService = new UserObuStatusUploadService();
    /**
     * 卡状态变更通知
     */
    UserCardStatusUploadService userCardStatusUploadService = new UserCardStatusUploadService();
    /**
     * 获取上传对象
     */
    public IUpload upload = CsntUpload.getInstance();

    @Override
    public Result entry(Map dataMap) {
        try {
            Record appRc = new Record().setColumns(dataMap);
            // 接收信息检查
            Map inCheckMap = checkInput(appRc);
            if (!(boolean) inCheckMap.get("bool")) {
                return (Result) inCheckMap.get("result");
            }
            // 先保存客服申请单信息
            Map saveMap = saveCustomerApp(appRc);
            boolean saveBl = (boolean) saveMap.get("bool");
            if (!saveBl) {
                return (Result) saveMap.get("result");
            }

            // 服务类型
            Map obuRetMap = new HashMap<>();
            Integer serviceType = appRc.get("serviceType");
            if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_OBU.getValue())
                    || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL.getValue())
                    || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_OBU.getValue())) {
                // 换签操作
                obuRetMap = obuStatusChange(appRc);
                boolean obuBl = (boolean) obuRetMap.get("bool");
                if (!obuBl) {
                    return (Result) obuRetMap.get("result");
                }
            }

            // 卡信息转换
            Map cardRetMap = new HashMap<>();
            if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_CARD.getValue())
                    || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL.getValue())
                    || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_CARD.getValue())) {
                // 换卡操作
                cardRetMap = cardStatusChange(appRc);
                boolean obuBl = (boolean) cardRetMap.get("bool");
                if (!obuBl) {
                    return (Result) cardRetMap.get("result");
                }
            }


            // 生成新的订单信息
            OnlineOrders onlineOrders = dataToOnlineOrders(appRc);
            //存储数据
            String ids = "id,opTime";
            final Map finalObuRetMap = obuRetMap;
            final Map finalCardRetMap = cardRetMap;
            boolean flag = Db.tx(() -> {
                if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_OBU.getValue())
                        || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL.getValue())
                        || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_OBU.getValue())) {

                    Record obuInfo = (Record) finalObuRetMap.get("obuinfo");
                    Record obuBlacklistRecord = (Record) finalObuRetMap.get("obublacklist");
                    EtcIssuedRecord etcIssuedRecord = (EtcIssuedRecord) finalObuRetMap.get("etcissued");

                    if (!Db.update(TABLE_OBUINFO, obuInfo)) {
                        logger.error("{}更新TABLE_OBUINFO表失败", serviceName);
                        return false;
                    }

                    obuInfo.set("createTime", new Date());
                    obuInfo.set("opTime", new Date());
                    if (!Db.save(TABLE_OBUINFO_HISTORY, ids, obuInfo)) {
                        logger.error("{}插入TABLE_OBUINFO_HISTORY表失败", serviceName);
                        return false;
                    }

                    if (!Db.save(TABLE_OBUBLACKLIST, obuBlacklistRecord)) {
                        logger.error("{}插入TABLE_OBUBLACKLIST表失败", serviceName);
                        return false;
                    }
                    if (!etcIssuedRecord.save()) {
                        logger.error("{}插入etcIssuedRecord表失败", serviceName);
                        return false;
                    }
                }
                // 卡信息更新
                if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_CARD.getValue())
                        || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL.getValue())
                        || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_CARD.getValue())) {

                    Record cardInfo = (Record) finalCardRetMap.get("cardinfo");
                    Record cardBlacklistRecord = (Record) finalCardRetMap.get("cardblacklist");
                    EtcIssuedRecord etcIssuedRecord = (EtcIssuedRecord) finalCardRetMap.get("etcissued");

                    if (!Db.update(TABLE_CARDINFO, cardInfo)) {
                        logger.error("{}更新TABLE_CARDINFO表失败", serviceName);
                        return false;
                    }

                    cardInfo.set("createTime", new Date());
                    cardInfo.set("createTime", new Date());
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
                }

                // 保存订单信息
                if (!onlineOrders.save()) {
                    logger.error("{}插入onlineOrders表失败，id={}", serviceName, onlineOrders.getOrderId());
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}客服申请订单成功", serviceName);
                return Result.success(null, "客服申请订单成功");
            } else {
                logger.error("{}客服申请订单失败,入库失败", serviceName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "卡状态变更失败");
            }

        } catch (ClassCastException c) {
            logger.error("{}参数类型异常:{}", serviceName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Throwable e) {
            logger.error("{}客服申请订单接收异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    private Map saveCustomerApp(Record appRc) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        Record custAppRc = new Record();
        // 申请单编号
        custAppRc.set("orderId", appRc.get("orderId"));
        //订单生成时间
        custAppRc.set("appCreateTime", DateUtil.parseDate(appRc.get("createTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 车辆营改增编号
        custAppRc.set("vehicleCode", appRc.get("vehicleCode"));
        // 用户证件类型
        custAppRc.set("userIdType", appRc.get("userIdType"));
        // 用户证件号码
        custAppRc.set("userIdNum", appRc.get("userIdNumJm"));

        try {
            // 账户名称
            custAppRc.set("accountName", appRc.get("accountName")==null?null:MyAESUtil.Encrypt( appRc.getStr("accountName")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 账户手机号
        custAppRc.set("mobile", appRc.get("mobileJm"));
        // 用户卡编码
        custAppRc.set("cardId", appRc.get("cardId"));
        // OBU编号
        custAppRc.set("obuId", appRc.get("obuId"));
        // 老的订单编号
        custAppRc.set("oldOrderId", appRc.get("oldOrderId"));
        // 服务类型
        custAppRc.set("serviceType", appRc.get("serviceType"));
        // 渠道类型
        custAppRc.set("channelType", appRc.get("channelType"));
        // 变更原因
        custAppRc.set("reason", appRc.get("reason"));

        custAppRc.set("createTime", new Date());
        custAppRc.set("updateTime", new Date());

        boolean flag = false;
        flag = Db.tx(() -> {
            Db.delete(TABLE_CUSTOMER_APPLICATION, "orderId", custAppRc);

            if (!Db.save(TABLE_CUSTOMER_APPLICATION, "orderId", custAppRc)) {
                logger.error("{}保存TABLE_CUSTOMER_APPLICATION表失败", serviceName);
                return false;
            }
            return true;
        });
        if (flag) {
            logger.info("{}[orderId={}]订单接收卡信息成功", serviceName, appRc.get("orderId"));
        } else {
            logger.error("{}[orderId={}]数据库入库失败", serviceName, appRc.get("orderId"));
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR));
            return outMap;
        }

        return outMap;
    }

    /**
     * 读取老订单信息，生成新订单
     *
     * @param appRc
     * @return
     */
    private OnlineOrders dataToOnlineOrders(Record appRc) {

        // 老的订单编号
        String oldOrderId = appRc.get("oldOrderId");
        OnlineOrders onlineOrders = OnlineOrders.dao.findById(oldOrderId);
        // 新的订单编号
        onlineOrders.setOrderId(appRc.get("orderId"));
        onlineOrders.setObuId(null);
        onlineOrders.setCardId(null);
        onlineOrders.setOrderCreateTime(DateUtil.parseDate(appRc.get("createTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        onlineOrders.setDelivery(null);
        onlineOrders.setExpressType(null);
        onlineOrders.setExpressId(null);
        //****************************************补办业务需确认*************************************************
        Integer serviceType = appRc.get("serviceType");

        if (serviceType.equals(OfflineBusinessTypeEnum.REISSUE_OBU.getValue())) {
            onlineOrders.setOrderType(OfflineBusinessTypeEnum.CANCEL_OBU.getValue());
        } else if (serviceType.equals(OfflineBusinessTypeEnum.REISSUE_CARD.getValue())) {
            onlineOrders.setOrderType(OfflineBusinessTypeEnum.CHANGE_CARD.getValue());
        } else {
            onlineOrders.setOrderType(serviceType);
        }


        onlineOrders.setStatus(1);
        onlineOrders.setPostStatus(1);
        onlineOrders.setCreateTime(new Date());
        onlineOrders.setUpdateTime(new Date());
        return onlineOrders;
    }

    private Map checkInput(Record inMap) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 申请单编号
        String orderId = inMap.get("orderId");
        // 订单生成时间
        String createTime = inMap.get("createTime");
        // 车辆营改增编号
        String vehicleCode = inMap.get("vehicleCode");
        // 用户证件类型
        Integer userIdType = inMap.get("userIdType");
        // 用户证件号码
        String userIdNum = inMap.get("userIdNum");
        // 账户手机号
        String mobile = inMap.get("mobile");

        if (StringUtil.isEmpty(orderId, createTime, vehicleCode, userIdType, userIdNum, mobile)) {
            logger.error("{}参数orderId, createTime, vehicleCode, userIdType, userIdNum, mobile不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("orderId, createTime, vehicleCode, userIdType, userIdNum, mobile"));
            outMap.put("bool", false);
            return outMap;
        }
        String userIdNumJm = new String(userIdNum);
        String mobileJm = new String(mobile);

        if (SysConfig.getEncryptionFlag()) {
            try {
                userIdNumJm = MyAESUtil.Encrypt(userIdNumJm);
                inMap.set("userIdNumJm",userIdNumJm);

                mobileJm = MyAESUtil.Encrypt(mobileJm);
                inMap.set("mobileJm",mobileJm);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error("{}参数obuId不能为空", serviceName);
                outMap.put("result",  Result.bizError(799, "证件号码加密失败"));
                outMap.put("bool", false);
                return outMap;
            }
        }
        // 老的订单编号
        String oldOrderId = inMap.get("oldOrderId");
        // 服务类型
        Integer serviceType = inMap.get("serviceType");
        // 渠道类型
        String channelType = inMap.get("channelType");
        // 变更原因
        String reason = inMap.get("reason");
        if (StringUtil.isEmpty(serviceType, channelType, reason)) {
            logger.error("{}参数serviceType, channelType, reason不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("serviceType, channelType, reason"));
            outMap.put("bool", false);
            return outMap;
        }
        boolean obuFlag = false;
        boolean cardFlag = false;
        String obuId = "";
        String cardId = "";
        if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_OBU.getValue())
                || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_OBU.getValue())) {
            // OBU编码
            obuId = inMap.get("obuId");
            if (StringUtil.isEmpty(obuId)) {
                logger.error("{}参数obuId不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("obuId"));
                outMap.put("bool", false);
                return outMap;
            }
            obuFlag = true;
        } else if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_CARD.getValue())
                || serviceType.equals(OfflineBusinessTypeEnum.REISSUE_CARD.getValue())) {
            // 用户卡编码
            cardId = inMap.get("cardId");
            if (StringUtil.isEmpty(cardId)) {
                logger.error("{}参数cardId不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("cardId"));
                outMap.put("bool", false);
                return outMap;
            }
            cardFlag = true;
        } else if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL.getValue())) {
            // OBU编码
            obuId = inMap.get("obuId");
            if (StringUtil.isEmpty(obuId)) {
                logger.error("{}参数obuId不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("obuId"));
                outMap.put("bool", false);
                return outMap;
            }
            // 用户卡编码
            cardId = inMap.get("cardId");
            if (StringUtil.isEmpty(cardId)) {
                logger.error("{}参数cardId不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("cardId"));
                outMap.put("bool", false);
                return outMap;
            }
            obuFlag = true;
            cardFlag = true;
        } else {
            outMap.put("result", Result.bizError(605, "服务类型有误"));
            outMap.put("bool", false);
            return outMap;
        }

        // 判断该申请单是否存在（onlineorders表是否存在）
        OnlineOrders onlineOrders = OnlineOrders.dao.findById(orderId);
        if (onlineOrders != null) {
            logger.error("{}已经有该申请单的订单信息", serviceName);
            outMap.put("result", Result.bizError(605, "已经有该申请单的订单信息"));
            outMap.put("bool", false);
            return outMap;
        }
        // 判断原订单是否存在
        OnlineOrders onlineOldOrders = OnlineOrders.dao.findById(oldOrderId);
        if (onlineOldOrders == null) {
            logger.error("{}未找到原订单信息", serviceName);
            outMap.put("result", Result.bizError(605, "未找到原订单信息"));
            outMap.put("bool", false);
            return outMap;
        }

        Record userRc = Db.findFirst(DbUtil.getSql("queryCenterEtcUserByIdtypeNum"), userIdType, userIdNumJm);
        if (userRc == null) {
            logger.error("{}未找到该客户信息", serviceName);
            outMap.put("result", Result.bizError(605, "未找到该客户信息"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!onlineOldOrders.getUserId().equals(userRc.getStr("id"))) {
            logger.error("{}输入的证件信息与原订单的客户不匹配", serviceName);
            outMap.put("result", Result.bizError(605, "输入的证件信息与原订单的客户不匹配"));
            outMap.put("bool", false);
            return outMap;
        }
        //判断card,obu是否存在
        // 判断obu信息
        if (obuFlag) {
            Record obuInfo = Db.findFirst(DbUtil.getSql("queryEtcObuInfoById"), obuId);
            if (obuInfo == null) {
                logger.error("{}未找到该OBU信息", serviceName);
                outMap.put("result", Result.bizError(605, "未找到该OBU信息"));
                outMap.put("bool", false);
                return outMap;
            }
            if (!vehicleCode.equals(obuInfo.getStr("vehicleId"))) {
                logger.error("{}该OBU与车牌不匹配", serviceName);
                outMap.put("result", Result.bizError(605, "该OBU与车牌不匹配"));
                outMap.put("bool", false);
                return outMap;
            }
        }
        if (cardFlag) {
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}未找到该ETC卡信息", serviceName);
                outMap.put("result", Result.bizError(605, "未找到该ETC卡信息"));
                outMap.put("bool", false);
                return outMap;
            }
            if (!vehicleCode.equals(cardInfo.getStr("vehicleId"))) {
                logger.error("{}该ETC卡与车牌不匹配", serviceName);
                outMap.put("result", Result.bizError(605, "该ETC卡与车牌不匹配"));
                outMap.put("bool", false);
                return outMap;
            }
        }
        return outMap;
    }

    private Map cardStatusChange(Record appRc) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // ETC卡编号
        String cardId = appRc.get("cardId");
        // 车辆营改增编号
        String vehicleId = appRc.get("vehicleCode");
        // 用户证件类型
        Integer userIdType = appRc.get("userIdType");
        // 用户证件号码
        String userIdNum = appRc.get("userIdNum");
        String userIdNumJm = appRc.get("userIdNumJm");
        // 服务类型
        Integer serviceType = appRc.get("serviceType");
        // 渠道类型
        String channelType = appRc.get("channelType");
        // 变更原因
        String reason = appRc.get("reason");

        // 取卡信息
        Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
        if (cardInfo == null) {
            logger.error("{}发行系统未找到当前卡:{}", serviceName, cardId);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前卡信息"));
            return outMap;
        }
        String cardType = cardInfo.get("cardType").toString().substring(0,1);
        if (CARDTYPE_DEP.equals(cardType) && cardInfo.getInt("status") == 1) {
            // 储蓄卡不能执行换卡操作
            logger.error("{}正常储蓄卡不能走换卡操作:{}", serviceName, cardId);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "正常储蓄卡不能走换卡操作，请先注销"));
            return outMap;
        }


        int status = 0;
        if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_CARD)
                || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL)) {
            status = CardYGZStatusEnum.CANCEL_WITH_CARD.getValue();
        } else {
            status = CardYGZStatusEnum.CANCEL_WITHOUT_CARD.getValue();
        }


        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNumJm, userIdType);
        EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
        if (etcOflUserinfo != null
                && etcOflVehicleinfo != null
                && etcOflVehicleinfo.getDepVehicleId() != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", true);
                outMap.put("result", result);
                return outMap;
            }
            //调用4.5 卡状态变更通知
            result = userCardStatusUploadService.entry(Kv.by("cardId", cardId)
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId())
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("type", OfflineBusinessTypeEnum.changeBusinessType(serviceType))
                    .set("status", status));
            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !result.getMsg().contains(MSG_NORMAL1)
                    && !result.getMsg().contains(MSG_NORMAL + OfflineBusinessTypeEnum.changeBusinessType(serviceType))) {
                logger.error("{}调用卡状态变更通知接口失败:{}", serviceName, result);
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
        }

        //卡黑名单上传
        Record cardBlacklistRecord = new Record();
        BaseUploadResponse response = uploadCardBlacklist(cardId, serviceType, cardBlacklistRecord);
        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                && !response.getErrorMsg().contains(REPEAT_MSG)) {
            logger.error("{}上传黑名单信息失败:{}", serviceName, response);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg()));
            return outMap;
        }


        //设置卡信息的状态
        cardInfo.set("status", status);
        //卡信息上传及变更
        response = uploadBasicCardInfo(cardInfo);
        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}上传卡营改增信息失败:{}", serviceName, response);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg()));
            return outMap;
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

        //记录发行记录
        EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
        etcIssuedRecord.setUuid(StringUtil.getUUID());
        etcIssuedRecord.setVehicleId(cardInfo.getStr("vehicleId"));
        etcIssuedRecord.setUserId(cardInfo.get("userId"));
        etcIssuedRecord.setCardId(cardId);
        etcIssuedRecord.setCardStatus(status);
        etcIssuedRecord.setBusinessType(serviceType);
        etcIssuedRecord.setReason(reason);
        etcIssuedRecord.setOpTime(currentDate);
        etcIssuedRecord.setCreateTime(currentDate);
        etcIssuedRecord.setUpdateTime(currentDate);

        outMap.put("cardinfo", cardInfo);
        outMap.put("cardblacklist", cardBlacklistRecord);
        outMap.put("etcissued", etcIssuedRecord);
        return outMap;
    }


    /**
     * @param appRc 申请信息
     * @return
     */
    private Map obuStatusChange(Record appRc) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // OBU编码
        String obuId = appRc.get("obuId");
        // 车辆营改增编号
        String vehicleId = appRc.get("vehicleCode");
        // 用户证件类型
        Integer userIdType = appRc.get("userIdType");
        // 用户证件号码
        String userIdNum = appRc.get("userIdNum");
        String userIdNumJm = appRc.get("userIdNumJm");
        // 服务类型
        Integer serviceType = appRc.get("serviceType");
        // 渠道类型
        String channelType = appRc.get("channelType");
        // 变更原因
        String reason = appRc.get("reason");

        int status = 0;
        if (serviceType.equals(OfflineBusinessTypeEnum.CHANGE_OBU)
                || serviceType.equals(OfflineBusinessTypeEnum.CHANGE_ALL)) {
            status = ObuYGZStatusEnum.OLD_CANCEL_WITH_SIGN.getValue();
        } else {
            status = ObuYGZStatusEnum.OLD_CANCEL_WITHOUT_SIGN.getValue();
        }

        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNumJm, userIdType);
        EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
        if (etcOflUserinfo != null
                && etcOflVehicleinfo != null
                && etcOflVehicleinfo.getDepVehicleId() != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
            //调用4.4 OBU状态变更通知
            result = userObuStatusUploadService.entry(Kv.by("obuId", obuId)
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId())
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("type", OfflineBusinessTypeEnum.changeBusinessType(serviceType))
                    .set("status", ObuOflStatusEnum.getStatusByBusinessType(serviceType)));
            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !result.getMsg().contains(MSG_NORMAL1)
                    && !result.getMsg().contains(MSG_NORMAL + OfflineBusinessTypeEnum.changeBusinessType(serviceType))) {
                logger.error("{}调用OBU状态变更通知接口失败:{}", serviceName, result);
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
        }

        // 取obu信息
        Record obuInfo = Db.findFirst(DbUtil.getSql("queryEtcObuInfoById"), obuId);
        if (obuInfo == null) {
            logger.error("{}发行系统未找到当前OBU:{}", serviceName, obuId);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前OBU信息"));
            return outMap;
        }


        //OBU黑名单上传
        Record obuBlacklistRecord = new Record();
        BaseUploadResponse response = uploadObuBlacklist(obuId, serviceType, obuBlacklistRecord);
        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                && !response.getErrorMsg().contains(REPEAT_MSG)) {
            logger.error("{}上传黑名单信息失败:{}", serviceName, response);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg()));
            return outMap;
        }

        //设置obu信息的状态
        obuInfo.set("status", status);
        //OBU信息上传及变更
        response = uploadBasicObuInfo(obuInfo);

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}上传OBU营改增信息失败:{}", serviceName, response);
            outMap.put("bool", false);
            outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg()));
            return outMap;
        }


        //添加obu属性
        Date currentDate = new Date();
//        obuInfo.set("channelType", channelType);
        obuInfo.set("opTime", currentDate);
        obuInfo.set("updateTime", currentDate);

        obuBlacklistRecord.set("id", StringUtil.getUUID());
        obuBlacklistRecord.set("insertTime", currentDate);
        //上传状态 0-未上传 1-已上传
        obuBlacklistRecord.set("uploadStatus", 1);
        obuBlacklistRecord.set("uploadTime", currentDate);

        //记录发行记录
        EtcIssuedRecord etcIssuedRecord = new EtcIssuedRecord();
        etcIssuedRecord.setUuid(StringUtil.getUUID());
        etcIssuedRecord.setVehicleId(obuInfo.getStr("vehicleId"));
        etcIssuedRecord.setUserId(obuInfo.get("userId"));
        etcIssuedRecord.setObuId(obuId);
        etcIssuedRecord.setObuStatus(status);
        etcIssuedRecord.setBusinessType(serviceType);
        etcIssuedRecord.setReason(reason);
        etcIssuedRecord.setOpTime(currentDate);
        etcIssuedRecord.setCreateTime(currentDate);
        etcIssuedRecord.setUpdateTime(currentDate);

        outMap.put("obuinfo", obuInfo);
        outMap.put("obublacklist", obuBlacklistRecord);
        outMap.put("etcissued", etcIssuedRecord);
        return outMap;
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
        obuBlacklistRecord.set("creationTime", new Date());
        obuBlacklistRecord.set("OBUId", obuId);
        obuBlacklistRecord.set("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
        obuBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());
        // 状态
        sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());

        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("OBUId", obuId);
        // 类型
        sedMsg.put("type", ObuBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));

//        String json = Jackson.getJson().toJson(sedMsg);
//        String fileName = BASIC_OBUBLACKLISTUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
//        BaseUploadResponse response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        response = uploadYGZ(sedMsg, BASIC_OBUBLACKLISTUPLOAD_REQ);
        logger.info("{}上传OBU黑名单响应信息:{}", serviceName, response);
        return response;
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

        logger.info("{}上传OBU的内容为:{}", serviceName, etcObuinfoJson);
        BaseUploadResponse response = upload(etcObuinfoJson, BASIC_OBUUPLOAD_REQ);
        logger.info("{}上传OBU响应信息:{}", serviceName, response);
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
        cardBlacklistRecord.set("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        cardBlacklistRecord.set("cardId", cardId);
        cardBlacklistRecord.set("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
        cardBlacklistRecord.set("status", BlackListStatusEnum.CREATE.getValue());
        // 状态
        sedMsg.put("status", BlackListStatusEnum.CREATE.getValue());
        // 发行服务机构
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        // OBU黑名单生成 时间
        sedMsg.put("creationTime", DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        // OBU序号编码
        sedMsg.put("cardId", cardId);
        // 类型
        sedMsg.put("type", CardBlackListTypeEnum.getBlacklistTypeByBusinessType(businessType));
//        String json = Jackson.getJson().toJson(sedMsg);
//        String fileName = BASIC_CARDBLACKLISTUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
//        BaseUploadResponse response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        BaseUploadResponse response = new BaseUploadResponse();
        response = uploadYGZ(sedMsg, BASIC_CARDBLACKLISTUPLOAD_REQ);
        logger.info("{}上传卡黑名单响应信息:{}", serviceName, response);
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

        logger.info("{}上传卡的内容为:{}", serviceName, etcCardinfoJson);
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传卡响应信息:{}", serviceName, response);
        return response;
    }

}

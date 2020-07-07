package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardinfoUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardoflService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OfflineIssuerOpenUserService
 * @Description TODO
 * 该接口无需加解密
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class OfflineCardReceiveService implements IReceiveService, BaseUploadService {
    protected static Logger logger = LoggerFactory.getLogger(OfflineCardReceiveService.class);

    private String serviceName = "[8806卡信息上传及变更通知]";

    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

    private final String TABLE_ETC_CARDINFO = "etc_cardinfo";
    private final String TABLE_ETC_CARDINFO_HIS = "etc_cardinfo_history";
    private final String TABLE_ETC_ISSUED_RECORD = "etc_issued_record";
    private final String MSG_NORMAL = "或者正常";
    /**
     * 8842卡信息新增及变更通知
     */
    UserCardinfoUploadService userCardinfoUploadService = new UserCardinfoUploadService();
    /**
     * 获取卡信息
     */
    UserCardoflService userCardoflService = new UserCardoflService();


    @Override
    public Result entry(Map dataMap) {
        try {
            //检查必填参数
            // 输入数据检查
            Record dataRc = new Record().setColumns(dataMap);
            Map inCheckMap = checkInput(dataRc);
            if (!(boolean) inCheckMap.get("bool")) {
                return (Result) inCheckMap.get("result");
            }
            String vehicleId = dataRc.get("vehicleId");

            // 取对应的车辆信息
            Record vehicleIdRc = Db.findFirst(DbUtil.getSql("queryOflVehInfoByVehId"), vehicleId);

            if (vehicleIdRc == null) {
                logger.error("{}未找到卡对应的车辆信息vehid={}", serviceName, vehicleId);
                return Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "未找到卡对应的车辆信息");
            }

            // 判断是维护还是新增,修改取原Card
            Integer operation = dataRc.get("operation");
            Record cardRc = new Record();
            if (operation == OperationEnum.UPDATE.getValue()) {
                cardRc = Db.findFirst(DbUtil.getSql("queryCardInfoBycardId"), dataRc.get("cardId").toString());
                if (cardRc == null) {
                    logger.error("{}未找到原卡信息，不能维护cardid={}", serviceName, dataRc.get("cardId"));
                    return Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "未找到原卡信息，不能维护");
                }
            } else {
                //查询当前车辆是否已经发行了OBU
                Record obuRecord = Db.findFirst(DbUtil.getSql("queryOBUidByVeh"), vehicleId);
                if (obuRecord == null) {
                    logger.error("{}当前车辆还未发行OBU,请先发行OBU", serviceName);
                    return Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "当前车辆还未发行OBU,请先发行OBU");
                }
                //设置当前车辆发行的OBUId
                dataRc.set("obuId", obuRecord.getStr("id"));

                // 判断车辆信息是否绑卡
                Record vehRecord = findYgCardByVeh(vehicleId);
                if (vehRecord == null) {
                    vehRecord = Db.findFirst(DbUtil.getSql("CheckCenterCardInfoByVeh"), vehicleId);
                    if (vehRecord == null) {
                        //由于易构数据未及时同步到汇聚平台，导致状态更新不及时
//                        vehRecord = findHJPTCardByVeh(vehicleId);
                    }
                }
                if (vehRecord != null) {
                    logger.error("{}该车已经发卡", serviceName);
                    return Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "该车已经发卡");
                }

                // 判断车辆信息是否存在
                Record vehRd = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
                if (vehRd == null) {
                    return new Result(724, "未找到该车辆信息");
                }
                if(!dataRc.getStr("userId").equals(vehRd.get("userId"))) {
                    return new Result(794, "车辆信息的用户ID与上送用户ID不一致");
                }

                // 判断车辆是否绑卡
                if (vehicleIdRc.get("accountId") == null || vehicleIdRc.get("bankPost") == null
                        || vehicleIdRc.get("certsn") == null || vehicleIdRc.get("genTime") == null
                        || vehicleIdRc.get("linkMobile") == null ) {
                    logger.error("{}车辆未绑卡，请先绑定卡veh={}", serviceName, vehicleId);
                    return Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "车辆未绑卡，请先绑定卡");
                }


            }




            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            if (bl && operation != OperationEnum.UPDATE.getValue()) {
                Map offMap = callOffineCardInfo(dataRc);
                if (!(boolean) offMap.get("bool")) {
                    Result result = (Result) offMap.get("result");
                    logger.error("{}线下监管平台处理异常:{}", serviceName, result);
                    return result;
                }
            }

            // 上传营改增信息到部中心
            BaseUploadResponse response = uploadBasicCardInfo(dataRc);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传部中心数据异常:{}", serviceName, response);
                return Result.sysError("上传部中心数据异常,stateCode:" + response.getStateCode() + " errorMsg:" + response.getErrorMsg());
            }

            //输入信息转为CARd表
            Record cardRd = dataToCardInfo(cardRc, dataRc, vehicleIdRc);
            //写变更历史表
            Record issuedRc = dataToIssuerRc(dataRc);

            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                //保存至卡信息表
                if (operation == OperationEnum.UPDATE.getValue()) {
                    Db.delete(TABLE_ETC_CARDINFO, cardRd);
                }
                if (!Db.save(TABLE_ETC_CARDINFO, cardRd)) {
                    logger.error("{}保存TABLE_ETC_CARDINFO表数据失败", serviceName);
                    return false;
                }
                if (!Db.save(TABLE_ETC_CARDINFO_HIS, ids, cardRd)) {
                    logger.error("{}保存TABLE_ETC_CARDINFO_HIS表数据失败", serviceName);
                    return false;
                }
                if (issuedRc != null) {
                    if (!Db.save(TABLE_ETC_ISSUED_RECORD, issuedRc)) {
                        logger.error("{}保存TABLE_ETC_ISSUED_RECORD表数据失败", serviceName);
                        return false;
                    }
                }
                return true;
            });
            if (flag) {
                logger.info("{}卡信息接收信息成功", serviceName);
                return Result.success(null);
            } else {
                logger.error("{}数据库入库失败", serviceName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }

        } catch (Exception e) {
            logger.error("{}数据库入库异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    private Record findHJPTCardByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTCardInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }

    private Record findYgCardByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGCardInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
    }

    /**
     *
     */
    private Record dataToIssuerRc(Record dataRc) {
        Record rc = new Record();
        Integer operation = dataRc.get("operation");
        Integer businessType = dataRc.get("businessType");
        // 维护不写变更操作历史
        if (operation == OperationEnum.UPDATE.getValue()) {
            return null;
        }
        // 新办不写操作历史，只有换卡，换卡签全套，卡补办写操作历史
        if (businessType != OfflineBusinessTypeEnum.CHANGE_CARD.getValue()
                && businessType != OfflineBusinessTypeEnum.CHANGE_ALL.getValue()
                && businessType != OfflineBusinessTypeEnum.REISSUE_CARD.getValue()) {
            return null;
        }
        // 取得相应业务的老卡操作纪律
        Record issuedRc = Db.findFirst(DbUtil.getSql("queryIssuedRcByUserAndVehAndBusinessTyp"),
                dataRc.get("vehicleId"), dataRc.get("userId"), dataRc.get("businessType"));
        Integer beforeCardStatus = null;
        String beforeCardId = null;
        if (issuedRc != null) {
            beforeCardStatus = issuedRc.get("beforeCardStatus");
            beforeCardId = issuedRc.get("beforeCardId");
        }
        // 业务主键uuid
        rc.set("uuid", DbUtil.getUUid());
        // 车辆编号
        rc.set("vehicleId", dataRc.get("vehicleId"));
        // 客户编号
        rc.set("userId", dataRc.get("userId"));
        // obu编号
        rc.set("obuId", null);
        // obu状态
        rc.set("obuStatus", null);
        // ETC卡号
        rc.set("cardId", dataRc.get("cardId"));
        // 卡状态
        rc.set("cardStatus", CardOflStatusEnum.NORMAL.getValue());
        // 业务类型
        rc.set("businessType", dataRc.get("businessType"));
        // 业务类型
        rc.set("reason", null);
        // 老签状态
        rc.set("beforeObuStatus", null);
        // 老签编号
        rc.set("beforeObuId", null);
        // 老卡状态
        rc.set("beforeCardStatus", beforeCardStatus);
        // 老卡编号
        rc.set("beforeCardId", beforeCardId);
        // 网点编号
        rc.set("posId", dataRc.get("orgId"));
        // 信息录入人工号
        rc.set("operatorId", dataRc.get("operatorId"));
        // 操作时间
        rc.set("opTime", new Date());
        // 创建时间
        rc.set("createTime", new Date());
        // 更新时间
        rc.set("updateTime", new Date());

        return rc;
    }

    /**
     * 转换cardinfo数据格式
     */
    private Record dataToCardInfo(Record cardRc, Record dataRc, Record vehicleIdRc) {
        Integer operation = dataRc.get("operation");
        if (operation == OperationEnum.ADD.getValue()) {
            // 只有新增才写入相应信息，银行相关流水
            //卡编号
            cardRc.set("id", dataRc.get("cardId"));
            // 客服合作机构编号
            cardRc.set("agencyId", dataRc.get("agencyId"));
            // 客户编号
            cardRc.set("userId", dataRc.get("userId"));
            // 车辆编号
            cardRc.set("vehicleId", dataRc.get("vehicleId"));
            // 开卡方式
            cardRc.set("issuedType", dataRc.get("issueChannelType"));
            // 开卡渠道编号
            cardRc.set("channelId", dataRc.get("issueChannelId"));
            // 用户卡状态
            cardRc.set("status", CardOflStatusEnum.NORMAL.getValue());
            // 个人或单位银行卡号或账号
            cardRc.set("accountid", vehicleIdRc.get("accountId"));
            // 银行预留手机号
            cardRc.set("linkmobile", vehicleIdRc.get("linkMobile"));
            // 银行预留手机号
            cardRc.set("linkmobile", vehicleIdRc.get("linkMobile"));
            // 银行账户名称
            cardRc.set("bankusername", vehicleIdRc.get("bankUserName"));
            // 银行卡绑定用户身份证号
            cardRc.set("certsn", vehicleIdRc.get("certsn"));
            // 网点编号
            cardRc.set("posid", vehicleIdRc.get("posId"));
            // 银行绑卡请求时间
            cardRc.set("gentime", vehicleIdRc.get("genTime") == null ? null : DateUtil.formatDate(vehicleIdRc.get("genTime"), DateUtil.FORMAT_YYYYM_MDDH_HMMSS));
            // 银行绑卡9902请求流水号
            cardRc.set("trx_serno", vehicleIdRc.get("trx_serno"));
            // 员工推荐人工号
            cardRc.set("employeeid", vehicleIdRc.get("employeeId"));
            // 银行验证9901请求流水
            cardRc.set("org_trx_serno", vehicleIdRc.get("org_trx_serno"));
            // 绑定银行账户类型,
            cardRc.set("acc_type", vehicleIdRc.get("acc_type"));
            // 银行编码,
            cardRc.set("bankPost", vehicleIdRc.get("bankPost"));
            // 用户类型,
            cardRc.set("userType", vehicleIdRc.get("userType"));
            // 绑定卡类型,
            cardRc.set("bindCardType", vehicleIdRc.get("cardType"));
            // 渠道类型,
            cardRc.set("channelType", dataRc.get("channelType"));

            // 入库时间
            cardRc.set("createTime", new Date());
            // 企业ETC业务协议号,
            cardRc.set("protocolNumber", vehicleIdRc.get("protocolNumber"));
        }
        // 卡类型
        cardRc.set("cardType", dataRc.get("cardType"));
        // 卡品牌
        cardRc.set("brand", dataRc.get("brand"));
        // 卡型号
        cardRc.set("model", dataRc.get("model"));
        // 卡启用时间
        cardRc.set("enableTime", DateUtil.parseDate(dataRc.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 卡到期时间
        cardRc.set("expireTime", DateUtil.parseDate(dataRc.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 开卡时间
        cardRc.set("issuedTime", DateUtil.parseDate(dataRc.get("issuedTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 用户卡状态变更时间
        cardRc.set("statusChangeTime", new Date());
        // 操作
        cardRc.set("operation", operation);
        // 操作时间
        cardRc.set("opTime", new Date());
        // 数据是否可上传状态
        cardRc.set("uploadStatus", 2);
        // 信息录入网点id,
        cardRc.set("orgId", dataRc.get("orgId"));
        // 信息录入人工号,
        cardRc.set("operatorId", dataRc.get("operatorId"));
        // 更新入库
        cardRc.set("updateTime", new Date());

        return cardRc;
    }

    /**
     * 线下相关接口上传部中心
     *
     * @param dataRC
     * @return
     */
    private Map callOffineCardInfo(Record dataRC) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(dataRC.get("userId"));
        // 获取车辆信息
        EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(dataRC.get("vehicleId"));
        if (etcOflUserinfo != null
                && etcOflVehicleinfo != null
                && etcOflVehicleinfo.getDepVehicleId() != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), outMap.get("result"));
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
            String vehicleId = dataRC.get("vehicleId");
            String[] sp = vehicleId.split("_");
            if (sp.length != 2) {
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "车牌异常"));
                logger.error("{}[vehicleId={}]车牌异常:", serviceName, vehicleId);
                return outMap;
            }
            // 调用4.3卡信息新增及变更接口
            result = userCardinfoUploadService.entry(Kv.by("cardId", dataRC.get("cardId"))
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId())
                    .set("accountId", etcOflUserinfo.getDepUserId())
//                    .set("type", dataRC.get("businessType"))
                    .set("type",1)
                    .set("issuerId", CommonAttribute.ISSUER_CODE)
                    .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                    .set("cardType", dataRC.get("cardType"))
                    .set("brand", dataRC.get("brand"))
                    .set("model", dataRC.get("model"))
                    .set("plateNum", sp[0])
                    .set("plateColor", Integer.parseInt(sp[1]))
                    .set("enableTime", dataRC.get("enableTime"))
                    .set("expireTime", dataRC.get("expireTime"))
                    .set("issueChannelType", dataRC.get("issueChannelType"))
                    .set("issueChannelId", dataRC.get("issueChannelId")));

            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}调用[vehicleId={},cardId={}]卡信息新增及变更通知失败:{}",
                        serviceName, vehicleId, dataRC.get("cardId"), result);
                outMap.put("result", result);
                //调用查询接口判断是否重复上传
                result = userCardoflService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("obuId", dataRC.getStr("obuId")));
                if (!checkUserCardoflService(result, dataRC.get("cardId"))) {
                    logger.error("{}调用[vehicleId={},cardId={}]卡信息查询失败:{}",
                            serviceName, vehicleId, dataRC.get("cardId"), result);
                    outMap.put("bool", false);
                }

                return outMap;
            }
        }


        return outMap;
    }

    /**
     * 检查卡查询接口的结果
     *
     * @param result
     * @return
     */
    private boolean checkUserCardoflService(Result result, String cardId) {
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            return false;
        } else {
            Map<String, Object> map = (Map<String, Object>) result.getData();
            logger.info("{}查询当前车辆的卡信息为:{}", serviceName, map);
            //1- 存在 2- 不存在
            if (map != null && 1 == MathUtil.asInteger(map.get("result"))) {
                List<Map> encryptedData = (List<Map>) map.get("encryptedData");
                for (Map data : encryptedData) {
                    String id = (String) data.get("id");
                    if (cardId.equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Map checkInput(Record inMap) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        //业务类型
        Integer businessType = inMap.get("businessType");
        // 卡类型
        Integer cardType = inMap.get("cardType");
        // 用户卡编号
        String cardId = inMap.get("cardId");
        // 卡品牌
        Integer brand = inMap.get("brand");
        // 卡型号
        String model = inMap.get("model");
        // 车辆编号
        String vehicleId = inMap.get("vehicleId");
        // 客户编码
        String userId = inMap.get("userId");
        if (StringUtil.isEmpty(businessType, cardType, cardId, brand, model, vehicleId, userId)) {
            logger.error("{}参数businessType, cardType, cardId, brand, model, vehicleId,userId不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("businessType, cardType, cardId, brand, model, vehicleId,userId"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!SysConfig.CONNECT.get("gather.id").equals(cardId.substring(0,2))) {
            logger.error("{}参数cardId错误：{}", serviceName,cardId);
            outMap.put("result", Result.bizError(898,"卡号不属于该发行方"));
            outMap.put("bool", false);
            return outMap;
        }

        //卡启用时间
        String enableTime = inMap.get("enableTime");
        //卡到期时间
        String expireTime = inMap.get("expireTime");
        //业务办理方式
        Integer issueChannelType = inMap.get("issueChannelType");
        //业务办理渠道编号
        String issueChannelId = inMap.get("issueChannelId");
        //客服合作机构编号
        String agencyId = inMap.get("agencyId");
        //开卡时间
        String issuedTime = inMap.get("issuedTime");
        //渠道类型
        String channelType = inMap.get("channelType");
        //操作
        Integer operation = inMap.get("operation");
        if (StringUtil.isEmpty(enableTime, expireTime, issueChannelType, issueChannelId, agencyId, issuedTime, channelType, operation)) {
            logger.error("{}参数enableTime, expireTime, issueChannelType,issueChannelId,agencyId,issuedTime,channelType,operation不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("enableTime, expireTime, issueChannelType,issueChannelId,agencyId,issuedTime,channelType,operation"));
            outMap.put("bool", false);
            return outMap;
        }

        if (operation != OperationEnum.ADD.getValue() && operation != OperationEnum.UPDATE.getValue()) {
            logger.error("{}参数操作类型值有误{}", serviceName, operation);
            outMap.put("result", Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "操作类型有误，只能新增修改"));
            outMap.put("bool", false);
            return outMap;
        }

        return outMap;
    }

    /**
     * 上传用户卡信息到部中心
     *
     * @param dataRc
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record dataRc) {

        EtcCardinfoJson etcCardinfoJson = new EtcCardinfoJson();
        etcCardinfoJson.setId(dataRc.get("cardId"));
        etcCardinfoJson.setCardType(dataRc.get("cardType"));
        etcCardinfoJson.setBrand(dataRc.get("brand"));
        etcCardinfoJson.setModel(dataRc.get("model"));
        etcCardinfoJson.setAgencyId(dataRc.get("agencyId"));
        etcCardinfoJson.setUserId(dataRc.get("userId"));
        etcCardinfoJson.setVehicleId(dataRc.get("vehicleId"));
        etcCardinfoJson.setEnableTime(dataRc.get("enableTime"));
        etcCardinfoJson.setExpireTime(dataRc.get("expireTime"));
        etcCardinfoJson.setIssuedType(dataRc.get("issueChannelType"));
        etcCardinfoJson.setChannelId(dataRc.get("issueChannelId"));
        etcCardinfoJson.setIssuedTime(dataRc.get("issuedTime"));
        etcCardinfoJson.setStatus(CardYGZStatusEnum.NORMAL.getValue());
        etcCardinfoJson.setStatusChangeTime(DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        etcCardinfoJson.setOperation(OperationEnum.ADD.getValue());
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传用户卡响应信息:{}", serviceName, response);
        return response;
    }

}

package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuinfoUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuoflService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcObuinfoJson;
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
 * @ClassName OfflineObuReceiveService
 * @Description TODO
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class OfflineObuReceiveService implements IReceiveService, BaseUploadService {
    protected static Logger logger = LoggerFactory.getLogger(OfflineObuReceiveService.class);

    private String serviceName = "[8808OBU信息上传及变更通知接口]";

    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";

    private final String TABLE_ETC_OBUINFO = "etc_obuinfo";
    private final String TABLE_ETC_OBUINFO_HIS = "etc_obuinfo_history";
    private final String TABLE_ETC_ISSUED_RECORD = "etc_issued_record";
    private final String MSG_NORMAL = "车辆存在非核销挂起的OBU";

    /**
     * 8842卡信息新增及变更通知
     */
    UserObuinfoUploadService userObuinfoUploadService = new UserObuinfoUploadService();
    /**
     * 获取OBU信息
     */
    UserObuoflService userObuoflService = new UserObuoflService();


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

            // 判断是维护还是新增,修改取原Card
            Integer operation = dataRc.get("operation");
            Record obuRc = new Record();
            if (operation == OperationEnum.UPDATE.getValue()) {
                obuRc = Db.findFirst(DbUtil.getSql("queryObuInfoByObuId"), dataRc.get("obuId").toString());
                if (obuRc == null) {
                    logger.error("{}未找到原OBU信息，不能维护obuid={}", serviceName, dataRc.get("obuId"));
                    return Result.bizError(704, "未找到原OBU信息，不能维护");
                }
            } else {
                // 判断车辆信息是否绑卡
                String veh = dataRc.get("vehicleId");
//                Record vehRecord = findYgObuByVeh(veh);
//                if (vehRecord == null) {
                    Record vehRecord = Db.findFirst(DbUtil.getSql("CheckCenterObuInfoByVeh"), veh);
                    if (vehRecord == null) {
                        //由于易构数据未及时同步到汇聚平台，导致状态更新不及时
//                        vehRecord = findHJPTObuByVeh(veh);
                    }
//                }
                if (vehRecord != null) {
                    return new Result(704, "该车已经发obu");
                }

                // 判断车辆信息是否存在
                Record vehRd = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), veh);
                if (vehRd == null) {
                    return new Result(724, "未找到该车辆信息");
                }
                if(!dataRc.getStr("userId").equals(vehRd.get("userId"))) {
                    return new Result(794, "车辆信息的用户ID与上送用户ID不一致");
                }
            }



            // 获取车辆信息绑定新，如果银行未空则需要绑卡(有正常的卡，话签不需要检查)
            Record cdRc =  Db.findFirst(DbUtil.getSql("queryCardIdIsActive"), dataRc.getStr("vehicleId"));
            if (cdRc == null) {
                EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(dataRc.get("vehicleId"));
                if (etcOflVehicleinfo == null || etcOflVehicleinfo.getAccountId() == null || etcOflVehicleinfo.getBankPost() == null ||
                        etcOflVehicleinfo.getLinkMobile() == null  ) {
                    logger.error("{}该车辆未绑定银行信息={}", serviceName, dataRc.get("vehicleId"));
                    return Result.bizError(784, "该车辆未绑定银行信息");
                }
            }


            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            if (bl && operation != OperationEnum.UPDATE.getValue()) {
                Map offMap = callOffineObuInfo(dataRc);
                if (!(boolean) offMap.get("bool")) {
                    Result result = (Result) offMap.get("result");
                    logger.error("{}线下监管平台处理异常:{}", serviceName, result);
                    return result;
                }
            }

            // 上传营改增信息到部中心
            BaseUploadResponse response = uploadBasicObuInfo(dataRc);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传部中心数据异常:{}", serviceName, response);
                return Result.sysError("上传部中心数据异常,stateCode:" + response.getStateCode() + " errorMsg:" + response.getErrorMsg());
            }

            //输入信息转为Obu表
            Record obuRd = dataToObuInfo(obuRc, dataRc);
            //写变更历史表
            Record issuedRc = dataToIssuerRc(dataRc);

            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                //保存至卡信息表
                //保存至卡信息表
                if (operation == OperationEnum.UPDATE.getValue()) {
                    Db.delete(TABLE_ETC_OBUINFO, obuRd);
                }
                if (!Db.save(TABLE_ETC_OBUINFO, obuRd)) {
                    logger.error("{}保存TABLE_ETC_OBUINFO表失败", serviceName);
                    return false;
                }
                if (!Db.save(TABLE_ETC_OBUINFO_HIS, ids, obuRd)) {
                    logger.error("{}保存TABLE_ETC_OBUINFO_HIS表失败", serviceName);
                    return false;
                }
                if (issuedRc != null) {
                    if (!Db.save(TABLE_ETC_ISSUED_RECORD, issuedRc)) {
                        logger.error("{}保存TABLE_ETC_ISSUED_RECORD表失败", serviceName);
                        return false;
                    }
                }
                return true;
            });
            if (flag) {
                logger.info("{}车辆信息接收信息成功", serviceName);
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

    private Record findHJPTObuByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckHJPTObuInfoByVeh", kv);
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
    }

    private Record findYgObuByVeh(String veh) {
        Kv kv = new Kv().set("id", veh);
        SqlPara sqlPara = Db.getSqlPara("mysql.CheckYGObuInfoByVeh", kv);
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
        if (businessType != OfflineBusinessTypeEnum.CHANGE_OBU.getValue()
                && businessType != OfflineBusinessTypeEnum.CHANGE_ALL.getValue()
                && businessType != OfflineBusinessTypeEnum.REISSUE_OBU.getValue()) {
            return null;
        }
        // 取得相应业务的老卡操作纪律
        Record issuedRc = Db.findFirst(DbUtil.getSql("queryIssuedRcByUserAndVehAndBusinessTyp"),
                dataRc.get("vehicleId"), dataRc.get("userId"), dataRc.get("businessType"));
        Integer beforeObuStatus = null;
        String beforeObuId = null;
        if (issuedRc != null) {
            beforeObuStatus = issuedRc.get("beforeObuStatus");
            beforeObuId = issuedRc.get("beforeObuId");
        }
        // 业务主键uuid
        rc.set("uuid", DbUtil.getUUid());
        // 车辆编号
        rc.set("vehicleId", dataRc.get("vehicleId"));
        // 客户编号
        rc.set("userId", dataRc.get("userId"));
        // obu编号
        rc.set("obuId", dataRc.get("obuId"));
        // obu状态
        rc.set("obuStatus", ObuOflStatusEnum.NORMAL.getValue());
        // ETC卡号
        rc.set("cardId", null);
        // 卡状态
        rc.set("cardStatus", null);
        // 业务类型
        rc.set("businessType", dataRc.get("businessType"));
        // 原因
        rc.set("reason", null);
        // 老签状态
        rc.set("beforeObuStatus", beforeObuStatus);
        // 老签编号
        rc.set("beforeObuId", beforeObuId);
        // 老卡状态
        rc.set("beforeCardStatus", null);
        // 老卡编号
        rc.set("beforeCardId", null);
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
     * 转换obuinfo数据格式
     */
    private Record dataToObuInfo(Record obuRc, Record dataRc) {
        Integer operation = dataRc.get("operation");
        if (operation == OperationEnum.ADD.getValue()) {
            // 只有新增才写入相应信息
            //OBU编号
            obuRc.set("id", dataRc.get("obuId"));
            //客户编号
            obuRc.set("userId", dataRc.get("userId"));
            //车辆编号
            obuRc.set("vehicleId", dataRc.get("vehicleId"));
            //OBU 注册方式
            obuRc.set("registeredType", dataRc.get("registeredType"));
            //OBU 注册渠道编号
            obuRc.set("registeredChannelId", dataRc.get("registeredChannelId"));
            //OBU注册时间
            obuRc.set("registeredTime", DateUtil.parseDate(dataRc.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
            //OBU安装方式
            obuRc.set("installType", dataRc.get("installType"));
            //OBU安装/激活地点
            obuRc.set("installChannelId", dataRc.get("installChannelId"));
            //OBU安装/激活时间
            obuRc.set("installTime", DateUtil.parseDate(dataRc.get("installTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
            // 状态
            obuRc.set("status", ObuYGZStatusEnum.OLD_NORMAL.getValue());

            //渠道类型
            obuRc.set("channelType", dataRc.get("channelType"));


            // 入库时间
            obuRc.set("createTime", new Date());
        }
        // obu品牌
        obuRc.set("brand", dataRc.get("brand"));
        // obu型号
        obuRc.set("model", dataRc.get("model"));
        // OBU 单/双片标识
        obuRc.set("obuSign", dataRc.get("obuSign"));
        // OBU启用时间
        obuRc.set("enableTime", DateUtil.parseDate(dataRc.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // OBU到期时间
        obuRc.set("expireTime", DateUtil.parseDate(dataRc.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // OBU 状态变更时间
        obuRc.set("statusChangeTime", new Date());
        // 操作1-新增2-变更3-删除
        obuRc.set("operation", dataRc.get("operation"));
        // 是否激活
        obuRc.set("isActive", dataRc.get("activeTime") == null ? null : 1);
        // OBU 发行激活时间
        obuRc.set("activeTime", dataRc.get("activeTime"));
        // OBU 发行激活方式
        obuRc.set("activeType", dataRc.get("activeType"));
        // OBU 激活渠道
        obuRc.set("activeChannel", dataRc.get("activeChannel"));
        // 操作
        obuRc.set("operation", operation);
        // 操作时间
        obuRc.set("opTime", new Date());
        // 数据是否可上传状态
        obuRc.set("uploadStatus", 2);
        // 信息录入网点,
        obuRc.set("orgId", dataRc.get("orgId"));
        // 信息录入人工号,
        obuRc.set("operatorId", dataRc.get("operatorId"));
        // 更新入库
        obuRc.set("updateTime", new Date());

        return obuRc;
    }

    /**
     * 线下相关接口上传部中心
     *
     * @param dataRC
     * @return
     */
    private Map callOffineObuInfo(Record dataRC) {

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
                outMap.put("result", Result.bizError(704, "车牌异常"));
                logger.error("{}[vehicleId={}]车牌异常:",
                        serviceName, vehicleId);
                return outMap;
            }
            // 调用4.2obu信息新增及变更接口
            result = userObuinfoUploadService.entry(Kv.by("obuId", dataRC.get("obuId"))
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId())
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("issuerId", CommonAttribute.ISSUER_CODE)
                    .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
//                    .set("type", dataRC.get("businessType"))
                    .set("type", 1)
                    .set("brand", dataRC.get("brand"))
                    .set("model", dataRC.get("model"))
                    .set("obuSign", dataRC.get("obuSign"))
                    .set("plateNum", sp[0])
                    .set("plateColor", Integer.parseInt(sp[1]))
                    .set("enableTime", dataRC.get("enableTime"))
                    .set("expireTime", dataRC.get("expireTime"))
                    .set("issueChannelType", dataRC.get("issueChannelType"))
                    .set("issueChannelId", dataRC.get("issueChannelId"))
                    .set("activeTime", dataRC.get("activeTime"))
                    .set("activeType", dataRC.get("activeType"))
            );

            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}调用[vehicleId={},obuId={}]OBU信息新增及变更通知失败:{}",
                        serviceName, vehicleId, dataRC.get("obuId"), result);
                outMap.put("result", result);
                //调用查询接口判断是否重复上传
                result = userObuoflService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("vehicleId", etcOflVehicleinfo.getDepVehicleId()));
                if (!checkUserCardoflService(result, dataRC.get("obuId"))) {
                    logger.error("{}调用[vehicleId={},obuId={}]OBU信息查询失败:{}",
                            serviceName, vehicleId, dataRC.get("obuId"), result);
                    outMap.put("bool", false);
                }
                return outMap;
            }
        }
        return outMap;
    }

    /**
     * 检查OBU查询接口的结果
     *
     * @param result
     * @return
     */
    private boolean checkUserCardoflService(Result result, String obuId) {
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            return false;
        } else {
            Map<String, Object> map = (Map<String, Object>) result.getData();
            logger.info("{}查询当前车辆的OBU信息为:{}", serviceName, map);
            //1- 存在 2- 不存在
            if (map != null && 1 == MathUtil.asInteger(map.get("result"))) {
                List<Map> encryptedData = (List<Map>) map.get("encryptedData");
                for (Map data : encryptedData) {
                    String id = (String) data.get("obuId");
                    if (obuId.equals(id)) {
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
        // OBU 序号编码
        String obuId = inMap.get("obuId");
        // OBU 品牌
        Integer brand = inMap.get("brand");
        // OBU 型号
        String model = inMap.get("model");
        // OBU 单/双片标识
        Integer obuSign = inMap.get("obuSign");
        // 车辆编号
        String vehicleId = inMap.get("vehicleId");
        // 客户编码
        String userId = inMap.get("userId");
        if (StringUtil.isEmpty(businessType, obuId, brand, model, obuSign, vehicleId, userId)) {
            logger.error("{}参数businessType, obuId, brand, model, obuSign, vehicleId,userId不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("businessType, obuId, brand, model, obuSign, vehicleId,userId"));
            outMap.put("bool", false);
            return outMap;
        }

        if (!SysConfig.CONNECT.get("gather.id").equals(obuId.substring(0,2))) {
            logger.error("{}参数cardId错误：{}", serviceName,obuId);
            outMap.put("result", Result.bizError(898,"OBU不属于该发行方"));
            outMap.put("bool", false);
            return outMap;
        }

        //OBU 启用时间
        String enableTime = inMap.get("enableTime");
        //OBU 到期时间
        String expireTime = inMap.get("expireTime");
        //业务办理方式
        Integer issueChannelType = inMap.get("issueChannelType");
        //业务办理渠道编号
        String issueChannelId = inMap.get("issueChannelId");
        //OBU注册方式
        Integer registeredType = inMap.get("registeredType");
        //OBU注册渠道编号
        String registeredChannelId = inMap.get("registeredChannelId");
        //OBU注册时间
        String registeredTime = inMap.get("registeredTime");

        if (StringUtil.isEmpty(enableTime, expireTime, issueChannelType, issueChannelId, registeredType, registeredChannelId, registeredTime)) {
            logger.error("{}参数enableTime, expireTime, issueChannelType,issueChannelId,registeredType,registeredChannelId,registeredTime不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("enableTime, expireTime, issueChannelType,issueChannelId,registeredType,registeredChannelId,registeredTime"));
            outMap.put("bool", false);
            return outMap;
        }
        //OBU安装方式
        Integer installType = inMap.get("installType");
        //OBU安装/激活时间
        String installTime = inMap.get("installTime");
        //渠道类型
        String channelType = inMap.get("channelType");
        //操作
        Integer operation = inMap.get("operation");
        if (StringUtil.isEmpty(installType, installTime, channelType, operation)) {
            logger.error("{}参数installType, installTime, channelType,operation不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("installType, installTime, channelType,operation"));
            outMap.put("bool", false);
            return outMap;
        }

        if (operation != OperationEnum.ADD.getValue() && operation != OperationEnum.UPDATE.getValue()) {
            logger.error("{}参数操作类型值有误{}", serviceName, operation);
            outMap.put("result", Result.bizError(704, "操作类型有误，只能新增，修改"));
            outMap.put("bool", false);
            return outMap;
        }

        return outMap;
    }

    /**
     * 上传obu信息到部中心
     *
     * @param dataRc
     * @return
     */
    private BaseUploadResponse uploadBasicObuInfo(Record dataRc) {

        EtcObuinfoJson etcObuinfoJson = new EtcObuinfoJson();
        etcObuinfoJson.setId(dataRc.get("obuId"));
        etcObuinfoJson.setBrand(dataRc.get("brand"));
        etcObuinfoJson.setModel(dataRc.get("model"));
        etcObuinfoJson.setUserId(dataRc.get("userId"));
        etcObuinfoJson.setVehicleId(dataRc.get("vehicleId"));
        etcObuinfoJson.setObuSign(dataRc.get("obuSign"));
        etcObuinfoJson.setEnableTime(dataRc.get("enableTime"));
        etcObuinfoJson.setExpireTime(dataRc.get("expireTime"));
        etcObuinfoJson.setRegisteredType(dataRc.get("registeredType"));
        etcObuinfoJson.setRegisteredChannelId(dataRc.get("registeredChannelId"));
        etcObuinfoJson.setRegisteredTime(dataRc.get("registeredTime"));
        etcObuinfoJson.setInstallType(dataRc.get("installType"));
        etcObuinfoJson.setInstallChannelId(dataRc.get("installChannelId"));
        etcObuinfoJson.setInstallTime(dataRc.get("installTime"));
        etcObuinfoJson.setStatus(ObuYGZStatusEnum.OLD_NORMAL.getValue());
        etcObuinfoJson.setStatusChangeTime(DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());
        etcObuinfoJson.setOperation(OperationEnum.ADD.getValue());
        BaseUploadResponse response = upload(etcObuinfoJson, BASIC_OBUUPLOAD_REQ);
        logger.info("{}上传Obu响应信息:{}", serviceName, response);
        return response;
    }

}

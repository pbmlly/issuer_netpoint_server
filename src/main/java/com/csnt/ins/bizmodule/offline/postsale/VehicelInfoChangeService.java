package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserVehicleChangeService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 8818 车辆信息变更接口
 *
 * @author duwanjiang
 **/
public class VehicelInfoChangeService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(VehicelInfoChangeService.class);

    private final String serverName = "[8818 车辆信息变更接口]";

    /**
     * 车辆信息变更
     */
    UserVehicleChangeService userVehicleChangeService = new UserVehicleChangeService();

    private final String TABLE_VEHICLEINFO = "etc_vehicleinfo";
    private final String TABLE_VEHICLEINFO_HISTORY = "etc_vehicleinfo_history";
    private final String TABLE_OFL_VEHICLEINFO = "etc_ofl_vehicleinfo";


    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String REPEAT_MSG = "重复";

    /**
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);

            //车辆编号
            String id = record.get("id");
            //操作 2- 变更 3- 删除
            Integer operation = record.get("operation");

            String notNullParams = "id,userId,contact,registeredType,channelId,registeredTime,vin,engineNum,issueDate," +
                    "name,plateNum,registerDate,vehicleType,outsideDimensions,operation,channelType";
            if (StringUtil.isEmptyArg(record, notNullParams)) {
                logger.error("{}参数{}不能为空", serverName, notNullParams);
                return Result.paramNotNullError(notNullParams);
            }
            record.set("registerDate", record.getStr("registerDate").substring(0, 10));
            record.set("issueDate", record.getStr("issueDate").substring(0, 10));

            if (operation == OperationEnum.ADD.getValue()) {
                logger.error("{}当前接口不支持新增操作", serverName);
                return Result.sysError("当前接口不支持新增操作");
            }

            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), id);
            if (vehicleInfo == null) {
                logger.error("{}发行系统未找到当前车辆:{}", serverName, id);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前车辆");
            }

            // 检查客户是否在部中心线下渠道开户
            EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstEtcOflUserInfoByVehicleId(id);
            if (etcOflUserinfo != null) {
                //刷新用户凭证
                Result result = oflAuthTouch(etcOflUserinfo);
                //判断刷新凭证是否成功，失败则直接退出
                if (!result.getSuccess()) {
                    logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                    return result;
                }

                //检查车辆信息是否存在
                EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(id);
                if (etcOflVehicleinfo == null) {
                    logger.error("{}未查询到车辆EtcOflVehicleinfo表的开户信息", serverName);
                    return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆信息");
                }

                //判断当前车辆用户是否被修改
                String userId = etcOflVehicleinfo.getUserId();
                if (!userId.equals(record.get("userId"))) {
                    logger.error("{}当前车辆的userId被修改,oldUserId={}", serverName, userId);
                    return Result.sysError("当前车辆的userId被修改,oldUserId=" + userId);
                }
                if (etcOflVehicleinfo.getDepVehicleId() != null) {
                    if (record.get("code") == null) {
                        logger.error("{}未获取到短信验证码={}", serverName, record.get("code"));
                        return Result.bizError(704, "验证码不能为空");
                    }
                    //4.8  车辆信息变更通知
                    result = userVehicleChangeService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                            .set("openId", etcOflUserinfo.getOpenId())
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                            .set("code", record.getStr("code"))
                            .set("vin", record.getStr("vin"))
                            .set("engineNum", record.getStr("engineNum"))
                            .set("issueDate", record.getStr("issueDate"))
                            .set("name", record.getStr("name"))
                            .set("registerDate", record.getStr("registerDate"))
                            .set("fileNum", record.getStr("fileNum"))
                            .set("maintenaceMass", record.get("maintenaceMass"))
                            .set("permittedWeight", record.get("permittedWeight"))
                            .set("outsideDimensions", record.getStr("outsideDimensions"))
                            .set("permittedTowWeight", record.get("permittedTowWeight"))
                            .set("vehicleModel", record.getStr("vehicleModel"))
                            .set("testRecord", record.getStr("testRecord"))
                            .set("wheelCount", record.get("wheelCount"))
                            .set("axleDistance", record.get("axleDistance"))
                            .set("axisType", record.getStr("axisType")));
                    if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                            && !result.getMsg().contains(REPEAT_MSG)) {
                        logger.error("{}车辆信息变更通知失败:{}", serverName, result);
                        return result;
                    }
                }

            }

            //拷贝参数
            vehicleInfo.setColumns(record);


            //删除信息 先判断是否绑定有卡和OBU
            if (operation == OperationEnum.DELETE.getValue()) {
                //判断当前车辆是否绑定了卡和OBU
                long count = Db.findFirst(DbUtil.getSql("queryEtcCardinfoCountByVehicleId"), id).get("num");
                if (count > 0) {
                    logger.error("{}当前用户有绑定的卡,不能删除", serverName);
                    return Result.sysError("当前用户有绑定的卡,不能删除");
                }
                count = Db.findFirst(DbUtil.getSql("queryEtcObuinfoCountByVehicleId"), id).get("num");
                if (count > 0) {
                    logger.error("{}当前用户有绑定的OBU,不能删除", serverName);
                    return Result.sysError("当前用户有绑定的OBU,不能删除");
                }
            }


            //车辆信息上传及变更
            BaseUploadResponse response = uploadBasicVehicleInfo(vehicleInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传车辆营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }


            //添加车辆属性
            Date currentDate = new Date();
            vehicleInfo.set("updateTime", currentDate);
            vehicleInfo.set("ownerName", record.get("name"));
            vehicleInfo.remove("accountId", "userType", "linkMobile", "bankUserName", "certsn");
            vehicleInfo.remove("protocolNumber", "posId", "genTime", "trx_serno", "employeeId");
            vehicleInfo.remove("org_trx_serno", "cardType", "acc_type", "bankPost", "name", "plateNum");
            vehicleInfo.remove("code");
            // etc_ofl_vehicleInfo数据转换
//            Record oflVehRc = Db.findFirst(DbUtil.getSql("queryEtcOflVehinfoByVehicleId"),record.get("id").toString());
//            Record oflVeh = dataToOflVeh(record);

            if (SysConfig.getEncryptionFlag()) {
                // 加密存储
                vehicleInfo.set("ownerName",vehicleInfo.getStr("ownerName")==null?null:MyAESUtil.Encrypt( vehicleInfo.getStr("ownerName")));
                vehicleInfo.set("ownerIdNum",vehicleInfo.getStr("ownerIdNum")==null?null:MyAESUtil.Encrypt( vehicleInfo.getStr("ownerIdNum")));
                vehicleInfo.set("ownerTel",vehicleInfo.getStr("ownerTel")==null?null:MyAESUtil.Encrypt( vehicleInfo.getStr("ownerTel")));
                vehicleInfo.set("address",vehicleInfo.getStr("address")==null?null:MyAESUtil.Encrypt( vehicleInfo.getStr("address")));
                vehicleInfo.set("contact",vehicleInfo.getStr("contact")==null?null:MyAESUtil.Encrypt( vehicleInfo.getStr("contact")));

            }

            //存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                Db.update(TABLE_VEHICLEINFO, vehicleInfo);

                vehicleInfo.set("createTime", currentDate);
                vehicleInfo.set("opTime", new Date());
                Db.save(TABLE_VEHICLEINFO_HISTORY, ids, vehicleInfo);
//                Db.update(TABLE_OFL_VEHICLEINFO,"vehicleId", oflVeh);

                return true;
            });
            if (flag) {
                logger.info("{}车辆信息变更成功", serverName);
                return Result.success(null, "车辆信息变更成功");
            } else {
                logger.error("{}车辆信息变更失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "车辆信息变更失败");
            }
        } catch (Throwable t) {
            logger.error("{}车辆信息变更异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     *
     */
    private Record dataToOflVeh(Record dataRc) {
        Record vehRc = new Record();

        // 车辆编号
        vehRc.set("vehicleId", dataRc.get("id"));
        // 银行卡号或账号
        vehRc.set("accountId", dataRc.get("accountId"));
        // 银行预留手机号
        vehRc.set("linkMobile", dataRc.get("linkMobile"));
        // 银行账户名称
        vehRc.set("bankUserName", dataRc.get("bankUserName"));
        // 银行卡绑定用户身份证号
        vehRc.set("certsn", dataRc.get("certsn"));
        // 企业用户ETC 业务协议号
        vehRc.set("protocolNumber", dataRc.get("protocolNumber"));
        // 网点编号
        vehRc.set("posId", dataRc.get("posId"));
        // 银行绑卡请求时间
        vehRc.set("genTime", dataRc.get("genTime"));
        // 银行绑卡校验请求流水号
        vehRc.set("trx_serno", dataRc.get("trx_serno"));
        // 员工推荐人工号
        vehRc.set("employeeId", dataRc.get("employeeId"));
        // 原请求流水
        vehRc.set("org_trx_serno", dataRc.get("org_trx_serno"));
        // 绑定银行账户类型
        vehRc.set("acc_type", dataRc.get("acc_type"));

        vehRc.set("updateTime", new Date());
        return vehRc;
    }

    /**
     * 上传车辆信息到部中心
     *
     * @param vehicleInfo
     * @return
     */
    private BaseUploadResponse uploadBasicVehicleInfo(Record vehicleInfo) {
        EtcVehicleinfoJson etcVehicleinfoJson = new EtcVehicleinfoJson();
        etcVehicleinfoJson._setOrPut(vehicleInfo.getColumns());
        logger.info("{}上传车辆的内容为:{}", serverName, etcVehicleinfoJson);
        etcVehicleinfoJson.setRegisteredTime(DateUtil.parseDate(vehicleInfo.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcVehicleinfoJson.setRegisterDate(DateUtil.parseDate(vehicleInfo.get("registerDate"), DateUtil.FORMAT_YYYY_MM_DD));
        etcVehicleinfoJson.setIssueDate(DateUtil.parseDate(vehicleInfo.get("issueDate"), DateUtil.FORMAT_YYYY_MM_DD));

//        String json = Jackson.getJson().toJson(etcVehicleinfoJson);
//        String fileName = BASIC_VEHICLEUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(etcVehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);
        logger.info("{}上传车辆响应信息:{}", serverName, response);
        return response;
    }

}

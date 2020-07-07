package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.*;
import com.csnt.ins.bizmodule.order.queryuserid.GenerateUserIdService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.model.json.PlateCheckJson;
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
 * @ClassName OfflineIssuerOpenUserService
 * @Description TODO
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class OfflineVehicleReceiveService implements IReceiveService, BaseUploadService {
    protected static Logger logger = LoggerFactory.getLogger(OfflineVehicleReceiveService.class);
    GenerateUserIdService generateUserIdService = new GenerateUserIdService();

    private String serviceName = "[8804线下渠道车辆信息接收]";

    private final String ETC_OFL_USERINFO = "etc_ofl_userinfo";
    private final String MAP_KEY_ISCHECK = "isCheck";
    private final String MAP_KEY_MSG = "msg";
    private final String BASIC_PLATECHECK_REQ = "BASIC_PLATECHECK_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String TABLE_ETC_OFL_VEHILCEINFO = "etc_ofl_vehicleinfo";
    private final String TABLE_ETC_VEHILCEINFO = "etc_vehicleinfo";
    private final String MSG_AUTHENTICATION = "已认证";
    private final String MSG_SIGNOFL = "车辆存在签约渠道绑定数据";
    private final String TABLE_VEHILCEINFO_HIS = "etc_vehicleinfo_history";
    private final String CHECK_NOMARL = "在分对分渠道办理过ETC";
    private final String CHECK_NOMARL1 = "青海发行,办理过发行业务";

    private final Integer CUSTOMER_TYPE_COMPANY = 2;
    private final Integer CUSTOMER_TYPE_PERSONAL = 1;

    /**
     * 8831获取账户信息
     */
    UserAccountUploadService userAccountUploadService = new UserAccountUploadService();
    /**
     * 8850车牌发行验证
     */
    IssuePcoflUploadService issuePcoflUploadService = new IssuePcoflUploadService();
    /**
     * 8840车辆支付渠道绑定/解绑通知
     */
    UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();
    /**
     * 8852车辆信息上传
     */
    CertifyVloflUploadService certifyVloflUploadService = new CertifyVloflUploadService();
    /**
     * 8832获取车辆列表
     */
    UserVpoflService userVpoflService = new UserVpoflService();


    /**
     * 车牌唯一性校验结果
     * 1- 可发行
     * 2- 不可发行
     */
    private final String VELCHEL_CHECK_NORMAL = "1";


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
            // 判断车辆新试否存在线上渠道订单
            Record checkRc = Db.findFirst(DbUtil.getSql("queryOnlineOrderByVeh"), dataRc.get("id").toString());
            if (checkRc != null) {
                return Result.bizError(704, "该车辆信息已经在线上渠道申请订单信息");
            }


            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));

            boolean openOflBl = false;
            EtcOflUserinfo etcOflUserinfo = null;
            if (bl) {
                // 读取etc_ofl_userinfo 表判断客户是否存在,检查线下车辆是否存在，如果不存在 ,新开变为true
                // 检查客户是否在部中心线下渠道开户
                etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(dataRc.get("userId").toString());

                // 如果客户线下未开户则开户
                if (etcOflUserinfo == null) {
                    Record userRc = Db.findFirst(DbUtil.getSql("queryCenterEtcUserById"),dataRc.get("userId").toString());
                    if (userRc == null) {
                        return Result.bizError(794, "未找到对应的客户信息");
                    }

                    Result result = callOffineOpenUser(userRc,dataRc.get("userId").toString());
                    if (!result.getSuccess()) {
                        return result;
                    }
                    //取账号返回的相关信息
                    Map tkMap = (Map) result.getData();
                    Record oflUserRc = dataToOflUser(userRc, tkMap, dataRc.get("userId").toString());
                    if (Db.save(ETC_OFL_USERINFO, oflUserRc)) {
                        logger.info("{}保存线下监管平台ETC_OFL_USERINFO表数据成功", serviceName);
                    } else {
                        logger.error("{}保存线下监管平台ETC_OFL_USERINFO表数据失败", serviceName);
                        return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR,
                                "保存线下监管平台ETC_OFL_USERINFO表数据失败");
                    }
                    etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(dataRc.get("userId").toString());

                }

                EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(dataRc.get("id"));
                if (etcOflUserinfo != null
                        && (etcOflVehicleinfo == null
                        || etcOflVehicleinfo.getDepVehicleId() == null)) {
                    openOflBl = true;
                }
            }
            // 转换etc_ofl_vehicleinfo
            Record etcOflVehRc = dataToEtcOflVeh(dataRc);
            if (etcOflVehRc == null) {
                return Result.bizError(799, "转换车辆信息失败");
            }

            if (openOflBl) {
                // 新开户上传相关信息
                Map openMap = callOffineOpenVeh(dataRc, etcOflUserinfo, etcOflVehRc);
                // 判断相关新上传是否成功
                boolean suBl = (boolean) openMap.get("bool");
                if (!suBl) {
                    logger.error("{}上传线下绑定信息失败", serviceName);
                    return (Result) openMap.get("result");
                }
            } else {

                Map vehMap = checkVehicle(dataRc.get("id"), dataRc.get("type"));
                boolean checkBl = (boolean) vehMap.get(MAP_KEY_ISCHECK);
                if (!checkBl) {
                    // 车牌验证失败
                    logger.error("{}车牌唯一性校验失败", serviceName);
                    return Result.vehCheckError((String) vehMap.get(MAP_KEY_MSG));
                }

            }
            //  上传部中心车辆信息
            Map vehUploadMap = uploadCenterVehInfo(dataRc);
            if (!(boolean) vehUploadMap.get("isUpload")) {
                logger.error("{}[车辆信息上传失败:{}",
                        serviceName, vehUploadMap.get(MAP_KEY_MSG));
                return Result.sysError((String) vehUploadMap.get(MAP_KEY_MSG));
            }

            // 转换为etc_vehicleinfo
            Record etcVehRc = dataToVehicleinfo(dataRc);
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                //保存车辆信息表
                Db.delete(TABLE_ETC_VEHILCEINFO, etcVehRc);
                if (!Db.save(TABLE_ETC_VEHILCEINFO, etcVehRc)) {
                    logger.error("{}保存TABLE_ETC_VEHILCEINFO表失败", serviceName);
                    return false;
                }
                if (!Db.save(TABLE_VEHILCEINFO_HIS, ids, etcVehRc)) {
                    logger.error("{}保存TABLE_ETC_VEHILCEINFO_HIS表失败", serviceName);
                    return false;
                }
                Db.delete(TABLE_ETC_OFL_VEHILCEINFO, "vehicleId", etcOflVehRc);
                if (!Db.save(TABLE_ETC_OFL_VEHILCEINFO, etcOflVehRc)) {
                    logger.error("{}保存TABLE_ETC_OFL_VEHILCEINFO表失败", serviceName);
                    return false;
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


    private Map checkInput(Record inMap) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        //车辆编号
        String id = inMap.get("id");
        // 客户编码
        String userId = inMap.get("userId");
        // 业务类型
        Integer type = inMap.get("type");
        // 指定联系人姓名
        String contact = inMap.get("contact");
        // 录入方式
        Integer registeredType = inMap.get("registeredType");
        // 录入渠道编号
        String channelId = inMap.get("channelId");
        // 录入时间
        String registeredTime = inMap.get("registeredTime");
        // 车架号
        String vin = inMap.get("vin");
        // 发动机号
        String engineNum = inMap.get("engineNum");
        // 发证日期
        String issueDate = inMap.get("issueDate");
        if (StringUtil.isEmpty(id, userId, type, vin, engineNum, issueDate, contact, registeredType, channelId, registeredTime)) {
            logger.error("{}参数id, userId, type, vin, engineNum, issueDate, contact, registeredType, channelId, registeredTime不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("id, userId, type, vin, engineNum, issueDate, contact, registeredType, channelId, registeredTime"));
            outMap.put("bool", false);
            return outMap;
        }
        inMap.set("issueDate", issueDate.substring(0, 10));
        //姓名
        String name = inMap.get("name");
        //客户类型
        Integer userType = inMap.get("userType");
        //绑定的卡类型
        Integer cardType = inMap.get("cardType");
        //车牌号
        String plateNum = inMap.get("plateNum");
        //注册日期
        String registerDate = inMap.get("registerDate");
        //使用性质
        Integer useCharacter = inMap.get("useCharacter");
        //车辆类型
        String vehicleType = inMap.get("vehicleType");
        if (StringUtil.isEmpty(name, userType, plateNum, registerDate, vehicleType, useCharacter)) {
            logger.error("{}参数name,userType, plateNum, registerDate,vehicleType,useCharacter不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("name,userType, plateNum, registerDate,vehicleType,useCharacter"));
            outMap.put("bool", false);
            return outMap;
        }
        inMap.set("registerDate", registerDate.substring(0, 10));

        //车辆尺寸
        String outsideDimensions = inMap.get("outsideDimensions");
        //银行卡号或账号
        String accountId = inMap.get("accountId");
        //银行预留手机号
        String linkMobile = inMap.get("linkMobile");
        //银行账户名称
        String bankUserName = inMap.get("bankUserName");
        if (StringUtil.isEmpty(outsideDimensions, accountId, linkMobile, bankUserName)) {
            logger.error("{}参数 outsideDimensions, accountId, linkMobile,bankUserName不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError(" outsideDimensions, accountId, linkMobile,bankUserName"));
            outMap.put("bool", false);
            return outMap;
        }

        //银行卡绑定用户身份证号
        String certsn = inMap.get("certsn");
        //网点编号
        String posId = inMap.get("posId");
        //银行绑卡请求时间
        String genTime = inMap.get("genTime");
        //银行绑卡校验请求流水号
        String trxSerno = inMap.get("trx_serno");
        //绑定银行账户类型
        Integer accType = inMap.get("acc_type");
        //银行编码
        String bankPost = inMap.get("bankPost");
        //渠道类型
        String channelType = inMap.get("channelType");

        if (StringUtil.isEmpty(certsn, posId, genTime, trxSerno, accType, bankPost, channelType)) {
            logger.error("{}参数certsn, posId, genTime, trxSerno,accType,bankPost,channelType不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("certsn, posId, genTime, trxSerno,accType,bankPost,channelType"));
            outMap.put("bool", false);
            return outMap;
        }
        return outMap;
    }

    /**
     * 数据转换为车辆绑卡信息
     */
    private Record dataToEtcOflVeh(Record appMap) {
        Record vehRc = new Record();

        // 车辆编号
        vehRc.set("vehicleId", appMap.get("id"));
        // 客户编号
        vehRc.set("userId", appMap.get("userId"));

        if (SysConfig.getEncryptionFlag()) {

           try {
                // 银行卡号或账号
                vehRc.set("accountId",MyAESUtil.Encrypt( appMap.get("accountId")));
               // 银行预留手机号
               vehRc.set("linkMobile",MyAESUtil.Encrypt( appMap.get("linkMobile")));
               // 银行账户名称
               vehRc.set("bankUserName",MyAESUtil.Encrypt( appMap.get("bankUserName")));
               // 银行卡绑定用户身份证号
               vehRc.set("certsn",MyAESUtil.Encrypt( appMap.get("certsn")));
           } catch (Exception e) {
                e.printStackTrace();
               return null;
            }
        } else {
            // 银行卡号或账号
            vehRc.set("accountId", appMap.get("accountId"));
            // 银行预留手机号
            vehRc.set("linkMobile", appMap.get("linkMobile"));

            // 银行账户名称
            vehRc.set("bankUserName", appMap.get("bankUserName"));
            // 银行卡绑定用户身份证号
            vehRc.set("certsn", appMap.get("certsn"));
        }

        // 客户类型
        vehRc.set("userType", appMap.get("userType"));
        // 企业用户ETC 业务协议号
        vehRc.set("protocolNumber", appMap.get("protocolNumber"));
        // 网点编号
        vehRc.set("posId", appMap.get("posId"));
        // 银行绑卡请求时间
        vehRc.set("genTime", DateUtil.parseDate(appMap.get("genTime"), DateUtil.FORMAT_YYYYM_MDDH_HMMSS));
        // 银行绑卡校验请求流水号
        vehRc.set("trx_serno", appMap.get("trx_serno"));
        // 员工推荐人工号
        vehRc.set("employeeId", appMap.get("employeeId")==null? "999999999":appMap.get("employeeId"));
        // 原请求流水
        vehRc.set("org_trx_serno", appMap.get("org_trx_serno"));
        // 绑定银行账户类型
        vehRc.set("acc_type", appMap.get("acc_type"));
        // 绑定卡类型 1-信用卡 2-借记卡
        vehRc.set("cardType", appMap.get("cardType"));
        // 银行编码
        vehRc.set("bankPost", appMap.get("bankPost"));
        // 渠道类型
        vehRc.set("channelType", appMap.get("channelType"));
        // 绑定状态1:绑定2:解绑
        vehRc.set("bindStatus", 1);
//        绑定卡类型
        vehRc.set("cardType", appMap.get("cardType"));
        // 创建时间
        vehRc.set("createTime", new Date());
        // 更新
        vehRc.set("updateTime", new Date());
        return vehRc;
    }

    /**
     * 上传车辆信息到部中心
     *
     * @param appMap
     * @return
     */
    private Map uploadCenterVehInfo(Record appMap) {

        EtcVehicleinfoJson vehicleinfoJson = new EtcVehicleinfoJson();
        // 车辆编号
        vehicleinfoJson.setId(appMap.get("id"));
        // 收费车型
        vehicleinfoJson.setType(appMap.get("type"));
        // 所属客户编号
        vehicleinfoJson.setUserId(appMap.get("userId"));
        // 机动车所有人名称
        vehicleinfoJson.setOwnerName(appMap.get("ownerName"));
        // 机动车所有人证件类型
        vehicleinfoJson.setOwnerIdType(appMap.get("ownerIdType"));
        // 机动车所有人证件号码
        vehicleinfoJson.setOwnerIdNum(appMap.get("ownerIdNum"));
        // 所有人联系方式
        vehicleinfoJson.setOwnerTel(appMap.get("ownerTel"));
        // 所有人联系地址
        vehicleinfoJson.setAddress(appMap.get("address"));
        // 指定联系人姓名
        vehicleinfoJson.setContact(appMap.get("contact"));
        // 录入方式1-线上，2-线下
        vehicleinfoJson.setRegisteredType(appMap.get("registeredType"));
        // 录入渠道编号
        vehicleinfoJson.setChannelId(appMap.get("channelId"));
        // 录入时间
        vehicleinfoJson.setRegisteredTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
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
        vehicleinfoJson.setAxisType(appMap.get("axisType")==null?null:appMap.get("axisType").toString());

        // 操作
        vehicleinfoJson.setOperation(OperationEnum.ADD.getValue());

        BaseUploadResponse response = upload(vehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);

        logger.info("{}上传车辆响应信息:{}", serviceName, response);
        Map outMap = new HashMap<>();

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.info("{}[vehicleId={}]上传车辆信息异常:{}",
                    serviceName, appMap.get("id"), response);
            outMap.put("isUpload", false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
        } else {
            outMap.put("isUpload", true);
            outMap.put(MAP_KEY_MSG, "");
        }
        return outMap;

    }

    private Record dataToVehicleinfo(Record dataRc) {
        Record vehRc = new Record();

        //车牌号
        vehRc.set("id", dataRc.get("id"));
        //收费车型
        vehRc.set("type", dataRc.get("type"));
        //客户编码
        vehRc.set("userId", dataRc.get("userId"));

        if (SysConfig.getEncryptionFlag()) {
            try {
                //机动车所有人名称
                vehRc.set("ownerName",MyAESUtil.Encrypt( dataRc.get("ownerName")));
                //机动车所有人证件号码
                vehRc.set("ownerIdNum",MyAESUtil.Encrypt( dataRc.get("ownerIdNum")));
                //所有人联系电话
                vehRc.set("ownerTel",MyAESUtil.Encrypt( dataRc.get("ownerTel")));
                //所有人联系地址
                vehRc.set("address",MyAESUtil.Encrypt( dataRc.get("address")));
                //指定联系人姓名
                vehRc.set("contact", MyAESUtil.Encrypt( dataRc.get("contact")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //机动车所有人名称
            vehRc.set("ownerName", dataRc.get("ownerName"));
            //机动车所有人证件号码
            vehRc.set("ownerIdNum", dataRc.get("ownerIdNum"));
            //所有人联系电话
            vehRc.set("ownerTel", dataRc.get("ownerTel"));
            //所有人联系地址
            vehRc.set("address", dataRc.get("address"));
            //指定联系人姓名
            vehRc.set("contact", dataRc.get("contact"));
        }

        //机动车所有人证件类型
        vehRc.set("ownerIdType", dataRc.get("ownerIdType"));

        //录入方式1-线上，2-线下
        vehRc.set("registeredType", dataRc.get("registeredType"));
        //录入渠道编号
        vehRc.set("channelId", dataRc.get("channelId"));
        //录入时间
        vehRc.set("registeredTime", DateUtil.parseDate(dataRc.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        //行驶证车辆类型
        vehRc.set("vehicleType", dataRc.get("vehicleType"));
        //行驶证品牌型号
        vehRc.set("vehicleModel", dataRc.get("vehicleModel"));
        //车辆使用性质
        vehRc.set("useCharacter", dataRc.get("useCharacter"));
        //车辆识别代号
        vehRc.set("vin", dataRc.get("vin"));
        //车辆发动机号
        vehRc.set("engineNum", dataRc.get("engineNum"));
        //注册日期
        vehRc.set("registerDate", DateUtil.parseDate(dataRc.get("registerDate"), DateUtil.FORMAT_YYYY_MM_DD));
        //发证日期
        vehRc.set("issueDate", DateUtil.parseDate(dataRc.get("issueDate"), DateUtil.FORMAT_YYYY_MM_DD));
        //档案编号
        vehRc.set("fileNum", dataRc.get("fileNum"));
        //核定载人数
        vehRc.set("approvedCount", dataRc.get("approvedCount"));
        //总质量
        vehRc.set("totalMass", dataRc.get("totalMass"));
        //整备质量
        vehRc.set("maintenanceMass", dataRc.get("maintenanceMass"));
        //核定载质量
        vehRc.set("permittedWeight", dataRc.get("permittedWeight"));
        //外廓尺寸
        vehRc.set("outsideDimensions", getOutsideDimensions(dataRc.get("outsideDimensions")));
        //准牵引总质量
        vehRc.set("permittedTowWeight", dataRc.get("permittedTowWeight"));
        //检验记录
        vehRc.set("testRecord", dataRc.get("testRecord"));
        //车轮数
        vehRc.set("wheelCount", dataRc.get("wheelCount"));
        //车轴数
        vehRc.set("axleCount", dataRc.get("axleCount"));
        //轴距
        vehRc.set("axleDistance", dataRc.get("axleDistance"));
        //轴型
        vehRc.set("axisType", dataRc.get("axisType"));
        //轴型
        vehRc.set("vehUserType", dataRc.get("vehUserType"));

        //车脸识别特征版本号
        vehRc.set("vehicleFeatureVersion", dataRc.get("vehicleFeatureVersion"));
        //预付费/代扣账户编码
        vehRc.set("payAccountNum", dataRc.get("payAccountNum"));
        //操作  1-新增，2-变更，3-删除
        vehRc.set("operation", 1);
        //渠道类型
        vehRc.set("channelType", dataRc.get("channelType"));
        //信息录入网点id
        vehRc.set("orgId", dataRc.get("posId"));
        //信息录入人工号
        vehRc.set("operatorId", dataRc.get("operatorId"));
        //操作时间
        vehRc.set("opTime", new Date());
//        //绑定卡类型
//        vehRc.set("cardType",  dataRc.get("cardType"));

        //数据是否可上传状态
        vehRc.set("uploadStatus", 2);

        //创建时间
        vehRc.set("createTime", new Date());
        //更新时间
        vehRc.set("updateTime", new Date());
        return vehRc;
    }

    /**
     * @return
     */
    private Map callOffineOpenVeh(Record dataRc, EtcOflUserinfo etcOflUserinfo, Record etcOflVehRc) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);
        // 刷新用户凭证
        if (etcOflUserinfo != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), outMap.get("result"));
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
        }

        // 调用5.1车牌发行验证
        String vehicleId = dataRc.get("id");
        String[] sp = vehicleId.split("_");
        if (sp.length != 2) {
            outMap.put("bool", false);
            outMap.put("result", Result.bizError(704, "车牌异常"));
            logger.error("{}[vehicleId={}]车牌异常:",
                    serviceName, vehicleId);
            return outMap;
        }
        //进行下线监控接口的车牌唯一性校验，获得部中心的vehicleId
        Result result = issuePcoflUploadService.entry(Kv.by("plateNum", sp[0])
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateColor", Integer.parseInt(sp[1])));
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}5.1车牌发行验证失败:{}", serviceName, result);
            outMap.put("bool", false);
            outMap.put("result", result);
            return outMap;
        }
        // 缺解析Data格式,认证过就会返回失败
        Map map = (Map) result.getData();
        int rs = (int) map.get("result");
        //1- 发行方已办理
        //2- 统一平台办理
        //3- 未关联
        //4- 未办理
        //5- 办理中
        if (rs != 5 && rs != 4 && rs != 3 &&
                !(rs == 1 && map.get("info").toString().contains(CHECK_NOMARL)) &&
                !(rs == 1 && map.get("info").toString().contains(CHECK_NOMARL1))) {
            outMap.put("bool", false);
            outMap.put("result", Result.bizError(704, (String) map.get("info")));
            return outMap;
        }
        // 部中心返回 ,更新到etcOflVehRc这里面
        String vehUuid = (String) map.get("vehicleId");
        if (vehUuid == null) {
            // 根据查询接口取车辆UUID
            Result vehList = userVpoflService.entry(Kv.by("pageNo", 1)
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId())
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("pageSize", 100));
            if (vehList.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}获取用户的车辆列表失败:{}", serviceName, vehList);
                outMap.put("bool", false);
                outMap.put("result", vehList);
                return outMap;
            }
            Map lsMap = (Map) vehList.getData();
            List<Map> enDataMap = (List) lsMap.get("encryptedData");
            for (Map vehMap : enDataMap) {
                if (sp[0].equals(vehMap.get("plateNum")) && Integer.parseInt(sp[1]) == (int) vehMap.get("plateColor")) {
                    vehUuid = (String) vehMap.get("vehicleId");
                    break;
                }
            }

            if (vehUuid == null) {
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(704, "该车辆已在平台认证过"));
                return outMap;
            }
        }

        etcOflVehRc.set("depVehicleId", vehUuid);
        etcOflVehRc.set("depUserId", etcOflUserinfo.getDepUserId());
        // 5.3车辆信息上传服务
        Integer character = 2;
        Integer useCharacter = dataRc.get("useCharacter");
        if (useCharacter != null) {
            if (useCharacter == 3 || useCharacter == 3) {
                character = 1;
            }
        }

        //5.3车辆信息上传
        Result resultVlog = certifyVloflUploadService.entry(Kv.by("vehicleId", vehUuid)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("headtype", 1)
                .set("vin", dataRc.get("vin"))
                .set("engineNum", dataRc.get("engineNum"))
                .set("issueDate", dataRc.get("issueDate"))
                .set("name", dataRc.get("name"))
                .set("plateNum", sp[0])
                .set("registerDate", dataRc.get("registerDate"))
                .set("useCharacter", character)
                .set("vehicleType", dataRc.get("vehicleType"))
                .set("type", dataRc.get("type"))
                .set("fileNum", dataRc.get("fileNum"))
                .set("approvedCount", dataRc.get("approvedCount"))
                .set("totalMass", dataRc.get("totalMass"))
                .set("maintenaceMass", dataRc.get("maintenaceMass"))
                .set("permittedWeight", dataRc.get("permittedWeight"))
                .set("outsideDimensions", getOutsideDimensions(dataRc.get("outsideDimensions")))
                .set("permittedTowWeight", dataRc.get("permittedTowWeight"))
                .set("vehicleModel", dataRc.get("vehicleModel"))
                .set("testRecord", dataRc.get("testRecord"))
                .set("wheelCount", dataRc.get("wheelCount"))
                .set("axleCount", dataRc.get("axleCount"))
                .set("axleDistance", dataRc.get("axleDistance"))
                .set("axisType", dataRc.get("axisType")));

        if (resultVlog.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                && !resultVlog.getMsg().contains(MSG_AUTHENTICATION)) {
            logger.error("{}5.3车辆信息上传服务失败:{}", serviceName, resultVlog);
            outMap.put("bool", false);
            outMap.put("result", resultVlog);
            return outMap;
        }

        // 4.1车辆支付渠道绑定、解绑通知
        Integer accType = dataRc.get("acc_type");
        // 1-信用卡 2-借记卡
        int drAC = 2;
        if (accType == 2) {
            drAC = 1;
        }
        // 取channelType取值
        Record agRc = Db.findFirst(DbUtil.getSql("queryTblAgencyById"), dataRc.get("bankPost").toString());
        String channelType = "103";
        if (agRc != null) {
            channelType = agRc.get("SIGNCHANNEL");
        }

        //4.1车辆支付渠道绑定、解绑通知
        Result result1 = userSignoflUploadService.entry(Kv.by("vehicleId", vehUuid)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateNum", sp[0])
                .set("plateColor", Integer.parseInt(sp[1]))
                .set("signType", dataRc.get("registeredType"))
                .set("issueChannelId", dataRc.get("channelId"))
                .set("channelType", channelType)
                .set("cardType", drAC)
                .set("account", dataRc.get("accountId"))
                .set("enableTime", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                .set("closeTime", "2099-12-31T23:59:59")
                .set("info", null)
                .set("status", 1));
        if (result1.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                && !result1.getMsg().contains(MSG_SIGNOFL)) {
            logger.error("{}4.1车辆支付渠道绑定、解绑通知失败:{}", serviceName, result1);
            outMap.put("bool", false);
            outMap.put("result", result1);
            return outMap;
        }

        return outMap;
    }

    private String getVehicleUuid(EtcOflUserinfo etcOflUserinfo, String plateNum, int plateColor) {
        Result result1 = userVpoflService.entry(Kv.by("pageNo", 1)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("pageSize", 100));


        return null;
    }


    /**
     * 查询客编
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    public Map getCustomerNum(String userIdType, String userIdNum) {

        Map outMap = new HashMap<>();
        outMap.put("flag", "");
        outMap.put("id", "");
        outMap.put("isnew", 0);

        // 到我们自己库中查询是否存在客户编号
        String userId = generateUserIdService.queryUserIdByUserIdNum(userIdType, userIdNum);
        if (StringUtil.isEmpty(userId)) {
            //进行查客编
            Map userMap = generateUserIdService.postQueryUserIdServer(userIdType, userIdNum);
            if (userMap.get("id") == null) {
                logger.error("{}生成客编异常", serviceName);
                return outMap;
            }
            userId = (String) userMap.get("id");
            outMap.put("flag", CommonAttribute.ISSUER_TYPE_EG);
            outMap.put("id", userId);
            outMap.put("isnew", userMap.get("isnew"));
            return outMap;
        } else {
            // 本地库找到
            outMap.put("flag", CommonAttribute.ISSUER_TYPE_CSNT);
            outMap.put("id", userId);
            outMap.put("isnew", 0);
            return outMap;
        }
    }

    /**
     * 调用部中心，检查车牌唯一性
     *
     * @param vehicleId,vehicleType
     * @return
     */
    public Map checkVehicle(String vehicleId, int vehicleType) {

        PlateCheckJson plateCheckJson = new PlateCheckJson();

        Map outMap = new HashMap<>();
        outMap.put(MAP_KEY_ISCHECK, false);
        outMap.put(MAP_KEY_MSG, "");

        String[] sp = vehicleId.split("_");
        if (sp.length != 2) {
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, "上送车牌有误");
            logger.error("{}[vehicleId={}]车牌唯一性校验上传异常:",
                    serviceName, vehicleId);
            return outMap;
        }
        String plateNum = sp[0];
        int plateColor = Integer.parseInt(sp[1]);

        plateCheckJson.setVehiclePlate(plateNum);
        plateCheckJson.setVehicleColor(plateColor);
        plateCheckJson.setVehicleType(vehicleType);
        //1- 线下发行
        //2- 普通互联网发行
        //3- 互联网信用卡成套发行
        plateCheckJson.setIssueType(1);
        BaseUploadResponse response = upload(plateCheckJson, BASIC_PLATECHECK_REQ);

        logger.info("{}车牌唯一性校验响应信息:{}", serviceName, response);

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}[vehicleId={}]车牌唯一性校验上传异常:{}",
                    serviceName, vehicleId, response);
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
            return outMap;
        }

        if (VELCHEL_CHECK_NORMAL.equals(response.getResult())) {
            outMap.put(MAP_KEY_ISCHECK, true);
            outMap.put(MAP_KEY_MSG, null);
            return outMap;
        } else {
            logger.error("{}[vehicleId={}]车牌唯一性校验失败:{}", serviceName, vehicleId, response.getInfo());
            //更新审核结果未不通过
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, response.getInfo());
            return outMap;
        }
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

package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.IssuePcoflUploadService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.PlateCheckJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 8800线下开户检查查询
 *
 * @author cml
 **/
public class QueryIssusrCheckService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(QueryIssusrCheckService.class);

    private final String serverName = "[8800线下开户检查查询]";

    private final String BASIC_PLATECHECK_REQ = "BASIC_PLATECHECK_REQ_";
    private final String CHECK_NOMARL = "在分对分渠道办理过ETC";
    private final String CHECK_NOMARL1 = "青海发行,办理过发行业务";


    /**
     * 车牌唯一性校验结果
     * 1- 可发行
     * 2- 不可发行
     */
    private final Integer VEHICLE_CHECK_NORMAL = 1;
    /**
     * 车牌唯一性校验结果
     * 1- 可发行
     * 2- 不可发行
     */
    private final Integer VEHICLE_CHECK_NONNORMAL = 2;

    /**
     * 业务错误编码
     */
    private final Integer BIZ_ERROR_CODE = 704;
    /**
     * 8850车牌发行验证
     */
    IssuePcoflUploadService issuePcoflUploadService = new IssuePcoflUploadService();

    private final String MAP_KEY_ISCHECK = "isCheck";
    private final String MAP_KEY_MSG = "msg";
    private final String MAP_RES = "res";


    /**
     * 处理流程：
     * 1. 根据车辆id查询当前车辆是否含有正常、挂失状态的卡、签(operation < 3)，
     * 1. 如果存在，则判断当前用户与车辆的用户是否相等，如果不相等，则不允许发行并报错，否则result=2
     * 2. 如果不存在，则允许发行，result=1
     * 2. 如果步骤1中result=1，判断etc_ofl_userInfo是否有数据，
     * 1. 如果没有，调用营改增2.1.7车牌唯一性校验，修改result的状态
     * 2. 如果有，则直接调用5.1车牌唯一性校验，修改result的状态
     * 3. 如果result=1，则进行开户，返回etc_ofl_userinfo、etc_ofl_vehicleinfo where accountid is not null 中的数据
     * 4. 如果result=2，则不能进行发行
     * 3. 如果步骤1中result=2，可以发行，则返回这etc_userinfo、etc_vehicleinfo、etc_cardinfo、etc_obuinfo四个表的数据
     * <p>
     * <p>
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        long startTime = System.currentTimeMillis();
        try {
            // 获取用户信息
            Record userRc = null;
            // 取车辆信息
            Record vehRc = null;
            // 取OBU信息
            Record obuRc = null;
            // 取ETC卡信息
            Record cardRc = null;
            //校验结果
            String vehInfo = "";
            // 车牌检查结果
            int result = 0;
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);

            //证件类型
            Integer userIdType = record.get("userIdType");
            //证件号码
            String userIdNum = record.get("userIdNum");
            //车牌号码
            String vehiclePlate = record.get("vehiclePlate");
            //车牌颜色
            Integer vehicleColor = record.get("vehicleColor");
            //车型编码
            Integer vehicleType = record.get("vehicleType");

            if (StringUtil.isEmpty(userIdType, userIdNum, vehiclePlate, vehicleColor, vehicleType)) {
                logger.error("{}参数userIdType, userIdNum, vehiclePlate, vehicleColor, vehicleType不能为空", serverName);
                return Result.paramNotNullError("id, name, userIdType, phone, address, registeredType");
            }
            // 加密
            if (SysConfig.getEncryptionFlag()) {
                //证件号码
                userIdNum = MyAESUtil.Encrypt( record.get("userIdNum"));
            }
            String vehicleId = vehiclePlate + "_" + vehicleColor;

            //  查询该车辆是否申请了订单信息
            Record checkRc = Db.findFirst(DbUtil.getSql("queryOnlineOrderByVeh"), vehicleId);
            if (checkRc != null) {
                logger.error("{}该车辆信息已经在线上渠道申请订单信息", serverName);
                return Result.bizError(BIZ_ERROR_CODE, "该车辆信息已经在线上渠道申请订单信息");
            }

            //  查询该车辆是否已经进入待注销状态
            checkRc = Db.findFirst(DbUtil.getSql("queryCardCancelByVeh"), vehicleId);
            if (checkRc != null) {
                logger.error("{}该车辆信息已经进入了待注销状态", serverName);
                return Result.bizError(BIZ_ERROR_CODE, "该车辆信息已经进入了待注销状态");
            }

            //1、 根据车辆id查询当前车辆是否含有正常、挂失状态的卡、签(operation < 3)
            Record vehicleRecord = Db.findFirst(DbUtil.getSql("queryEtcVehicleByVehicleId"), vehicleId);
            if (vehicleRecord != null ) {
                // 判断车型是否一致
                if (vehicleRecord.getInt("type") != vehicleType.intValue()) {
                    logger.error("{}系统车型与输入车型不一致[type={}]", serverName, vehicleRecord.get("type"));
                    return Result.bizError(BIZ_ERROR_CODE, "系统车型与输入车型不一致");
                }
            }
            if (vehicleRecord == null
                    || (StringUtil.isEmpty(vehicleRecord.getStr("cardId")) && StringUtil.isEmpty(vehicleRecord.getStr("obuId")))) {

                // 判断车辆用户信息与客户信息所属关系是否一致
                // 获取用户信息
                userRc = Db.findFirst(DbUtil.getSql("queryEtcUserByUserId"), userIdType, userIdNum);
                if (userRc != null && vehicleRecord != null &&  !vehicleRecord.getStr("userId").equals(userRc.get("id"))) {
                    logger.error("{}车辆不属于该客户[olduserId={}]", serverName, vehicleRecord.get("userId"));
                    return Result.bizError(BIZ_ERROR_CODE, "车辆不属于该客户");
                }
                logger.info("{}当前车辆[vehicleId={}]不含有正常、挂失状态的卡、签", serverName, vehicleId);
                result = VEHICLE_CHECK_NORMAL;

            } else {
                logger.info("{}当前车辆[vehicleId={}]含有正常、挂失状态的卡、签", serverName, vehicleId);
                result = VEHICLE_CHECK_NONNORMAL;

                // 获取用户信息
                userRc = Db.findFirst(DbUtil.getSql("queryEtcUserByUserId"), userIdType, userIdNum);

                // 判断车辆用户信息与客户信息所属关系是否一致
                if (userRc == null || !vehicleRecord.getStr("userId").equals(userRc.get("id"))) {
                    logger.error("{}车辆不属于该客户[olduserId={}]", serverName, vehicleRecord.get("userId"));
                    return Result.bizError(BIZ_ERROR_CODE, "车辆不属于该客户");
                }
            }

            logger.info("{}通过数据库判断当前车辆是否可发行耗时[{}]",
                    serverName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
            startTime = System.currentTimeMillis();

            //2、判断是否可以发行
            if (result == VEHICLE_CHECK_NORMAL) {
                // 车牌唯一性验证
                // 是否开启线下接口
                boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));

                Map ckMap = new HashMap<>();

                EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, record.getInt("userIdType"));
                if (bl && etcOflUserinfo != null) {
                    // 开启线下
                    Map offMap = offlineCheckVeh(record, etcOflUserinfo);
                    boolean vehBl = (boolean) offMap.get(MAP_KEY_ISCHECK);
                    if (!vehBl) {
                        return (Result) offMap.get("result");
                    }
                    // 取返回的接口
                    Result vehRs = (Result) offMap.get("result");
                    Map daMap = (Map) vehRs.getData();
                    int res = (int) daMap.get("result");
                    vehInfo = (String) daMap.get("info");
                    logger.info("{}线下认证结果:{}-{}", serverName, res, vehInfo);
                    // 3-未关联,4-未办理 5-办理中
                    if (res == 3 || res == 4 || res == 5 ||
                            (res == 1 && vehInfo.contains(CHECK_NOMARL)  ) ||
                            (res == 1 && vehInfo.contains(CHECK_NOMARL1)  )) {
                        result = VEHICLE_CHECK_NORMAL;
                    } else {
                        result = VEHICLE_CHECK_NONNORMAL;

                        // 部中心上传车辆信息后就反馈1； 可能营改增新失败，所有1通过，在后续检查
//                        EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
//                        if (etcOflVehicleinfo != null) {
//                            result = VEHICLE_CHECK_NORMAL;
//                        } else {
//                            result = VEHICLE_CHECK_NONNORMAL;
//                        }
                    }
                } else {
                    // 未启线下流程，调用2.1.7接口验证车牌唯一性
                    ckMap = checkVehicle(vehiclePlate, vehicleColor, vehicleType);
                    if (!(boolean) ckMap.get(MAP_KEY_ISCHECK)) {
                        return Result.vehCheckError((String) ckMap.get(MAP_KEY_MSG));
                    }
                    result = Integer.parseInt(ckMap.get(MAP_RES).toString());
                    vehInfo = (String) ckMap.get(MAP_KEY_MSG);
                }

                logger.info("{}查询部中心车牌唯一性校验耗时[{}]",
                        serverName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
                startTime = System.currentTimeMillis();

                //3、再次判断发行结果
                if (result == VEHICLE_CHECK_NORMAL) {
                    // 取车辆信息
                    vehRc = Db.findFirst(DbUtil.getSql("queryEtcOflVehicleById"), vehicleId);
                    // 取用户信息
                    userRc = Db.findFirst(DbUtil.getSql("queryEtcUserWithOflUserByUserIdNumAndType"), userIdType, userIdNum);
                } else {
                    logger.error("{}当前车辆无法发行[result={},info={}]", serverName, result, vehInfo);
                    return Result.sysError("当前车辆无法发行:" + vehInfo);
                }

            } else {
                // 取车辆信息
                vehRc = Db.findFirst(DbUtil.getSql("queryEtcVehicleById"), vehicleId);
                // 取OBU信息
                obuRc = Db.findFirst(DbUtil.getSql("queryEtcObuByVeh"), vehicleId);
                // 取ETC卡信息
                cardRc = Db.findFirst(DbUtil.getSql("queryEtcCardByVeh"), vehicleId);
            }


            // 取卡，obu的状态操作日志
            Integer cardBusinessType = queryEtcCardIssuseRcByVeh(vehicleId);
            Integer obuBusinessType = queryEtcObuIssuseRcByVeh(vehicleId);

            logger.info("{}查询四类数据耗时[{}]", serverName, DateUtil.diffTime(startTime, System.currentTimeMillis()));

            Map dtMap = new HashMap<>();
            dtMap.put("result", result);
            dtMap.put("info", vehInfo);
            dtMap.put("cardBusinessType", cardBusinessType);
            dtMap.put("obuBusinessType", obuBusinessType);

            if (vehRc != null && SysConfig.getEncryptionFlag()) {
                //解密
                vehRc.set("ownerName",MyAESUtil.Decrypt( vehRc.get("ownerName")));
                vehRc.set("ownerIdNum",MyAESUtil.Decrypt( vehRc.get("ownerIdNum")));
                vehRc.set("ownerTel",MyAESUtil.Decrypt( vehRc.get("ownerTel")));
                vehRc.set("address",MyAESUtil.Decrypt( vehRc.get("address")));
                vehRc.set("contact",MyAESUtil.Decrypt( vehRc.get("contact")));
                vehRc.set("accountid",MyAESUtil.Decrypt( vehRc.get("accountid")));
                vehRc.set("linkmobile",MyAESUtil.Decrypt( vehRc.get("linkmobile")));
                vehRc.set("bankusername",MyAESUtil.Decrypt( vehRc.get("bankusername")));
                vehRc.set("certsn",MyAESUtil.Decrypt( vehRc.get("certsn")));
            }

            if (cardRc != null && SysConfig.getEncryptionFlag()) {
                //解密
                cardRc.set("accountId",MyAESUtil.Decrypt( cardRc.get("accountId")));
                cardRc.set("linkMobile",MyAESUtil.Decrypt( cardRc.get("linkMobile")));
                cardRc.set("bankUserName",MyAESUtil.Decrypt( cardRc.get("bankUserName")));
                cardRc.set("certsn",MyAESUtil.Decrypt( cardRc.get("certsn")));
            }

            if (userRc != null && SysConfig.getEncryptionFlag()) {
                //解密
                userRc.set("userIdNum",MyAESUtil.Decrypt( userRc.get("userIdNum")));
                userRc.set("tel",MyAESUtil.Decrypt( userRc.get("tel")));
                userRc.set("address",MyAESUtil.Decrypt( userRc.get("address")));
                userRc.set("userName",MyAESUtil.Decrypt( userRc.get("userName")));
                userRc.set("agentName",MyAESUtil.Decrypt( userRc.get("agentName")));
                userRc.set("agentIdNum",MyAESUtil.Decrypt( userRc.get("agentIdNum")));
                userRc.set("bankAccount",MyAESUtil.Decrypt( userRc.get("bankAccount")));
            }

            dtMap.put("veh", vehRc);
            dtMap.put("card", cardRc);
            dtMap.put("obu", obuRc);
            dtMap.put("user", userRc);
            return Result.success(dtMap);
        } catch (Throwable t) {
            logger.error("{}查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 线下检查车牌发行情况
     *
     * @param record
     * @param etcOflUserinfo
     * @return
     */
    private Map offlineCheckVeh(Record record, EtcOflUserinfo etcOflUserinfo) {
        Map outMap = new HashMap<>();
        outMap.put(MAP_KEY_ISCHECK, false);
        outMap.put("result", null);

        // 刷新用户凭证
        if (etcOflUserinfo != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), outMap.get("result"));
                outMap.put(MAP_KEY_ISCHECK, false);
                outMap.put("result", result);
                return outMap;
            }
        }

        //5.1车牌发行验证
        Result result = issuePcoflUploadService.entry(Kv.by("plateNum", record.get("vehiclePlate"))
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateColor", record.get("vehicleColor")));
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}5.1车牌发行验证失败:{}", serverName, result);
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put("result", result);
            return outMap;
        } else {
            outMap.put(MAP_KEY_ISCHECK, true);
            outMap.put("result", result);
            return outMap;
        }

    }

    /**
     * 调用部中心，检查车牌唯一性
     *
     * @param vehiclePlate 车牌
     *                     vehicleColor 车辆颜色
     *                     vehicleType 车型
     * @return
     */
    public Map checkVehicle(String vehiclePlate, Integer vehicleColor, Integer vehicleType) {

        PlateCheckJson plateCheckJson = new PlateCheckJson();

        String vehicleId = vehiclePlate + "_" + vehicleColor;
        Map outMap = new HashMap<>();
        outMap.put(MAP_KEY_ISCHECK, false);
        outMap.put(MAP_KEY_MSG, "");
        outMap.put(MAP_RES, "0");


        plateCheckJson.setVehiclePlate(vehiclePlate);
        plateCheckJson.setVehicleColor(vehicleColor);
        plateCheckJson.setVehicleType(vehicleType);
        //1- 线下发行
        //2- 普通互联网发行
        //3- 互联网信用卡成套发行
        plateCheckJson.setIssueType(1);
        BaseUploadResponse response = upload(plateCheckJson, BASIC_PLATECHECK_REQ);

        logger.info("{}车牌唯一性校验响应信息:{}", serverName, response);

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}[vehicleId={}]车牌唯一性校验上传异常:{}",
                    serverName, vehicleId, response);
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
            return outMap;
        } else {
            outMap.put(MAP_KEY_ISCHECK, true);
            outMap.put(MAP_KEY_MSG, response.getInfo());
            outMap.put(MAP_RES, response.getResult());
            return outMap;
        }

    }

    /**
     * 根据车牌查询对应的卡是否有换卡，换卡签全套，卡补办的操作，有则返回相应的业务类型
     *
     * @param vehicleId
     * @return
     */
    private Integer queryEtcCardIssuseRcByVeh(String vehicleId) {
        Record rc = Db.findFirst(DbUtil.getSql("queryEtcCardIssuseRcByVeh"), vehicleId);
        if (rc == null) {
            return null;
        } else {
            return rc.getInt("cardBusinessType");
        }
    }

    /**
     * 根据车牌查询对应的卡是否有换签，换卡签全套，签补办的操作，有则返回相应的业务类型
     *
     * @param vehicleId
     * @return
     */
    private Integer queryEtcObuIssuseRcByVeh(String vehicleId) {
        Record rc = Db.findFirst(DbUtil.getSql("queryEtcObuIssuseRcByVeh"), vehicleId);
        if (rc == null) {
            return null;
        } else {
            return rc.getInt("obuBusinessType");
        }
    }

}

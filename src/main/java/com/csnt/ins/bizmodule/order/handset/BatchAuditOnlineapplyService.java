package com.csnt.ins.bizmodule.order.handset;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.*;
import com.csnt.ins.bizmodule.order.queryuserid.GenerateUserIdService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.OrderProcessTypeEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.expection.BizException;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.json.EtcUserinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.model.json.PlateCheckJson;
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
 * @ClassName BatchAuditOnlineapplyService
 * @Description 自动审核未处理的订单信息
 * @Author CML
 * @Date 2019/7/9 20:22
 * Version 1.0
 **/
public class BatchAuditOnlineapplyService implements  BaseUploadService {
    Logger logger = LoggerFactory.getLogger(BatchAuditOnlineapplyService.class);
    GenerateUserIdService generateUserIdService = new GenerateUserIdService();

    private final String serverName = "[自动审核工行推送订单]";
    private final String TABLE_ONLINEAPPLY = "onlineapply";
    private final String BASIC_PLATECHECK_REQ = "BASIC_PLATECHECK_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String ETC_OFL_USERINFO = "etc_ofl_userinfo";
    private final String ETC_OFL_VEHICLEINFO = "etc_ofl_vehicleinfo";

    private final Integer CUSTOMER_TYPE_COMPANY = 2;
    private final Integer CUSTOMER_TYPE_PERSONAL = 1;
    // 正常
    private final int CUSTOMER_STATUS_NORMAL = 1;

    // 线上
    private final int REGISTEREDTYPE_ONLINE = 1;

    // 订单类型 OBU
    private final int ORDERTYPE_OBU = 1;

    // 邮寄类型
    private final int POSTSTATUS_UNMAILED = 1;

    // 状态初始化
    private final int STATUS_INIT = 0;

    // 客车
    private final String VELCHEL_CAR = "01";

    // 货车
    private final String VELCHEL_TRUCK = "11";
    private final String MSG_AUTHENTICATION = "已认证";
    private final String MSG_SIGNOFL = "车辆存在签约渠道绑定数据";
    /**
     * 获取上传对象
     */
    public IUpload upload = CsntUpload.getInstance();

    /**
     * 审核状态
     * 0-	通过
     * 1-	不通过
     */
    private final String AUDIT_STATUS_PASS = "1";
    /**
     * 车牌唯一性校验结果
     * 1- 可发行
     * 2- 不可发行
     */
    private final String VELCHEL_CHECK_NORMAL = "1";

    /**
     * 车辆类型 11 货车
     */
    private final String VEHCILE_TYPE_TRUCK = "11";
    /**
     * 车辆类型 01 客车
     */
    private final String VEHCILE_TYPE_CUSTOMER = "01";

    private final String MAP_KEY_ISCHECK = "isCheck";
    private final String MAP_KEY_MSG = "msg";


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

    protected static final String LRLN = "\r\n";

    /**
     * ============================
     * 程序入口
     * ============================
     */
    public void entry() {
        logger.info("{}开始执行同步流程", serverName);
        long startTime = System.currentTimeMillis();
        try {
            //1.查询日终对账结果
            List<Record> dailyRecons = query();
            for (Record dailyRecon : dailyRecons) {
                try {
                    Map map = new HashMap<>();
                    // 订单编号
                    map.put("id",dailyRecon.getStr("Id"));
                    // 操作员默认为 99999
                    map.put("userId","99999");
                    // 默认为审核通过
                    map.put("identityFlag","1");
                    // 默认为审核通过
                    map.put("auditDesc","审核通过");
                    Result result =  AuditOnline(map);
                    if (!result.getSuccess()) {
                        // 如果不成功，修改申请记录为审核不成功
                        Db.tx(() -> {
                            //更新申请表的审核状态为未通过
                            Record onlineApply = new Record();
                            //id
                            onlineApply.set("id", dailyRecon.getStr("Id"));
                            //1-通过，2-未通过
                            onlineApply.set("ExamineResult", 2);
                            //1-通过，2-未通过，
                            onlineApply.set("flowStatus", 2);
                            //描述
                            onlineApply.set("ExamineDescription", result.getMsg());
                            //审核时间
                            onlineApply.set("approvalTime", new Date());
                            //审核人员
                            onlineApply.set("approvalId", "99999");

                            Db.update(TABLE_ONLINEAPPLY, "id", onlineApply);
                            return true;
                        });
                    }
                } catch (Exception e) {
                    logger.error("{}自动审核异常:{}：{}", serverName, e.toString(), e);
                }
            }

        } catch (Exception e) {
            logger.error("{}执行同步流程失败:{}", serverName, e.toString(), e);
        }
        logger.info("{}同步流程执行完成,耗时[{}]", serverName, new Date());
    }

    /**
     * 查询工行申请未审核的订单
     *
     * @return
     */
    private List<Record> query() {
        return Db.find(Db.getSql("mysql.queryAuditOnlineApply"));
    }

    public Result AuditOnline(Map dataMap) {
        logger.info("开始执行审核流程");
        long startTime = System.currentTimeMillis();
        try {
            Record orderInfo = new Record().setColumns(dataMap);
            String appUserId = orderInfo.get("userId");
            String id = orderInfo.get("id");
            String identityFlag = orderInfo.get("identityFlag");
            String auditDesc = orderInfo.get("auditDesc");

            if (StringUtil.isEmpty(id, identityFlag, auditDesc, appUserId)) {
                return Result.paramNotNullError("id,identityFlag,auditDesc,appUserId");
            }
            logger.info("{}在线订单申请审核信息接口:{}", serverName, orderInfo);

            List<Record> datas;
            try {
                datas = Db.find(DbUtil.getSql("queryOnlineApplyById"), id);
                if (datas.size() == 0) {
                    return Result.sysError("未检查到该申请信息！");
                }
                logger.info("{}[{}]查询到的申请数据有{}条", serverName, orderInfo, datas.size());
            } catch (Exception e) {
                logger.error("{}数据查询异常:{}", serverName, e.toString(), e);
                return Result.sysError("查询数据异常");
            }


            Record onlineApply = datas.get(0);
            String orderId = onlineApply.getStr("bookid");
            // 审核通过
            if (AUDIT_STATUS_PASS.equals(identityFlag)) {
                // 检查车牌唯一性 调用5.1接口检查
//                Map checkMap = checkVehicle(onlineApply);
//                if (!((boolean) checkMap.get(MAP_KEY_ISCHECK))) {
//                    // 退出当前数据处理
//                    logger.error("{}车牌唯一性校验失败", serverName);
//                    return Result.vehCheckError((String) checkMap.get(MAP_KEY_MSG));
//                }

                // 检查该订单的车辆是否在其他渠道申请，或者绑卡

                Map inCheckMap = checkVeh(onlineApply.get("CarNumber") + "_" + onlineApply.get("Calcolor"));
                if (!(boolean) inCheckMap.get("bool")) {
                    return (Result) inCheckMap.get("result");
                }
                // 查询客户编号
                Map userMap = getCustomerNum(onlineApply.getStr("passporttype"), onlineApply.get("passportid"));
                logger.info("{}查询到客编:{}", serverName, userMap);
                String userId = "";
                Record oflUserIdRc = null;
                if (StringUtil.isNotEmpty(userMap.get("id"))) {
                    //找到了客户编号
                    userId = (String) userMap.get("id");
                    onlineApply.set("userId", userId);
                    int flag = (int) userMap.get("flag");
                    int isnew = (int) userMap.get("isnew");
                    if (flag != CommonAttribute.ISSUER_TYPE_CSNT) {
                        // 不在issuer_center，写入客户信息
//                        if (isnew == 1) {

                        //客户信息上传
                        Map userUploadMap = uploadCenterUserInfo(onlineApply, userId);
                        if (!(boolean) userUploadMap.get("isUpload")) {
                            logger.error("{}[orderId={}]客户信息上传失败:{}",
                                    serverName, orderId, userUploadMap.get(MAP_KEY_MSG));
                            return Result.sysError((String) userUploadMap.get(MAP_KEY_MSG));
                        }


//                        }

                    }

                    //  上传线下监管平台
                    // 判断数据是否上传线下部省平台  开户信息
                    boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
                    Map tkMap = null;
                    if (bl) {
                        // 判断etc_ofl_userinfo是否存在该客户信息
                        oflUserIdRc = Db.findFirst(DbUtil.getSql("queryOflUserByUserId"), userId);
                        if (oflUserIdRc == null) {
                            Result result = callOffineOpenUser(onlineApply, userId);
                            if (!result.getSuccess()) {
                                return result;
                            }
                            //取账号返回的相关信息
                            tkMap = (Map) result.getData();

                            Record oflUserRc = dataToOflUser(onlineApply, tkMap, userId);
                            // 保存开户的相关信息
                            if (Db.save(ETC_OFL_USERINFO, oflUserRc)) {
                                logger.info("{}保存线下监管平台ETC_OFL_USERINFO表数据成功", serverName);
                            } else {
                                logger.error("{}保存线下监管平台ETC_OFL_USERINFO表数据失败", serverName);
                                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR,
                                        "保存线下监管平台ETC_OFL_USERINFO表数据失败");
                            }
                        }
                    } else {
                        Map checkMap = checkVehicle(onlineApply);
                        if (!((boolean) checkMap.get(MAP_KEY_ISCHECK))) {
                            // 退出当前数据处理
                            logger.error("{}车牌唯一性校验失败", serviceName);
                            return Result.vehCheckError((String) checkMap.get(MAP_KEY_MSG));
                        }
                    }

                } else {
                    logger.error("{}[orderId={}]客编查询失败,未获得客户编号", serverName, orderId);
                    return Result.sysError("未取得客户编号");
                }


                // 转换etc_ofl_vehicleinfo
                EtcOflVehicleinfo etcOflVehInfo = EtcOflVehicleinfo.dao.findById(onlineApply.get("CarNumber") + "_" + onlineApply.get("Calcolor"));
                Record etcOflVehRc = dataToEtcOflVeh(onlineApply);
                // 检查客户是否在部中心线下渠道开户
                EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(onlineApply.get("userId"));
                if (oflUserIdRc != null) {
                    // 新开户上传相关信息
                    Map openMap = callOffineOpenVeh(onlineApply, etcOflUserinfo, etcOflVehRc);
                    // 判断相关新上传是否成功
                    boolean suBl = (boolean) openMap.get("bool");
                    if (!suBl) {
                        logger.error("{}上传线下绑定信息失败", serviceName);
                        return (Result) openMap.get("result");
                    }
                }


                // 上传车辆信息到部中心
                Map vehUploadMap = uploadCenterVehInfo(onlineApply, userId);
                if (!(boolean) vehUploadMap.get("isUpload")) {
                    logger.error("{}[orderId={}]车辆信息上传失败:{}",
                            serverName, orderId, vehUploadMap.get(MAP_KEY_MSG));
                    return Result.sysError((String) vehUploadMap.get(MAP_KEY_MSG));
                }


                Record vehRc = onlineApply2VehicleInfo(onlineApply, userId);
                Record orderRc = onlineApply2OnlineOrder(onlineApply, userId);
                //将订单转换为用户数据
                Record finalUserRc = onlineApplyToUserInfo(onlineApply, userId);
                //
                Db.tx(() -> {
                    //客户信息
                    if (finalUserRc != null) {
                        Db.delete(CommonAttribute.ETC_USERINFO, finalUserRc);
                        Db.save(CommonAttribute.ETC_USERINFO, finalUserRc);
                        Db.save(CommonAttribute.ETC_USERINFO_HISTORY, finalUserRc);
                    }
                    // 车辆信息，先删除，在新增
                    Db.delete(CommonAttribute.ETC_VEHICLEINFO, vehRc);
                    Db.save(CommonAttribute.ETC_VEHICLEINFO, vehRc);
                    Db.save(CommonAttribute.ETC_VEHICLEINFO_HISTORY, vehRc);
                    // 订单信息
                    Db.save(CommonAttribute.ONLINE_ORDERS, orderRc);


                    Db.delete(ETC_OFL_VEHICLEINFO, "vehicleId", etcOflVehRc);
                    Db.save(ETC_OFL_VEHICLEINFO, "vehicleId", etcOflVehRc);
//                    if (etcOflVehInfo ==null) {
////                        Db.save(CommonAttribute.ONLINE_ORDERS,"vehicleId", etcOflVehRc);
//                    } else {
//                        etcOflVehInfo.setDepUserId(etcOflVehRc.get("depUserId"));
//                        etcOflVehInfo.setDepVehicleId(etcOflVehRc.get("depVehicleId"));
//                        etcOflVehInfo.update();
//                    }


                    //更新申请信息表,结果通过
                    Object[] param1 = new Object[3];
                    param1[0] = appUserId;
                    param1[1] = auditDesc;
                    param1[2] = onlineApply.get("id");
                    Db.update(Db.getSql("mysql.updateOnlineAudit"), param1);
                    return true;
                });

            } else {
                // 审核不通过
                setAuditFailed(id, auditDesc, appUserId);
                return Result.success(null);
            }

        } catch (Exception e) {
            logger.error("{}执行审核流程失败:{},{}", serverName, e.toString(), e);
            return Result.sysError("处理异常");
        }
        logger.info("{}执行审核完成,耗时[{}]", serverName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return Result.success(null);
    }

    /**
     * 检查该车辆是否在其他渠道申请，或者绑卡
     */

    private Map checkVeh(String veh) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 判断该车辆是否申请了订单信息
        Record vehRc = Db.findFirst(DbUtil.getSql("checkVehIsApp"), veh);
        if (vehRc != null) {
            outMap.put("result", Result.bizError(704, "该车辆已申请订单，处理中"));
            outMap.put("bool", false);
            return outMap;
        }

        // 检查该车辆是否已经绑定银行，已经绑定银行卡，表示已经在其他渠道申请，不能在申请中
        Record oflVehRc = Db.findFirst(DbUtil.getSql("checkOflVehBindBank"), veh);
        if (oflVehRc != null) {
            outMap.put("result", Result.bizError(704, "该车辆已经在其他渠道绑卡"));
            outMap.put("bool", false);
            return outMap;
        }

        return outMap;
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
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
        }

        // 调用5.1车牌发行验证
        Result result = issuePcoflUploadService.entry(Kv.by("plateNum", dataRc.get("CarNumber"))
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateColor", dataRc.get("Calcolor")));
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}5.1车牌发行验证失败:{}", serviceName, result);
            outMap.put("bool", false);
            outMap.put("result", result);
            return outMap;
        }
        // 缺解析Data格式,认证过就会返回失败
        Map map = (Map) result.getData();
        int rs = (int) map.get("result");
        if (rs != 2 && rs != 3 && rs != 4) {
            outMap.put("bool", false);
            outMap.put("result", Result.bizError(704, (String) map.get("info")));
            return outMap;
        }
        String info = (String) map.get("info");
        // 部中心返回 ,更新到etcOflVehRc这里面
        String vehUuid = (String) map.get("vehicleId");
        if (vehUuid == null) {
            // 取车辆UUID
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
                if (dataRc.getStr("CarNumber").equals(vehMap.get("plateNum")) && dataRc.getInt("Calcolor") == (int) vehMap.get("plateColor")) {
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

        Result resultVlog = certifyVloflUploadService.entry(Kv.by("vehicleId", vehUuid)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("headtype", 1)
                .set("vin", dataRc.get("VIN"))
                .set("engineNum", dataRc.get("EngineNo"))
                .set("issueDate", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DD))
                .set("name", dataRc.get("CustomerName"))
                .set("plateNum", dataRc.get("CarNumber"))
                .set("registerDate", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DD))
                .set("useCharacter", character)
                .set("vehicleType", dataRc.get("velchel").toString())
                .set("type", getVehicleType((String) dataRc.get("velchel"), Integer.parseInt(dataRc.get("seats").toString())))
                .set("fileNum", dataRc.get("fileNum"))
                .set("approvedCount", dataRc.get("Seats"))
                .set("totalMass", dataRc.get("Seats"))
                .set("maintenaceMass", dataRc.get("Seats"))
                .set("permittedWeight", dataRc.get("Seats"))
                .set("outsideDimensions", getOutsideDimensions(dataRc.get("Dimension")))
                .set("permittedTowWeight", dataRc.get("Seats"))
                .set("vehicleModel", dataRc.get("Model"))
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

        return outMap;
    }

    /**
     * 数据转换为车辆绑卡信息
     */
    private Record dataToEtcOflVeh(Record appMap) {


        Record vehRc = new Record();

        // 车辆编号
        vehRc.set("vehicleId", appMap.get("CarNumber") + "_" + appMap.get("Calcolor"));
        // 客户编号
        vehRc.set("userId", appMap.get("userId"));
        // 银行卡号或账号
        vehRc.set("accountId", appMap.get("accountId"));
        // 银行预留手机号
        vehRc.set("linkMobile", appMap.get("Tel"));
        // 银行账户名称
        vehRc.set("bankUserName", null);
        // 银行卡绑定用户身份证号
        vehRc.set("certsn", null);
        // 客户类型
        // 办理类型 原数据是 1-储值卡；2-记账卡 cardType  ，，转换为1-记账，2-储值
        int cardtype = appMap.get("type") == null ? 0 : (int) appMap.get("type");
        if (cardtype == 1) {
            cardtype = 2;
        } else {
            cardtype = 1;
        }
        vehRc.set("userType", 1);
        // 企业用户ETC 业务协议号
        vehRc.set("protocolNumber", null);
        // 网点编号
        vehRc.set("posId", null);
        // 银行绑卡请求时间
        vehRc.set("genTime", null);
        // 银行绑卡校验请求流水号
        vehRc.set("trx_serno", null);
        // 员工推荐人工号
        vehRc.set("employeeId", null);
        // 原请求流水
        vehRc.set("org_trx_serno", null);
        // 绑定银行账户类型
        vehRc.set("acc_type", appMap.get("CardType"));
        // 绑定卡类型 1-信用卡 2-借记卡
        vehRc.set("cardType", cardtype);
        // 银行编码
        vehRc.set("bankPost", appMap.get("newbankid"));
        // 渠道类型
        vehRc.set("channelType", getChannelType(appMap.getStr("bankcode")));
        // 绑定状态1:绑定2:解绑
        vehRc.set("bindStatus", 1);

        // 创建时间
        vehRc.set("createTime", new Date());
        // 更新
        vehRc.set("updateTime", new Date());
        return vehRc;
    }

    /**
     * 线下开户
     *
     * @param dataRc
     * @param userId
     * @return
     */
    private Result callOffineOpenUser(Record dataRc, String userId) {

        Result result = offineOpenPersonalUser(dataRc);
        if (!result.getSuccess()) {
            logger.error("{}[userId={}]下线监管平台开户失败{}", serviceName, userId, result);
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
        tokenMap.put("accountId", dataMap.get("accountId"));
        //将accessToken、openId、expiresIn设置到encryptedData中
//        dataMap.put("accessToken", tokenMap.get("accessToken"));
//        dataMap.put("openId", tokenMap.get("openId"));
//        dataMap.put("expiresIn", tokenMap.get("expiresIn"));

        return result;
    }

    /**
     * 获取用户的accountId
     * 目前只有个人用户
     *
     * @param accountResult
     * @param record
     * @return
     */
    private Map getAccountId(Result accountResult, Record record) {
        Map daMap = (Map) accountResult.getData();
        List<Map> accountList = (List) daMap.get("encryptedData");
        Map dataMap = null;
        //查询匹配的账户
        for (Map accountMap : accountList) {
            int accountType = MathUtil.asInteger(accountMap.get("accountType"));
            //是否个人用户
            if (1 == accountType) {
                dataMap = accountMap;
            }
        }
        return dataMap;
    }

    /**
     * 线下渠道个人用户开户
     *
     * @param record
     * @return
     */
    @Override
    public Result offineOpenPersonalUser(Record record) {

        Result result = certifyCreituserService.entry(Kv.by("id", record.get("PassportId"))
                .set("name", record.get("CustomerName"))
                .set("userIdType", record.get("PassportType"))
                .set("positiveImageStr", null)
                .set("negativeImageStr", null)
                .set("phone", record.get("Tel"))
                .set("address", record.get("PostAddres"))
                .set("registeredType", 1)
                .set("issueChannelId", record.get("channelid")));

        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下渠道个人用户开户失败:{}", serviceName, result);
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
        // 用户证件号
        rc.set("userIdNum", appMap.get("PassportId"));
        // 用户证件类型
        rc.set("userIdType", appMap.get("PassportType"));
        // 用户名称
        rc.set("userName", appMap.get("CustomerName"));
        // 网点编号
        rc.set("posId", null);
        // 信息录入人工号
        rc.set("operatorId", null);
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


    /**
     * 判断车辆信息是否存在
     *
     * @param vehicle
     * @return
     */
    public boolean vehicleIsExist(String vehicle) {

        List list = Db.use(CommonAttribute.DB_SESSION_HJPT).find(DbUtil.getSql("querycheckvehicleinfoisexist"), vehicle);
        if (list.size() > 0) {
            return true;
        }
        Record record = Db.use(CommonAttribute.DB_SESSION_YG).findFirst(DbUtil.getSql("querycheckvehicleinfoisexistYG"), vehicle);
        if (record == null) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * 保存车辆信息
     *
     * @param appMap
     * @param userId
     * @return
     */
    private Record onlineApply2VehicleInfo(Record appMap, String userId) {

        Record record = new Record();

        // 车牌id
        record.set("id", appMap.get("carnumber") + "_" + appMap.get("calcolor"));
        // 收费车型type
        record.set("type", getVehicleType((String) appMap.get("velchel"), Integer.parseInt(appMap.get("seats").toString())));
        // 所属客户编号userid
        record.set("userid", userId);
        //机动车所有人名称 ownername
        record.set("ownername", appMap.get("customername"));
        //机动车所有人证件类型owneridtype
        record.set("owneridtype", appMap.get("passporttype"));
        //机动车所有人证件号码owneridnum
        record.set("owneridnum", appMap.get("passportid"));
        //所有人联系 ownertel
        record.set("ownertel", appMap.get("tel"));
        //所有人联系地址 address
        record.set("address", appMap.get("postaddres"));
        //指定联系人姓名 contact
        record.set("contact", appMap.get("postname"));
        //录入方式 registeredtype
        record.set("registeredtype", REGISTEREDTYPE_ONLINE);
        //录入渠道编号
        record.set("channelid", appMap.get("channelid"));
        //录入时间registeredtime
        record.set("registeredtime", new Date());
        //行驶证车辆类型 vehicletype
        record.set("vehicletype", null);
        //行驶证品牌型号 vehicleModel
        record.set("vehicleModel", appMap.get("model"));
        //车辆使用性质  usecharacter
        record.set("usecharacter", null);
        //车辆识别代号  VIN
        record.set("vin", appMap.get("vin"));
        //车辆发动机号  enginenum
        record.set("enginenum", appMap.get("engineno"));
        //注册日期  registerdate
        record.set("registerdate", null);
        //发证日期  issuedate
        record.set("issuedate", null);
        //档案编号  filenum
        record.set("filenum", null);
        //核定载人数  approvedcount
        record.set("approvedcount", VELCHEL_CAR.equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        //总质量  totalmass
        record.set("totalmass", null);
        //整备质量  maintenancemass
        record.set("maintenancemass", null);
        //核定载质量  permittedweight
        record.set("permittedweight", VELCHEL_TRUCK.equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        //外廓尺寸  outsidedimensions
        record.set("outsidedimensions", appMap.get("dimension") == null ? "" : ((String) appMap.get("dimension")).replace("*", "X").replace("m", "").replace("x", "X").replace("×", "X"));
        //准牵引总质量  permittedtowweight
        record.set("permittedtowweight", null);
        //检验记录  testrecord
        record.set("testrecord", null);
        //车轮数  wheelcount
        record.set("wheelcount", null);
        //车轴数  axlecount
        record.set("axlecount", null);
        //轴距  axledistance
        record.set("axledistance", null);
        //轴型   axistype
        record.set("axistype", null);
        //车脸识别特征版本号   vehiclefeatureversion
        record.set("vehiclefeatureversion", null);
        //车脸识别特征码   vehiclefeaturecode
        record.set("vehiclefeaturecode", null);
        //预付费/代扣账户编码   payaccountnum
        record.set("payaccountnum", null);
        //操作  operation
        record.set("operation", OperationEnum.ADD.getValue());
        //渠道类型  channeltype
        record.set("channeltype", getChannelType(appMap.getStr("bankcode")));
        // 信息录入网点 orgId
        record.set("orgid", null);
        // 信息录入人工号 operatorid
        record.set("operatorid", null);
        // 操作时间 opTime
        record.set("opTime", new Date());
        // 数据是否可上传状态 uploadstatus
        record.set("uploadstatus", null);

        // 创建时间createTime
        record.set("createTime", new Date());
        // 更新时间updateTime
        record.set("updateTime", new Date());

        return record;
    }

    /**
     * 保存客户信息
     *
     * @param appMap
     * @param userId
     * @return
     */
    private Record onlineApplyToUserInfo(Record appMap, String userId) {
        Record record = new Record();
        // 客户编号 id
        record.set("id", userId);
        //客户类型userType
        record.set("userType", appMap.get("customertype"));
        // 用户名称userName
        record.set("userName", appMap.get("customername"));
        // 开户人证件类型 userIdType
        record.set("userIdType", appMap.get("passporttype"));
        // 开户人证件号userIdNum
        record.set("userIdNum", appMap.get("passportid"));
        // 电话号码 tel
        record.set("tel", appMap.get("tel"));
        //  地址 address
        record.set("address", appMap.get("postarea") + " " + appMap.get("postaddres"));
        // 开户方式 registeredType
        record.set("registeredType", REGISTEREDTYPE_ONLINE);
        // 开户渠道编号 channelId
        record.set("channelId", appMap.get("channelid"));

        //开户时间  registeredTime
        record.set("registeredTime", new Date());
        // 部门/分支机构名称department
        record.set("department", null);
        //指定经办人姓名 agentName
        record.set("agentName", null);
        // 指定经办人证件类型 agentIdType
        record.set("agentIdType", null);
        // 指定经办人证件号 agentIdNum
        record.set("agentIdNum", null);
        // 单位开户行 bank
        record.set("bank", null);
        // 单位开户行地址bankAddr
        record.set("bankAddr", null);
        //单位开户行账号bankAccount
        record.set("bankAccount", null);
        // 单位纳税人识别号 taxpayerCode
        record.set("taxpayerCode", null);
        // 客户状态status
        record.set("status", CUSTOMER_STATUS_NORMAL);
        // 客户状态变更时间statusChangeTime
        record.set("statusChangeTime", new Date());
        // 人脸特征版本号 faceFeatureVersion
        record.set("faceFeatureVersion", null);
        // 人脸特征码faceFeatureCode
        record.set("faceFeatureCode", null);
        // 操作 operation
        record.set("operation", OperationEnum.ADD.getValue());
        // 渠道类型channelType
        record.set("channelType", getChannelType((String) appMap.get("bankcode")));

        // 是否需要上传部中心 0-不需要
        record.set("needupload", 0);
        // 操作时间 opTime
        record.set("opTime", new Date());
        // 上传状态uploadStatus
        record.set("uploadStatus", null);
        // 创建时间createTime
        record.set("createTime", new Date());
        // 更新时间updateTime
        record.set("updateTime", new Date());

        return record;
    }

    /*
       审核通过后保存订单信息
      */
    private Record onlineApply2OnlineOrder(Record appMap, String userId) {

        Record record = new Record();

        // 订单编号 orderId
        record.set("orderId", appMap.get("bookid"));
        // 订单生成时间orderCreateTime
        record.set("orderCreateTime", appMap.get("bookdate"));
        //车辆编号 vehicleId
        record.set("vehicleId", appMap.get("carnumber") + "_" + appMap.get("calcolor"));
        // 账户编号(客户编号)accountId
        record.set("accountId", userId);
        // 账户名称accountName
        record.set("accountName", appMap.get("customername"));
        //账户类型 userType1-个人客户；2-单位客户
        record.set("userType", appMap.get("customertype"));
        // 办理类型 原数据是 1-储值卡；2-记账卡 cardType  ，，转换为1-记账，2-储值
        int cardtype = appMap.get("type") == null ? 0 : (int) appMap.get("type");
        if (cardtype == 1) {
            cardtype = 2;
        } else {
            cardtype = 1;
        }
        record.set("cardType", cardtype);
        // 支付卡类型 payCardType
        record.set("payCardType", cardtype);
        // 账户手机号 mobile
        record.set("mobile", appMap.get("tel"));
        // 开户证件类型 userIdType
        record.set("userIdType", appMap.get("passporttype"));
        // 用户证件号码 userIdNum
        record.set("userIdNum", appMap.get("passportid"));
        // 收货人 postName
        record.set("postName", appMap.get("postname"));
        // 收货人电话 postPhone
        record.set("postPhone", appMap.get("posttel"));
        // 收货地址 postAddr
        record.set("postAddr", appMap.get("postarea") == null ? "" : appMap.get("postarea") + " " + appMap.get("postaddres"));
        // 车牌号码 plateNum
        record.set("plateNum", appMap.get("carnumber"));
        // 车牌颜色 plateColor
        record.set("plateColor", appMap.get("calcolor"));
        // 收费车型 vehicleType
        record.set("vehicleType", getVehicleType((String) appMap.get("velchel"), Integer.parseInt(appMap.get("seats").toString())));
        // 外廓尺寸 outsideDimensions
        record.set("outsideDimensions", appMap.get("dimension") == null ? "" : ((String) appMap.get("dimension")).replace("*", "X").replace("m", "").replace("x", "X").replace("×", "X"));
        // 车辆识别码 vin
        record.set("vin", appMap.get("vin"));
        // 核定人数 limitPerNum
        record.set("limitPerNum", "01".equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        // 总质量 totalWeight
        record.set("totalWeight", "11".equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        // 发动机号 engineNo
        record.set("engineNo", appMap.get("engineno"));
        // 车轮数 wheelCount
        record.set("wheelCount", null);
        // 车轴数 axleCount
        record.set("axleCount", null);
        // 轴距 axleDistance
        record.set("axleDistance", null);
        // 轴型 axisType
        record.set("axisType", null);
        // 订单类型 orderType
        record.set("orderType", ORDERTYPE_OBU);
        // 渠道类型 channelType
        record.set("channelType", getChannelType((String) appMap.get("bankcode")));
        // 银行编码 bankCode newbankid
        record.set("bankCode", appMap.get("newbankid"));
        // 创建时间 createTime
        record.set("createTime", new Date());
        // 更新时间 updateTime
        record.set("updateTime", new Date());
        // 邮寄状态 postStatus
        record.set("postStatus", POSTSTATUS_UNMAILED);
        // 状态 status 初始化
        record.set("status", STATUS_INIT);
        //车辆编号 vehicleCode
        record.set("vehicleCode", appMap.get("carnumber") + "_" + appMap.get("calcolor"));
        // 账户编号(客户编号)userId
        record.set("userId", userId);
        // 开户渠道编号 channelId
        record.set("channelId", appMap.get("channelid"));

        // 订单处理类型
        record.set("processType", OrderProcessTypeEnum.ORDER_NORMAL.getValue());

        return record;
    }

    /**
     * 取渠道类型
     *
     * @param oldid
     * @return
     */
    public String getChannelType(String oldid) {
        switch (oldid) {
            case "9100000":
                //工行
                return "040201";
            case "9000000":
                //建行
                return "040204";
            case "9500000":
                //中行
                return "040203";
            case "9200000":
                //农行
                return "040202";
            case "9400000":
                //邮储
                return "040205";
            case "9300000":
                //青海银行
                return "040206";
            default:
                throw new BizException("未取得渠道类型！");
        }

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
        String userId = generateUserIdService.queryUserIdCenterByUserIdNum(userIdType, userIdNum);
        if (StringUtil.isEmpty(userId)) {
            //进行查客编
            Map userMap = generateUserIdService.postQueryUserIdServer(userIdType, userIdNum);
            if (userMap.get("id") == null) {
                logger.error("{}生成客编异常", serverName);
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
     * @param onlineOrder
     * @return
     */
    public Map checkVehicle(Record onlineOrder) {

        PlateCheckJson plateCheckJson = new PlateCheckJson();

        String vehicleId = onlineOrder.get("carnumber") + "_" + onlineOrder.get("calcolor");
        Map outMap = new HashMap<>();
        outMap.put(MAP_KEY_ISCHECK, false);
        outMap.put(MAP_KEY_MSG, "");

        plateCheckJson.setVehiclePlate(onlineOrder.get("carnumber"));
        plateCheckJson.setVehicleColor(onlineOrder.get("calcolor"));
        plateCheckJson.setVehicleType(getVehicleType(onlineOrder.getStr("velchel"), onlineOrder.getInt("seats")));
        //1- 线下发行
        //2- 普通互联网发行
        //3- 互联网信用卡成套发行
        plateCheckJson.setIssueType(2);

        BaseUploadResponse response = upload(plateCheckJson, BASIC_PLATECHECK_REQ);

        logger.info("{}车牌唯一性校验响应信息:{}", serverName, response);

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}[vehicleId={}]车牌唯一性校验上传异常:{}",
                    serverName, vehicleId, response);
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
            return outMap;
        }

        if (VELCHEL_CHECK_NORMAL.equals(response.getResult())) {
            outMap.put(MAP_KEY_ISCHECK, true);
            outMap.put(MAP_KEY_MSG, null);
            return outMap;
        } else {
            logger.error("{}[vehicleId={}]车牌唯一性校验失败:{}", serverName, vehicleId, response.getInfo());
            //更新审核结果未不通过
            outMap.put(MAP_KEY_ISCHECK, false);
            outMap.put(MAP_KEY_MSG, response.getInfo());
            return outMap;
        }
    }

    /**
     * 更新申请表记录为审核不通过
     *
     * @param id
     * @param msg
     * @param appUserId
     */
    public void setAuditFailed(String id, String msg, String appUserId) {
        logger.info("{}审核不通过，id={},msg = {}", serverName, id, msg);

        Db.tx(() -> {
            //更新申请表的审核状态为未通过
            Record onlineApply = new Record();
            //id
            onlineApply.set("id", id);
            //1-通过，2-未通过
            onlineApply.set("ExamineResult", 2);
            //1-通过，2-未通过，
            onlineApply.set("flowStatus", 2);
            //描述
            onlineApply.set("ExamineDescription", msg);
            //审核时间
            onlineApply.set("approvalTime", new Date());
            //审核人员
            onlineApply.set("approvalId", appUserId);

            Db.update(TABLE_ONLINEAPPLY, "id", onlineApply);
            return true;
        });


    }

    /**
     * 获取收费车型
     *
     * @param velchel 车型 01-客车 11-货车
     * @param seats   车辆载重/座位数
     * @return
     */
    public int getVehicleType(String velchel, int seats) {

        if (StringUtil.isEmpty(velchel)) {
            logger.error("{}执行自动审核流程失败:车辆类型为空，不能转换为车型", serverName);
            throw new BizException("收费车型参数为空！");
        }
        if (VEHCILE_TYPE_TRUCK.equals(velchel)) {
            // 货车
            if (seats <= 2) {
                return 11;
            }
            if (seats <= 5) {
                return 12;
            }
            if (seats <= 10) {
                return 13;
            }
            if (seats <= 15) {
                return 14;
            } else {
                return 15;
            }
        } else {
            // 客车
            if (seats <= 7) {
                return 1;
            }
            if (seats <= 19) {
                return 2;
            }
            if (seats <= 39) {
                return 3;
            }
            if (seats >= 39) {
                return 4;
            }
        }

        return 0;
    }


    /**
     * 上传客户信息到部中心
     *
     * @param onlineOrder
     * @param userId
     * @return
     */
    private Map uploadCenterUserInfo(Record onlineOrder, String userId) {

        EtcUserinfoJson userinfoJson = new EtcUserinfoJson();
        // 客户编号
        userinfoJson.setId(userId);
        // 客户类型
        userinfoJson.setUserType(onlineOrder.get("customertype"));
        // 开户人名称
        userinfoJson.setUserName(onlineOrder.get("customername"));
        // 开户人证件类型
        userinfoJson.setUserIdType(onlineOrder.get("passporttype"));
        // 开户人证件号
        userinfoJson.setUserIdNum(onlineOrder.get("passportid"));
        // 开户人/指定经办人电号码
        userinfoJson.setTel(onlineOrder.get("tel"));
        // address
        userinfoJson.setAddress(onlineOrder.get("postaddres"));
        // 开户方式1 位数字
        //1-线上，2-线下
        userinfoJson.setRegisteredType(1);
        // 开户渠道编号
        userinfoJson.setChannelId(onlineOrder.get("channelid"));
        // 开户时间
        userinfoJson.setRegisteredTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 客户账户状态1- 正常 2- 注销
        userinfoJson.setStatus(1);
        // 客户状态变更时间
        userinfoJson.setStatusChangeTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 操作
        userinfoJson.setOperation(OperationEnum.ADD.getValue());


        //解密
        if (SysConfig.getEncryptionFlag()) {
            try {
                // 机动车所有人名称
                userinfoJson.setUserName(MyAESUtil.Decrypt( onlineOrder.getStr("customername")));
                // 机动车所有人证件号码
                userinfoJson.setUserIdNum(MyAESUtil.Decrypt( onlineOrder.getStr("passportid")));
                // 所有人联系方式
                userinfoJson.setTel(MyAESUtil.Decrypt( onlineOrder.getStr("tel")));
                // 所有人联系地址
                userinfoJson.setAddress(MyAESUtil.Decrypt( onlineOrder.getStr("postaddres")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        BaseUploadResponse response = upload(userinfoJson, BASIC_USERUPLOAD_REQ);

        logger.info("{}上传用户响应信息:{}", serverName, response);
        Map outMap = new HashMap<>();

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.info("{}[userId={}]客户信息上传失败:{}", serverName, userId, response);
            outMap.put("isUpload", false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
        } else {
            outMap.put("isUpload", true);
            outMap.put(MAP_KEY_MSG, "");
        }
        return outMap;

    }

    /**
     * 上传车辆信息到部中心
     *
     * @param appMap
     * @param userId
     * @return
     */
    private Map uploadCenterVehInfo(Record appMap, String userId) {

        EtcVehicleinfoJson vehicleinfoJson = new EtcVehicleinfoJson();
        // 车辆编号
        vehicleinfoJson.setId(appMap.get("carnumber") + "_" + appMap.get("calcolor"));
        // 收费车型
        vehicleinfoJson.setType(getVehicleType(appMap.getStr("velchel"), appMap.getInt("seats")));
        // 所属客户编号
        vehicleinfoJson.setUserId(userId);
        // 机动车所有人名称
        vehicleinfoJson.setOwnerName(appMap.get("customername"));
        // 机动车所有人证件类型
        vehicleinfoJson.setOwnerIdType(appMap.get("passporttype"));
        // 机动车所有人证件号码
        vehicleinfoJson.setOwnerIdNum(appMap.get("passportid"));
        // 所有人联系方式
        vehicleinfoJson.setOwnerTel(appMap.get("tel"));
        // 所有人联系地址
        vehicleinfoJson.setAddress(appMap.get("postaddres"));
        // 指定联系人姓名
        vehicleinfoJson.setContact(appMap.get("postname"));
        // 录入方式1-线上，2-线下
        vehicleinfoJson.setRegisteredType(1);
        // 录入渠道编号
        vehicleinfoJson.setChannelId(appMap.get("channelid"));
        // 录入时间
        vehicleinfoJson.setRegisteredTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 行驶证品牌型号
        vehicleinfoJson.setVehicleModel(appMap.get("model"));
        // 车辆识别代号
        vehicleinfoJson.setVin(appMap.get("vin"));
        // 车辆发动机号 engineNum
        vehicleinfoJson.setEngineNum(appMap.get("engineno"));
        // 核定载人数
        vehicleinfoJson.setApprovedCount(VEHCILE_TYPE_CUSTOMER.equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        // 核定载质量
        vehicleinfoJson.setPermittedTowWeight(VEHCILE_TYPE_TRUCK.equals(appMap.get("velchel")) ? appMap.get("seats") : 0);
        // 外廓尺寸
        vehicleinfoJson.setOutsideDimensions(getOutsideDimensions(appMap.get("dimension")));

        // 操作
        vehicleinfoJson.setOperation(OperationEnum.ADD.getValue());

        //解密
        if (SysConfig.getEncryptionFlag()) {

            try {
                // 机动车所有人名称
                vehicleinfoJson.setOwnerName(MyAESUtil.Decrypt( appMap.getStr("customername")));
                // 机动车所有人证件号码
                vehicleinfoJson.setOwnerIdNum(MyAESUtil.Decrypt( appMap.getStr("passportid")));
                // 所有人联系方式
                vehicleinfoJson.setOwnerTel(MyAESUtil.Decrypt( appMap.getStr("tel")));
                // 所有人联系地址
                vehicleinfoJson.setAddress(MyAESUtil.Decrypt( appMap.getStr("postaddres")));
                // 指定联系人姓名
                vehicleinfoJson.setContact(MyAESUtil.Decrypt( appMap.getStr("postname")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        BaseUploadResponse response = upload(vehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);

        logger.info("{}上传车辆响应信息:{}", serverName, response);
        Map outMap = new HashMap<>();

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.info("{}[vehicleId={}]上传车辆信息异常:{}",
                    serverName, appMap.get("id"), response);
            outMap.put("isUpload", false);
            outMap.put(MAP_KEY_MSG, response.getErrorMsg());
        } else {
            outMap.put("isUpload", true);
            outMap.put(MAP_KEY_MSG, "");
        }
        return outMap;

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

}

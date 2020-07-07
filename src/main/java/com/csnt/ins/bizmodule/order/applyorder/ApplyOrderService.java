package com.csnt.ins.bizmodule.order.applyorder;


import com.alibaba.druid.util.Base64;
import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.*;
import com.csnt.ins.bizmodule.order.queryuserid.GenerateUserIdService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.OrderProcessStatusEnum;
import com.csnt.ins.enumobj.OrderProcessTypeEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.json.EtcUserinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.model.json.PlateCheckJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 8920 在线订单接收接口
 *
 * @author cml
 */
public class ApplyOrderService implements IReceiveService, BaseUploadService {
    Logger logger = LoggerFactory.getLogger(ApplyOrderService.class);
    GenerateUserIdService generateUserIdService = new GenerateUserIdService();

    private String serviceName = "[8920在线订单接收接口]";
    /**
     * 车牌唯一性校验结果
     * 1- 可发行
     * 2- 不可发行
     */
    private final String VELCHEL_CHECK_NORMAL = "1";
    private final Integer CUSTOMER_TYPE_COMPANY = 2;
    private final Integer CUSTOMER_TYPE_PERSONAL = 1;

    private final String MAP_KEY_ISCHECK = "isCheck";
    private final String MAP_KEY_MSG = "msg";
    private final String BASIC_PLATECHECK_REQ = "BASIC_PLATECHECK_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String TABLE_ONLINE_PICTURE = "onlinepicture";
    private final String ETC_OFL_USERINFO = "etc_ofl_userinfo";
    private final String MSG_AUTHENTICATION = "已认证";
    private final String MSG_SIGNOFL = "车辆存在签约渠道绑定数据";
    private final String TABLE_ETC_OFL_VEHILCEINFO = "etc_ofl_vehicleinfo";
    private final String CHECK_NOMARL = "在分对分渠道办理过ETC";
    private final String CHECK_NOMARL1 = "青海发行,办理过发行业务";

    /**
     * 获取上传对象
     */
    public IUpload upload = CsntUpload.getInstance();

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


    @Override
    public Result entry(Map dataMap) {
        try {
            // 输入数据检查
            Record onlineApply = new Record().setColumns(dataMap);
//            onlineApply.set("userType", CUSTOMER_TYPE_PERSONAL);
            Map inCheckMap = checkInput(onlineApply);
            if (!(boolean) inCheckMap.get("bool")) {
                return (Result) inCheckMap.get("result");
            }
            String orderId = onlineApply.get("orderId");

            String userIdNum = onlineApply.getStr("userIdNum");
            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( onlineApply.getStr("userIdNum"));
            }

            // 查询客户编号
            Map userMap = getCustomerNum(onlineApply.getStr("userIdType"), userIdNum);
//            Map userMap = new HashMap<>();
//            userMap.put("id","63010119012719888");
//            userMap.put("flag",5);
//            userMap.put("isnew",1);

            logger.info("{}查询到客编:{}", serviceName, userMap);
            String userId = "";
            Record oflUserIdRc = null;
            if (StringUtil.isNotEmpty(userMap.get("id"))) {
                //找到了客户编号
                userId = (String) userMap.get("id");
                onlineApply.set("userId", userId);
                int flag = (int) userMap.get("flag");
                int isnew = (int) userMap.get("isnew");
                if (flag != CommonAttribute.ISSUER_TYPE_CSNT) {

                    Map userUploadMap = uploadCenterUserInfo(onlineApply, userId);
                    if (!(boolean) userUploadMap.get("isUpload")) {
                        logger.error("{}[orderId={}]客户信息上传失败:{}",
                                serviceName, orderId, userUploadMap.get(MAP_KEY_MSG));
                        return Result.sysError((String) userUploadMap.get(MAP_KEY_MSG));
                    } else {
                        // 先保存客户信息，客户信息上传成功先保存客户信息
                        Record finalUserRc = onlineApplyToUserInfo(onlineApply, userId);
                        if (finalUserRc == null) {
                           return  Result.bizError(799, "客户信息加密失败") ;
                        }
                        Db.tx(() -> {
                            Db.delete(CommonAttribute.ETC_USERINFO, finalUserRc);
                            Db.save(CommonAttribute.ETC_USERINFO, finalUserRc);
                            Db.save(CommonAttribute.ETC_USERINFO_HISTORY, finalUserRc);
                            return true;
                        });

                    }

                }
                //  上传线下监管平台
                // 判断数据是否上传线下部省平台  开户信息
                boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
                Map tkMap = null;
                // TODO: 2019/9/1 世纪恒通暂不开发线下监控
                if (bl ) {
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
                        if (Db.save(ETC_OFL_USERINFO, oflUserRc)) {
                            logger.info("{}保存线下监管平台ETC_OFL_USERINFO表数据成功", serviceName);
                        } else {
                            logger.error("{}保存线下监管平台ETC_OFL_USERINFO表数据失败", serviceName);
                            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR,
                                    "保存线下监管平台ETC_OFL_USERINFO表数据失败");
                        }
                    }
                } else {
                    Map checkMap = checkVehicle(onlineApply);
                    // 测试  默认成功
//                    checkMap.put(MAP_KEY_ISCHECK,true);
                    if (!((boolean) checkMap.get(MAP_KEY_ISCHECK))) {
                        // 退出当前数据处理
                        logger.error("{}车牌唯一性校验失败", serviceName);
                        return Result.vehCheckError((String) checkMap.get(MAP_KEY_MSG));
                    }
                }

            } else {
                logger.error("{}[orderId={}]客编查询失败,未获得客户编号", serviceName, orderId);
                return Result.sysError("未取得客户编号");
            }

            // 转换etc_ofl_vehicleinfo
            Record etcOflVehRc = dataToEtcOflVeh(onlineApply);
            // 检查客户是否在部中心线下渠道开户
            EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(onlineApply.get("userId"));
            if (etcOflUserinfo != null ) {
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
                        serviceName, orderId, vehUploadMap.get(MAP_KEY_MSG));
                return Result.sysError((String) vehUploadMap.get(MAP_KEY_MSG));
            }

            Record vehRc = onlineApply2VehicleInfo(onlineApply, userId);
            if (vehRc == null) {
                return  Result.bizError(799, "车辆信息加密失败") ;
            }
            Record orderRc = onlineApply2OnlineOrder(onlineApply, userId);
            if (orderRc == null) {
                return  Result.bizError(799, "订单信息加密失败") ;
            }
            // 转换图片数据
            Record picRc = onlinePicture(onlineApply);
            Db.tx(() -> {

                // 车辆信息，先删除，在新增
                Db.delete(CommonAttribute.ETC_VEHICLEINFO, vehRc);
                Db.save(CommonAttribute.ETC_VEHICLEINFO, vehRc);
                Db.save(CommonAttribute.ETC_VEHICLEINFO_HISTORY, vehRc);
                // 订单信息
                Db.save(CommonAttribute.ONLINE_ORDERS, orderRc);
                Db.save(TABLE_ONLINE_PICTURE, "id", picRc);

                Db.delete(TABLE_ETC_OFL_VEHILCEINFO, "vehicleId", etcOflVehRc);
                Db.save(TABLE_ETC_OFL_VEHILCEINFO, "vehicleId", etcOflVehRc);
                return true;
            });


            return Result.success(null);

        } catch (Throwable t) {
            logger.error("{}订单确认异常:{}", serviceName, t.toString(), t);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    private Map checkInput(Record inMap) {

        //打印输入日志
        Map logMap = new HashMap<>();
        logMap.put("orderId",inMap.get("orderId"));
        logMap.put("createTime",inMap.get("createTime"));
        logMap.put("userName",inMap.get("userName"));
        logMap.put("userType",inMap.get("userType"));
        logMap.put("cardType",inMap.get("cardType"));
        logMap.put("payCardType",inMap.get("payCardType"));
        logMap.put("mobile",inMap.get("mobile"));
        logMap.put("userIdType",inMap.get("userIdType"));
        logMap.put("userIdNum",inMap.get("userIdNum"));
        logMap.put("plateNum",inMap.get("plateNum"));
        logMap.put("plateColor",inMap.get("plateColor"));
        logMap.put("vehicleType",inMap.get("vehicleType"));

        logMap.put("department",inMap.get("department"));
        logMap.put("agentName",inMap.get("agentName"));
        logMap.put("agentIdType",inMap.get("agentIdType"));
        logMap.put("agentIdNum",inMap.get("agentIdNum"));
        logMap.put("bank",inMap.get("bank"));
        logMap.put("bankAddr",inMap.get("bankAddr"));
        logMap.put("bankAccount",inMap.get("bankAccount"));
        logMap.put("taxpayerCode",inMap.get("taxpayerCode"));

        logger.info("{}[msgtype=8920]当前请求参数为:{}", serviceName,logMap);

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        //订单编号
        String orderId = inMap.get("orderId");
        // 检查订单编号是否存在
        Record orderInfo = Db.findFirst(DbUtil.getSql("queryOnlineOrderByOrderId"), orderId);
        if (orderInfo != null) {
            logger.error("{}该订单编号已经存在，orderInfo={}", serviceName, orderInfo);
            outMap.put("result", Result.bizError(704, "该订单编号已经存在"));
            outMap.put("bool", false);
            return outMap;
        }


        // 订单创建时间
        String createTime = inMap.get("createTime");
        // 客户名称
        String userName = inMap.get("userName");
        //用户类型
        Integer userType = inMap.get("userType");
        //办理类型
        Integer cardType = inMap.get("cardType");
        //支付卡类型
        Integer payCardType = inMap.get("payCardType");

        if (StringUtil.isEmpty(orderId, createTime, userName, userType, cardType, payCardType)) {
            logger.error("{}参数orderId, createTime, userName, userType,cardType,payCardType不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("orderId, createTime, userName, userType,cardType,payCardType"));
            outMap.put("bool", false);
            return outMap;
        }
        //账户手机号
        String mobile = inMap.get("mobile");
        //开户证件类型
        Integer userIdType = inMap.get("userIdType");
        //开户证件号码
        String userIdNum = inMap.get("userIdNum");
        //地址信息
        String address = inMap.get("address");
        //渠道编码
        String channelId = inMap.get("channelId");

        // 取渠道对应的银行code
        Record channelIdInfo = Db.findFirst(DbUtil.getSql("queryBankIdByChannelId"), channelId);
        if (channelIdInfo == null || channelIdInfo.get("bankid") == null) {
            logger.error("{}参数未获取到渠道对应的银行编码，channelId={}", serviceName, channelId);
            outMap.put("result", Result.bizError(704, "未获取到渠道对应的银行编码"));
            outMap.put("bool", false);
            return outMap;
        } else {
            inMap.set("newbankid", channelIdInfo.get("bankid"));
        }

        //渠道类型
        String channelType = inMap.get("channelType");
        if (StringUtil.isEmpty(mobile, userIdType, userIdNum, address, channelId, channelType)) {
            logger.error("{}参数mobile, userIdType, userIdNum, address,channelId,channelType不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("mobile, userIdType, userIdNum, address,channelId,channelType"));
            outMap.put("bool", false);
            return outMap;
        }
        //车牌号码
        String plateNum = inMap.get("plateNum");
        //车牌颜色
        Integer plateColor = inMap.get("plateColor");
        //收费车型
        Integer vehicleType = inMap.get("vehicleType");
        //外廓尺寸
        String outsideDimensions = inMap.get("outsideDimensions");
        //车辆识别码
        String vin = inMap.get("vin");
        if (StringUtil.isEmpty(plateNum, plateColor, vehicleType, outsideDimensions, vin)) {
            logger.error("{}参数plateNum, plateColor, vehicleType, outsideDimensions,vin不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("plateNum, plateColor, vehicleType, outsideDimensions,vin"));
            outMap.put("bool", false);
            return outMap;
        }

        // 判断公司客户必输项
        if (userType.equals(CUSTOMER_TYPE_COMPANY)) {
            //部门/分支机构名称
            String department = inMap.get("department");
            //指定经办人姓名
            String agentName = inMap.get("agentName");
            //指定经办人证件类型
            Integer agentIdType = inMap.get("agentIdType");
            //指定经办人证件号
            String agentIdNum = inMap.get("agentIdNum");

            if (StringUtil.isEmpty(department, agentName, agentIdType, agentIdNum)) {
                logger.error("{}单位客户参数department, agentName, agentIdType, agentIdNum不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("department, agentName, agentIdType, agentIdNum"));
                outMap.put("bool", false);
                return outMap;
            }

        }

        //订单类型
        Integer orderType = inMap.get("orderType");
        //订单状态
        Integer orderStatus = inMap.get("orderStatus");
        //身份证正面照
        String imgPositive = inMap.get("imgPositive");
        //身份证反面照
        String imgBack = inMap.get("imgBack");
        //行驶证首页照
        String imgHome = inMap.get("imgHome");
        //行驶证信息照
        String imgInfo = inMap.get("imgInfo");
        //车头照
        String imgHeadstock = inMap.get("imgHeadstock");
        //手持身份证照
        String imgHold = inMap.get("imgHold");

        if (StringUtil.isEmpty(orderType, orderStatus, imgPositive, imgBack, imgHome, imgInfo, imgHeadstock)) {
            logger.error("{}参数orderType, orderStatus, imgPositive, imgBack,imgHome,imgInfo,imgHeadstock不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("orderType, orderStatus, imgPositive, imgBack,imgHome,imgInfo,imgHeadstock"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgPositive)) {
            logger.error("{}身份证正面照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "身份证正面照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgBack)) {
            logger.error("{}身份证反面照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "身份证反面照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgHome)) {
            logger.error("{}行驶证首页照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "行驶证首页照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgInfo)) {
            logger.error("{}行驶证信息照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "行驶证信息照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgHeadstock)) {
            logger.error("{}车头照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "车头照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (StringUtil.isNotEmpty(imgHold)) {
            if (!isLess200KCheckImgSize(imgHold)) {
                logger.error("{}手持身份证照文件过大，", serviceName);
                outMap.put("result", Result.bizError(704, "手持身份证照文件过大"));
                outMap.put("bool", false);
                return outMap;
            }
        }


        // 判断该车辆是否申请了订单信息
        String veh = plateNum + "_" + plateColor;
        Record vehRc = Db.findFirst(DbUtil.getSql("checkVehIsApp"), veh);
        if (vehRc != null) {
            outMap.put("result", Result.bizError(704, "该车辆已申请订单，处理中,渠道："+vehRc.getStr("issname") ));
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
        // 判断车辆是否有卡签信息
        Record vehicleRecord = Db.findFirst(DbUtil.getSql("queryEtcVehicleByVehicleId"), veh);
        if (vehicleRecord != null
                && (StringUtil.isNotEmpty(vehicleRecord.getStr("cardId")) || StringUtil.isNotEmpty(vehicleRecord.getStr("obuId")))) {
            outMap.put("result", Result.bizError(704, "该车辆有未核销的卡、签"));
            outMap.put("bool", false);
            return outMap;
        }

        return outMap;
    }

    /**
     * 调用部中心，检查车牌唯一性
     *
     * @param onlineOrder
     * @return
     */
    public Map checkVehicle(Record onlineOrder) {

        PlateCheckJson plateCheckJson = new PlateCheckJson();

        String vehicleId = onlineOrder.get("plateNum") + "_" + onlineOrder.get("plateColor");
        Map outMap = new HashMap<>();
        outMap.put(MAP_KEY_ISCHECK, false);
        outMap.put(MAP_KEY_MSG, "");

        plateCheckJson.setVehiclePlate(onlineOrder.get("plateNum"));
        plateCheckJson.setVehicleColor(onlineOrder.get("plateColor"));
        plateCheckJson.setVehicleType(onlineOrder.get("vehicleType"));
        //1- 线下发行
        //2- 普通互联网发行
        //3- 互联网信用卡成套发行
        plateCheckJson.setIssueType(2);
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
        userinfoJson.setUserType(onlineOrder.get("userType"));
        // 开户人名称
        userinfoJson.setUserName(onlineOrder.get("userName"));
        // 开户人证件类型
        userinfoJson.setUserIdType(onlineOrder.get("userIdType"));
        // 开户人证件号
        userinfoJson.setUserIdNum(onlineOrder.get("userIdNum"));
        // 开户人/指定经办人电号码
        userinfoJson.setTel(onlineOrder.get("mobile"));
        // address
        userinfoJson.setAddress(onlineOrder.get("address"));
        // 开户方式1 位数字
        //1-线上，2-线下
        userinfoJson.setRegisteredType(1);
        // 开户渠道编号
        userinfoJson.setChannelId(onlineOrder.get("channelId"));

        //部门/分支机构名称
        userinfoJson.setDepartment(onlineOrder.get("department"));
        //指定经办人姓名
        userinfoJson.setAgentName(onlineOrder.get("agentName"));
        //指定经办人证件类型
        userinfoJson.setAgentIdType(onlineOrder.get("agentIdType"));

        //指定经办人证件号
        userinfoJson.setAgentIdNum(onlineOrder.get("agentIdNum"));
        // 开户时间
        userinfoJson.setRegisteredTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 客户账户状态1- 正常 2- 注销
        userinfoJson.setStatus(1);
        // 客户状态变更时间
        userinfoJson.setStatusChangeTime(DateUtil.formatDateTime(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 操作
        userinfoJson.setOperation(OperationEnum.ADD.getValue());

        BaseUploadResponse response = upload(userinfoJson, BASIC_USERUPLOAD_REQ);

        logger.info("{}上传用户响应信息:{}", serviceName, response);
        Map outMap = new HashMap<>();

        if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.info("{}[userId={}]客户信息上传失败:{}", serviceName, userId, response);
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
        vehicleinfoJson.setId(appMap.get("plateNum") + "_" + appMap.get("plateColor"));
        // 收费车型
        vehicleinfoJson.setType(appMap.get("vehicleType"));
        // 所属客户编号
        vehicleinfoJson.setUserId(userId);
        // 机动车所有人名称
        vehicleinfoJson.setOwnerName(appMap.get("userName"));
        // 机动车所有人证件类型
        vehicleinfoJson.setOwnerIdType(appMap.get("userIdType"));
        // 机动车所有人证件号码
        vehicleinfoJson.setOwnerIdNum(appMap.get("userIdNum"));
        // 所有人联系方式
        vehicleinfoJson.setOwnerTel(appMap.get("mobile"));
        // 所有人联系地址
        vehicleinfoJson.setAddress(appMap.get("address"));
        // 指定联系人姓名
        vehicleinfoJson.setContact(appMap.get("userName"));
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
        vehicleinfoJson.setEngineNum(appMap.get("engineNo"));
        // 核定载人数
        vehicleinfoJson.setApprovedCount(appMap.get("limitPerNum"));
        // 核定载质量
        vehicleinfoJson.setPermittedTowWeight(appMap.get("totalWeight"));
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
        record.set("id", appMap.get("plateNum") + "_" + appMap.get("plateColor"));
        // 收费车型type
        record.set("type", appMap.get("vehicleType"));
        // 所属客户编号userid
        record.set("userid", userId);
        //机动车所有人名称 ownername
        record.set("ownername", appMap.get("userName"));
        //机动车所有人证件类型owneridtype
        record.set("owneridtype", appMap.get("userIdType"));
        //机动车所有人证件号码owneridnum
        record.set("owneridnum", appMap.get("userIdNum"));
        //所有人联系 ownertel
        record.set("ownertel", appMap.get("mobile"));
        //所有人联系地址 address
        record.set("address", appMap.get("address"));

        //指定联系人姓名 contact
        record.set("contact", appMap.get("address"));

        if (SysConfig.getEncryptionFlag()) {
            //加密存储
            try {
                //机动车所有人名称 ownername
                record.set("ownername",MyAESUtil.Encrypt( appMap.getStr("userName")));
                //机动车所有人证件号码owneridnum
                record.set("owneridnum",MyAESUtil.Encrypt( appMap.getStr("userIdNum")));
                //所有人联系 ownertel
                record.set("ownertel",MyAESUtil.Encrypt( appMap.getStr("mobile")));
                //所有人联系地址 address
                record.set("address",MyAESUtil.Encrypt( appMap.getStr("address")));
                //所有人联系地址 address
                record.set("contact",MyAESUtil.Encrypt( appMap.getStr("address")));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        //录入方式 registeredtype
        record.set("registeredtype", CommonAttribute.REGISTEREDTYPE_ONLINE);
        //录入渠道编号
        record.set("channelid", appMap.get("channelid"));
        //录入时间registeredtime
        record.set("registeredtime", new Date());
        //行驶证车辆类型 vehicletype
        record.set("vehicletype", null);
        //行驶证品牌型号 vehicleModel
        record.set("vehicleModel", null);
        //车辆使用性质  usecharacter
        record.set("usecharacter", null);
        //车辆识别代号  VIN
        record.set("vin", appMap.get("vin"));
        //车辆发动机号  enginenum
        record.set("enginenum", appMap.get("engineNo"));
        //注册日期  registerdate
        record.set("registerdate", null);
        //发证日期  issuedate
        record.set("issuedate", null);
        //档案编号  filenum
        record.set("filenum", null);
        //核定载人数  approvedcount
        record.set("approvedcount", appMap.get("limitPerNum"));
        //总质量  totalmass
        record.set("totalmass", appMap.get("totalWeight"));
        //整备质量  maintenancemass
        record.set("maintenancemass", null);
        //核定载质量  permittedweight
        record.set("permittedweight", null);
        //外廓尺寸  outsidedimensions
        record.set("outsidedimensions", getOutsideDimensions(appMap.get("outsideDimensions")));
        //准牵引总质量  permittedtowweight
        record.set("permittedtowweight", null);
        //检验记录  testrecord
        record.set("testrecord", null);
        //车轮数  wheelcount
        record.set("wheelcount", appMap.get("wheelCount"));
        //车轴数  axlecount
        record.set("axlecount", appMap.get("axlecount"));
        //轴距  axledistance
        record.set("axledistance", appMap.get("axleDistance"));
        //轴型   axistype
        record.set("axistype", appMap.get("axistype"));
        //车脸识别特征版本号   vehiclefeatureversion
        record.set("vehiclefeatureversion", null);
        //车脸识别特征码   vehiclefeaturecode
        record.set("vehiclefeaturecode", null);
        //预付费/代扣账户编码   payaccountnum
        record.set("payaccountnum", null);
        //操作  operation
        record.set("operation", OperationEnum.ADD.getValue());
        //渠道类型  channeltype
        record.set("channeltype", appMap.get("channelType"));
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
        record.set("userType", appMap.get("userType"));
        // 用户名称userName
        record.set("userName", appMap.get("userName"));
        // 开户人证件类型 userIdType
        record.set("userIdType", appMap.get("userIdType"));
        // 开户人证件号userIdNum
        record.set("userIdNum", appMap.get("userIdNum"));
        // 电话号码 tel
        record.set("tel", appMap.get("mobile"));
        //  地址 address
        record.set("address", appMap.get("address"));
        // 开户方式 registeredType
        record.set("registeredType", CommonAttribute.REGISTEREDTYPE_ONLINE);
        // 开户渠道编号 channelId
        record.set("channelId", appMap.get("channelId"));

        //开户时间  registeredTime
        record.set("registeredTime", new Date());

        // 部门/分支机构名称department
        record.set("department", appMap.get("department"));
        //指定经办人姓名 agentName
        record.set("agentName", appMap.get("agentName"));
        // 指定经办人证件类型 agentIdType
        record.set("agentIdType", appMap.get("agentIdType"));
        // 指定经办人证件号 agentIdNum
        record.set("agentIdNum", appMap.get("agentIdNum"));
        // 单位开户行 bank
        record.set("bank", appMap.get("bank"));
        // 单位开户行地址bankAddr
        record.set("bankAddr", appMap.get("bankAddr"));
        //单位开户行账号bankAccount
        record.set("bankAccount", appMap.get("bankAccount"));

        if (SysConfig.getEncryptionFlag()) {
            //存入表信息加密
            try {
                // 用户名称userName
                record.set("userName",MyAESUtil.Encrypt( appMap.getStr("userName")));
                // 开户人证件号userIdNum
                record.set("userIdNum", MyAESUtil.Encrypt( appMap.getStr("userIdNum")));
                // 电话号码 tel
                record.set("tel",  MyAESUtil.Encrypt( appMap.getStr("mobile")));
                //  地址 address
                record.set("address", MyAESUtil.Encrypt( appMap.getStr("address")));
                //指定经办人姓名 agentName
                record.set("agentName",MyAESUtil.Encrypt( appMap.getStr("agentName")));
                // 指定经办人证件号 agentIdNum
                record.set("agentIdNum",MyAESUtil.Encrypt( appMap.getStr("agentIdNum")));
                //单位开户行账号bankAccount
                record.set("bankAccount",MyAESUtil.Encrypt( appMap.getStr("bankAccount")));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // 单位纳税人识别号 taxpayerCode
        record.set("taxpayerCode", appMap.get("taxpayerCode"));
        // 客户状态status
        record.set("status", CommonAttribute.CUSTOMER_STATUS_NORMAL);
        // 客户状态变更时间statusChangeTime
        record.set("statusChangeTime", new Date());
        // 人脸特征版本号 faceFeatureVersion
        record.set("faceFeatureVersion", null);
        // 人脸特征码faceFeatureCode
        record.set("faceFeatureCode", null);
        // 操作 operation
        record.set("operation", OperationEnum.ADD.getValue());
        // 渠道类型channelType
        record.set("channelType", appMap.get("channelType"));

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
        record.set("orderId", appMap.get("orderId"));
        // 订单生成时间orderCreateTime
        record.set("orderCreateTime", DateUtil.parseDate(appMap.getStr("createTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        //车辆编号 vehicleId
        record.set("vehicleId", appMap.get("plateNum") + "_" + appMap.get("plateColor"));
        // 账户编号(客户编号)accountId
        record.set("accountId", userId);
        // 账户名称accountName
        record.set("accountName", appMap.get("userName"));
        //账户类型 userType1-个人客户；2-单位客户
        record.set("userType", appMap.get("userType"));
        // 办理类型
        record.set("cardType", appMap.get("cardType"));
        // 支付卡类型 payCardType
        record.set("payCardType", appMap.get("payCardType"));
        // 账户手机号 mobile
        record.set("mobile", appMap.get("mobile"));
        // 开户证件类型 userIdType
        record.set("userIdType", appMap.get("userIdType"));
        // 用户证件号码 userIdNum
        record.set("userIdNum", appMap.get("userIdNum"));
        // 收货人 postName
        record.set("postName", appMap.get("userName"));
        // 收货人电话 postPhone
        record.set("postPhone", appMap.get("mobile"));
        // 收货地址 postAddr
        record.set("postAddr", appMap.get("address"));

        if (SysConfig.getEncryptionFlag()) {
            try {
                // 账户名称accountName
                record.set("accountName", MyAESUtil.Encrypt( appMap.getStr("userName")));
                // 账户手机号 mobile
                record.set("mobile", MyAESUtil.Encrypt( appMap.getStr("mobile")));
                // 用户证件号码 userIdNum
                record.set("userIdNum",MyAESUtil.Encrypt( appMap.getStr("userIdNum")));
                // 收货人 postName
                record.set("postName", MyAESUtil.Encrypt( appMap.getStr("userName")));
                // 收货人电话 postPhone
                record.set("postPhone",MyAESUtil.Encrypt( appMap.getStr("mobile")));
                // 收货地址 postAddr
                record.set("postAddr", MyAESUtil.Encrypt( appMap.getStr("address")));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // 车牌号码 plateNum
        record.set("plateNum", appMap.get("plateNum"));
        // 车牌颜色 plateColor
        record.set("plateColor", appMap.get("plateColor"));
        // 收费车型 vehicleType
        record.set("vehicleType", appMap.get("vehicleType"));
        // 外廓尺寸 outsideDimensions
        record.set("outsideDimensions", getOutsideDimensions(appMap.get("outsideDimensions")));
        // 车辆识别码 vin
        record.set("vin", appMap.get("vin"));
        // 核定人数 limitPerNum
        record.set("limitPerNum", appMap.get("limitPerNum"));
        // 总质量 totalWeight
        record.set("totalWeight", appMap.get("totalWeight"));
        // 发动机号 engineNo
        record.set("engineNo", appMap.get("engineNo"));
        // 车轮数 wheelCount
        record.set("wheelCount", appMap.get("wheelCount"));
        // 车轴数 axleCount
        record.set("axleCount", appMap.get("axleCount"));
        // 轴距 axleDistance
        record.set("axleDistance", appMap.get("axleDistance"));
        // 轴型 axisType
        record.set("axisType", appMap.get("axisType"));
        // 订单类型 orderType
        record.set("orderType", CommonAttribute.ORDERTYPE_OBU);
        // 渠道类型 channelType
        record.set("channelType", appMap.get("channelType"));
        // 银行编码 bankCode newbankid
        record.set("bankCode", appMap.get("newbankid"));
        // 创建时间 createTime
        record.set("createTime", new Date());
        // 更新时间 updateTime
        record.set("updateTime", new Date());
        // 邮寄状态 postStatus
        record.set("postStatus", CommonAttribute.POSTSTATUS_UNMAILED);
        // 状态 status 初始化
        record.set("status", OrderProcessStatusEnum.PROCESSING.getValue());
        //车辆编号 vehicleCode
        record.set("vehicleCode", appMap.get("plateNum") + "_" + appMap.get("plateColor"));
        // 账户编号(客户编号)userId
        record.set("userId", userId);
        // 开户渠道编号 channelId
        record.set("channelId", appMap.get("channelId"));

        // 订单处理类型
        record.set("processType", OrderProcessTypeEnum.ORDER_VETC.getValue());

        // 订单状态
        record.set("orderStatus", appMap.get("orderStatus"));

        return record;
    }

    /**
     *  保存图片信息
     */

    /**
     * 保存在线订单图片,并保存订单的操作用户
     *
     * @param onlineOrder
     */
    private Record onlinePicture(Record onlineOrder) {
        Record onlinePicture = new Record();
        onlinePicture.set("id", StringUtil.getUUID());
        onlinePicture.set("userId", onlineOrder.getStr("orderId"));
        onlinePicture.set("bankCode", onlineOrder.getStr("newbankid"));
        onlinePicture.set("carNumber", onlineOrder.getStr("plateNum"));
        onlinePicture.set("calColor", onlineOrder.getStr("plateColor"));
        //将base64转为二进制流
        //身份证正面照
        byte[] imgPositive = Base64.base64ToByteArray(onlineOrder.get("imgPositive"));
        onlinePicture.set("imgPositive", imgPositive);

        // 身份证反面照
        byte[] imgBack = Base64.base64ToByteArray(onlineOrder.get("imgBack"));
        onlinePicture.set("imgBack", imgBack);

        // 行驶证首页照
        byte[] imgHome = Base64.base64ToByteArray(onlineOrder.get("imgHome"));
        onlinePicture.set("imgHome", imgHome);

        //行驶证信息照
        byte[] imgInfo = Base64.base64ToByteArray(onlineOrder.get("imgInfo"));
        onlinePicture.set("imgInfo", imgInfo);

        //车头照
        byte[] imgHeadstock = Base64.base64ToByteArray(onlineOrder.get("imgHeadstock"));
        onlinePicture.set("imgHeadstock", imgHeadstock);

        //手持身份证照
        if (StringUtil.isNotEmpty(onlineOrder.getStr("imgHold"))) {
            byte[] imgHold = Base64.base64ToByteArray(onlineOrder.get("imgHold"));
            onlinePicture.set("imgHold", imgHold);
        } else {
            onlinePicture.set("imgHold", null);
        }


        onlinePicture.set("createTime", new Date());
        //保存在线图片
//        Db.save(TABLE_ONLINE_PICTURE, "id", onlinePicture);
        return onlinePicture;
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

        Result result = certifyCreituserService.entry(Kv.by("id", record.get("userIdNum"))
                .set("name", record.get("userName"))
                .set("userIdType",record.get("userIdType"))
                .set("positiveImageStr", record.get("positiveImageStr"))
                .set("negativeImageStr",record.get("negativeImageStr"))
                .set("phone",record.get("mobile"))
                .set("address", record.get("address"))
                .set("registeredType",1)
                .set("issueChannelId", record.get("channelId")));

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

        Result result = certifyCreitcorpService.entry(Kv.by("id", record.get("userIdNum"))
                .set("name", record.get("userName"))
                .set("corpIdType",record.get("userIdType"))
                .set("positiveImageStr", record.get("imgPositive"))
                .set("negativeImageStr",record.get("imgBack"))
                .set("phone",record.get("mobile"))
                .set("address", record.get("address"))
                .set("registeredType",1)
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
        rc.set("userIdNum", appMap.get("userIdNum"));
        // 用户证件类型
        rc.set("userIdType", appMap.get("userIdType"));
        // 用户名称
        rc.set("userName", appMap.get("userName"));

        if (SysConfig.getEncryptionFlag()) {
            //存储字段加密
            try {
                // 用户证件号
                rc.set("userIdNum",MyAESUtil.Encrypt( appMap.getStr("userIdNum")));
                // 用户名称
                rc.set("userName",MyAESUtil.Encrypt( appMap.getStr("userName")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
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

    /**
     * 数据转换为车辆绑卡信息
     */
    private Record dataToEtcOflVeh(Record appMap) {
        Record vehRc = new Record();

        // 车辆编号
        vehRc.set("vehicleId", appMap.get("plateNum") + "_" + appMap.get("plateColor"));
        // 客户编号
        vehRc.set("userId", appMap.get("userId"));
        // 银行卡号或账号
        vehRc.set("accountId", appMap.get("accountId"));
        // 银行预留手机号
        vehRc.set("linkMobile", appMap.get("mobile"));
        // 银行账户名称
        vehRc.set("bankUserName", appMap.get("bankUserName"));
        // 银行卡绑定用户身份证号
        vehRc.set("certsn", appMap.get("certsn"));

        if (SysConfig.getEncryptionFlag()) {
            try {
                // 银行卡号或账号
                vehRc.set("accountId",MyAESUtil.Encrypt( appMap.getStr("accountId")));
                // 银行预留手机号
                vehRc.set("linkMobile", MyAESUtil.Encrypt( appMap.getStr("mobile")));
                // 银行账户名称
                vehRc.set("bankUserName",MyAESUtil.Encrypt( appMap.getStr("bankUserName")));
                // 银行卡绑定用户身份证号
                vehRc.set("certsn", MyAESUtil.Encrypt( appMap.getStr("certsn")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 客户类型
        vehRc.set("userType", appMap.get("userType"));
        // 企业用户ETC 业务协议号
        vehRc.set("protocolNumber", appMap.get("protocolNumber"));
        // 网点编号
        vehRc.set("posId", appMap.get("posId"));
        // 银行绑卡请求时间
        vehRc.set("genTime", DateUtil.parseDate(appMap.get("genTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 银行绑卡校验请求流水号
        vehRc.set("trx_serno", appMap.get("trx_serno"));
        // 员工推荐人工号
        vehRc.set("employeeId", appMap.get("employeeId"));
        // 原请求流水
        vehRc.set("org_trx_serno", appMap.get("org_trx_serno"));
        // 绑定银行账户类型
        vehRc.set("acc_type", appMap.get("acc_type"));
        // 绑定卡类型 1-信用卡 2-借记卡
        vehRc.set("cardType", appMap.get("cardType"));
        // 银行编码
        vehRc.set("bankPost", appMap.get("newbankid"));
        // 渠道类型
        vehRc.set("channelType", appMap.get("channelType"));
        // 绑定状态1:绑定2:解绑
        vehRc.set("bindStatus", 1);

        // 世纪恒通对应的合作渠道银行编号
        vehRc.set("issuerChannelId",  appMap.getStr("issuerChannelId"));

        // 创建时间
        vehRc.set("createTime", new Date());
        // 更新
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
            //调用凭证刷新接口
            Result resulttk = oflAuthTouch(etcOflUserinfo);
            if (!resulttk.getSuccess()) {
                outMap.put("bool", false);
                outMap.put("result", resulttk);
                return outMap;
            }
        }

        // 调用5.1车牌发行验证
        Result result = issuePcoflUploadService.entry(Kv.by("plateNum", dataRc.get("plateNum"))
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateColor", dataRc.get("plateColor")));
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}5.1车牌发行验证失败:{}", serviceName, result);
            outMap.put("bool", false);
            outMap.put("result", result);
            return outMap;
        }
        // 缺解析Data格式,认证过就会返回失败
        Map map = (Map) result.getData();
        int rs = (int) map.get("result");
        String nowDate = DateUtil.formatDate(new Date(),DateUtil.FORMAT_YYYY_MM_DD);
        if ( rs != 3 && rs != 4 &&  rs != 5 &&
                !(rs == 1 &&
                (map.get("info").toString().contains(CHECK_NOMARL) && map.get("info").toString().contains(CHECK_NOMARL)) )&&
                !(rs == 1 &&
                        (map.get("info").toString().contains(CHECK_NOMARL1) && map.get("info").toString().contains(CHECK_NOMARL1)) )) {
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
                if (dataRc.getStr("plateNum").equals(vehMap.get("plateNum")) && dataRc.getInt("plateColor") == (int) vehMap.get("plateColor")) {
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
                .set("vin", dataRc.get("vin"))
                .set("engineNum", dataRc.get("engineNo"))
                .set("issueDate", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DD))
                .set("name", dataRc.get("userName"))
                .set("plateNum", dataRc.get("plateNum"))
                .set("registerDate", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DD))
                .set("useCharacter", character)
                .set("vehicleType", dataRc.get("vehicleType").toString())
                .set("type", dataRc.get("vehicleType"))
                .set("fileNum", dataRc.get("fileNum"))
                .set("approvedCount", dataRc.get("limitPerNum"))
                .set("totalMass", dataRc.get("limitPerNum"))
                .set("maintenaceMass", dataRc.get("maintenaceMass"))
                .set("permittedWeight", dataRc.get("permittedWeight"))
                .set("outsideDimensions", getOutsideDimensions(dataRc.get("outsideDimensions")))
                .set("permittedTowWeight", null)
                .set("vehicleModel", null)
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
        Integer accType = dataRc.get("cardType");

        // 取channelType取值
        Record agRc = Db.findFirst(DbUtil.getSql("queryTblAgencyById"), dataRc.get("newbankid").toString());
        String channelType = "102";
        if (agRc != null) {
            channelType = agRc.get("SIGNCHANNEL");
        }
        if (dataRc.getStr("issuerChannelId") != null  && StringUtil.isNotEmpty(dataRc.getStr("issuerChannelId"))) {
            channelType = dataRc.getStr("issuerChannelId");
        }

        Result result1 = userSignoflUploadService.entry(Kv.by("vehicleId", vehUuid)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("plateNum", dataRc.get("plateNum"))
                .set("plateColor", dataRc.get("plateColor"))
                .set("signType", 1)
                .set("issueChannelId", dataRc.get("channelId"))
                .set("channelType", channelType)
                .set("cardType", 3)
                .set("account", null)
                .set("enableTime", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                .set("closeTime", getCloseTime(new Date()))
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
    /**
     * 更加开启时间获取关闭时间
     *
     * @param genTime
     * @return
     */
    private String getCloseTime(Date genTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(genTime);
        calendar.add(Calendar.YEAR, 10);
        return DateUtil.formatDate(calendar.getTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
    }

}

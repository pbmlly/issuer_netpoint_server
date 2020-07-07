package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.alibaba.druid.util.Base64;
import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserAccountUploadService;
import com.csnt.ins.bizmodule.order.queryuserid.GenerateUserIdService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.json.EtcUserinfoJson;
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
public class OfflineIssuerOpenUserService implements IReceiveService, BaseUploadService {
    protected static Logger logger = LoggerFactory.getLogger(OfflineIssuerOpenUserService.class);
    GenerateUserIdService generateUserIdService = new GenerateUserIdService();

    private String serviceName = "[8801线下渠道开户信息接收]";
    private final Integer CUSTOMER_TYPE_COMPANY = 2;
    private final Integer CUSTOMER_ISNEW = 1;
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String CERTIFY_CREITUSER_REQ = "CERTIFY_CREITUSER_REQ_";
    private final String CERTIFY_CREITCORP_REQ = "CERTIFY_CREITCORP_REQ_";

    private final String MAP_KEY_ISCHECK = "isCheck";
    private final String MAP_KEY_MSG = "msg";
    private final String ETC_OFL_PICTURE = "etc_ofl_picture";
    private final String ETC_OFL_USERINFO = "etc_ofl_userinfo";

    /**
     * 8831获取账户信息
     */
    UserAccountUploadService userAccountUploadService = new UserAccountUploadService();

    /**
     * 获取上传对象
     */
    public IUpload upload = CsntUpload.getInstance();

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
            // 查询客户编号
            Map userMap = getCustomerNum(dataRc.getStr("userIdType"), dataRc.get("userIdNum"));
            logger.info("{}查询到客编:{}", serviceName, userMap);
            String userId = "";
            int flag = 0;
            int isnew = 0;

            if (StringUtil.isNotEmpty(userMap.get("id"))) {
                //找到了客户编号
                userId = (String) userMap.get("id");
                flag = (int) userMap.get("flag");
                isnew = (int) userMap.get("isnew");
                /*if (isnew != CUSTOMER_ISNEW && flag == CommonAttribute.ISSUER_TYPE_CSNT) {
                    logger.error("{}[userIdType={},id={}]该客户已经存在无需新增", serviceName, dataRc.getStr("userIdType"), dataRc.get("userIdNum"));
                    return Result.sysError("该客户已经存在，客户编号：" + userId);
                }*/
            } else {
                logger.error("{}[userIdType={},id={}]客编查询失败,未获得客户编号", serviceName, dataRc.getStr("userIdType"), dataRc.get("userIdNum"));
                return Result.sysError("未取得客户编号");
            }

            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            Map tkMap = null;
            if (bl) {
                // 判断etc_ofl_userinfo是否存在该客户信息
                Record oflUserIdRc = Db.findFirst(DbUtil.getSql("queryOflUserByUserId"), userId);
                if (oflUserIdRc == null) {
                    //判断一下 证件号、证件类型、部门
                    Result result = checkUserUserIdNumTypeDepartment(dataRc);
                    if (!result.getSuccess()) {
                        return result;
                    }

                    result = callOffineOpenUser(dataRc, userId);
                    if (!result.getSuccess()) {
                        logger.error("{}[userId={}]下线渠道开户失败:{}",
                                serviceName, userId, result);
                        return result;
                    }
                    //取账号返回的相关信息
                    tkMap = (Map) result.getData();
                }
            }
            Record oflUserRc = dataToOflUser(dataRc, tkMap, userId);

            // 客户信息上传
            Map userUploadMap = uploadCenterUserInfo(dataRc, userId);
            if (!(boolean) userUploadMap.get("isUpload")) {
                logger.error("{}[userIdType={},id={} ]客户信息上传失败:{}",
                        serviceName, dataRc.getStr("userIdType"), dataRc.get("id"), userUploadMap.get(MAP_KEY_MSG));
                return Result.sysError((String) userUploadMap.get(MAP_KEY_MSG));
            }
            // 先保存客户信息，客户信息上传成功先保存客户信息
            Record finalUserRc = dataToUserInfo(dataRc, userId);

            // 图片信息
            Record picRc = dataToOflPicture(dataRc, userId);
            boolean dbFlag = Db.tx(() -> {
                Db.delete(CommonAttribute.ETC_USERINFO, finalUserRc);
                if (!Db.save(CommonAttribute.ETC_USERINFO, finalUserRc)) {
                    logger.error("{}保存ETC_USERINFO表数据失败", serviceName);
                    return false;
                }
                if (!Db.save(CommonAttribute.ETC_USERINFO_HISTORY, finalUserRc)) {
                    logger.error("{}保存ETC_USERINFO_HISTORY表数据失败", serviceName);
                    return false;
                }
                if (picRc != null) {
                    Db.delete(ETC_OFL_PICTURE, "userId", picRc);
                    if (!Db.save(ETC_OFL_PICTURE, picRc)) {
                        logger.error("{}保存ETC_OFL_PICTURE表数据失败", serviceName);
                        return false;
                    }
                }
                if (oflUserRc != null) {
                    if (!Db.save(ETC_OFL_USERINFO, oflUserRc)) {
                        logger.error("{}保存ETC_OFL_USERINFO表数据失败", serviceName);
                        return false;
                    }
                }
                return true;
            });

            if (dbFlag) {
                logger.info("{}线下监管平台开户成功", serviceName);
            } else {
                logger.error("{}[userId={}]线下监管平台开户失败,数据入库错误", serviceName, userId);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "线下监管平台开户失败,数据入库错误");
            }

            return Result.success(Kv.by("userId", userId));

        } catch (Exception e) {
            logger.error("{}用户开户异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    /**
     * 根据证件类型、证件号、部门
     *
     * @param dataRc
     * @return
     */
    private Result checkUserUserIdNumTypeDepartment(Record dataRc) {
        String userIdNum = "";
        if (SysConfig.getEncryptionFlag()) {
            //加密
            try {
                userIdNum =MyAESUtil.Encrypt( dataRc.get("userIdNum"));
            } catch (Exception e) {
                e.printStackTrace();
                return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR,
                        String.format("[证件号=%s]加密失败", userIdNum));
            }
        } else {
            userIdNum = dataRc.get("userIdNum");
        }
        int userIdType = dataRc.get("userIdType");
        int userType = dataRc.get("userType");

        String department = dataRc.get("department");
        Record record = null;

        // 公司客户
        if (userType == CUSTOMER_TYPE_COMPANY) {
            record = Db.findFirst(DbUtil.getSql("queryEtcUserWithOflUserByUserIdNumAndTypeDepartment"),
                    userIdType, userIdNum, department);
        } else {
            record = Db.findFirst(DbUtil.getSql("queryEtcUserWithOflUserByUserIdNumAndType"),
                    userIdType, userIdNum);
        }
        //判断是否开户
        if (record == null) {
            return Result.success(null);
        } else {
            if (userType == CUSTOMER_TYPE_COMPANY) {
                logger.error("{}当前用户的[userIdNum={},userIdType={},department={}]已经被注册",
                        serviceName, userIdNum, userIdType, department);
                return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR,
                        String.format("当前用户的[证件号=%s,证件类型=%d,部门=%s]已经被注册", userIdNum, userIdType, department));
            } else {
                logger.error("{}当前用户的[userIdNum={},userIdType={}]已经被注册",
                        serviceName, userIdNum, userIdType);
                return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR,
                        String.format("当前用户的[证件号=%s,证件类型=%d]已经被注册", userIdNum, userIdType));
            }

        }

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

        if (SysConfig.getEncryptionFlag()) {

            try {
                // 用户证件号
                rc.set("userIdNum",MyAESUtil.Encrypt( appMap.get("userIdNum")));
                // 用户名称
                rc.set("userName",MyAESUtil.Encrypt( appMap.get("userName")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 用户证件号
            rc.set("userIdNum", appMap.get("userIdNum"));
            // 用户名称
            rc.set("userName", appMap.get("userName"));
        }


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
     * 存储图片信息
     */
    private Record dataToOflPicture(Record appMap, String userId) {

        if (StringUtil.isEmpty(appMap.get("positiveImageStr"), appMap.get("negativeImageStr"))) {
            return null;
        }
        Record record = new Record();

        // 客户编号 userId
        record.set("userId", userId);
        // 证件类型
        record.set("userIdType", appMap.get("userIdType"));

        if (SysConfig.getEncryptionFlag()) {
            // 证件号码
            try {
                record.set("userIdNum",MyAESUtil.Encrypt( appMap.get("userIdNum")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            // 证件号码
            record.set("userIdNum", appMap.get("userIdNum"));
        }


        //证件正面照
        byte[] positiveImageStr = Base64.base64ToByteArray(appMap.get("positiveImageStr"));
        record.set("positiveImageStr", positiveImageStr);
        //证件反面
        byte[] negativeImageStr = Base64.base64ToByteArray(appMap.get("negativeImageStr"));
        record.set("negativeImageStr", negativeImageStr);

        return record;
    }

    /**
     * 保存客户信息
     *
     * @param appMap
     * @param userId
     * @return
     */
    private Record dataToUserInfo(Record appMap, String userId) {
        Record record = new Record();


        // 客户编号 id
        record.set("id", userId);
        //客户类型userType
        record.set("userType", appMap.get("userType"));

        if (SysConfig.getEncryptionFlag()) {
            //加密处理
            // 用户名称userName
            try {
                record.set("userName",MyAESUtil.Encrypt( appMap.get("userName")));
                // 开户人证件号userIdNum
                record.set("userIdNum", MyAESUtil.Encrypt( appMap.get("userIdNum")));
                // 电话号码 tel
                record.set("tel",MyAESUtil.Encrypt( appMap.get("tel")));
                //  地址 address
                record.set("address", MyAESUtil.Encrypt( appMap.get("address")));
                //指定经办人姓名 agentName
                record.set("agentName", MyAESUtil.Encrypt( appMap.get("agentName")));
                // 指定经办人证件号 agentIdNum
                record.set("agentIdNum", MyAESUtil.Encrypt( appMap.get("agentIdNum")));
                //单位开户行账号bankAccount
                record.set("bankAccount", MyAESUtil.Encrypt( appMap.get("bankAccount")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // 用户名称userName
            record.set("userName", appMap.get("userName"));
            // 开户人证件号userIdNum
            record.set("userIdNum", appMap.get("userIdNum"));
            // 电话号码 tel
            record.set("tel", appMap.get("tel"));
            //  地址 address
            record.set("address", appMap.get("address"));
            //指定经办人姓名 agentName
            record.set("agentName", appMap.get("agentName"));
            // 指定经办人证件号 agentIdNum
            record.set("agentIdNum", appMap.get("agentIdNum"));
            //单位开户行账号bankAccount
            record.set("bankAccount", appMap.get("bankAccount"));
        }



        // 开户人证件类型 userIdType
        record.set("userIdType", appMap.get("userIdType"));

        // 开户方式 registeredType
        record.set("registeredType", appMap.get("registeredType"));
        // 开户渠道编号 channelId
        record.set("channelId", appMap.get("channelId"));

        //开户时间  registeredTime
        record.set("registeredTime", DateUtil.parseDate(appMap.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 部门/分支机构名称department
        record.set("department", appMap.get("department"));

        // 指定经办人证件类型 agentIdType
        record.set("agentIdType", appMap.get("agentIdType"));

        // 单位开户行 bank
        record.set("bank", appMap.get("bank"));
        // 单位开户行地址bankAddr
        record.set("bankAddr", appMap.get("bankAddr"));

        // 单位纳税人识别号 taxpayerCode
        record.set("taxpayerCode", appMap.get("taxpayerCode"));
        // 客户状态status
        record.set("status", CommonAttribute.CUSTOMER_STATUS_NORMAL);
        // 客户状态变更时间statusChangeTime
        record.set("statusChangeTime", new Date());
        // 人脸特征版本号 faceFeatureVersion
        record.set("faceFeatureVersion", appMap.get("faceFeatureVersion"));
        // 人脸特征码faceFeatureCode
        record.set("faceFeatureCode", appMap.get("faceFeatureCode"));
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

        // 信息录入网点id
        record.set("orgId", appMap.get("orgId"));
        // 信息录入人工号
        record.set("operatorId", appMap.get("operatorId"));

        return record;
    }

    /**
     * 上传客户信息到部中心
     *
     * @param rcData
     * @param userId
     * @return
     */
    private Map uploadCenterUserInfo(Record rcData, String userId) {

        EtcUserinfoJson userinfoJson = new EtcUserinfoJson();
        // 客户编号
        userinfoJson.setId(userId);
        // 客户类型
        userinfoJson.setUserType(rcData.get("userType"));
        // 开户人名称
        userinfoJson.setUserName(rcData.get("userName"));
        // 开户人证件类型
        userinfoJson.setUserIdType(rcData.get("userIdType"));
        // 开户人证件号
        userinfoJson.setUserIdNum(rcData.get("userIdNum"));
        // 开户人/指定经办人电号码
        userinfoJson.setTel(rcData.get("tel"));
        // address
        userinfoJson.setAddress(rcData.get("address"));
        // 开户方式1 位数字
        //1-线上，2-线下
        userinfoJson.setRegisteredType(rcData.get("registeredType"));
        // 开户渠道编号
        userinfoJson.setChannelId(rcData.get("channelId"));
        // 开户时间
        userinfoJson.setRegisteredTime(DateUtil.parseDate(rcData.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        // 客户账户状态1- 正常 2- 注销
        userinfoJson.setStatus(1);
        // 客户状态变更时间
        userinfoJson.setStatusChangeTime(new Date());

        //部门/分支机构名称
        userinfoJson.setDepartment(rcData.get("department"));
        //指定经办人姓名
        userinfoJson.setAgentName(rcData.get("agentName"));
        //指定经办人证件类型
        userinfoJson.setAgentIdType(rcData.get("agentIdType"));

        //指定经办人证件号
        userinfoJson.setAgentIdNum(rcData.get("agentIdNum"));

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

    private Map checkInput(Record inMap) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        //证件号码
        String userIdNum = inMap.get("userIdNum");
        // 姓名
        String userName = inMap.get("userName");
        // 用户证件类型
        Integer userIdType = inMap.get("userIdType");
        // 客户类型
        Integer userType = inMap.get("userType");
        // 手机号码
        String tel = inMap.get("tel");
        // 地址
        String address = inMap.get("address");
        // 开户方式
        Integer registeredType = inMap.get("registeredType");
        if (StringUtil.isEmpty(userIdNum, userName, userIdType, userType, tel, address, registeredType)) {
            logger.error("{}参数userIdNum, userName, userIdType,userType, tel, address, registeredType不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("userIdNum, userName, userIdType,userType, tel, address, registeredType"));
            outMap.put("bool", false);
            return outMap;
        }
        //开户渠道编号
        String channelId = inMap.get("channelId");
        //开户时间
        String registeredTime = inMap.get("registeredTime");
        //信息录入网点id
        String orgId = inMap.get("orgId");
        //信息录入人工号
        String operatorId = inMap.get("operatorId");
        //渠道类型
        String channelType = inMap.get("channelType");
        if (StringUtil.isEmpty(channelId, registeredTime, orgId, operatorId, channelType)) {
            logger.error("{}参数channelId, registeredTime, orgId, operatorId,channelType不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("channelId, registeredTime, orgId, operatorId,channelType"));
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
            //正面图片
            String positiveImageStr = inMap.get("positiveImageStr");
            //反面图片
            String negativeImageStr = inMap.get("negativeImageStr");

            if (StringUtil.isEmpty(department, agentName, agentIdType, agentIdNum)) {
                logger.error("{}单位客户参数department, agentName, agentIdType, agentIdNum不能为空", serviceName);
                outMap.put("result", Result.paramNotNullError("department, agentName, agentIdType, agentIdNum"));
                outMap.put("bool", false);
                return outMap;
            }
            if (positiveImageStr != null) {
                if (!isLess200KCheckImgSize(positiveImageStr)) {
                    logger.error("{}身份证正面照文件过大", serviceName);
                    outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "身份证正面照文件过大,请在200K以内"));
                    outMap.put("bool", false);
                    return outMap;
                }
            }

            if (negativeImageStr != null) {
                if (!isLess200KCheckImgSize(negativeImageStr)) {
                    logger.error("{}身份证反面照文件过大", serviceName);
                    outMap.put("result", Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "身份证反面照文件过大,请在200K以内"));
                    outMap.put("bool", false);
                    return outMap;
                }
            }


        }

        return outMap;
    }

    /**
     * @return
     */
    private Result callOffineOpenUser(Record dataRc, String userId) {
        Result result = new Result();
        //客户类型嗯嗯。
        Integer userType = dataRc.get("userType");

        Record enRc = new Record().setColumns(dataRc);

        if (SysConfig.getEncryptionFlag()) {
            // 字段加密
            try {
                enRc.set("userIdNum", MyAESUtil.Encrypt( enRc.get("userIdNum")));
                enRc.set("userName", MyAESUtil.Encrypt( enRc.get("userName")));
                enRc.set("tel", MyAESUtil.Encrypt( enRc.get("tel")));
                enRc.set("address", MyAESUtil.Encrypt( enRc.get("address")));
                enRc.set("agentName", enRc.get("agentName")==null?null: MyAESUtil.Encrypt( enRc.get("agentName")));
                enRc.set("agentIdNum", enRc.get("agentIdNum")==null?null: MyAESUtil.Encrypt( enRc.get("agentIdNum")));
                enRc.set("bankAccount", enRc.get("bankAccount")==null?null: MyAESUtil.Encrypt( enRc.get("bankAccount")));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (userType.equals(CUSTOMER_TYPE_COMPANY)) {
            // 调用2.2 线下渠道单位用户开户
            result = offineOpenCorpUser(enRc);
        } else {
            // 调用2.1 线下渠道个人用户开户
            result = offineOpenPersonalUser(enRc);
        }
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
        if (dataMap == null) {
             return  new Result(788,"未取得部中心的客户Id");
        }
        tokenMap.put("accountId", dataMap.get("accountId"));

        //将accessToken、openId、expiresIn设置到accountResult的encryptedData中
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

    /*    *//**
     * 线下渠道个人用户开户
     *//*
    private Map offineOpenPersonalUser(Record record) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);
        outMap.put("data", null);

        Result result = certifyCreituserService.entry(Kv.by("id", record.get("userIdNum"))
                .set("name", record.get("userName"))
                .set("userIdType", record.get("userIdType"))
                .set("positiveImageStr", record.get("positiveImageStr"))
                .set("negativeImageStr", record.get("negativeImageStr"))
                .set("phone", record.get("tel"))
                .set("address", record.get("address"))
                .set("registeredType", record.get("registeredType"))
                .set("issueChannelId", record.get("channelId")));

        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下渠道个人用户开户失败:{}", serviceName, result);
            outMap.put("bool", false);
            outMap.put("result", result);
            return outMap;
        }
        outMap.put("data", result.getData());

        return outMap;
    }

    *//**
     * 线下渠道单位用户开户
     *//*
    private Map offineOpenCorpUser(Record record) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);
        outMap.put("data", null);

        Result result = certifyCreitcorpService.entry(Kv.by("id", record.get("userIdNum"))
                .set("name", record.get("userName"))
                .set("corpIdType", record.get("userIdType"))
                .set("positiveImageStr", record.get("positiveImageStr"))
                .set("negativeImageStr", record.get("negativeImageStr"))
                .set("phone", record.get("tel"))
                .set("address", record.get("address"))
                .set("registeredType", record.get("registeredType"))
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
            outMap.put("bool", false);
            outMap.put("result", result);
            return outMap;
        }
        outMap.put("data", result.getData());

        return outMap;

    }*/

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
}

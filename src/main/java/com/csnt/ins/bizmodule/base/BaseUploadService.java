package com.csnt.ins.bizmodule.base;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.service.AuthTouchService;
import com.csnt.ins.bizmodule.offline.service.CertifyCreitcorpService;
import com.csnt.ins.bizmodule.offline.service.CertifyCreituserService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.utils.*;
import com.jfinal.json.Jackson;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 公共上传服务
 *
 * @author duwanjiang
 * @date 2018/3/19
 */
public interface BaseUploadService {

    Logger logger = LoggerFactory.getLogger(BaseUploadService.class);

    String serviceName = "[公共上传服务]";
    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();
    /**
     * 用户凭证刷新服务
     */
    AuthTouchService authTouchService = new AuthTouchService();

    /**
     * 8820线下监管平台个人用户开户
     */
    CertifyCreituserService certifyCreituserService = new CertifyCreituserService();

    /**
     * 8821线下监管平台单位用户开户
     */
    CertifyCreitcorpService certifyCreitcorpService = new CertifyCreitcorpService();

    /**
     * 四类数据上传部中心
     *
     * @param model
     * @return
     */
    default BaseUploadResponse upload(Model model, String reqName) {
        logger.info("{}开始进行四类数据上传", serviceName);
        long startTime = System.currentTimeMillis();
        BaseUploadResponse response = new BaseUploadResponse();
        model.set("operation", OperationEnum.ADD.getValue());
        String json = Jackson.getJson().toJson(model);
        String fileName = reqName + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        if (SysConfig.CONFIG.getBoolean("is.close.ygz", false)) {
            // TODO: 2019/8/30 测试注释营改增接口
            response.setStateCode(200);
            response.setResult("1");
        } else {
            response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        }

        logger.info("{}当前上传四类基础信息[operation=1]的响应为:{},耗时[{}]",
                serviceName, response, DateUtil.diffTime(startTime, System.currentTimeMillis()));

        //707,重新上传更新  兼容--此户车辆信息已经上传
        if (response.getStateCode() == 704 || response.getStateCode() == 707 || response.getErrorMsg().contains("此户车辆信息已经上传")) {
            model.set("operation", OperationEnum.UPDATE.getValue());
            json = Jackson.getJson().toJson(model);
            fileName = reqName + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
            request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
            response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
            logger.info("{}当前上传四类基础信息[operation=2]的响应为:{},耗时[{}]",
                    serviceName, response, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        }
        return response;
    }

    /**
     * 黑名单数据上传
     *
     * @param sedMsg
     * @param reqName
     * @return
     */
    default BaseUploadResponse uploadYGZ(Object sedMsg, String reqName) {
        logger.info("{}开始进行营改增数据上传", serviceName);
        long startTime = System.currentTimeMillis();
        BaseUploadResponse response = new BaseUploadResponse();
        String json = Jackson.getJson().toJson(sedMsg);
        String fileName = reqName + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        if (SysConfig.CONFIG.getBoolean("is.close.ygz", false)) {
            // TODO: 2019/8/30 测试注释营改增接口
            response.setStateCode(200);
            response.setResult("1");
        } else {
            response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        }

        logger.info("{}上传营改增平台响应信息:{},耗时[{}]",
                serviceName, response, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return response;
    }

    /**
     * 线下凭证刷新
     *
     * @param etcOflUserinfo
     * @return
     */
    default Result oflAuthTouch(EtcOflUserinfo etcOflUserinfo) {
        logger.info("{}开始进行线下凭证刷新", serviceName);
        long startTime = System.currentTimeMillis();
        Result result = new Result();
        // 刷新用户凭证
        if (etcOflUserinfo != null) {
            Record userInfoRecord = Db.findFirst(DbUtil.getSql("queryEtcUserWithOflUserByUserIdNumAndType"),
                    etcOflUserinfo.getUserIdType(), etcOflUserinfo.getUserIdNum());
            if (userInfoRecord == null) {
                logger.error("{}用户凭证刷新异常，未查询到开户用户信息", serviceName);
                return Result.sysError("用户凭证刷新异常，未查询到开户用户信息");
            }
            //首先调用token刷新接口
            result = authTouchService.entry(Kv.by("openId", etcOflUserinfo.getOpenId())
                    .set("accessToken", etcOflUserinfo.getAccessToken()));
            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}调用线下监管平台凭证刷新[userIdNum={},userIdType={},tel={}]失败:{}",
                        serviceName, userInfoRecord.get("userIdNum"), userInfoRecord.get("userIdType"), userInfoRecord.get("tel"), result);

                int userType = userInfoRecord.getInt("userType");
                //1-个人用户 2-企业用户
                if (userType == 1) {
                    result = offineOpenPersonalUser(userInfoRecord);
                } else {
                    result = offineOpenCorpUser(userInfoRecord);
                }
            }

            if (result.getSuccess()) {
                //获取并更新开户反馈的授权token信息
                Map tokenMap = (Map) result.getData();
                if (StringUtil.isNotEmpty(tokenMap.get("openId"))) {
                    etcOflUserinfo.setOpenId(String.valueOf(tokenMap.get("openId")));
                }
                etcOflUserinfo.setAccessToken(String.valueOf(tokenMap.get("accessToken")));
                etcOflUserinfo.setExpiresIn(MathUtil.asInteger(tokenMap.get("expiresIn")));
                etcOflUserinfo.setUpdateTime(new Date());
                //更新用户的token
                etcOflUserinfo.update();
            }
        } else {
            result = Result.sysError("线下监管平台开户表未查询到当前用户");
        }
        logger.info("{}完成进行线下凭证刷新:{},耗时[{}]",
                serviceName, result, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return result;
    }

    /**
     * 线下监管平台个人用户开户
     */
    default Result offineOpenPersonalUser(Record record) {
        logger.info("{}开始进行线下监管平台个人用户开户", serviceName);
        long startTime = System.currentTimeMillis();
        Result result = new Result();

        if (SysConfig.getEncryptionFlag()) {
            try {
                result = certifyCreituserService.entry(Kv.by("id",MyAESUtil.Decrypt( record.get("userIdNum")))
                        .set("name",MyAESUtil.Decrypt( record.get("userName")))
                        .set("userIdType", record.get("userIdType"))
                        .set("positiveImageStr", null)
                        .set("negativeImageStr", null)
                        .set("phone",MyAESUtil.Decrypt( record.get("tel")))
                        .set("address", MyAESUtil.Decrypt( record.get("address")))
                        .set("registeredType", record.get("registeredType"))
                        .set("issueChannelId", record.get("channelId")));
            } catch (Exception e) {
                e.printStackTrace();
                result.setSuccess(false);
            }
        } else{
            result = certifyCreituserService.entry(Kv.by("id", record.get("userIdNum"))
                    .set("name", record.get("userName"))
                    .set("userIdType", record.get("userIdType"))
                    .set("positiveImageStr", null)
                    .set("negativeImageStr", null)
                    .set("phone", record.get("tel"))
                    .set("address", record.get("address"))
                    .set("registeredType", record.get("registeredType"))
                    .set("issueChannelId", record.get("channelId")));
        }



        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下监管平台个人用户[userIdNum={},userIdType={},tel={}]开户失败:{}",
                    serviceName, record.get("userIdNum"), record.get("userIdType"), record.get("tel"), result);
            result.setSuccess(false);
        }
        logger.info("{}完成进行线下监管平台个人用户开户,耗时[{}]",
                serviceName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return result;
    }

    /**
     * 线下监管平台单位用户开户
     */
    default Result offineOpenCorpUser(Record record) {
        logger.info("{}开始进行线下监管平台单位用户开户", serviceName);
        long startTime = System.currentTimeMillis();
        Result result = new Result();
        if (SysConfig.getEncryptionFlag()) {
            //  需要送加密信息，这里解密
            try {
                result = certifyCreitcorpService.entry(Kv.by("id", MyAESUtil.Decrypt( record.get("userIdNum")))
                        .set("name",MyAESUtil.Decrypt( record.get("userName")) )
                        .set("corpIdType", record.get("userIdType"))
                        .set("positiveImageStr", null)
                        .set("negativeImageStr", null)
                        .set("phone",MyAESUtil.Decrypt( record.get("tel")) )
                        .set("address", MyAESUtil.Decrypt( record.get("address")))
                        .set("registeredType", record.get("registeredType"))
                        .set("issueChannelId", record.get("channelId"))
                        .set("department", record.get("department"))
                        .set("agentName", MyAESUtil.Decrypt( record.get("agentName")))
                        .set("agentIdType", record.get("agentIdType"))
                        .set("agentIdNum", MyAESUtil.Decrypt( record.get("agentIdNum")))
                        .set("bank", record.get("bank"))
                        .set("bankAddr", record.get("bankAddr"))
                        .set("bankAccount", MyAESUtil.Decrypt( record.get("bankAccount")))
                        .set("taxpayerCode", record.get("taxpayerCode"))
                );
            } catch (Exception e) {
                e.printStackTrace();
                result.setSuccess(false);
            }
        } else {
            result = certifyCreitcorpService.entry(Kv.by("id", record.get("userIdNum"))
                    .set("name", record.get("userName"))
                    .set("corpIdType", record.get("userIdType"))
                    .set("positiveImageStr", null)
                    .set("negativeImageStr", null)
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
        }


        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}调用线下监管平台单位用户[userIdNum={},userIdType={},tel={}]开户失败:{}",
                    serviceName, record.get("userIdNum"), record.get("userIdType"), record.get("tel"), result);
            result.setSuccess(false);
        }
        logger.info("{}完成进行线下监管平台单位用户开户,耗时[{}]",
                serviceName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return result;

    }
}

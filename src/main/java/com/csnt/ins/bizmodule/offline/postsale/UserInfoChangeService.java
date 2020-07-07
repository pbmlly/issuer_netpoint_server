package com.csnt.ins.bizmodule.offline.postsale;

import com.alibaba.druid.util.Base64;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserChangeService;
import com.csnt.ins.bizmodule.offline.service.UserCorpchangeService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.json.EtcUserinfoJson;
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
 * 8817 用户信息变更接口
 *
 * @author duwanjiang
 **/
public class UserInfoChangeService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(UserInfoChangeService.class);

    private final String serverName = "[8817 用户信息变更接口]";


    private final String TABLE_USERINFO = "etc_userinfo";
    private final String TABLE_USERINFO_HISTORY = "etc_userinfo_history";

    private final String TABLE_ETC_OFL_PICTURE = "etc_ofl_picture";

    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String REPEAT_MSG = "重复";

    /**
     * 8820线下渠道个人用户变更
     */
    UserChangeService userChangeService = new UserChangeService();
    /**
     * 8820线下渠道单位用户变更
     */
    UserCorpchangeService userCorpchangeService = new UserCorpchangeService();

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

            //证件类型
            Integer userIdType = record.get("userIdType");
            //证件号码
            String userIdNum = record.get("userIdNum");
            //客户类型 1-个人客户，2-单位客户
            Integer userType = record.get("userType");
            //操作 2- 变更 3- 删除
            Integer operation = record.get("operation");

            String notNullParams = "userIdNum,userName,userIdType,userType,tel,address,registeredType,channelId,registeredTime,operation," +
                    "opTime,orgId,operatorId,channelType";
            if (StringUtil.isEmptyArg(record, notNullParams)) {
                logger.error("{}参数{}不能为空", serverName, notNullParams);
                return Result.paramNotNullError(notNullParams);
            }

            //如果是企业用户
            if (userType == 2) {
                notNullParams = "department,agentName,agentIdType,agentIdNum";
                if (StringUtil.isEmptyArg(record, notNullParams)) {
                    logger.error("{}参数{}不能为空", serverName, notNullParams);
                    return Result.paramNotNullError(notNullParams);
                }
            }

            if (operation == OperationEnum.ADD.getValue()) {
                logger.error("{}当前接口不支持新增操作", serverName);
                return Result.sysError("当前接口不支持新增操作");
            }

            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( record.getStr("userIdNum"));
            }

            // 取用户信息
            Record userInfo = Db.findFirst(DbUtil.getSql("queryEtcUserByUserId"), userIdType, userIdNum);
            if (userInfo == null) {
                logger.error("{}发行系统未找到当前用户:[userIdType={},userIdNum={}]", serverName, userIdType, userIdNum);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前用户");
            }

            //拷贝参数
            userInfo.setColumns(record);


            Record etcOflPictureRecord = new Record();
            etcOflPictureRecord.set("userId", userInfo.getStr("userId"));
            etcOflPictureRecord.set("userIdType", userInfo.getStr("userIdType"));
            etcOflPictureRecord.set("userIdNum", userIdNum);
            String positiveImageStr = record.getStr("positiveImageStr");
            String negativeImageStr = record.getStr("negativeImageStr");
            if (operation == OperationEnum.UPDATE.getValue()) {
                //修改信息

                //判断图片是否存在
                if (StringUtil.isNotEmpty(positiveImageStr)) {
                    if (!isLess200KCheckImgSize(positiveImageStr)) {
                        logger.error("{}当前positiveImageStr图片大小超过200K", serverName);
                        return Result.sysError("positiveImageStr图片大小超过200K");
                    } else {
                        //证件正面照
                        etcOflPictureRecord.set("positiveImageStr", Base64.base64ToByteArray(positiveImageStr));
                    }
                }
                if (StringUtil.isNotEmpty(negativeImageStr)) {
                    if (!isLess200KCheckImgSize(negativeImageStr)) {
                        logger.error("{}当前negativeImageStr图片大小超过200K", serverName);
                        return Result.sysError("negativeImageStr图片大小超过200K");
                    } else {
                        //证件反面
                        etcOflPictureRecord.set("negativeImageStr", Base64.base64ToByteArray(negativeImageStr));
                    }
                }
            } else if (operation == OperationEnum.DELETE.getValue()) {
                //删除信息
                String userId = userInfo.get("id");
                //判断当前用户是否绑定了卡和OBU
                long count = Db.findFirst(DbUtil.getSql("queryEtcCardinfoCountByUserId"), userId).get("num");
                if (count > 0) {
                    logger.error("{}当前用户有绑定的卡,不能删除", serverName);
                    return Result.sysError("当前用户有绑定的卡,不能删除");
                }
                count = Db.findFirst(DbUtil.getSql("queryEtcObuinfoCountByUserId"), userId).get("num");
                if (count > 0) {
                    logger.error("{}当前用户有绑定的OBU,不能删除", serverName);
                    return Result.sysError("当前用户有绑定的OBU,不能删除");
                }
            }


            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            if (bl) {
                Result result = sendOfflineUserChange(userInfo, userType, userInfo.getStr("id"));
                if (!result.getSuccess()) {
                    logger.error("{}上传用户信息变更失败:{}", serverName, result);
                    return result;
                }
            }

            //用户信息上传及变更
            BaseUploadResponse response = uploadBasicUserInfo(userInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传用户营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }

            //添加用户属性
            Date currentDate = new Date();
            userInfo.set("updateTime", currentDate);
            userInfo.remove("code");

            if (SysConfig.getEncryptionFlag()) {
                userInfo.set("userIdNum", userIdNum);
                userInfo.set("tel", MyAESUtil.Encrypt( userInfo.getStr("tel")));
                userInfo.set("address", MyAESUtil.Encrypt( userInfo.getStr("address")));
                userInfo.set("userName", MyAESUtil.Encrypt( userInfo.getStr("userName")));
                userInfo.set("agentName", MyAESUtil.Encrypt( userInfo.getStr("agentName")));
                userInfo.set("agentIdNum", MyAESUtil.Encrypt( userInfo.getStr("agentIdNum")));
                userInfo.set("bankAccount", MyAESUtil.Encrypt( userInfo.getStr("bankAccount")));
            }

            //存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                Db.update(TABLE_USERINFO, userInfo);

                userInfo.set("createTime", currentDate);
                Db.save(TABLE_USERINFO_HISTORY, ids, userInfo);

                //更新图片
                if (StringUtil.isNotEmpty(negativeImageStr) ||
                        StringUtil.isNotEmpty(negativeImageStr)) {
                    Db.delete(TABLE_ETC_OFL_PICTURE, "userId", etcOflPictureRecord);
                    Db.save(TABLE_ETC_OFL_PICTURE, "userId", etcOflPictureRecord);
                }
                return true;
            });
            if (flag) {
                logger.info("{}用户信息变更成功", serverName);
                return Result.success(null, "用户信息变更成功");
            } else {
                logger.error("{}用户信息变更失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "用户信息变更失败");
            }
        } catch (Throwable t) {
            logger.error("{}用户信息变更异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }


    /**
     * 上传用户信息到部中心
     *
     * @param userInfo
     * @return
     */
    private BaseUploadResponse uploadBasicUserInfo(Record userInfo) {
        EtcUserinfoJson etcUserinfoJson = new EtcUserinfoJson();
        etcUserinfoJson._setOrPut(userInfo.getColumns());
        logger.info("{}上传用户的内容为:{}", serverName, etcUserinfoJson);

        etcUserinfoJson.setRegisteredTime(DateUtil.parseDate(userInfo.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcUserinfoJson.setStatusChangeTime(new Date());

//        String json = Jackson.getJson().toJson(etcUserinfoJson);
//        String fileName = BASIC_USERUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(etcUserinfoJson, BASIC_USERUPLOAD_REQ);
        logger.info("{}上传用户响应信息:{}", serverName, response);
        return response;
    }

    /**
     * 检查图片的大小是否小于200K
     *
     * @param imgBase64Str
     * @return
     */
    private boolean isLess200KCheckImgSize(String imgBase64Str) {
        Integer size = StringUtil.getBase64ImgSize(imgBase64Str);
        logger.info("{}当前传入图片大小为[{}]字节", serverName, size);
        if (size / 1024 > SysConfig.getPictureSizeLimit()) {
            return false;
        }
        return true;
    }

    /**
     * 上传线下监控用户接口
     *
     * @param record
     * @param userType
     * @param userId
     * @return
     */
    private Result sendOfflineUserChange(Record record, Integer userType, String userId) {
        Result result = Result.success(null);
        // 查询是否线下开户是否存在
        boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));

        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(userId);

        if (etcOflUserinfo == null || !bl) {
            return result;
        }
        if (record.get("code") == null) {
            return Result.bizError(704, "验证码不能为空");
        }
        if (etcOflUserinfo != null) {
            //刷新用户凭证
            result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                return result;
            }
        }


        if (userType == 1) {
            // 个人客户信息变更
            result = userChangeService.entry(Kv.by("name", record.get("userName"))
                    .set("positiveImageStr", record.get("positiveImageStr"))
                    .set("negativeImageStr", record.get("negativeImageStr"))
                    .set("phone", record.get("tel"))
                    .set("address", record.get("address"))
                    .set("code", record.get("code"))
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId()));

            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}8822个人用户信息变更通知失败:{}", serviceName, result);
                return result;
            }
        } else {
            // 单位客户信息变更
            result = userCorpchangeService.entry(Kv.by("name", record.get("userName"))
                    .set("positiveImageStr", record.get("positiveImageStr"))
                    .set("negativeImageStr", record.get("negativeImageStr"))
                    .set("phone", record.get("tel"))
                    .set("address", record.get("address"))
                    .set("agentName", record.get("agentName"))
                    .set("agentIdType", record.get("agentIdType"))
                    .set("agentIdNum", record.get("agentIdNum"))
                    .set("bank", record.get("bank"))
                    .set("bankAddr", record.get("bankAddr"))
                    .set("bankAccount", record.get("bankAccount"))
                    .set("taxpayerCode", record.get("taxpayerCode"))
                    .set("code", record.get("code"))
                    .set("accountId", etcOflUserinfo.getDepUserId())
                    .set("accessToken", etcOflUserinfo.getAccessToken())
                    .set("openId", etcOflUserinfo.getOpenId()));

            if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}8823单位用户信息变更失败:{}", serviceName, result);
                return result;
            }
        }

        return result;
    }
}

package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.IssueMsgReqService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 8819 发送短信验证码接口
 *
 * @author duwanjiang
 **/
public class SendSMSValidateService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(SendSMSValidateService.class);

    private final String serverName = "[8819 发送短信验证码接口]";

    /**
     * 5.5  发送短信验证码
     */
    IssueMsgReqService issueMsgReqService = new IssueMsgReqService();


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

            //客户证件编码
            String userIdNum = record.get("userIdNum");
            //用户证件类型
            Integer userIdType = record.get("userIdType");
            //手机号
            String mobile = record.get("mobile");
            //渠道类型
            String channelType = record.get("channelType");

            if (StringUtil.isEmpty(userIdNum, userIdType, mobile, channelType)) {
                logger.error("{}参数userIdNum, userIdType, mobile, channelType不能为空", serverName);
                return Result.paramNotNullError("userIdNum, userIdType, mobile, channelType");
            }

            if (SysConfig.getEncryptionFlag()) {
                //加密，证件号码查询应用用加密后证件号码查询
                userIdNum = MyAESUtil.Encrypt( record.getStr("userIdNum"));
            }


            // 检查客户是否在部中心线下渠道开户
            EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, userIdType);
            if (etcOflUserinfo != null) {
                //刷新用户凭证
                Result result = oflAuthTouch(etcOflUserinfo);
                //判断刷新凭证是否成功，失败则直接退出
                if (!result.getSuccess()) {
                    logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                    return result;
                }

                //5.5  发送短信验证码
                result = issueMsgReqService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("mobile", mobile));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    logger.error("{}发送短信验证码失败:{}", serverName, result);
                    return result;
                }
                logger.info("{}发送短信校验成功", serverName);
                return Result.success(null, "发送短信校验成功");
            } else {
                logger.error("{}当前用户还未在总行开户", serverName);
                return Result.sysError("当前用户还未在总行开户");
            }

        } catch (Throwable t) {
            logger.error("{}发送短信校验异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }


}

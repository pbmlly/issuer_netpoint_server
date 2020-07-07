package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserSignoflUploadService;
import com.csnt.ins.enumobj.BankIdEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 8814 银行卡绑定/解绑通知接口
 *
 * @author duwanjiang
 **/
public class BindingOrUnBindingBankCardService implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(BindingOrUnBindingBankCardService.class);

    private final String serverName = "[8814 银行卡绑定/解绑通知接口]";

    /**
     * 4.1  车辆支付渠道绑定/ 解绑通知
     */
    UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();

    private final String TABLE_VEHICLEINFO = "etc_vehicleinfo";
    private final String TABLE_VEHICLEINFO_HISTORY = "etc_vehicleinfo_history";
    private final String REPEAT_MSG = "重复";
    private final String REPEATSING_MSG = "存在";

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

            //车辆编码
            String vehicleId = record.get("vehicleId");
            //用户类型
            Integer userType = record.get("userType");
            //银行卡号或账号
            String accountId = record.get("accountId");
            //银行预留手机号
            String linkMobile = record.get("linkMobile");
            //银行账户名称
            String bankUserName = record.get("bankUserName");
            //银行卡绑定用户身份证号
            String certsn = record.get("certsn");
            //企业用户ETC 业务协议号
            String protocolNumber = record.get("protocolNumber");
            //网点编号
            String posId = record.get("posId");
            //银行绑卡请求时间YYYY-MM-DDTHH:mm:ss
            String genTimeStr = record.get("genTime");
            //银行绑卡校验请求流水号
            String trxSerno = record.get("trx_serno");
            //员工推荐人工号
            String employeeId = record.get("employeeId");
            //员工推荐人工号
            String orgTrxSerno = record.get("org_trx_serno");
            //原请求流水
            Integer cardType = record.get("cardType");
            //绑定银行账户类型
            Integer accType = record.get("acc_type");
            //银行编码
            String bankPost = record.get("bankPost");
            //绑定状态
            Integer status = record.get("status");
            //渠道类型
            String channelType = record.get("channelType");

            if (StringUtil.isEmpty(vehicleId, userType, accountId, linkMobile, bankUserName,
                    certsn, channelType, posId, genTimeStr, trxSerno, employeeId, orgTrxSerno, cardType, accType, bankPost, status)) {
                logger.error("{}参数vehicleId, userType, accountId, linkMobile, bankUserName, certsn, channelType, posId,genTime,trxSerno,employeeId,orgTrxSerno,cardType,accType,bankPost,status不能为空", serverName);
                return Result.paramNotNullError("vehicleId, userType, accountId, linkMobile, bankUserName, certsn, channelType, posId,genTime,trxSerno,employeeId,orgTrxSerno,cardType,accType,bankPost,status");
            }

            //判断企业账户是否为空 1-个人 2-企业
            if (userType == 2) {
                if (StringUtil.isEmpty(protocolNumber)) {
                    logger.error("{}企业账号不能为空", serverName);
                    return Result.paramNotNullError("protocolNumber");
                }
            }

            Date genTime;
            try {
                genTime = DateUtil.parseDate(genTimeStr, DateUtil.FORMAT_YYYYM_MDDH_HMMSS);
                if (genTime == null) {
                    genTime = DateUtil.parseDate(genTimeStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                }
            } catch (Exception e) {
                logger.error("{}转换时间异常:{}", serverName, e.toString(), e);
                return Result.sysError("genTime时间格式异常");
            }

            EtcOflVehicleinfo etcOflVehicleinfo = new EtcOflVehicleinfo();

            //检查车辆信息是否存在
            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
            if (vehicleInfo == null) {
                logger.error("{}发行系统未找到当前车辆:{}", serverName, vehicleId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前车辆");
            }

            // 判断是否有签
            Record issRc = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByObu"), vehicleId);
            if (issRc != null ) {
                // 存在OBU 只能发行原有银行信息,
                Record cardRc = Db.findFirst(DbUtil.getSql("queryEtcVehinfoBycard"), vehicleId);
                if (cardRc != null) {
                    if (!cardRc.getStr("bankpost").equals(bankPost)) {
                        logger.error("{}储蓄卡用户只能绑定原银行={}", serverName, issRc);
                        return Result.bizError(789, "储蓄卡用户只能绑定原银行");
                    }
                }

            }


            etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflVehicleinfo == null) {
//                logger.error("{}未查询到车辆EtcOflVehicleinfo表的开户信息", serverName);
//                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆信息");
                etcOflVehicleinfo = new EtcOflVehicleinfo();
                etcOflVehicleinfo.setVehicleId(vehicleId);
                etcOflVehicleinfo.setUserId(vehicleInfo.getStr("userId"));
                etcOflVehicleinfo.setCreateTime(new Date());
                etcOflVehicleinfo.setUpdateTime(new Date());
                etcOflVehicleinfo.save();
            }

            // 检查客户是否在部中心线下渠道开户
            EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstEtcOflUserInfoByVehicleId(vehicleId);
            if (etcOflUserinfo != null) {
                //刷新用户凭证
                Result result = oflAuthTouch(etcOflUserinfo);
                //判断刷新凭证是否成功，失败则直接退出
                if (!result.getSuccess()) {
                    logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                    return result;
                }

                //检查车辆信息是否存在
//                etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
//                if (etcOflVehicleinfo == null) {
//                    logger.error("{}未查询到车辆EtcOflVehicleinfo表的开户信息", serverName);
//                    return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆信息");
//                }


                String vehiclePlate = vehicleId.split("_")[0];
                Integer vehicleColor = MathUtil.asInteger(vehicleId.split("_")[1]);
                //4.1  车辆支付渠道绑定/ 解绑通知
                if (etcOflVehicleinfo.getDepVehicleId() != null) {
                    result = userSignoflUploadService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                            .set("openId", etcOflUserinfo.getOpenId())
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                            .set("plateNum", vehiclePlate)
                            .set("plateColor", vehicleColor)
                            //签约方式 1-线上 2-线下
                            .set("signType", 2)
                            .set("issueChannelId", posId)
                            .set("channelType", getChannelType(bankPost))
                            //绑定的卡类型 1-信用卡 2-借记卡
                            .set("cardType", cardType)
                            .set("account", accountId)
                            .set("enableTime", DateUtil.formatDate(genTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                            .set("closeTime", getCloseTime(genTime))
//                        .set("info", "绑定")
                            //绑定状态 1:绑定 2:解绑
                            .set("status", status));
                    if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                            && !result.getMsg().contains(REPEATSING_MSG)) {
                        logger.error("{}解绑/绑定银行卡失败:{}", serverName, result);
                        return result;
                    }
                }

            }

            //设置新的绑卡信息
            etcOflVehicleinfo.setVehicleId(vehicleId);
            etcOflVehicleinfo.setUserType(userType);

            if (SysConfig.getEncryptionFlag()) {
                //银行卡号或账号
                etcOflVehicleinfo.setAccountId(MyAESUtil.Encrypt( record.getStr("accountId")));
                //银行预留手机号
                etcOflVehicleinfo.setLinkMobile(MyAESUtil.Encrypt( record.getStr("linkMobile")));
                //银行账户名称
                etcOflVehicleinfo.setBankUserName(MyAESUtil.Encrypt( record.getStr("bankUserName")));
                //银行卡绑定用户身份证号
                etcOflVehicleinfo.setCertsn(MyAESUtil.Encrypt( record.getStr("certsn")));
            } else {
                //银行卡号或账号
                etcOflVehicleinfo.setAccountId(accountId);
                //银行预留手机号
                etcOflVehicleinfo.setLinkMobile(linkMobile);
                //银行账户名称
                etcOflVehicleinfo.setBankUserName(bankUserName);
                //银行卡绑定用户身份证号
                etcOflVehicleinfo.setCertsn(certsn);
            }

            //企业用户ETC 业务协议号
            etcOflVehicleinfo.setProtocolNumber(protocolNumber);
            //网点编号
            etcOflVehicleinfo.setPosId(posId);
            //银行绑卡请求时间
            etcOflVehicleinfo.setGenTime(genTime);
            //银行绑卡校验请求流水号
            etcOflVehicleinfo.setTrxSerno(trxSerno);
            //员工推荐人工号
            etcOflVehicleinfo.setEmployeeId(employeeId);
            //原请求流水
            etcOflVehicleinfo.setOrgTrxSerno(orgTrxSerno);
            //绑定的卡类型1-信用卡 2-借记卡
            etcOflVehicleinfo.setCardType(cardType);
            //绑定银行账户类型
            etcOflVehicleinfo.setAccType(accType);
            //银行编码
            etcOflVehicleinfo.setBankPost(bankPost);
            //渠道类型
            etcOflVehicleinfo.setChannelType(channelType);
            //绑定状态1:绑定 2:解绑
            etcOflVehicleinfo.setBindStatus(status);


            //存储数据
            String ids = "id,opTime";
            final EtcOflVehicleinfo finalEtcOflVehicleinfo = etcOflVehicleinfo;
            boolean flag = Db.tx(() -> {
                if (!finalEtcOflVehicleinfo.update()) {
                    logger.error("{}保存finalEtcOflVehicleinfo表失败", serviceName);
                    return false;
                };
                vehicleInfo.set("channelType",channelType);
                vehicleInfo.set("updateTime",new Date());
                vehicleInfo.set("opTime", new Date());
                Db.update(TABLE_VEHICLEINFO, vehicleInfo);

                vehicleInfo.set("createTime", new Date());
                vehicleInfo.set("opTime", new Date());
                vehicleInfo.set("isuploadBank", 1);

                Db.save(TABLE_VEHICLEINFO_HISTORY, ids, vehicleInfo);

                return true;
            });
            if (flag) {
                logger.info("{}绑定/解绑银行卡成功", serverName);
                return Result.success(null, "绑定/解绑银行卡成功");
            } else {
                logger.error("{}绑定/解绑银行卡失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }
        } catch (Throwable t) {
            logger.error("{绑定/解绑银行卡异常:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
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

    /**
     * 获取对应的ChannelType
     *
     * @param bankPost
     * @return
     */
    private String getChannelType(String bankPost) {

        String channelType = "103";
        Record rc = Db.findFirst(DbUtil.getSql("queryTblAgency"), bankPost);

        if (rc != null) {
            channelType = rc.getStr("SIGNCHANNEL");
        }
        return channelType;
    }
}

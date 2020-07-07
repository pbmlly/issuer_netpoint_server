package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.IssueMsgReqService;
import com.csnt.ins.bizmodule.offline.service.UserSignoflUploadService;
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

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 8820 车辆解除绑定
 *
 * @author cml
 **/
public class VehicleIdUnbindServiece implements IReceiveService, BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(VehicleIdUnbindServiece.class);

    private final String serverName = "[8820 车辆解除绑定]";
    private final String REPEATSING_MSG = "已经";
    private final String NOM_MEG = "车辆无绑定的签约渠道";

    /**
     * 4.1  车辆支付渠道绑定/ 解绑通知
     */
    UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();

    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String TABLE_VEHICLEINFO_HISTORY = "etc_vehicleinfo_history";
    private final String REPEAT_MSG = "重复";
    private final String TABLE_VEH = "etc_vehicleinfo";

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
            String vehicleId = record.get("vehicleId");
            //车辆编号
            String opId = record.get("opId");

            if (StringUtil.isEmpty(vehicleId)) {
                logger.error("{}参数vehicleId不能为空", serverName);
                return Result.paramNotNullError("vehicleId");
            }
            // 判断车辆是否有卡，有签
            Record issRc = Db.findFirst(DbUtil.getSql("queryIssuInfoByVehicleId"), vehicleId,vehicleId);
            if (issRc != null) {
                logger.error("{}该车牌有未核销的卡，签={}", serverName, issRc);
                return Result.bizError(789, "该车牌有未核销的卡，签");
            }



            //检查车辆信息是否存在
            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
            if (vehicleInfo == null) {
                logger.error("{}发行系统未找到当前车辆:{}", serverName, vehicleId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前车辆");
            }

            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflVehicleinfo == null) {
                 etcOflVehicleinfo = new EtcOflVehicleinfo();
//                logger.error("{}未查询到车辆EtcOflVehicleinfo表的开户信息", serverName);
//                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆信息");
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

                String vehiclePlate = vehicleId.split("_")[0];
                Integer vehicleColor = MathUtil.asInteger(vehicleId.split("_")[1]);
                //4.1  车辆支付渠道绑定/ 解绑通知
                if (etcOflVehicleinfo.getDepVehicleId() != null && etcOflVehicleinfo.getBankPost() != null) {
                    String account = "";
                    if (SysConfig.getEncryptionFlag()) {
                        account = MyAESUtil.Decrypt(etcOflVehicleinfo.getAccountId());
                    } else {
                        account = etcOflVehicleinfo.getAccountId();
                    }
                    result = userSignoflUploadService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                            .set("openId", etcOflUserinfo.getOpenId())
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                            .set("plateNum", vehiclePlate)
                            .set("plateColor", vehicleColor)
                            //签约方式 1-线上 2-线下
                            .set("signType", vehicleInfo.get("registeredType"))
                            .set("issueChannelId", vehicleInfo.get("channelId"))
                            .set("channelType", getChannelType(etcOflVehicleinfo.getBankPost()))
//                            //绑定的卡类型 1-信用卡 2-借记卡
                            .set("cardType", etcOflVehicleinfo.getCardType())
                            .set("account", account)
                            .set("enableTime", DateUtil.formatDate(etcOflVehicleinfo.getGenTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                            .set("closeTime", getCloseTime(etcOflVehicleinfo.getGenTime()))
//                        .set("info", "绑定")
                            //绑定状态 1:绑定 2:解绑
                            .set("status", 2));
                    if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                            && !result.getMsg().contains(REPEATSING_MSG)
                            && !result.getMsg().contains(NOM_MEG)) {
                        logger.error("{}解绑/绑定银行卡失败:{}", serverName, result);
                        return result;
                    }
                }

            }

            //删除车辆信息
            BaseUploadResponse response = uploadBasicVehicleInfo(vehicleInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传车辆营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }


             if (etcOflVehicleinfo != null) {
                 etcOflVehicleinfo.setBankPost(null);
                 etcOflVehicleinfo.setAccountId(null);
                 etcOflVehicleinfo.setUserType(null);
                 etcOflVehicleinfo.setLinkMobile(null);
                 etcOflVehicleinfo.setBankUserName(null);
                 etcOflVehicleinfo.setCertsn(null);
                 etcOflVehicleinfo.setProtocolNumber(null);
                 etcOflVehicleinfo.setPosId(null);
                 etcOflVehicleinfo.setGenTime(null);
                 etcOflVehicleinfo.setTrxSerno(null);
                 etcOflVehicleinfo.setOrgTrxSerno(null);
                 etcOflVehicleinfo.setCardType(null);
                 etcOflVehicleinfo.setAccType(null);

             }
            String ids = "id,opTime";
            final EtcOflVehicleinfo finalEtcOflVehicleinfo = etcOflVehicleinfo;
            boolean flag = Db.tx(() -> {
                Db.delete(TABLE_VEH,"id",vehicleInfo);

                vehicleInfo.set("createTime", new Date());
                vehicleInfo.set("opTime", new Date());
                vehicleInfo.set("operatorId", opId);
                Db.save(TABLE_VEHICLEINFO_HISTORY, ids, vehicleInfo);
                finalEtcOflVehicleinfo.update();
                return true;
            });
            if (flag) {
                logger.info("{}车辆解除绑定成功", serverName);
                return Result.success(null, "车辆解除绑定成功");
            } else {
                logger.error("{}车辆解除绑定失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "车辆解除绑定失败");
            }

        } catch (Throwable t) {
            logger.error("{}发送短信校验异常:{}", serverName, t.getMessage(), t);
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
        etcVehicleinfoJson.setRegisteredTime(vehicleInfo.get("registeredTime"));
        etcVehicleinfoJson.setRegisterDate(vehicleInfo.get("registerDate"));
        etcVehicleinfoJson.setIssueDate(vehicleInfo.get("issueDate"));

        if (SysConfig.getEncryptionFlag()) {
            try {
                //解密上传部中心
                etcVehicleinfoJson.setOwnerName(MyAESUtil.Decrypt( vehicleInfo.getStr("ownerName")));
                etcVehicleinfoJson.setOwnerIdNum(MyAESUtil.Decrypt( vehicleInfo.getStr("ownerIdNum")));
                etcVehicleinfoJson.setAddress(MyAESUtil.Decrypt( vehicleInfo.getStr("address")));
                etcVehicleinfoJson.setOwnerTel(MyAESUtil.Decrypt( vehicleInfo.getStr("ownerTel")));
                etcVehicleinfoJson.setContact(MyAESUtil.Decrypt( vehicleInfo.getStr("contact")));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        etcVehicleinfoJson.setOperation(3);
//        String json = Jackson.getJson().toJson(etcVehicleinfoJson);
//        String fileName = BASIC_VEHICLEUPLOAD_REQ + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
//        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = uploadYGZ(etcVehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);
        logger.info("{}上传车辆响应信息:{}", serverName, response);
        return response;
    }
}

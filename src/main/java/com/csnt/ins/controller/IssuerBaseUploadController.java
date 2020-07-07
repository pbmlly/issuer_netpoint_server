package com.csnt.ins.controller;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.enumobj.CardYGZStatusEnum;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.model.json.EtcUserinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.Date;

/**
 * 4类基础信息上传，删除
 * @ClassName IssuerChangeUserIdController
 * @Description TODO
 * @Author cml
 * @Date 2019/7/23 21:06
 * Version 1.0
 **/
public class IssuerBaseUploadController extends Controller {

    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();


    /**
     * 获取上传对象
     */
    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String NOR_MAORL = "不存在";
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

    public void index() {
        String vehicleId = getPara("vehicleId");
        String obuId = getPara("obuId");
        String userId = getPara("userId");
        String cardId = getPara("cardId");
        String operation = getPara("operation");
        if (StringUtil.isEmpty(operation)) {
            renderJson("操作类型不能为空");
            return;
        }
        if (!"1".equals(operation) && !"3".equals(operation)) {
            renderJson(new String("操作类型不正确") + operation);
            return;
        }

        if (vehicleId != null) {
            // 取车辆信息
            Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
            if (vehicleInfo == null) {
                renderJson(new String("未找到该车辆信息") + vehicleId);
                return;
            } else {
                BaseUploadResponse res =  uploadBasicVehicleInfo(vehicleInfo,Integer.parseInt(operation));
                if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    renderJson(new String(res.getErrorMsg()));
                    return ;
                }
            }
        }

        // 取客户信息
        if (userId != null) {
            Record userRc = Db.findFirst(DbUtil.getSql("queryEtcUserInfoByUserId"), userId);
            if (userRc == null) {
                renderJson(new String("客户编号不存在") + userId);
                return;
            }  else {
                // 上传客户信息
                BaseUploadResponse res =  uploadBasicUserInfo(userRc,Integer.parseInt(operation));
                if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    renderJson("上传客户信息失败：" + new String(res.getErrorMsg()));
                    return ;
                }
            }
        }


        // 如果OBU不为空
        if (obuId != null) {
            Record obuRc = Db.findFirst(DbUtil.getSql("queryEtcObuInfoByOBUId"), obuId);
            if (obuRc == null) {
                renderJson(new String("未找到该OBU信息") + obuId);
                return;
            }
            // 上传OBU信息
            BaseUploadResponse res =  uploadBasicObuInfo(obuRc,Integer.parseInt(operation));
            if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                renderJson(new String(res.getErrorMsg()));
                return ;
            }
        }

        if (cardId != null) {
            Record cardRc = Db.findFirst(DbUtil.getSql("queryEtcCardInfoByCardId"), cardId);
            if (cardRc == null) {
                renderJson(new String("未找到该ETC卡信息") + obuId);
                return;
            } else  {
                // 上传CARD信息
                BaseUploadResponse res =  uploadBasicCardInfo(cardRc,Integer.parseInt(operation));
                if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    renderJson(new String(res.getErrorMsg()));
                    return ;
                }
            }
        }


        renderJson(new String("成功"));
    }

    /**
     * 上传OBU信息到部中心
     *
     * @param obuInfo
     * @return
     */
    private BaseUploadResponse uploadBasicObuInfo(Record obuInfo,int operation) {
        //installType=1时 installChannelId =0
        if (1 == MathUtil.asInteger(obuInfo.get("installType"))) {
            obuInfo.set("installChannelId", 0);
        }
        obuInfo.set("operation",operation);

        EtcObuinfoJson etcObuinfoJson = new EtcObuinfoJson();
        etcObuinfoJson._setOrPut(obuInfo.getColumns());
        // 时间需要转换为字符串
        etcObuinfoJson.setEnableTime(DateUtil.formatDate(obuInfo.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setExpireTime(DateUtil.formatDate(obuInfo.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setRegisteredTime(DateUtil.formatDate(obuInfo.get("registeredTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setInstallTime(DateUtil.formatDate(obuInfo.get("installTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcObuinfoJson.setStatusChangeTime(DateUtil.getCurrentTime_yyyy_MM_ddTHHmmss());

         BaseUploadResponse response = upload(etcObuinfoJson, BASIC_OBUUPLOAD_REQ);
         return response;
    }

    /**
     * 上传车辆信息到部中心
     *
     * @param vehicleInfo
     * @return
     */
    private BaseUploadResponse uploadBasicVehicleInfo(Record vehicleInfo,int operation) {
        EtcVehicleinfoJson etcVehicleinfoJson = new EtcVehicleinfoJson();
        vehicleInfo.set("operation",operation);

        if (SysConfig.getEncryptionFlag()) {
            // 加密存储
            try {
                vehicleInfo.set("ownerName",vehicleInfo.getStr("ownerName")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerName")));
                vehicleInfo.set("ownerIdNum",vehicleInfo.getStr("ownerIdNum")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerIdNum")));
                vehicleInfo.set("ownerTel",vehicleInfo.getStr("ownerTel")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerTel")));
                vehicleInfo.set("address",vehicleInfo.getStr("address")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("address")));
                vehicleInfo.set("contact",vehicleInfo.getStr("contact")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("contact")));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        etcVehicleinfoJson._setOrPut(vehicleInfo.getColumns());
        BaseUploadResponse response = upload(etcVehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);
        return response;
    }
    /**
     * 上传用户信息到部中心
     *
     * @param userInfo
     * @return
     */
    private BaseUploadResponse uploadBasicUserInfo(Record userInfo,int operation) {
        EtcUserinfoJson etcUserinfoJson = new EtcUserinfoJson();

        if (SysConfig.getEncryptionFlag()) {
            try {
                userInfo.set("userIdNum",  MyAESUtil.Decrypt( userInfo.getStr("userIdNum")));
                userInfo.set("tel", MyAESUtil.Decrypt( userInfo.getStr("tel")));
                userInfo.set("address", MyAESUtil.Decrypt( userInfo.getStr("address")));
                userInfo.set("userName", MyAESUtil.Decrypt( userInfo.getStr("userName")));
                userInfo.set("agentName", MyAESUtil.Decrypt( userInfo.getStr("agentName")));
                userInfo.set("agentIdNum", MyAESUtil.Decrypt( userInfo.getStr("agentIdNum")));
                userInfo.set("bankAccount", MyAESUtil.Decrypt( userInfo.getStr("bankAccount")));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        etcUserinfoJson._setOrPut(userInfo.getColumns());

        etcUserinfoJson.setRegisteredTime(userInfo.get("registeredTime"));
        etcUserinfoJson.setStatusChangeTime(new Date());
        etcUserinfoJson.setOperation(operation);
        BaseUploadResponse response = upload(etcUserinfoJson, BASIC_USERUPLOAD_REQ);
        return response;
    }
    /**
     * 上传卡信息到部中心
     *
     * @param cardInfo
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record cardInfo,int operation) {

        EtcCardinfoJson etcCardinfoJson = new EtcCardinfoJson();
        etcCardinfoJson._setOrPut(cardInfo.getColumns());
        // 时间需要转换为字符串
        etcCardinfoJson.setEnableTime(DateUtil.formatDate(cardInfo.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setExpireTime(DateUtil.formatDate(cardInfo.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setIssuedTime(DateUtil.formatDate(cardInfo.get("issuedTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setStatusChangeTime(DateUtil.formatDate(cardInfo.get("statusChangeTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setOperation(operation);
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        return response;
    }

    /**
     * 四类数据上传部中心
     *
     * @param model
     * @return
     */
    private BaseUploadResponse upload(Model model, String reqName) {
         long startTime = System.currentTimeMillis();
        BaseUploadResponse response = new BaseUploadResponse();
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
        return response;
    }
}

package com.csnt.ins.controller;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName IssuerChangeUserIdController
 * @Description TODO
 * @Author cml
 * @Date 2019/7/23 21:06
 * Version 1.0
 **/
public class IssuerChangeUserIdController extends Controller {

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


    public void index() {
        String vehicleId = getPara("vehicleId");
        String obuId = getPara("obuId");
        String userId = getPara("userId");
        String cardId = getPara("cardId");
        if (StringUtil.isEmpty(vehicleId, userId)) {
            renderJson("车辆编号、用户ID不能为空");
            return;
        }
        // 取车辆信息
        Record vehicleInfo = Db.findFirst(DbUtil.getSql("queryEtcVehinfoByVehicleId"), vehicleId);
        if (vehicleInfo == null) {
            renderJson(new String("未找到该车辆信息") + vehicleId);
            return;
        }
        // 取客户信息
        Record userRc = Db.findFirst(DbUtil.getSql("queryEtcUserInfoByUserId"), userId);
        if (userRc == null) {
            renderJson(new String("新客户编号不存在") + userId);
            return;
        }

        // 取老客户信息
        Record userOldRc = Db.findFirst(DbUtil.getSql("queryEtcUserInfoByUserId"), vehicleInfo.getStr("userId"));
        if (userOldRc != null) {
            renderJson(new String("车辆对应的客户编号存在") + vehicleInfo.getStr("userId"));
            return;
        }

        // 如果OBU不为空

        if (obuId != null) {
            Record obuRc = Db.findFirst(DbUtil.getSql("queryEtcObuInfoByOBUId"), obuId);
            if (obuRc == null) {
                renderJson(new String("未找到该OBU信息") + obuId);
                return;
            }
            // 判断OBU的客户编号是否与车辆的OBU信息一致
            if (!vehicleInfo.getStr("userId").equals(obuRc.getStr("userId"))) {
                renderJson(new String("OBU与车辆对应的客编不一致") + vehicleInfo.getStr("userId") + "," + obuRc.getStr("userId"));
                return;
            }
            // 删除OBU信息
            BaseUploadResponse res =  uploadBasicObuInfo(obuRc,3);
            if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                renderJson(new String(res.getErrorMsg()));
                return ;
            }
            obuRc.set("userId",userId);
            res =  uploadBasicObuInfo(obuRc,1);

            if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !res.getErrorMsg().contains(NOR_MAORL)) {
                renderJson(new String(res.getErrorMsg()));
                return ;
            }

            Db.tx(() -> {
                // 修改客户编号
                Db.update("etc_obuinfo", "id", obuRc);
                obuRc.set("opTime", new Date());
                Db.save("etc_obuinfo_history", "id,opTime", obuRc);
                return true;
            });
        }

        // 处理卡信息
        if (cardId != null) {

        }

        // 删除车辆信息
        BaseUploadResponse res =  uploadBasicVehicleInfo(vehicleInfo,3);
        if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            renderJson(new String(res.getErrorMsg()));
            renderJson(new String(res.getErrorMsg()));
            return ;
        }

        // 上传车辆信息
        vehicleInfo.set("userId",userId);
        res =  uploadBasicVehicleInfo(vehicleInfo,1);
        if (res.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            renderJson(new String(res.getErrorMsg()));
            return ;
        }
        Db.tx(() -> {
            // 修改客户编号
            Db.update("etc_vehicleinfo", "id", vehicleInfo);
            vehicleInfo.set("opTime", new Date());
            Db.save("etc_vehicleinfo_history", "id,opTime", vehicleInfo);
            return true;
        });


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

        if (SysConfig.getEncryptionFlag()) {
            // 加密存储
            try {
                vehicleInfo.set("ownerName",vehicleInfo.getStr("ownerName")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerName")));
                vehicleInfo.set("ownerIdNum",vehicleInfo.getStr("ownerIdNum")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerIdNum")));
                vehicleInfo.set("ownerTel",vehicleInfo.getStr("ownerTel")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("ownerTel")));
                vehicleInfo.set("address",vehicleInfo.getStr("address")==null?null:MyAESUtil.Decrypt( vehicleInfo.getStr("address")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        vehicleInfo.set("operation",operation);
        etcVehicleinfoJson._setOrPut(vehicleInfo.getColumns());
        BaseUploadResponse response = upload(etcVehicleinfoJson, BASIC_VEHICLEUPLOAD_REQ);
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

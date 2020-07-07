package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.UserObuInfoModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

public class UserObuinfoUploadService extends BaseDown implements IReceiveService {


    public UserObuinfoUploadService() {
        serviceName = "[OBU信息新增及变更通知]";
        uploadFileNamePrefix = "USER_OBUINFO_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,type,issuerId,vehicleId,obuId,brand,model,obuSign,plateNum,plateColor,enableTime,expireTime,issueChannelType,issueChannelId,activeTime,activeType";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserObuInfoModel model = new UserObuInfoModel();
        model.setIssuerId(record.get("issuerId"));
        model.setVehicleId(record.getStr("vehicleId"));
        model.setObuId(record.getStr("obuId"));
        model.setBrand(record.getStr("brand"));
        model.setModel(record.getStr("model"));
        model.setObuSign(record.getInt("obuSign"));
        model.setPlateNum(record.getStr("plateNum"));
        model.setPlateColor(record.getInt("plateColor"));
        model.setEnableTime(record.getStr("enableTime"));
        model.setExpireTime(record.getStr("expireTime"));
        model.setIssueChannelId(record.getStr("issueChannelId"));
        model.setIssueChannelType(record.getInt("issueChannelType"));
        model.setActiveType(record.get("activeType"));
        model.setActiveTime(record.get("activeTime"));

//        UserObuInfoRequest request = new UserObuInfoRequest();
//        request.setEncryptedData(UtilJson.toJson(model));
//        request.setAccessToken(record.getStr("accessToken"));
//        request.setAccountId(record.getStr("accountId"));
//        request.setOpenId(record.getStr("openId"));
//        request.setType(record.getInt("type"));

//        return UtilJson.toJson(request);


        Kv kv = Kv.create();
        kv.set("encryptedData", model)
                .set("accessToken", record.getStr("accessToken"))
                .set("accountId", record.getStr("accountId"))
                .set("openId", record.getStr("openId"))
                .set("type", record.getStr("type"));
        return kv.toJson();

    }


}
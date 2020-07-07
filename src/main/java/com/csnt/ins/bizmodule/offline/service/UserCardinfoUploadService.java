package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserCardInfoModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

public class UserCardinfoUploadService extends BaseUpload implements IReceiveService {


    public UserCardinfoUploadService() {
        serviceName = "[卡信息新增及变更通知]";
        uploadFileNamePrefix = "USER_CARDINFO_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,type,issuerId,vehicleId,cardId,cardType,brand,model,plateNum,plateColor,enableTime,expireTime,issueChannelType,issueChannelId";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserCardInfoModel model = new UserCardInfoModel();
//        model.setIssuerId("630101");
//        model.setVehicleId("f0c64493b92c40f99ab39cf7ce09ea6e");
//        model.setCardId("63010101001010003011");
//        model.setCardType(123);
//        model.setBrand(1);
//        model.setModel("11010210");
//        model.setPlateNum("京A88890");
//        model.setPlateColor(1);
//        model.setEnableTime("2019-05-10T10:00:00");
//        model.setExpireTime("2019-09-10T10:00:00");
//        model.setIssueChannelType(2);
//        model.setIssueChannelId("63010110100101010001");
        model.setIssuerId(record.getStr("issuerId"));
        model.setVehicleId(record.getStr("vehicleId"));
        model.setCardId(record.getStr("cardId"));
        model.setCardType(record.getInt("cardType"));
        model.setBrand(record.getInt("brand"));
        model.setModel(record.getStr("model"));
        model.setPlateNum(record.getStr("plateNum"));
        model.setPlateColor(record.getInt("plateColor"));
        model.setEnableTime(record.getStr("enableTime"));
        model.setExpireTime(record.getStr("expireTime"));
        model.setIssueChannelType(record.getInt("issueChannelType"));
        model.setIssueChannelId(record.getStr("issueChannelId"));

//        UserCardInfoRequest request=new UserCardInfoRequest();
//        request.setAccessToken(record.getStr("accessToken"));
//        request.setAccountId(record.getStr("openId"));
//        request.setOpenId(record.getStr("accountId"));
//        request.setType(record.getInt("type"));
//        String encryptedData= AESTools.encrypt(UtilJson.toJson(model), SysConfig.getSdkAesKey());
//        request.setEncryptedData(encryptedData);
//        String content= SignatureManager.getSignContent(UtilJson.toJson(request),fileName);
//        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));
//
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

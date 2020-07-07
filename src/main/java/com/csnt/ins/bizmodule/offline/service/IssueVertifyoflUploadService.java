package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.IssueVertifyOFLModel;
import com.csnt.ins.model.offline.IssueVertifyOFLRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

public class IssueVertifyoflUploadService extends BaseUpload implements IReceiveService {


    public IssueVertifyoflUploadService() {
        serviceName = "[证件信息验证]";
        uploadFileNamePrefix = "ISSUE_VERTIFYOFL_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "plateNum,plateColor,driverId,driverName,driverIdType,vin,engineNum,issueDate,plateNum,registerDate,useCharacter,vehicleType";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        IssueVertifyOFLRequest request = new IssueVertifyOFLRequest();
        request.setPlateNum(record.getStr("plateNum"));
        request.setPlateColor(record.getInt("plateColor"));
        request.setType(1);

        IssueVertifyOFLModel model = new IssueVertifyOFLModel();
//        model.setDriverId("410327199109235710");
//        model.setDriverName("皮卡丘");
//        model.setDriverIdType(101);
//        model.setVin("111");
//        model.setEngineNum("1");
//        model.setIssueDate("2019-01-01");
//        model.setPlateNum("京A88890");
//        model.setRegisterDate("2019-01-01");
//        model.setUseCharacter(0);
//        model.setVehicleType("1");
        model.setDriverId(record.getStr("driverId"));
        model.setDriverName(record.getStr("driverName"));
        model.setDriverIdType(record.getInt("driverIdType"));
        model.setVin(record.getStr("vin"));
        model.setEngineNum(record.getStr("engineNum"));
        model.setIssueDate(record.getStr("issueDate"));
        model.setPlateNum(record.getStr("plateNum"));
        model.setRegisterDate(record.getStr("registerDate"));
        model.setUseCharacter(record.getInt("useCharacter"));
        model.setVehicleType(record.getStr("vehicleType"));

//        String encryptedData = AESTools.encrypt(UtilJson.toJson(model), SysConfig.getSdkAesKey());
//        request.setEncryptedData(encryptedData);
//        String content = SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
//        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));

//        return UtilJson.toJson(request);

        Kv kv = Kv.create();
        kv.set("encryptedData", model)
                .set("accessToken", record.getStr("accessToken"))
                .set("openId", record.getStr("openId"))
                .set("plateNum", record.getStr("plateNum"))
                .set("plateColor", record.getStr("plateColor"))
                .set("type", record.getStr("type"));
        return kv.toJson();
    }
}

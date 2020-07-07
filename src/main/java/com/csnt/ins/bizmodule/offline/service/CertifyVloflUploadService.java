package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.VloflModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertifyVloflUploadService extends BaseUpload implements IReceiveService {
    protected static Logger logger = LoggerFactory.getLogger(CertifyVloflUploadService.class);

    public CertifyVloflUploadService() {
        serviceName = "[车辆信息上传]";
        uploadFileNamePrefix = "CERTIFY_VLOFL_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,vin,vehicleId,engineNum,issueDate,name,plateNum,registerDate,useCharacter,vehicleType,type,outsideDimensions";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        VloflModel model = new VloflModel();
//        model.setVin("1123213222222");
//        model.setVehicleId("f0c64493b92c40f99ab39cf7ce09ea6e");
//        model.setEngineNum("450322F");
//        model.setIssueDate("2019-01-01");
//        model.setName("奥巴马");
//        model.setPlateNum("京A88890");
//        model.setRegisterDate("2019-01-01");
//        model.setUseCharacter(2);
//        model.setVehicleType("小轿车");
//        model.setType(1);
//        model.setFileNum("370111111111");
//        model.setApprovedCount(5);
//        model.setTotalMass(1650);
//        model.setMaintenaceMass(1236);
//        model.setPermittedWeight(3000);
//        model.setOutsideDimensions("3000x4000x2400 ");
//        model.setPermittedTowWeight(4000);
//        model.setVehicleModel("丰台 RAV4");
//        model.setTestRecord("111");
//        model.setWheelCount(0);
//        model.setAxleCount(0);
//        model.setAxleDistance(0);
//        model.setAxisType("111");
        model.setVin(record.getStr("vin"));
        model.setVehicleId(record.getStr("vehicleId"));
        model.setEngineNum(record.getStr("engineNum"));
        model.setIssueDate(record.getStr("issueDate"));
        model.setName(record.getStr("name"));
        model.setPlateNum(record.getStr("plateNum"));
        model.setRegisterDate(record.getStr("registerDate"));
        model.setUseCharacter(record.getInt("useCharacter"));
        model.setVehicleType(record.getStr("vehicleType"));
        model.setType(record.getInt("type"));
        model.setFileNum(record.getStr("fileNum"));
        model.setApprovedCount(record.getInt("approvedCount"));
        model.setTotalMass(record.getInt("totalMass"));
        model.setMaintenaceMass(record.getInt("maintenaceMass"));
        model.setPermittedWeight(record.getInt("permittedWeight"));
        model.setOutsideDimensions(record.getStr("outsideDimensions"));
        model.setPermittedTowWeight(record.getInt("permittedTowWeight"));
        model.setVehicleModel(record.getStr("vehicleModel"));
        model.setTestRecord(record.getStr("testRecord"));
        model.setWheelCount(record.getInt("wheelCount"));
        model.setAxleCount(record.getInt("axleCount"));
        model.setAxleDistance(record.getInt("axleDistance"));
        model.setAxisType(record.getStr("axisType"));

//        VloflRequest request = new VloflRequest();
//        request.setAccessToken(record.getStr("accessToken"));
//        request.setAccountId(record.getStr("accountId"));
//        request.setOpenId(record.getStr("openId"));
//        request.setType(1);
//        String encryptedData = AESTools.encrypt(UtilJson.toJson(model), SysConfig.getSdkAesKey());
//        request.setEncryptedData(encryptedData);
//        String content = SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
//        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));

//        return UtilJson.toJson(request);

        Kv kv = Kv.create();
        kv.set("encryptedData", model)
                .set("accessToken", record.getStr("accessToken"))
                .set("accountId", record.getStr("accountId"))
                .set("openId", record.getStr("openId"))
                .set("type", record.getStr("headtype"));
        return kv.toJson();
    }
}

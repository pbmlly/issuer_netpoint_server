package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserSignoflRequest;
import com.csnt.ins.model.offline.UserVehChangeModel;
import com.csnt.ins.model.offline.UserVehChangeRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

/**
 * 车辆信息变更通知
 *
 * @author duwanjiang
 * @date 2019-08-20
 */
public class UserVehicleChangeService extends BaseUpload implements IReceiveService {


    public UserVehicleChangeService() {
        serviceName = "[车辆信息变更通知]";
        uploadFileNamePrefix = "USER_VEHICLECHANGE_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,vehicleId,code,vin,engineNum,issueDate,name,registerDate,outsideDimensions";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserVehChangeModel userVehChangeModel = new UserVehChangeModel();
        userVehChangeModel.setVin(record.getStr("vin"));
        userVehChangeModel.setEngineNum(record.getStr("engineNum"));
        userVehChangeModel.setIssueDate(record.getStr("issueDate"));
        userVehChangeModel.setName(record.getStr("name"));
        userVehChangeModel.setRegisterDate(record.getStr("registerDate"));
        userVehChangeModel.setFileNum(record.getStr("fileNum"));
        userVehChangeModel.setMaintenaceMass(record.getInt("maintenaceMass"));
        userVehChangeModel.setPermittedWeight(record.getInt("permittedWeight"));
        userVehChangeModel.setOutsideDimensions(record.getStr("outsideDimensions"));
        userVehChangeModel.setPermittedTowWeight(record.getInt("permittedTowWeight"));
        userVehChangeModel.setVehicleModel(record.getStr("vehicleModel"));
        userVehChangeModel.setTestRecord(record.getStr("testRecord"));
        userVehChangeModel.setWheelCount(record.getInt("wheelCount"));
        userVehChangeModel.setAxleDistance(record.getInt("axleDistance"));
        userVehChangeModel.setAxisType(record.getStr("axisType"));

//        UserVehChangeRequest userVehChangeRequest = new UserVehChangeRequest();
//        userVehChangeRequest.setEncryptedData(UtilJson.toJson(userVehChangeModel));
//        userVehChangeRequest.setCode(record.getStr("code"));
//        userVehChangeRequest.setSign(record.getStr("sign"));
//        userVehChangeRequest.setVehicleId(record.getStr("vehicleId"));
//
//        return UtilJson.toJson(userVehChangeRequest);
        Kv kv = Kv.create();
        kv.set("encryptedData", userVehChangeModel)
                .set("accessToken", record.getStr("accessToken"))
                .set("accountId", record.getStr("accountId"))
                .set("vehicleId", record.getStr("vehicleId"))
                .set("openId", record.getStr("openId"))
                .set("code", record.getStr("code"));
        return kv.toJson();
    }

}

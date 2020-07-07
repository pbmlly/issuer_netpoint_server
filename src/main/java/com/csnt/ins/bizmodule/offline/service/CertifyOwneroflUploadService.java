package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.OwnerOflModel;
import com.csnt.ins.model.offline.OwnerOflRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.csnt.ins.utils.sdk.AESTools;
import com.csnt.ins.utils.sdk.SignatureManager;
import com.csnt.ins.utils.sdk.SignatureTools;
import com.jfinal.plugin.activerecord.Record;

public class CertifyOwneroflUploadService extends BaseUpload implements IReceiveService {

    public CertifyOwneroflUploadService() {
        serviceName = "[车主身份信息上传]";
        uploadFileNamePrefix = "CERTIFY_OWNEROFL_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,vehicleId,type,driverId,driverName,driverIdType,positiveImageStr,negativeImageStr,driverPhone,driverAddr";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        OwnerOflModel model = new OwnerOflModel();
//        model.setDriverId("410327199109235720");
//        model.setDriverName("皮卡丘");
//        model.setDriverIdType(101);
//        model.setPositiveImageStr("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");
//        model.setNegativeImageStr("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");
//        model.setDriverPhone("13045760617");
//        model.setDriverAddr("北京市朝阳区 XX 大厦");
        model.setDriverId(record.getStr("driverId"));
        model.setDriverName(record.getStr("driverName"));
        model.setDriverIdType(record.getInt("driverIdType"));
        model.setPositiveImageStr(record.getStr("positiveImageStr"));
        model.setNegativeImageStr(record.getStr("negativeImageStr"));
        model.setDriverPhone(record.getStr("driverPhone"));
        model.setDriverAddr(record.getStr("driverAddr"));
        String encryptedData = AESTools.encrypt(UtilJson.toJson(model), SysConfig.getSdkAesKey());

        OwnerOflRequest request = new OwnerOflRequest();
//        request.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        request.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        request.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        request.setVehicleId("f0c64493b92c40f99ab39cf7ce09ea6e");
        request.setAccessToken(record.getStr("accessToken"));
        request.setOpenId(record.getStr("openId"));
        request.setAccountId(record.getStr("accountId"));
        request.setVehicleId(record.getStr("vehicleId"));
        request.setEncryptedData(encryptedData);
        String content = SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));
        return UtilJson.toJson(request);
    }
}

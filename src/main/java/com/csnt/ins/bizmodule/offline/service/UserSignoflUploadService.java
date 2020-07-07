package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserSignoflRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserSignoflUploadService extends BaseUpload implements IReceiveService {


    public UserSignoflUploadService() {
        serviceName = "[车辆支付渠道绑定/解绑通知]";
        uploadFileNamePrefix = "USER_SIGNOFL_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,vehicleId,plateNum,plateColor,signType,issueChannelId,channelType,cardType,enableTime,closeTime,status";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserSignoflRequest request = new UserSignoflRequest();
//        request.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        request.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        request.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        request.setVehicleId("f0c64493b92c40f99ab39cf7ce09ea6e");
//        request.setPlateColor(1);
//        request.setPlateNum("京A88890");
//        request.setSignType(2);
//        request.setIssueChannelId("1101010100101010001");
//        request.setChannelType("001");
//        request.setCardType(1);
//        request.setAccount("17301742237");
//        request.setEnableTime("2019-09-13T00:00:00");
//        request.setCloseTime("2019-09-13T00:00:00");
//        request.setInfo("k");
//        request.setStatus(1);
        request.setAccessToken(record.getStr("accessToken"));
        request.setAccountId(record.getStr("accountId"));
        request.setOpenId(record.getStr("openId"));
        request.setVehicleId(record.getStr("vehicleId"));
        request.setPlateColor(record.getInt("plateColor"));
        request.setPlateNum(record.getStr("plateNum"));
        request.setSignType(record.getInt("signType"));
        request.setIssueChannelId(record.getStr("issueChannelId"));
        request.setChannelType(record.getStr("channelType"));
        request.setCardType(record.getInt("cardType"));
        request.setAccount(record.getStr("account"));
        request.setEnableTime(record.getStr("enableTime"));
        request.setCloseTime(record.getStr("closeTime"));
        request.setInfo(record.getStr("info"));
        request.setStatus(record.getInt("status"));

        return UtilJson.toJson(request);
    }

}

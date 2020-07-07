package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.UserVQOFLRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserVqoflService extends BaseDown implements IReceiveService {


    public UserVqoflService() {
        serviceName = "[获取车辆信息]";
        uploadFileNamePrefix = "USER_VQOFL_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,vehicleId";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserVQOFLRequest userVqoflRequest = new UserVQOFLRequest();
//        userVqoflRequest.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        userVqoflRequest.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        userVqoflRequest.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        userVqoflRequest.setVehicleId("f0c64493b92c40f99ab39cf7ce09ea6e");
        userVqoflRequest.setAccessToken(record.getStr("accessToken"));
        userVqoflRequest.setOpenId(record.getStr("openId"));
        userVqoflRequest.setAccountId(record.getStr("accountId"));
        userVqoflRequest.setVehicleId(record.getStr("vehicleId"));
        return UtilJson.toJson(userVqoflRequest);
    }

}

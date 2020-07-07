package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserObuStatusRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserObuStatusUploadService extends BaseUpload implements IReceiveService {


    public UserObuStatusUploadService() {
        serviceName = "[OBU状态变更通知]";
        uploadFileNamePrefix = "USER_OBUSTATUS_REQ_" + CommonAttribute.ISSUER_ISS_SENDER + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,type,obuId,status";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserObuStatusRequest userObuStatusRequest = new UserObuStatusRequest();
//        userObuStatusRequest.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        userObuStatusRequest.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        userObuStatusRequest.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        userObuStatusRequest.setObuId("6301dfdgdfsdfdsabcd");
//        userObuStatusRequest.setType(8);
//        userObuStatusRequest.setStatus(1);
        userObuStatusRequest.setAccessToken(record.getStr("accessToken"));
        userObuStatusRequest.setAccountId(record.getStr("accountId"));
        userObuStatusRequest.setOpenId(record.getStr("openId"));
        userObuStatusRequest.setObuId(record.getStr("obuId"));
        userObuStatusRequest.setType(record.getInt("type"));
        userObuStatusRequest.setStatus(record.getInt("status"));

        return UtilJson.toJson(userObuStatusRequest);
    }

}

package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserCardStatusRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserCardStatusUploadService extends BaseUpload implements IReceiveService {


    public UserCardStatusUploadService() {
        serviceName = "[卡状态变更通知]";
        uploadFileNamePrefix = "USER_CARDSTATUS_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,type,cardId,status";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserCardStatusRequest userCardStatusRequest = new UserCardStatusRequest();
//        userCardStatusRequest.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        userCardStatusRequest.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        userCardStatusRequest.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        userCardStatusRequest.setCardId("64010101001010003011");
//        userCardStatusRequest.setType(3);
//        userCardStatusRequest.setStatus(4);
        userCardStatusRequest.setAccessToken(record.getStr("accessToken"));
        userCardStatusRequest.setAccountId(record.getStr("accountId"));
        userCardStatusRequest.setOpenId(record.getStr("openId"));
        userCardStatusRequest.setCardId(record.getStr("cardId"));
        userCardStatusRequest.setType(record.getInt("type"));
        userCardStatusRequest.setStatus(record.getInt("status"));

        return UtilJson.toJson(userCardStatusRequest);
    }

}

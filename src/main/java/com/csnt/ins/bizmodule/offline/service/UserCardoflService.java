package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.UserCardOFLRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserCardoflService extends BaseDown implements IReceiveService {


    public UserCardoflService() {
        serviceName = "[获取卡信息]";
        uploadFileNamePrefix = "USER_CARDOFL_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,obuId";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserCardOFLRequest request = new UserCardOFLRequest();
//        request.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        request.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        request.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        request.setObuId("6301dfdgdfsdfdsabcd");
        request.setAccessToken(record.getStr("accessToken"));
        request.setOpenId(record.getStr("openId"));
        request.setAccountId(record.getStr("accountId"));
        request.setObuId(record.getStr("obuId"));

        return UtilJson.toJson(request);
    }

}

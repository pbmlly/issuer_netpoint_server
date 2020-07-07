package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.IssuePCOFLRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class IssuePcoflUploadService extends BaseDown implements IReceiveService {


    public IssuePcoflUploadService() {
        serviceName = "[车牌发行验证]";
        uploadFileNamePrefix = "ISSUE_PCOFL_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,plateNum,plateColor";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        IssuePCOFLRequest pcoflRequest = new IssuePCOFLRequest();
//        pcoflRequest.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        pcoflRequest.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        pcoflRequest.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        pcoflRequest.setPlateColor(1);
//        pcoflRequest.setPlateNum("京A84590");
        pcoflRequest.setAccessToken(record.getStr("accessToken"));
        pcoflRequest.setAccountId(record.getStr("accountId"));
        pcoflRequest.setOpenId(record.getStr("openId"));
        pcoflRequest.setPlateColor(record.getInt("plateColor"));
        pcoflRequest.setPlateNum(record.getStr("plateNum"));
        return UtilJson.toJson(pcoflRequest);
    }

}

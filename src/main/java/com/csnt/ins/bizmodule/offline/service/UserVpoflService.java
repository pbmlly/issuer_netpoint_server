package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.UserVPOFLRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class UserVpoflService extends BaseDown implements IReceiveService {


    public UserVpoflService() {
        serviceName = "[获取车辆列表]";
        uploadFileNamePrefix = "USER_VPOFL_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,pageNo,pageSize";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserVPOFLRequest request = new UserVPOFLRequest();
//        request.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        request.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
//        request.setAccountId("96c3cf8b19034e9c9d098b7771d83913");
//        request.setPageNo(1);
//        request.setPageSize(10);
        request.setAccessToken(record.getStr("accessToken"));
        request.setOpenId(record.getStr("openId"));
        request.setAccountId(record.getStr("accountId"));
        request.setPageNo(record.getInt("pageNo"));
        request.setPageSize(record.getInt("pageSize"));
        return UtilJson.toJson(request);
    }

}

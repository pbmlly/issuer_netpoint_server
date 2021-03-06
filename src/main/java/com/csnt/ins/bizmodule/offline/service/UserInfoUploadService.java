package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.UserInfoRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInfoUploadService extends BaseDown implements IReceiveService {
    protected static Logger logger = LoggerFactory.getLogger(UserInfoUploadService.class);

    public UserInfoUploadService() {
        serviceName = "[获取用户基本信息]";
        uploadFileNamePrefix = "USER_INFO_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserInfoRequest request = new UserInfoRequest();
//        request.setAccessToken("264dde61d29548dab435f7ec41368e1d");
//        request.setOpenId("0089af47d82141bab1fe61b1fd7e309d");
        request.setAccessToken(record.getStr("accessToken"));
        request.setOpenId(record.getStr("openId"));

        return UtilJson.toJson(request);
    }

}

package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.IssueMsgRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class IssueMsgReqService extends BaseUpload implements IReceiveService {


    public IssueMsgReqService() {
        serviceName = "[发送短信验证码]";
        uploadFileNamePrefix = "ISSUE_MSG_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,mobile";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        IssueMsgRequest request = new IssueMsgRequest();
        request.setAccessToken(record.getStr("accessToken"));
        request.setMobile(record.getStr("mobile"));
        request.setOpenId(record.getStr("openId"));

        return UtilJson.toJson(request);
    }

}

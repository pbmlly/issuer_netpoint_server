package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ap.ct.util.UtilJson;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.TouchTokenRequest;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName AutoAuditOnlineapplyService
 * @Description TODO
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class AuthTouchService extends BaseDown implements IReceiveService {
    protected static Logger logger = LoggerFactory.getLogger(AuthTouchService.class);

    public AuthTouchService() {
        serviceName = "[刷新接口调用凭证]";
        uploadFileNamePrefix = "AUTH_TOUCH_REQ_";
    }

    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "openId,accessToken";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }


    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        TouchTokenRequest request = new TouchTokenRequest();
//        request.setAccessToken("2efad6d0a43a412b86200b51840a6d7a");
//        request.setOpenId("734a8ced34fa4b439c0662d5936d651b");
        request.setAccessToken(record.getStr("accessToken"));
        request.setOpenId(record.getStr("openId"));
//        request.setAppId(SysConfig.getSdkAppId());
//        String content = SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
//        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));

        return UtilJson.toJson(request);
    }
}

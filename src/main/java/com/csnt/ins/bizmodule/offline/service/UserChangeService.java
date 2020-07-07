package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.CreditUserModel;
import com.csnt.ins.model.offline.UserUserchangeModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

/**
 * @ClassName AutoAuditOnlineapplyService
 * @Description 8822线下渠道个人用户变更
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class UserChangeService extends BaseUpload implements IReceiveService {


    public UserChangeService() {
        serviceName = "[8822个人用户信息变更通知]";
        uploadFileNamePrefix = "USER_USERCHANGE_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,code,name,phone,address";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserUserchangeModel model = new UserUserchangeModel();

        model.setName(record.getStr("name"));
        model.setPositiveImageStr(record.getStr("positiveImageStr"));
        model.setNegativeImageStr(record.getStr("negativeImageStr"));
        model.setPhone(record.getStr("phone"));
        model.setAddress(record.getStr("address"));

        Kv kv = Kv.create();
        kv.set("encryptedData",model)
                .set("accessToken",record.getStr("accessToken"))
                .set("accountId",record.getStr("accountId"))
                .set("openId",record.getStr("openId"))
                .set("code",record.getStr("code"));
        return kv.toJson();
    }

}

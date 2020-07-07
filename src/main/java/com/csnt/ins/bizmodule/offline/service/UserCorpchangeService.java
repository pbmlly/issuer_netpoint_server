package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.bizmodule.offline.base.BaseUpload;
import com.csnt.ins.model.offline.UserCorpchangeModel;
import com.csnt.ins.model.offline.UserUserchangeModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;

/**
 * @ClassName AutoAuditOnlineapplyService
 * @Description 8823线下渠道个人用户开户
 * @Author chenmaolin
 * @Date 2019/6/28 20:06
 * Version 1.0
 **/
public class UserCorpchangeService extends BaseUpload implements IReceiveService {


    public UserCorpchangeService() {
        serviceName = "[8823单位用户信息变更]";
        uploadFileNamePrefix = "USER_CORPCHANGE_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "accessToken,openId,accountId,code,name,phone,address,agentName,agentIdType,agentIdNum";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        UserCorpchangeModel model = new UserCorpchangeModel();

        model.setName(record.getStr("name"));
        model.setPositiveImageStr(record.getStr("positiveImageStr"));
        model.setNegativeImageStr(record.getStr("negativeImageStr"));
        model.setPhone(record.getStr("phone"));
        model.setAddress(record.getStr("address"));
        model.setAgentName(record.getStr("agentName"));
        model.setAgentIdType(record.getInt("agentIdType"));
        model.setAgentIdNum(record.getStr("agentIdNum"));
        model.setBank(record.getStr("bank"));
        model.setBankAddr(record.getStr("bankAddr"));
        model.setBankAccount(record.getStr("bankAccount"));
        model.setTaxpayerCode(record.getStr("taxpayerCode"));


        Kv kv = Kv.create();
        kv.set("encryptedData",model)
                .set("accessToken",record.getStr("accessToken"))
                .set("accountId",record.getStr("accountId"))
                .set("openId",record.getStr("openId"))
                .set("code",record.getStr("code"));
        return kv.toJson();
    }

}

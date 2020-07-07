package com.csnt.ins.bizmodule.offline.service;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.offline.base.BaseDown;
import com.csnt.ins.model.offline.CreditCorpModel;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
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
public class CertifyCreitcorpService extends BaseDown implements IReceiveService {
    protected static Logger logger = LoggerFactory.getLogger(CertifyCreitcorpService.class);

    public CertifyCreitcorpService() {
        serviceName = "[线下渠道单位用户开户]";
        uploadFileNamePrefix = "CERTIFY_CREITCORP_REQ_";
    }


    @Override
    protected Result checkParam(Record params) {
        //检查必填参数
        String paramNames = "id,name,corpIdType,positiveImageStr,negativeImageStr,phone,address,registeredType,issueChannelId,department,agentName,agentIdType,agentIdNum";
        if (StringUtil.isEmptyArg(params, paramNames)) {
            return Result.paramNotNullError(paramNames);
        }
        return null;
    }

    @Override
    protected String rebuildRecord2Json(Record record, String fileName) {
        CreditCorpModel model = new CreditCorpModel();
//        model.setId("630327199109235111");
//        model.setName("谢单位");
//        model.setCorpIdType(201);
//        model.setPositiveImageStr("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");
//        model.setNegativeImageStr("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");
//        model.setPhone("17301742240");
//        model.setAddress("北京市朝阳区 XX 大厦");
//        model.setRegisteredType(2);
//        model.setIssueChannelId("1101010100101010002");
//        model.setDepartment("本部");
//        model.setAgentName("张三");
//        model.setAgentIdType(101);
//        model.setAgentIdNum("630327199109245639");
//        model.setBank("中国银行");
//        model.setBankAddr("北京朝阳区支行");
//        model.setBankAccount("4401234543543331");
//        model.setTaxpayerCode("110 24345453002");
        model.setId(record.getStr("id"));
        model.setName(record.getStr("name"));
        model.setCorpIdType(record.getInt("corpIdType"));
        model.setPositiveImageStr(record.getStr("positiveImageStr"));
        model.setNegativeImageStr(record.getStr("negativeImageStr"));
        model.setPhone(record.getStr("phone"));
        model.setAddress(record.getStr("address"));
        model.setRegisteredType(record.getInt("registeredType"));
        model.setIssueChannelId(record.getStr("issueChannelId"));
        model.setDepartment(record.getStr("department"));
        model.setAgentName(record.getStr("agentName"));
        model.setAgentIdType(record.getInt("agentIdType"));
        model.setAgentIdNum(record.getStr("agentIdNum"));
        model.setBank(record.getStr("bank"));
        model.setBankAddr(record.getStr("bankAddr"));
        model.setBankAccount(record.getStr("bankAccount"));
        model.setTaxpayerCode(record.getStr("taxpayerCode"));
//        String encryptedData = AESTools.encrypt(UtilJson.toJson(model), SysConfig.getSdkAesKey());
//
//        CreditCorpRequest request = new CreditCorpRequest();
//        request.setAppId(SysConfig.getSdkAppId());
//        request.setAppSecret(SysConfig.getSdkAppSecret());
//        request.setEncryptedData(encryptedData);
//        String content = SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
//        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));
//        return UtilJson.toJson(request);
        return Kv.by("encryptedData", model).toJson();
    }
}

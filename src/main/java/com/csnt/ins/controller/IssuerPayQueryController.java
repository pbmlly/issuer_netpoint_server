package com.csnt.ins.controller;

import com.alibaba.fastjson.JSONObject;
import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserVpoflService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.json.SupPayQueryRequest;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.csnt.ins.utils.UtilJson;
import com.csnt.ins.utils.sdk.SignatureManager;
import com.csnt.ins.utils.sdk.SignatureTools;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.kit.Kv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询请款结果
 * @ClassName IssuerPayQueryController
 * @Description TODO
 * @Author cml
 * @Date 2019/7/23 21:06
 * Version 1.0
 **/
public class IssuerPayQueryController extends Controller
        implements  BaseUploadService {

    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();

    /**
     * 获取上传对象
     */
    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String NOR_MAORL = "不存在";
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

    public void index() {
        String messageId = getPara("messageId");
        String bankId = getPara("bankId");
        if (StringUtil.isEmpty(messageId)) {
            renderJson("包号不能为空");
            return;
        }
        if (StringUtil.isEmpty(bankId)) {
            renderJson("银行编号为空");
            return;
        }
        String fileName = "ISSBS_PAYQUERY_REQ_" + CommonAttribute.ISSUER_ISS_SENDER + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";

        Map sedMap =  new HashMap<>();
        SupPayQueryRequest request = new SupPayQueryRequest();
        request.setIssuerId("630101");
        request.setMessageId(Long.parseLong(messageId));
        request.setBankId(bankId);
        String content= SignatureManager.getSignContent(UtilJson.toJson(request), fileName);
        request.setSign(SignatureTools.rsa256Sign(content, SysConfig.getPrivateKeyBase64()));

        String json = Jackson.getJson().toJson(request);
        BaseUploadRequest request1 = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        BaseUploadResponse response = upload.uploadRequestSender(request1, SysConfig.getRequestTimeout());

        renderJson(response);
    }

    public String getJsonName() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String time = dateFormat.format(date);
        return "ISSBS_PAYQUERY_REQ_" + "63010101" + "_" + time + ".json";
    }

}

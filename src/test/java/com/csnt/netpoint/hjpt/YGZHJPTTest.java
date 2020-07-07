package com.csnt.netpoint.hjpt;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.json.Jackson;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName YGZHJPTTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/9/2 0:57
 * Version 1.0
 **/
public class YGZHJPTTest {
    /**
     * 上传网络传输对象
     */
    protected IUpload upload = CsntUpload.getInstance();


    @Before
    public void getUpload() {
        upload.setAccount(SysConfig.getAccount());
        upload.setConnectIp(SysConfig.getConnectIp());
        upload.setConnectPort(MathUtil.asInteger(SysConfig.getConnectPort()));
        upload.setPassword(SysConfig.getPassword());
        upload.start(4,60);
    }

    @Test
    public void testUploadUserInfo() {
        String reqName = "BASIC_USERUPLOAD_REQ_";
        Map sedMsg = new HashMap<>();
        sedMsg.put("id", "63010117091100048");
        sedMsg.put("userType", 2);
        sedMsg.put("userName", "青海新千房地产开发有限责任公司");
        sedMsg.put("userIdType", "203");
        sedMsg.put("userIdNum", "916301007104859540");
        sedMsg.put("tel", "13519734895");
        sedMsg.put("address", "青海省西宁市城东区中建国大街66号");
        sedMsg.put("registeredType", 2);
        sedMsg.put("channelId", "6301010200101020001");
        sedMsg.put("registeredTime", "2019-08-08T12:20:53");
        sedMsg.put("department", "青海新千房地产开发有限责任公司");
        sedMsg.put("agentName", "魁英秀");
        sedMsg.put("agentIdType", 101);
        sedMsg.put("agentIdNum", "632122198607217521");
        sedMsg.put("status", 1);
        sedMsg.put("statusChangeTime", "2017-09-11T10:47:54");
        sedMsg.put("operation", 1);

        uploadInfo(reqName, sedMsg);
    }
    @Test
    public void testUploadCardInfo() {
        String reqName = "BASIC_CARDUPLOAD_REQ_";
        Map sedMsg = new HashMap<>();
        sedMsg.put("id", "63011824220101002900");
        sedMsg.put("cardType", 231);
        sedMsg.put("brand", 1);
        sedMsg.put("model", "SophiaV60C");
        sedMsg.put("agencyId", "63010199999");
        sedMsg.put("userId", "63010118102349780");
        sedMsg.put("vehicleId", "青ACB316_0");
        sedMsg.put("enableTime", "2018-10-23T00:00:00");
        sedMsg.put("expireTime", "2023-10-23T00:00:00");
        sedMsg.put("issuedType", 2);
        sedMsg.put("channelId", "6301019999901050001");
        sedMsg.put("issuedTime", "2018-10-23T14:52:55");
        sedMsg.put("status", 4);
        sedMsg.put("statusChangeTime", "2018-10-23T14:52:55");
        sedMsg.put("operation", 2);

        uploadInfo(reqName, sedMsg);
    }


    @Test
    public void testUploadObuInfo() {
        String reqName = "BASIC_OBUUPLOAD_REQ_";
        Map sedMsg = new HashMap<>();
        sedMsg.put("id", "6301181010201979");
        sedMsg.put("brand", 2);
        sedMsg.put("model", "V600E");
        sedMsg.put("userId", "63010118102349780");
        sedMsg.put("vehicleId", "青ACB316_0");
        sedMsg.put("enableTime", "2018-09-23T00:00:00");
        sedMsg.put("expireTime", "2028-09-23");
        sedMsg.put("registeredType", 2);
        sedMsg.put("registeredChannelId", "6301019999901050001");
        sedMsg.put("registeredTime", "2018-10-23T14:55:12");
        sedMsg.put("installType", 2);
        sedMsg.put("installChannelId", "6301019999901050001");
        sedMsg.put("installTime", "2018-10-23T14:55:12");
        //* 老版本：
        // * 1-	正常
        // * 2-	有签挂起
        // * 3-	无签挂起
        // * 4-	有签注销
        // * 5-	无签注销
        // * 6-	标签挂失
        // * 7-	已过户
        // * 8-	维修中
        sedMsg.put("status", 4);
        sedMsg.put("statusChangeTime", "2018-10-23T14:55:12");
        sedMsg.put("operation", 2);

        uploadInfo(reqName, sedMsg);

    }
    @Test
    public void testUploadObuBlacklist() {
        String reqName = "BASIC_OBUBLACKLISTUPLOAD_REQ_";
        Map sedMsg = new HashMap<>();
        sedMsg.put("issuerId", CommonAttribute.ISSUER_CODE);
        sedMsg.put("brand", 1);
        sedMsg.put("model", "UTTAG-1");
        sedMsg.put("userId", "63010119083021709");
        sedMsg.put("vehicleId", "青BS2820_0");
        sedMsg.put("enableTime", "2020-01-01T00:00:00");
        sedMsg.put("expireTime", "2029-09-01");
        sedMsg.put("registeredType", 2);
        sedMsg.put("registeredChannelId", "6301019999921230004");
        sedMsg.put("registeredTime", "2019-09-01T13:52:31");
        sedMsg.put("installType", 2);
        sedMsg.put("installChannelId", "6301019999921230004");
        sedMsg.put("installTime", "2019-09-01T13:52:31");

        sedMsg.put("status", 4);
        sedMsg.put("statusChangeTime", "2019-09-01T13:57:22");
        sedMsg.put("operation", 3);

        uploadInfo(reqName, sedMsg);

    }

    private void uploadInfo(String reqName, Map sedMsg) {
        BaseUploadResponse response = new BaseUploadResponse();
        String json = Jackson.getJson().toJson(sedMsg);
        String fileName = reqName + SysConfig.getGatherCode() + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(StringUtil.UTF8), fileName);
        response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        System.out.println(response);
    }
}

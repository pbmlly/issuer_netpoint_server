package com.csnt.netpoint.hjpt;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.*;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName HJPTTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/8/23 16:01
 * Version 1.0
 **/
public class HJPTTest {

    /**
     * 上传网络传输对象
     */
    protected IUpload upload = CsntUpload.getInstance();

    private String plateNum = "青G05442";

    @Before
    public void getUpload() {
        upload.setAccount(SysConfig.getAccount());
        upload.setConnectIp(SysConfig.getConnectIp());
        upload.setConnectPort(MathUtil.asInteger(SysConfig.getConnectPort()));
        upload.setPassword(SysConfig.getPassword());
        upload.start(4,60);
    }

    @Test
    public void testCertifyCreituserService() {
        uploadCertifyCreituserService();
    }

    /**
     * 开户
     * @return
     */
    public Map uploadCertifyCreituserService1() {
        CertifyCreituserService certifyCreituserService = new CertifyCreituserService();
        certifyCreituserService.upload = upload;
        Result result = certifyCreituserService.entry(Kv.by("id", "131124199208111410")
                .set("Name", "郭志佳")
                .set("userIdType", 101)
                .set("phone", "15349702229")
                .set("registeredType", 2)
                .set("issueChannelId", "6301019999901030001")
                .set("address", "青海省西宁市东大街33号"));
        System.out.println(result);
        return (Map) result.getData();
    }
    /**
     * 开户
     * @return
     */
    public Map uploadCertifyCreituserService() {
        CertifyCreituserService certifyCreituserService = new CertifyCreituserService();
        certifyCreituserService.upload = upload;
        Result result = certifyCreituserService.entry(Kv.by("id", "630121199309167905")
                .set("Name", "付秋霞")
                .set("userIdType", 101)
                .set("phone", "18797208055")
                .set("registeredType", 2)
                .set("issueChannelId", "6301019999921210010")
                .set("address", "青海省西宁市城西区"));
        System.out.println(result);
        return (Map) result.getData();
    }
    /**
     * 企业开户
     * @return
     */
    @Test
    public void uploadCertifyCreitcorpService() {
        CertifyCreitcorpService certifyCreitcorpService = new CertifyCreitcorpService();
        certifyCreitcorpService.upload = upload;
        Result result = certifyCreitcorpService.entry(Kv.by("id", "91510107MA6B38AW12")
                .set("name", "马乙四么兰")
                .set("corpIdType", 201)
                .set("phone", "17070507666")
                .set("registeredType", 2)
                .set("issueChannelId", "6301019999921210008")
                .set("address", "青海省化隆回族自治县德恒隆乡")
                .set("department", "四川大浪淘沙商务信息有限公司")
                .set("agentName", "马乙四么兰")
                .set("agentIdType", 101)
                .set("agentIdNum", "63212719910111357X")
        );
        System.out.println(result);
    }

    @Test
    public void testCertifyVloflUploadService() {
        uploadCertifyVloflUploadService();
    }
    /**
     * 车辆信息上传
     * @return
     */
    public Map uploadCertifyVloflUploadService1() {
        Map data = uploadIssuePcoflUploadService();
        CertifyVloflUploadService certifyVloflUploadService = new CertifyVloflUploadService();
        certifyVloflUploadService.upload = upload;
        Result result = certifyVloflUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("accountId", data.get("accountId"))
                .set("vin", "JTEBX3FJXF5078105")
                .set("vehicleId", "2c7f53d744d449c38419117cd4f8819f")
                .set("engineNum", "2TR1524402")
                .set("issueDate", "2019-01-01")
                .set("name", "郭志佳")
                .set("plateNum", plateNum)
                .set("registerDate", "2019-01-01")
                .set("useCharacter", 2)
                .set("vehicleType", "小轿车")
                .set("type", 1)
                .set("approvedCount", 5)
                .set("totalMass", 1650)
                .set("maintenaceMass", 1236)
                .set("permittedWeight", 3000)
                .set("outsideDimensions", "3000x4000x2400")
                .set("headtype",1)
        );
        System.out.println(result);
        return (Map) result.getData();
    }
    /**
     * 车辆信息上传
     * @return
     */
    public Map uploadCertifyVloflUploadService() {
        Map data = uploadIssuePcoflUploadService();
        CertifyVloflUploadService certifyVloflUploadService = new CertifyVloflUploadService();
        certifyVloflUploadService.upload = upload;
        Result result = certifyVloflUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("accountId", data.get("accountId"))
                .set("vin", "LFV3A23C9H3130678")
                .set("vehicleId", data.get("vehicleId"))
                .set("engineNum", "450322F")
                .set("issueDate", "2019-01-01")
                .set("name", "张十")
                .set("plateNum", plateNum)
                .set("registerDate", "2019-01-01")
                .set("useCharacter", 2)
                .set("vehicleType", "小轿车")
                .set("type", 1)
                .set("approvedCount", 5)
                .set("totalMass", 1650)
                .set("maintenaceMass", 1236)
                .set("permittedWeight", 3000)
                .set("outsideDimensions", "3000x4000x2400")
                .set("headtype",1)
        );
        System.out.println(result);
        return (Map) result.getData();
    }

    @Test
    public void testuploadUserSignoflUploadService() {
        uploadUserSignoflUploadService();
    }

    /**
     * 车辆支付渠道绑定/解绑通知
     * "vehicleId" -> "64e32e79ffed475d936e5669855afe56"
     * @return
     */
    public Map uploadUserSignoflUploadService() {
        Map data = uploadIssuePcoflUploadService();
        UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();
        userSignoflUploadService.upload = upload;
        Result result = userSignoflUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("accountId", data.get("accountId"))
                .set("vehicleId", data.get("vehicleId"))
                .set("plateNum", plateNum)
                .set("plateColor", 0)
                .set("signType", 2)
                .set("issueChannelId", "6301019999901030001")
                .set("channelType", "001")
                .set("cardType", 1)
                .set("enableTime", "2019-08-21T02:22:11")
                .set("closeTime", "2029-08-21T02:22:11")
                .set("status",1)
        );
        System.out.println(result);
        return (Map) result.getData();
    }

    /**
     * 账户信息查询
     */
    @Test
    public void testUserAccountUploadService() {
        UserAccountUploadService();
    }

    /**
     * 账户信息查询
     */
    public Map UserAccountUploadService() {
        Map data = uploadCertifyCreituserService();
        UserAccountUploadService userAccountUploadService = new UserAccountUploadService();
        userAccountUploadService.upload = upload;
        Result result = userAccountUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId")));
        System.out.println(result);
        Map getData = (Map) result.getData();
        if (getData != null) {
            List encryptedDataList = (List) getData.get("encryptedData");
            data.put("accountId", ((Map<Object, Object>)encryptedDataList.get(0)).get("accountId"));
        }
        return data;
    }

    @Test
    public void testUserObuoflService() {
        Map data = uploadCertifyCreituserService();
        UserObuoflService userObuoflService = new UserObuoflService();
        userObuoflService.upload = upload;
        Result result = userObuoflService.entry(Kv.by("accessToken", "d62f7b0e48184251afdf4c6ae8650c0f")
                .set("openId", "37140de930a847939204acc1392d23ba")
                .set("accountId", "7d5d3edb39034007b3828bafabe02be6")
                .set("vehicleId", "9dc4f19da3ed47eda72515fb6dddbcc8"));
        System.out.println(result);
    }

    @Test
    public void testUserCardoflService() {
        Map data = uploadCertifyCreituserService();
        UserCardoflService userCardoflService = new UserCardoflService();
        userCardoflService.upload = upload;
        Result result = userCardoflService.entry(Kv.by("accessToken", "d62f7b0e48184251afdf4c6ae8650c0f")
                .set("openId", "37140de930a847939204acc1392d23ba")
                .set("accountId", "7d5d3edb39034007b3828bafabe02be6")
                .set("obuId", "6301191010101898"));
        System.out.println(result);
    }

    /**
     * 车牌发行校验
     */
    @Test
    public void testIssuePcoflUploadService() {
        uploadIssuePcoflUploadService();
    }

    /**
     * 车牌唯一性校验
     */
    public Map uploadIssuePcoflUploadService() {
        Map data = UserAccountUploadService();
        IssuePcoflUploadService issuePcoflUploadService = new IssuePcoflUploadService();
        issuePcoflUploadService.upload = upload;
        Result result = issuePcoflUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("accountId", data.get("accountId"))
                .set("plateColor", 0)
                .set("plateNum", plateNum));
        System.out.println(result);
        Map getData = (Map) result.getData();
        if (getData != null) {
            data.put("vehicleId", getData.get("vehicleId"));
        }
        return data;
    }

    @Test
    public void testuploadIssueVertifyoflUploadService(){
        uploadIssueVertifyoflUploadService();
    }
    /**
     *   5.4证件信息验证
     */
    public Map uploadIssueVertifyoflUploadService() {
        Map data = UserAccountUploadService();
        IssueVertifyoflUploadService issueVertifyoflUploadService = new IssueVertifyoflUploadService();
        issueVertifyoflUploadService.upload = upload;

        Result result = issueVertifyoflUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("type", 1)
                .set("plateColor", 0)
                .set("plateNum", plateNum)
                .set("driverId","131124199208111410")
                .set("driverName","郭志佳")
                .set("driverIdType",101)
                .set("vin","JTEBX3FJXF5078105")
                .set("engineNum","2TR1524402")
                .set("issueDate","2019-02-11")
                .set("plateNum",plateNum)
                .set("registerDate","2019-02-11")
                .set("useCharacter",2)
                .set("vehicleType",1)
        );
        System.out.println(result);
        return data;
    }

    @Test
    public void testUploadUserCardinfoReqUploadService(){
        uploadUserCardinfoReqUploadService();
    }
    /**
     *   4.3  卡信息 新增及 变 更通知
     */
    public Map uploadUserCardinfoReqUploadService() {
        Map data = UserAccountUploadService();
        UserCardinfoUploadService userCardinfoUploadService = new UserCardinfoUploadService();
        userCardinfoUploadService.upload = upload;

        Result result = userCardinfoUploadService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("type", 1)
                .set("accountId", data.get("accountId"))
                .set("issuerId", CommonAttribute.ISSUER_CODE)
                .set("vehicleId", "2b3883ef6f8b46398c193b3f646dfccb")
                .set("cardId", "63011927230208068296")
                .set("cardType", 132)
                .set("brand", 1)
                .set("model", "SLE77CLFX2407PM")
                .set("plateNum", "青BZ1598")
                .set("plateColor", 0)
                .set("enableTime", "2019-09-01T10:09:46")
                .set("expireTime", "2029-09-01T10:09:46")
                .set("issueChannelType", 2)
                .set("issueChannelId", "6301019999921230004")
        );
        System.out.println(result);
        return data;
    }

    @Test
    public void testQueryUserObuoflService(){
        queryUserObuoflService();
    }

    /**
     *  3.5  获取 OBU 信息
     */
    public Map queryUserObuoflService() {
        Map data = UserAccountUploadService();
        UserObuoflService userObuoflService = new UserObuoflService();
        userObuoflService.upload = upload;

        Result result = userObuoflService.entry(Kv.by("accessToken", data.get("accessToken"))
                .set("openId", data.get("openId"))
                .set("accountId", data.get("accountId"))
                .set("vehicleId", data.get("vehicleId"))
        );
        System.out.println(result);
        return data;
    }
    /**
     *  2.3  刷新 接口调用凭证
     */
    @Test
    public void queryAuthTouchService() {
        AuthTouchService authTouchService = new AuthTouchService();
        authTouchService.upload = upload;

        Result result = authTouchService.entry(Kv.by("openId", "2a2c765b3b5d47a1ab04384e5a601b21")
                .set("accessToken", "19b379ab7a014a0cb9865fed05bb63fd")
        );
        System.out.println(result);
    }
    /**
     * 3.2  获取 账户信息
     */
    @Test
    public void queryUserAccountUploadService() {
        UserAccountUploadService userAccountUploadService = new UserAccountUploadService();
        userAccountUploadService.upload = upload;

        Result result = userAccountUploadService.entry(Kv.by("openId", "a76d4abf4ed940c886b9c2485c851a54")
                .set("accessToken", "dae9c15aed4d4356a3be6c561aa0d6a3")
        );
        System.out.println(result);
    }

    /**
     *  4.5 卡状态 变更通知
     */
    @Test
    public void testUserCardStatusUploadService() {
//        Map data = UserAccountUploadService();
        UserCardStatusUploadService userCardStatusUploadService = new UserCardStatusUploadService();
        userCardStatusUploadService.upload = upload;

        Result result = userCardStatusUploadService.entry(Kv.by("accessToken","dc49f5704fb043dba99ca609fe785e40")
                .set("openId", "7ac10b9071244943b0794815246ff87d")
                .set("accountId", "74ae40a4f30943d7b61b33819ca78d38")
                //3- 换卡
                .set("type", 3)
                .set("cardId", "63011929230203112717")
                //4-  有卡注销
                .set("status", 4)
        );
        System.out.println(result);
    }

}

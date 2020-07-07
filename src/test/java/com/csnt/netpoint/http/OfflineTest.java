package com.csnt.netpoint.http;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName HttpTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/22 10:21
 * Version 1.0
 **/
public class OfflineTest {

    @Test
    public void httpPostTest() {
        Map<String, String> headers = new HashMap();
        //按照调用顺序
        //2.1 线下渠道个人用户 CertifyCreituserService
//        headers.put("msgtype", "8820");
        //2.2 线下渠道单位用户 CertifyCreitcorpService
//        headers.put("msgtype", "8821");
        //2.3 刷新接口调用凭证 AuthTouchService
//        headers.put("msgtype", "8822");
        //3.1 获取用户基本信息 UserInfoUploadService
//        headers.put("msgtype", "8830");
        //3.2 获取账户信息 UserAccountUploadService
//        headers.put("msgtype", "8831");
        //5.1 车牌发行验证 IssuePcoflUploadService
//        headers.put("msgtype", "8850");
        //5.3 车辆信息上传 CertifyVloflUploadService
//        headers.put("msgtype", "8852");
        //5.2 车主身份信息上传 CertifyOwneroflUploadService
//        headers.put("msgtype", "8851");
        //3.3 获取车辆列表 UserVpoflService
//        headers.put("msgtype", "8832");
        //3.4 获取车辆信息 UserVqoflService
//        headers.put("msgtype", "8833");
        //4.1 车辆支付渠道绑定/解绑通知 UserSignoflUploadService
//        headers.put("msgtype", "8840");
        //4.2 OBU信息新增及变更通知  UserObuinfoUploadService
//        headers.put("msgtype", "8841");
        //3.5 获取OBU信息 UserObuoflService
//        headers.put("msgtype", "8834");
        //4.3 卡信息新增及变更通知 UserCardinfoUploadService
//        headers.put("msgtype", "8842");
        //3.6 获取卡信息 UserCardoflService
//        headers.put("msgtype", "8835");
        //4.4 OBU状态变更通知 UserObuStatusUploadService
//        headers.put("msgtype", "8843");
        //4.5 OBU状态变更通知 UserCardStatusUploadService
        headers.put("msgtype", "8844");
        //5.4 证件信息验证 IssueVertifyoflUploadService
//        headers.put("msgtype", "8853");

        Map map = new HashMap();
        String data = JsonKit.toJson(map);
        String outData = HttpKit.post("http://localhost:8001/offline", data, headers);
        System.out.println(outData);
    }

}

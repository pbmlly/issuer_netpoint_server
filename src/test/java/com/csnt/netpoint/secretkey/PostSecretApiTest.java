package com.csnt.netpoint.secretkey;

import com.csnt.ins.bizmodule.storecard.recharge.StoreCardRechargeRequestService;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.junit.Test;

import java.util.Map;

/**
 * @ClassName PostSecretApiTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/9/7 23:04
 * Version 1.0
 **/
public class PostSecretApiTest {

    @Test
    public void testStoreCardRechargeRequestService(){
        StoreCardRechargeRequestService service = new StoreCardRechargeRequestService();
        Record record = new Record();
        String paramStr = "{\"amount\":\"10000\",\"appID\":\"6301191523010900\",\"auditNo\":\"28\",\"clientID\":\"323231212232\"," +
                "\"deviceNo\":\"640111043298\",\"mac1\":\"A1B2C3D4\",\"operatorNo\":\"000000000000\",\"provinceID\":\"6401\",\"rand\":\"CDB26431\"," +
                "\"remain\":\"0\",\"seqNo\":\"0\",\"transTime\":\"2019-07-03 22:28:04\",\"version\":\"64\"}";
        record.set("bVer","");
        record.set("szAPPID","");
        record.set("szRnd","");
        record.set("wSeqNo","");
        record.set("bTransFlag","");
        record.set("szDeviceNo","");
        record.set("auditNo","");
        record.set("szDateTime","");
        record.set("szMAC1","");
        Map header = Kv.by("Content-Type","application/json");
        System.out.println(HttpKit.post(SysConfig.CONNECT.get("secret.key.url") + "api/cardcredit", paramStr,header));
    }
}

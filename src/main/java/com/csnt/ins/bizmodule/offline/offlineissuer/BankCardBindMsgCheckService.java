package com.csnt.ins.bizmodule.offline.offlineissuer;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.NettyUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 8802银行卡绑卡校验
 *
 * @author cml
 **/
public class BankCardBindMsgCheckService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(BankCardBindMsgCheckService.class);

    private final String serverName = "[8803银行卡绑卡短信校验]";
    private final String TRAN_CODE = "9902";


    /**
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            logger.info("{}银行绑卡参数：{}", serverName, record);

            Map checekMap = checkInput(record);
            if (!(boolean) checekMap.get("bool")) {
                return (Result) checekMap.get("result");
            }

            Map sendMap = getSendMsg(record);
            boolean isActive = (boolean) sendMap.get("isActive");
            if (!isActive) {
                return (Result) sendMap.get("result");
            }

            String sdData = (String) sendMap.get("data");

            //发送银行
            String bankId = record.get("bankPost");
            Map sedResMap = NettyUtil.accept(sdData, bankId);
            boolean sedActive = (boolean) sedResMap.get("isActive");
            if (!sedActive) {
                return Result.sysError(serverName + sedResMap.get("errMsg"));
            }
            String resJson = (String) sedResMap.get("retMsg");
            Map resMap = FastJson.getJson().parse(resJson, Map.class);

            // 测试代码，，，，固定写死返回
//            Map bankMap = new HashMap<>();
//            bankMap.put("acc_type","3");
//            bankMap.put("return_msg","验证通过");
//            bankMap.put("return_code","0");
//            resMap = bankMap;
            // 测试代码.......................


            return Result.success(resMap);
        } catch (Throwable t) {
            logger.error("{}查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    private Map checkInput(Record inMap) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        //银行卡号或账号
        String accountId = inMap.get("accountId");
        //用户类型
        Integer userType = inMap.get("userType");
        //银行预留手机号
        String linkMobile = inMap.get("linkMobile");
        //银行账户名称
        String userName = inMap.get("userName");
        //用户证件号
        String certsn = inMap.get("certsn");

        if (StringUtil.isEmpty(accountId, userType, linkMobile, userName, certsn)) {
            logger.error("{}参数accountId, userType, linkMobile, userName, certsn不能为空", serverName);
            outMap.put("result", Result.paramNotNullError("accountId, userType, linkMobile, userName, certsn"));
            outMap.put("bool", false);
            return outMap;
        }
        //企业用户ETC业务协议号
        String protocolNumber = inMap.get("protocolNumber");
        //网点编号
        String posid = inMap.get("posid");
        //请 求 日 期 时 间
        String genTime = inMap.get("genTime");
        //请求流水
        String trxSerno = inMap.get("trx_serno");
        //员工统一认证号
        String employeeId = inMap.get("employeeId");
        //源请求流水
        String orgTrxSerno = inMap.get("org_trx_serno");
        //手机验证码
        String chkcode = inMap.get("chkcode");
        //渠道类型
        String channelType = inMap.get("channelType");

        if (StringUtil.isEmpty(posid, genTime, trxSerno, employeeId, orgTrxSerno, chkcode)) {
            logger.error("{}参数posid, genTime, trxSerno, employeeId,orgTrxSerno,chkcode不能为空", serverName);
            outMap.put("result", Result.paramNotNullError("protocolNumber, posid, genTime, trxSerno, employeeId,orgTrxSerno,chkcode"));
            outMap.put("bool", false);
            return outMap;
        }
        //判断企业账户是否为空 1-个人 2-企业
        if (userType == 2) {
            if (StringUtil.isEmpty(protocolNumber)) {
                logger.error("{}企业用户参数protocolNumber不能为空", serverName);
                outMap.put("result", Result.sysError("企业用户ETC业务协议号不能为空"));
                outMap.put("bool", false);
            }
        } else {
            inMap.set("protocolNumber", "");
        }
        return outMap;
    }

    private Map getSendMsg(Record dataRc) {

        Map outMap = new HashMap<>();
        outMap.put("isActive", true);
        outMap.put("result", null);
        outMap.put("data", null);

        Map bkMap = new HashMap<>();
        bkMap.put("accountid", dataRc.get("accountid"));
//        bkMap.put("usertype",dataRc.get("userType"));
        bkMap.put("linkmobile", dataRc.get("linkmobile"));
        bkMap.put("username", dataRc.get("username"));
        bkMap.put("certsn", dataRc.get("certsn"));
        bkMap.put("posid", dataRc.get("posid"));
        bkMap.put("gentime", dataRc.get("gentime"));
        bkMap.put("trx_serno", dataRc.get("trx_serno"));
        bkMap.put("employeeid", dataRc.get("employeeid"));
//        bkMap.put("protocolnumber",dataRc.get("protocolNumber"));
        bkMap.put("org_trx_serno", dataRc.get("org_trx_serno"));
        bkMap.put("chkcode", dataRc.get("chkcode"));

        String bankId = dataRc.get("bankPost");
        if (bankId.equals("63010102001")
                || bankId.equals("63010102003")
                || bankId.equals("63010102002")
                || bankId.equals("63010102004")
                || bankId.equals("63010102005")
                || bankId.equals("63010102033")) {
            bkMap.put("usertype", dataRc.get("userType"));
            bkMap.put("protocolnumber", dataRc.get("protocolNumber"));
        }


        String json = Jackson.getJson().toJson(bkMap);
        String dataGbk = "";
        String unicode = "";
        try {
            byte[] gbkBytes = json.getBytes(StringUtil.GBK);
            dataGbk = new String(gbkBytes, StringUtil.GBK);
        } catch (Exception e) {
            logger.error("{}json字符串转GBK失败：{}", serverName, json);
            outMap.put("result", Result.sysError(serverName + e.getMessage()));
            outMap.put("isActive", false);
            return outMap;
        }
        String md5 = UtilMd5.EncoderByMd5Gbk(dataGbk);

        String pattern = "00000000";
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        byte[] responseBytes = dataGbk.getBytes(StringUtil.GBK);
        String sendData = decimalFormat.format(responseBytes.length) + TRAN_CODE + md5 + dataGbk;
        outMap.put("data", sendData);
        return outMap;
    }
}

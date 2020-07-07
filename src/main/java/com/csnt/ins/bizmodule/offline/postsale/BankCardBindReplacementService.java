package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.NettyUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
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
public class BankCardBindReplacementService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(BankCardBindReplacementService.class);

    private final String serverName = "[8821银行卡换卡检查]";
    private final String TRAN_CODE = "9906";
    private final String BANK_RETURN_SUCCESS = "0";

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

            Map checekMap = checkInput(record);
            if (!(boolean) checekMap.get("bool")) {
                return (Result) checekMap.get("result");
            }
            //发送银行
            String bankId = record.get("bankPost");

            //取车辆的绑定信息
            String vehicleId = record.get("vehicleId");
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflVehicleinfo == null) {
                logger.error("{}该车辆未绑定银行,vehicleId:{}", serverName, vehicleId);
                return Result.bizError(755, "该卡是总对总发行不能使用该接口。");
            }
            if (!bankId.equals(etcOflVehicleinfo.getBankPost())) {
                logger.error("{}换卡不能换绑定银行,vehicleId:{}", serverName, vehicleId);
                return Result.bizError(755, "换卡不能换绑定银行。");
            }
            // 检查该车辆是否有多个正常的卡信息，如果有错则报错
            Record cardInfo = Db.findFirst(DbUtil.getSql("countNomorlCardId"), vehicleId);
            if (cardInfo != null) {
               if (cardInfo.getInt("num") > 1) {
                   logger.error("{}该车辆有多张正常卡,数量:{}", serverName, cardInfo.getInt("num"));
                   return Result.bizError(755, "该车辆有多张正常卡，请选注销多余的正常卡。");
               }
            }


            Map sendMap = getSendMsg(record);
            boolean isActive = (boolean) sendMap.get("isActive");
            if (!isActive) {
                return (Result) sendMap.get("result");
            }

            String sdData = (String) sendMap.get("data");




            logger.info("{}转发银行[bankPost={}]开始:{}", serverName, bankId, sdData);
            Map sedResMap = NettyUtil.accept(sdData, bankId);
            boolean sedActive = (boolean) sedResMap.get("isActive");
            if (!sedActive) {
                logger.error("{}发送银行失败:{}", serverName, sedResMap.get("errMsg"));
                return Result.sysError(serverName + sedResMap.get("errMsg"));
            }
            String resJson = (String) sedResMap.get("retMsg");
            logger.info("{}发送银行[bankPost]完成,响应内容:{}", serverName, bankId, resJson);
            Map resMap = FastJson.getJson().parse(resJson, Map.class);

            if (!BANK_RETURN_SUCCESS.equals(resMap.get("return_code"))) {
                return Result.sysError(serverName + "银行返回失败：" + resMap.get("return_msg"));
            }

            // 测试代码，，，，固定写死返回
//            Map bankMap = new HashMap<>();
//            bankMap.put("acc_type","1");
//            bankMap.put("return_msg","成功");
//            bankMap.put("return_code","0");
//            Map resMap = bankMap;
//             测试代码.......................

            return Result.success(resMap);
        } catch (Throwable t) {
            logger.error("{}转发银行失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 参数检查
     *
     * @param inMap
     * @return
     */
    private Map checkInput(Record inMap) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 车辆编号
        String vehicleId = inMap.get("vehicleId");
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

        if (StringUtil.isEmpty(vehicleId,accountId, userType, linkMobile, userName, certsn)) {
            logger.error("{}参数vehicleId,accountId, userType, linkMobile, userName, certsn不能为空", serverName);
            outMap.put("result", Result.paramNotNullError("vehicleId,accountId, userType, linkMobile, userName, certsn"));
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
        //渠道类型
        String channelType = inMap.get("channelType");

        if (StringUtil.isEmpty(posid, genTime, trxSerno, employeeId)) {
            logger.error("{}参数posid, genTime, trxSerno, employeeId不能为空", serverName);
            outMap.put("result", Result.paramNotNullError("posid, genTime, trxSerno, employeeId"));
            outMap.put("bool", false);
            return outMap;
        }

        //1-个人用户  2-企业用户
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

        bkMap.put("linkmobile", dataRc.get("linkmobile"));
        bkMap.put("username", dataRc.get("username"));
        bkMap.put("certsn", dataRc.get("certsn"));
        bkMap.put("posid", dataRc.get("posid"));
        bkMap.put("gentime", dataRc.get("gentime"));
        bkMap.put("trx_serno", dataRc.get("trx_serno"));
        bkMap.put("employeeid", dataRc.get("employeeid"));
        String json = Jackson.getJson().toJson(bkMap);
        String dataGbk = "";
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

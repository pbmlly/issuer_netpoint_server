package com.csnt.ins.bizmodule.offline.postsale;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.json.FastJson;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 8802银行卡绑卡校验
 *
 * @author cml
 **/
public class BankCardBindMsgReplacementConfirmService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(BankCardBindMsgReplacementConfirmService.class);

    private final String serverName = "[8822银行卡换卡请求确认]";
    private final String TRAN_CODE = "9905";
    private final String NORMAIL_CODE = "0";
    private final String TABLE_ETC_CARDINFO = "etc_cardinfo";
    private final String TABLE_ETC_CARDINFO_HIS = "etc_cardinfo_history";
    private final String BANK_RETURN_SUCCESS = "0";
    private final String BANK_RETURN_NOMARL = "66666";
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
            // 检查该车辆是否有多个正常的卡信息，如果有错则报错
            //取车辆的绑定信息
            String vehicleId = record.get("vehicleId");
            Record cardInfo = Db.findFirst(DbUtil.getSql("countNomorlCardId"), vehicleId);
            if (cardInfo != null) {
                if (cardInfo.getInt("num") > 1) {
                    logger.error("{}该车辆有多张正常卡,数量:{}", serverName, cardInfo.getInt("num"));
                    return Result.bizError(755, "该车辆有多张正常卡，请选注销多余的正常卡。");
                }
            } else {
                logger.error("{}该车辆未有正常的卡", serverName);
                return Result.bizError(756, "该车辆未有正常的卡。");
            }

            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflVehicleinfo == null) {
                logger.error("{}未查询到车辆EtcOflVehicleinfo表的开户信息", serverName);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "未查询到车辆信息");
            }
            if (!record.getStr("bankPost").equals(etcOflVehicleinfo.getBankPost())) {
                logger.error("{}换卡不能换绑定银行,vehicleId:{}", serverName, vehicleId);
                return Result.bizError(755, "换卡不能换绑定银行。");
            }

            Record cardRc = Db.findFirst(DbUtil.getSql("queryNomorlCardId"), vehicleId);
            String cardId = cardRc == null?null:cardRc.getStr("id");

            Map sendMap = getSendMsg(record,cardId);
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

            if (!BANK_RETURN_SUCCESS.equals(resMap.get("return_code"))
                    &&  !BANK_RETURN_NOMARL.equals(resMap.get("return_code"))) {
                return Result.sysError(serverName + "银行返回失败："+ resMap.get("return_msg"));
            }

            // 测试代码，，，，固定写死返回
//            Map bankMap = new HashMap<>();
//            bankMap.put("acc_type","3");
//            bankMap.put("return_msg","验证通过");
//            bankMap.put("return_code","0");
//            Map resMap = bankMap;
            // 测试代码.......................
            logger.info("{}银行绑定参数后反馈：{}", serverName, resMap);
//            if (!NORMAIL_CODE.equals(resMap.get("return_code"))) {
//                logger.error("{}银行反馈:{}", serverName, cardInfo.getInt("num"));
//                return Result.bizError(Integer.parseInt(resMap.get("return_code").toString()),"银行返回错误：" + resMap.get("return_msg").toString());
//            }

            // 4.1车辆支付渠道绑定、解绑通知
            Integer accType = Integer.parseInt(resMap.get("acc_type").toString());
            // 1-信用卡 2-借记卡
            int drAC = 2;
            if (accType == 2) {
                drAC = 1;
            }
            record.set("cardType",drAC);
            // 更新CARDId,车辆信息表

            //设置新的绑卡信息
            etcOflVehicleinfo.setVehicleId(vehicleId);

            etcOflVehicleinfo.setUserType(record.get("userType"));


            if (SysConfig.getEncryptionFlag()) {
                //需要加密
                //银行卡号或账号
                etcOflVehicleinfo.setAccountId(MyAESUtil.Encrypt( record.getStr("accountId")));
                //银行预留手机号
                etcOflVehicleinfo.setLinkMobile(MyAESUtil.Encrypt( record.getStr("linkMobile")));
                //银行账户名称
                etcOflVehicleinfo.setBankUserName(MyAESUtil.Encrypt( record.getStr("userName")));
                //银行卡绑定用户身份证号
                etcOflVehicleinfo.setCertsn(MyAESUtil.Encrypt( record.getStr("certsn")));
            } else{
                //银行卡号或账号
                etcOflVehicleinfo.setAccountId(record.get("accountId"));
                //银行预留手机号
                etcOflVehicleinfo.setLinkMobile(record.get("linkMobile"));
                //银行账户名称
                etcOflVehicleinfo.setBankUserName(record.get("userName"));
                //银行卡绑定用户身份证号
                etcOflVehicleinfo.setCertsn(record.get("certsn"));
            }

            //企业用户ETC 业务协议号
            etcOflVehicleinfo.setProtocolNumber(record.get("protocolNumber"));
            //网点编号
            etcOflVehicleinfo.setPosId(record.get("posid"));
            //银行绑卡请求时间
            Date genTime;
            try {
                genTime = DateUtil.parseDate(record.getStr("genTime"), DateUtil.FORMAT_YYYYM_MDDH_HMMSS);
                if (genTime == null) {
                    genTime = DateUtil.parseDate(record.getStr("genTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                }
            } catch (Exception e) {
                logger.error("{}转换时间异常:{}", serverName, e.toString(), e);
                return Result.sysError("genTime时间格式异常");
            }
            etcOflVehicleinfo.setGenTime(genTime);
            //银行绑卡校验请求流水号
            etcOflVehicleinfo.setTrxSerno(record.get("trx_serno"));
            //员工推荐人工号
            etcOflVehicleinfo.setEmployeeId(record.get("employeeId"));
            //原请求流水
            etcOflVehicleinfo.setOrgTrxSerno(record.get("org_trx_serno"));
            //绑定的卡类型1-信用卡 2-借记卡
            etcOflVehicleinfo.setCardType(record.get("cardType"));
            //绑定银行账户类型
            etcOflVehicleinfo.setAccType(Integer.parseInt(resMap.get("acc_type").toString()));
            //银行编码
            etcOflVehicleinfo.setBankPost(record.get("bankPost"));
            //渠道类型
            etcOflVehicleinfo.setChannelType(record.get("channelType"));
            //绑定状态1:绑定 2:解绑
            etcOflVehicleinfo.setBindStatus(1);

            // 更新卡信息
            // 个人或单位银行卡号或账号

            cardRc.set("accountid", etcOflVehicleinfo.getAccountId());
            // 银行预留手机号
            cardRc.set("linkmobile", etcOflVehicleinfo.getLinkMobile());
            // 银行账户名称
            cardRc.set("bankusername", etcOflVehicleinfo.getBankUserName());
            // 银行卡绑定用户身份证号
            cardRc.set("certsn", etcOflVehicleinfo.getCertsn());
            // 网点编号
            cardRc.set("posid", etcOflVehicleinfo.getPosId());
            // 银行绑卡请求时间
            cardRc.set("gentime", etcOflVehicleinfo.getGenTime() == null ? null : DateUtil.formatDate(etcOflVehicleinfo.getGenTime(), DateUtil.FORMAT_YYYYM_MDDH_HMMSS));
            // 银行绑卡9902请求流水号
            cardRc.set("trx_serno", etcOflVehicleinfo.getTrxSerno());
            // 员工推荐人工号
            cardRc.set("employeeid", etcOflVehicleinfo.getEmployeeId());
            // 银行验证9901请求流水
            cardRc.set("org_trx_serno", etcOflVehicleinfo.getOrgTrxSerno());
            // 绑定银行账户类型,
            cardRc.set("acc_type", etcOflVehicleinfo.getAccType());
            // 银行编码,
            cardRc.set("bankPost",etcOflVehicleinfo.getBankPost());
            // 用户类型,
            cardRc.set("userType", etcOflVehicleinfo.getUserType());
            // 绑定卡类型,
            cardRc.set("bindCardType", etcOflVehicleinfo.getCardType());
            // 绑定卡类型,
            cardRc.set("updateTime", new Date());
            //存储数据
            String ids = "id,opTime";
            boolean flag = Db.tx(() -> {
                if (!etcOflVehicleinfo.update()) {
                    logger.error("{}保存线下车辆绑卡信息etcOflVehicleinfo表失败", serverName);
                    return false;
                }
                if (!Db.update(TABLE_ETC_CARDINFO, "id", cardRc)) {
                    logger.error("{}保存卡注销信息到TABLE_ETC_CARDINFO表失败", serverName);
                    return false;
                }
                cardRc.set("opTime",new Date());
                cardRc.set("isuploadBank",1);
                //操作员
                String opId = record.get("opId");
                cardRc.set("operatorId",opId);
                if (!Db.save(TABLE_ETC_CARDINFO_HIS, ids, cardRc)) {
                    logger.error("{}保存TABLE_ETC_CARDINFO_HIS表数据失败", serverName);
                    return false;
                }
                return true;
            });
            if (flag) {
                logger.info("{}卡换帮银行卡成功,cardid = {}", serverName,cardRc.get("id"));
                return Result.success(null, "卡换帮银行卡成功");
            } else {
                logger.error("{}卡换帮银行卡失败,入库失败", serverName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }


        } catch (Throwable t) {
            logger.error("{}换卡失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

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
        //源请求流水
        String orgTrxSerno = inMap.get("org_trx_serno");
        //手机验证码
        String chkcode = inMap.get("chkcode");
        //绑定的卡类型
//        Integer cardType = inMap.getInt("cardType");
        //渠道类型
        String channelType = inMap.get("channelType");
        //渠道类型
        String bankPost = inMap.get("bankPost");
        //操作员
        String opId = inMap.get("opId");
        if (StringUtil.isEmpty(posid, genTime, trxSerno, employeeId, orgTrxSerno, chkcode,bankPost,channelType,opId)) {
            logger.error("{}参数posid, genTime, trxSerno, employeeId,orgTrxSerno,chkcode,bankPost,channelType,opId不能为空", serverName);
            outMap.put("result", Result.paramNotNullError("protocolNumber, posid, genTime, trxSerno, employeeId,orgTrxSerno,chkcode,bankPost,channelType,opId"));
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

    private Map getSendMsg(Record dataRc,String cardId) {

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
        bkMap.put("vehicleId", dataRc.get("vehicleId"));
        bkMap.put("CardId", cardId);
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

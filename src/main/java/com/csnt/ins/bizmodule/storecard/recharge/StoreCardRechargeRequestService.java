package com.csnt.ins.bizmodule.storecard.recharge;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.CardYGZStatusEnum;
import com.csnt.ins.enumobj.ReChargeBusinessTypeEnum;
import com.csnt.ins.enumobj.ReChargeStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * @ClassName StoreCardRechargeRequestService
 * @Description 8701储值卡充值请求接口
 * @Author duwanjiang
 * @Date 2019/9/6 10:26
 * Version 1.0
 **/
public class StoreCardRechargeRequestService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(StoreCardRechargeRequestService.class);

    private final String serviceName = "[8701储值卡充值请求接口]";

    private final String TABLE_ETCTS_STORECARD_RECHARGE_LIST = "etcts_storecard_recharge_list";

    /**
     * 调用老新秘钥接口--圈存
     */
    private final String API_CARDCREDIT = "api/CardCredit";


    /**
     * 判断业务类型:
     * a、充值：
     * 1、判断储值卡是否存
     * 2、判断卡类型是否为储值卡
     * 3、判断储值卡状态是否正常
     * 4、判断充值金额不能<0
     * 5、业务类型是否是充值
     * 6、调用秘钥接口
     * 7、入库为待确认状态
     * 8、响应给客户端
     * b、抵充：
     * 1、判断流水号是否存在
     * 2、判断流水的状态是否为待确认
     * 3、更新流水的状态为已抵消
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        Record resultRecord = new Record();
        try {
            Record record = new Record().setColumns(dataMap);
            //判断业务类型
            int businessType = record.getInt("businessType");
            String id = record.getStr("id");

            if (StringUtil.isEmpty(businessType,id)) {
                logger.error("{}businessType, id不能为空", serviceName);
                return Result.paramNotNullError("businessType, id");
            }
            if (ReChargeBusinessTypeEnum.RECHARGE.equals(businessType)) {
                String paramStr = "id,paidAmount,giftAmount,rechargeAmount,remainAmount,cardId," +
                        "userName,userIdType,userIdNum,plateNum,plateColor,rechargeChannelType,channelId,bVer,szAPPID,szRnd,wSeqNo," +
                        "bTransFlag,szDeviceNo,auditNo,szDateTime,szMAC1,businessType,payType,opTime,orgId,operatorId,channelType";
                if (StringUtil.isEmptyArg(record, paramStr)) {
                    logger.error("{}请求的{}不能为空", serviceName, paramStr);
                    return Result.paramNotNullError(paramStr);
                }
            }

            String cardId = record.getStr("cardId");

            if (ReChargeBusinessTypeEnum.RECHARGE.equals(businessType)) {
                //a、充值操作
                //1、判断储值卡是否在我卡表中
                Record cardRecord = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
                if (cardRecord == null) {
                    logger.error("{}当前[cardId={}]储值卡在系统库中未找到", serviceName, cardId);
                    return Result.sysError("当前储值卡不存在,无法充值");
                }

                //2、判断卡类型是否为储值卡 2XX -储值卡  1XX-记账卡
                int cardType = cardRecord.getInt("cardType");
                if (cardType < 200) {
                    logger.error("{}当前[cardId={}]卡为记账卡,不能充值", serviceName, cardId);
                    return Result.sysError("当前卡为记账卡,不能充值");
                }

                //3、判断储值卡状态是否正常
                int status = cardRecord.getInt("status");
                if (!CardYGZStatusEnum.NORMAL.equals(status)) {
                    logger.error("{}当前[cardId={}]储值卡为[{}]状态,不是[正常]状态,无法充值", serviceName, cardId, CardYGZStatusEnum.getName(status));
                    return Result.sysError(String.format("当前储值卡为[%s]状态,不是[正常]状态,无法充值", CardYGZStatusEnum.getName(status)));
                }

                //4、判断充值金额不能<0
                //充值金额
                long rechargeAmount = record.getLong("rechargeAmount");
                if (rechargeAmount <= 0) {
                    logger.error("{}充值金额不能小于等于0分");
                    return Result.sysError("充值金额不能小于等于0分");
                }
                //实收金额
                long paidAmount = record.getLong("paidAmount");
                if (paidAmount < 0) {
                    logger.error("{}实收金额不能小于0分");
                    return Result.sysError("实收金额不能小于0分");
                }
                //赠送金额
                long giftAmount = record.getLong("giftAmount");
                if (giftAmount < 0) {
                    logger.error("{}赠送金额不能小于0分");
                    return Result.sysError("赠送金额不能小于0分");
                }


                //5、调用秘钥接口
                Result result = postSecretKeyApi(record);
                if (!result.getSuccess()) {
                    logger.error("{}调用秘钥接口失败", serviceName);
                    return result;
                }

                Map resultMap = (Map) result.getData();
                //MAC1
                resultRecord.set("szMAC", resultMap.get("mac2"));


                //6、入库为待确认状态
                Record reChargeRecord = getDbRecord(record);
                if (Db.save(TABLE_ETCTS_STORECARD_RECHARGE_LIST, reChargeRecord)) {
                    logger.info("{}充值成功", serviceName);
                } else {
                    logger.error("{}充值入库失败", serviceName);
                    return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "充值入库失败");
                }

            } else if (ReChargeBusinessTypeEnum.RECHARGE_OFFSET.equals(businessType)) {
                //b、充值抵充操作
                //1、判断流水号是否存在
                Record reChargeRecord = Db.findFirst(DbUtil.getSql("queryReChargeListById"), id);
                if (reChargeRecord == null) {
                    logger.error("{}抵充充值流水时,未找到对应的流水[id={}]", serviceName, id);
                    return Result.sysError("抵充充值流水时,未找到对应的流水");
                }

                //2、判断流水的状态是否为待确认
                int reChargeStatus = reChargeRecord.getInt("status");
                if (!ReChargeStatusEnum.UNCONFIRM.equals(reChargeStatus)) {
                    logger.error("{}当前流水为[{}]状态,非[待确认]状态,不能进行抵充[id={}]",
                            serviceName, ReChargeStatusEnum.getName(reChargeStatus), id);
                    return Result.sysError(String.format("当前流水为[%s]状态,非[待确认]状态,不能进行抵充", ReChargeStatusEnum.getName(reChargeStatus)));
                }

                //3、更新流水的状态为已抵消
                Record offsetRecord = new Record();
                offsetRecord.set("id", id);
                //设置为已抵充状态
                offsetRecord.set("status", ReChargeStatusEnum.OFFSET.getValue());
                offsetRecord.set("updateTime", new Date());
                if (Db.update(TABLE_ETCTS_STORECARD_RECHARGE_LIST, offsetRecord)) {
                    logger.info("{}充值抵充成功", serviceName);
                } else {
                    logger.error("{}充值抵充入库失败", serviceName);
                    return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "充值抵充入库失败");
                }

            } else {
                logger.error("{}当前业务类型为[{}],不能进行[充值]和[充值抵充]",
                        serviceName, ReChargeBusinessTypeEnum.getName(businessType));
                return Result.sysError(String.format("当前业务类型为[%s],不能进行[充值]和[充值抵充]操作", ReChargeBusinessTypeEnum.getName(businessType)));
            }

        } catch (Exception e) {
            logger.error("{}储值卡充值异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
        //6、响应给客户端
        return Result.success(resultRecord);
    }

    /**
     * 获取入库的对象
     *
     * @param record
     * @return
     */
    private Record getDbRecord(Record record) {
        Record reChargeRecord = new Record();
        reChargeRecord.set("id", record.getStr("id"));
        reChargeRecord.set("paidAmount", record.get("paidAmount"));
        reChargeRecord.set("giftAmount", record.get("giftAmount"));
        reChargeRecord.set("rechargeAmount", record.get("rechargeAmount"));
        reChargeRecord.set("remainAmount", record.get("remainAmount"));
        reChargeRecord.set("cardId", record.get("cardId"));
        reChargeRecord.set("userName", record.get("userName"));
        reChargeRecord.set("userIdType", record.get("userIdType"));
        reChargeRecord.set("userIdNum", record.get("userIdNum"));
        if  (SysConfig.getEncryptionFlag()) {
            try {
                reChargeRecord.set("userName",  MyAESUtil.Encrypt( record.getStr("userName")));
                reChargeRecord.set("userIdNum",MyAESUtil.Encrypt( record.getStr("userIdNum")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reChargeRecord.set("plateNum", record.get("plateNum"));
        reChargeRecord.set("plateColor", record.get("plateColor"));
        reChargeRecord.set("rechargeChannelType", record.get("rechargeChannelType"));
        reChargeRecord.set("channelId", record.get("channelId"));
        reChargeRecord.set("businessType", record.get("businessType"));
        reChargeRecord.set("status", ReChargeStatusEnum.UNCONFIRM.getValue());
        reChargeRecord.set("payType", record.get("payType"));
        reChargeRecord.set("opTime", record.get("opTime"));
        reChargeRecord.set("orgId", record.get("orgId"));
        reChargeRecord.set("operatorId", record.get("operatorId"));
        reChargeRecord.set("channelType", record.get("channelType"));
        reChargeRecord.set("createTime", new Date());
        reChargeRecord.set("updateTime", new Date());
        return reChargeRecord;
    }

    /**
     * 推送秘钥接口
     *
     * @return
     */
    public Result postSecretKeyApi(Record record) {
        long startTime = System.currentTimeMillis();
        Result result = new Result();
        Date transTime = DateUtil.parseDate(record.get("szDateTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
        if (transTime == null) {
            logger.error("{}传入szDateTime时间格式不正确:{}", serviceName, record.get("szDateTime"));
            result = Result.sysError("传入szDateTime时间格式不正确");
            return result;
        }
        //客户端编号
        Map params = Kv.by("clientID", SysConfig.CONFIG.get("secret.key.clientid"))
                //省代码
                .set("provinceID", CommonAttribute.ISSUER_NETID)
                //卡片版本
                .set("version", record.getStr("bVer"))
                //用户卡内部编号
                .set("appID", record.getStr("szAPPID"))
                //随机数
                .set("rand", record.getStr("szRnd"))
                //终端编号
                .set("deviceNo", record.getStr("szDeviceNo"))
                //终端流水号
                .set("auditNo", record.getStr("auditNo"))
                //在线交易序号
                .set("seqNo", record.getStr("wSeqNo"))
                //圈存金额
                .set("amount", record.getStr("rechargeAmount"))
                //交易前余额
                .set("remain", record.getStr("remainAmount"))
                //MAC1
                .set("mac1", record.getStr("szMAC1"))
                //终端日期时间 yyyy-MM-dd HH:mm:ss，必须
                .set("transTime", DateUtil.formatDate(transTime, DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS))
                //操作员编号
                .set("operatorNo", record.getStr("operatorId"));
        String paramStr = FastJson.getJson().toJson(params);
        Map header = Kv.by("Content-Type", "application/json");
        String resData = HttpKit.post(SysConfig.CONNECT.get("secret.key.url") + API_CARDCREDIT, paramStr, header);
        logger.info("{}调取秘钥接口完成,耗时[{}],响应信息:{}", serviceName, DateUtil.diffTime(startTime, System.currentTimeMillis()), resData);
        Map resultMap = FastJson.getJson().parse(resData, Map.class);
        if (!resultMap.get("errorCode").equals("0000")) {
            String errorMsg = resultMap.get("errorMessage") == null ? "" : resultMap.get("errorMessage").toString();
            logger.error("{}调用秘钥接口失败:{}", serviceName, errorMsg);
            return Result.sysError("调用秘钥接口失败:" + errorMsg);
        }
        result.setData(resultMap);
        return result;
    }
}

package com.csnt.ins.bizmodule.storecard.chargeback;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ReChargeBusinessTypeEnum;
import com.csnt.ins.enumobj.ReChargeStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
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
 * @Description 8703储值卡充正请求接口
 * @Author duwanjiang
 * @Date 2019/9/6 10:26
 * Version 1.0
 **/
public class StoreCardChargebackRequestService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(StoreCardChargebackRequestService.class);

    private final String serviceName = "[8703储值卡充正请求接口]";

    private final String TABLE_ETCTS_STORECARD_RECHARGE_LIST = "etcts_storecard_recharge_list";

    private final String API_CARDDEBIT = "api/CardDebit";

    /**
     * 判断业务类型:
     * a、冲正：
     * 1、判断pid流水号是否存在
     * 2、判断pid流水号是否已经存在已冲正流水
     * 3、判断业务类型是否为充值
     * 4、判断流水状态是否为已确认状态
     * 5、冲正不能跨天
     * 6、冲正金额不能大于卡前余额
     * 7、调用秘钥接口
     * 8、入库为待确认状态
     * 9、响应给客户端
     * b、冲正抵充：
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
            String paramStr = "id,pid,remainAmount,rechargeChannelType,channelId,bVer,szAPPID,szRnd,wSeqNo,nAuditNo,bTransFlag," +
                    "szDeviceNo,szDateTime,szMAC,businessType,opTime,orgId,operatorId,channelType";
            if (StringUtil.isEmptyArg(record, paramStr)) {
                logger.error("{}请求的{}不能为空", serviceName, paramStr);
                return Result.paramNotNullError(paramStr);
            }

            //判断业务类型
            int businessType = record.getInt("businessType");
            if (ReChargeBusinessTypeEnum.CHARGE_BACK.equals(businessType)) {
                //a、冲正操作
                //1、判断pid流水号是否存在
                String pid = record.getStr("pid");
                Record reChargeRecord = Db.findFirst(DbUtil.getSql("queryReChargeListById"), pid);
                if (reChargeRecord == null) {
                    logger.error("{}充值冲正流水时,未找到对应的流水[id={}]", serviceName, pid);
                    return Result.sysError("充值冲正流水时,未找到对应的流水");
                }
                //2、判断pid流水号是否已经存在已冲正流水
                Record chargeBackCount = Db.findFirst(DbUtil.getSql("queryChargeBackCountBypId"), pid);
                if (chargeBackCount != null && chargeBackCount.getInt("num") > 0) {
                    logger.error("{}当前充值流水已被冲正,无法重复冲正[pid={}]", serviceName, pid);
                    return Result.sysError("当前充值流水已被冲正,无法重复冲正");
                }

                //3、判断业务类型是否为充值
                int reChargeBusinessType = reChargeRecord.getInt("businessType");
                if (!ReChargeBusinessTypeEnum.RECHARGE.equals(reChargeBusinessType)) {
                    logger.error("{}原流水业务类型为[{}],不能进行[充值冲正]和[冲正抵充]",
                            serviceName, ReChargeBusinessTypeEnum.getName(businessType));
                    return Result.sysError(String.format("当前业务类型为[%s],不能进行[充值冲正]和[冲正抵充]操作", ReChargeBusinessTypeEnum.getName(businessType)));
                }

                //4、判断流水状态是否为已确认状态
                int reChargeStatus = reChargeRecord.getInt("status");
                if (!ReChargeStatusEnum.CONFIRMED.equals(reChargeStatus)) {
                    logger.error("{}原流水为[{}]状态,非[已确认]状态,不能进行冲正[id={}]",
                            serviceName, ReChargeStatusEnum.getName(reChargeStatus), pid);
                    return Result.sysError(String.format("原流水为[%s]状态,非[已确认]状态,不能进行冲正", ReChargeStatusEnum.getName(reChargeStatus)));
                }

                //5、冲正不能跨天
                Date createTime = reChargeRecord.getDate("createTime");
                Date currentDate = DateUtil.parseDate(DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DD), DateUtil.FORMAT_YYYY_MM_DD);
                if (createTime.before(currentDate)) {
                    logger.error("{}只能冲正当日充值的流水[createTime={}]", serviceName, createTime);
                    return Result.sysError("只能冲正当日充值的流水");
                }

                //6、冲正金额不能大于卡前余额
                long remainAmount = record.getLong("remainAmount");
                long rechargeAmount = reChargeRecord.getLong("rechargeAmount");
                if (remainAmount < rechargeAmount) {
                    logger.error("{}当前卡内的交易前余额[{}]分小于被冲正流水的充值金额[{}]分,无法进行冲正",
                            serviceName, remainAmount, rechargeAmount);
                    return Result.sysError(String.format("当前卡内的交易前余额[%d]分小于被冲正流水的充值金额[%d]分,无法进行冲正", remainAmount, rechargeAmount));
                }

                //7、调用秘钥接口
                Result result = postSecretKeyApi(record, reChargeRecord);
                if (!result.getSuccess()) {
                    logger.error("{}调用秘钥接口失败", serviceName);
                    return result;
                }

                Map resultMap = (Map) result.getData();
                //MAC1
                resultRecord.set("szMAC", resultMap.get("mac1"));
                Date serverTime = DateUtil.parseDate((String) resultMap.get("serverTime"), DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS);
                //终端交易日期时间
                resultRecord.set("szDateTime", DateUtil.formatDate(serverTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));

                //8、入库为待确认状态
                Record dbRecord = getDbRecord(record, reChargeRecord);
                if (Db.save(TABLE_ETCTS_STORECARD_RECHARGE_LIST, dbRecord)) {
                    logger.info("{}冲正成功", serviceName);
                } else {
                    logger.error("{}冲正入库失败", serviceName);
                    return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "冲正入库失败");
                }

            } else if (ReChargeBusinessTypeEnum.CHARGE_BACK_OFFSET.equals(businessType)) {
                //b、冲正抵充操作
                //1、判断流水号是否存在
                String id = record.getStr("id");
                Record reChargeRecord = Db.findFirst(DbUtil.getSql("queryReChargeListById"), id);
                if (reChargeRecord == null) {
                    logger.error("{}抵充冲正流水时,未找到对应的流水[id={}]", serviceName, id);
                    return Result.sysError("抵充冲正流水时,未找到对应的流水");
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
                    logger.info("{}冲正抵充成功", serviceName);
                } else {
                    logger.error("{}冲正抵充入库失败", serviceName);
                    return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "冲正抵充入库失败");
                }

            } else {
                logger.error("{}当前业务类型为[{}],不能进行[充值冲正]和[冲正抵充]",
                        serviceName, ReChargeBusinessTypeEnum.getName(businessType));
                return Result.sysError(String.format("当前业务类型为[%s],不能进行[充值冲正]和[冲正抵充]操作", ReChargeBusinessTypeEnum.getName(businessType)));
            }

        } catch (Exception e) {
            logger.error("{}储值卡冲正异常:{}", serviceName, e.toString(), e);
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
    private Record getDbRecord(Record record, Record reChargeRecord) {
        reChargeRecord.set("id", record.getStr("id"));
        reChargeRecord.set("pid", record.getStr("pid"));
        //将充值金额改为负数
        reChargeRecord.set("rechargeAmount", -reChargeRecord.getLong("rechargeAmount"));
        reChargeRecord.set("rechargeChannelType", record.get("rechargeChannelType"));
        reChargeRecord.set("channelId", record.get("channelId"));
        reChargeRecord.set("businessType", record.get("businessType"));
        reChargeRecord.set("status", ReChargeStatusEnum.UNCONFIRM.getValue());
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
     * @param record
     * @param reChargeRecord
     * @return
     */
    public Result postSecretKeyApi(Record record, Record reChargeRecord) {
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
                .set("Rand", record.getStr("szRnd"))
                //终端编号
                .set("deviceNo", record.getStr("szDeviceNo"))
                //终端流水号
                .set("auditNo", record.getStr("nAuditNo"))
                //交易类型标识
                .set("transFlag", record.getStr("bTransFlag"))
                //在线交易序号
                .set("seqNo", record.getStr("wSeqNo"))
                //圈存金额
                .set("amount", reChargeRecord.getStr("rechargeAmount"))
                //交易前余额
                .set("remain", reChargeRecord.getStr("remainAmount"))
                //终端日期时间 yyyy-MM-dd HH:mm:ss，必须
                .set("transTime", DateUtil.formatDate(transTime, DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS))
                //操作员编号
                .set("operatorNo", record.getStr("operatorId"));
        String paramStr = FastJson.getJson().toJson(params);
        Map header = Kv.by("Content-Type", "application/json");
        String resData = HttpKit.post(SysConfig.CONNECT.get("secret.key.url") + API_CARDDEBIT, paramStr, header);
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

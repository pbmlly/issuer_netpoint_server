package com.csnt.ins.bizmodule.storecard.query;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.order.commonquery.QueryVehicleService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 8705储值卡充值流水查询接口
 *
 * @Author: duwanjiang
 * @Date: 2019/9/6 16:34
 */
public class StoreCardRechargeListQueryService implements IReceiveService {

    private Logger logger = LoggerFactory.getLogger(QueryVehicleService.class);

    private final String serviceName = "[8705储值卡充值流水查询接口]";

    @Override
    public Result entry(Map dataMap) {
        //返回容器
        Map outMap = new HashMap(4);
        List<Record> dataList;
        try {
            Record record = new Record().setColumns(dataMap);

            //客户证件类型
            Integer userIdType = record.get("userIdType") == null ? null : Integer.parseInt(record.get("userIdType").toString());
            //客户证件号
            String userIdNum = record.get("userIdNum") == null ? null : record.get("userIdNum").toString();
            //网点编号
            String channelId = record.get("channelId") == null ? null : record.get("channelId").toString();
            //员工编号
            String operatorId = record.get("operatorId") == null ? null : record.get("operatorId").toString();
            //支付类型
            Integer payType = record.get("payType") == null ? null :  Integer.parseInt(record.get("payType").toString());
            //车牌号
            String plateNum = record.get("plateNum") == null ? null : record.get("plateNum").toString();
            //车牌颜色
            Integer plateColor = record.get("plateColor") == null ? null : Integer.parseInt(record.get("plateColor").toString());
            //卡号
            String cardId = record.get("cardId") == null ? null : record.get("cardId").toString();
            //开始时间
            String startTime = record.get("startTime") == null ? null : record.get("startTime").toString();
            //结束时间
            String endTime = record.get("endTime") == null ? null : record.get("endTime").toString();
            //页码
            Integer pageNo = record.get("pageNo") == null ? null : Integer.parseInt(record.get("pageNo").toString());
            //页大小
            Integer pageSize = record.get("pageSize") == null ? null : Integer.parseInt(record.get("pageSize").toString());

            if (SysConfig.getEncryptionFlag()) {
                if (StringUtil.isNotEmpty(userIdNum)) {
                    userIdNum = MyAESUtil.Encrypt(userIdNum);
                }
            }
            //参数校验
            if (StringUtil.isEmpty(pageNo, pageSize)) {
                logger.error("{}传入的参数pageNo, pageSize不能为空", serviceName);
                return Result.paramNotNullError("pageNo, pageSize");
            }

            try {
                if (startTime != null && endTime != null) {

                    Date startDate = DateUtil.parseDate(startTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                    Date endDate = DateUtil.parseDate(endTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                    if (DateUtil.diffDays(startDate, endDate) > 31) {
                        logger.error("{}[{}-{}]时间范围不能超过31天", serviceName, startTime, endTime);
                        return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:时间范围不能超过31天");
                    }

                }
            } catch (Exception e) {
                logger.error("{}输入[{}-{}]时间格式异常:{}", serviceName, startTime, endTime, e);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:startTime-endTime时间时间格式是否为yyyy-MM-ddTHH:mm:ss");
            }

            if (pageNo <= 0) {
                logger.error("{}页码小于0[pageNo={}]", serviceName, pageNo);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:页码小于0");
            }
            if (pageSize <= 0 || pageSize > 100) {
                logger.error("{}页大小不合规[pageSize={}]", serviceName, pageSize);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:页大小不合规");
            }
            int stNum = (pageNo - 1) * pageSize;
            int endNum = pageSize;

            //sql构造
            Kv kv = new Kv();
            Kv sizeKv = new Kv();
            StringBuilder condition = new StringBuilder();
            List<Object> param = new ArrayList<>();

            DbUtil.initParam("userIdType", userIdType, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("userIdNum", userIdNum, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("channelId", channelId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("operatorId", operatorId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("plateNum", plateNum, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("plateColor", plateColor, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("payType", payType, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("cardId", cardId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("createTime", DateUtil.parseDate(startTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.GREATER_THAN_EQUAL);
            DbUtil.initParam("createTime", DateUtil.parseDate(endTime, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.LESS_THAN_EQUAL);
            kv.set("condition", condition.toString().replaceFirst("and", " "));
            sizeKv.set("condition", condition.toString().replaceFirst("and", " "));
            kv.set("limitsize", stNum + "," + endNum);

            //查询数据库数据
            SqlPara sqlPara = DbUtil.getSqlPara("listReChargeListByCondition", kv);
            //查询总条数
            SqlPara sqlSizePara = DbUtil.getSqlPara("listReChargeListByConditionSize", sizeKv);

            for (Object paramNode : param) {
                sqlPara.addPara(paramNode);
                sqlSizePara.addPara(paramNode);
            }

            dataList = Db.find(sqlPara);
            //解密输出
            if (dataList != null && SysConfig.getEncryptionFlag()) {

                for (int i=0; i < dataList.size();i++) {
                    Record map  = dataList.get(i);
                    map.set("userName",map.get("userName")== null?null:MyAESUtil.Decrypt( record.get("userName")));
                    map.set("userIdNum",map.get("userIdNum")== null?null:MyAESUtil.Decrypt( record.get("userIdNum")));
                }
            }


            outMap.put("rows", dataList);

            Record countRecord = Db.findFirst(sqlSizePara);
            outMap.put("count", countRecord.get("count"));
            outMap.put("rechargeAmountSum", countRecord.get("rechargeAmountSum"));
            logger.info("{}查询到数据有{}条", serviceName, dataList.size());
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        return Result.success(outMap);
    }
}

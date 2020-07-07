package com.csnt.ins.bizmodule.order.fastorderservice;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseInfoReceiveService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author luoxiaojian
 * @Description: 订单查询服务类 8902
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/27.
 */
public class OrderQueryService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);

    private String serviceName = "[8902订单查询]";

    @Override
    public Result entry(Map dataMap) {
        List<Record> datas = new ArrayList<>();
        try {
            Record record = new Record().setColumns(dataMap);
            String posId = null == record.get("posId") ? null : record.get("posId").toString();
            String operatorId = null == record.get("userId") ? null : record.get("userId").toString();
            Integer status = null == record.get("status") ? null : Integer.parseInt(record.get("status") + "");
            Integer orderStatus = null == record.get("orderStatus") ? null : Integer.parseInt(record.get("orderStatus") + "");
            Integer postStatus = null == record.get("postStatus") ? null : Integer.parseInt(record.get("postStatus") + "");
            String startDateStr = null == record.get("startDate") ? null : record.get("startDate").toString();
            String endDateStr = null == record.get("endDate") ? null : record.get("endDate").toString();
            Integer type = null == record.get("type") ? null : Integer.parseInt(record.get("type") + "");
            String plateNum = null == record.get("plateNum") ? null : record.get("plateNum").toString();
            String plateColor = null == record.get("plateColor") ? null : record.get("plateColor").toString();
            String accountName = null == record.get("accountName") ? null : record.get("accountName").toString();
            String mobile = null == record.get("mobile") ? null : record.get("mobile").toString();
            if (StringUtil.isEmpty(posId, operatorId, status, startDateStr, endDateStr)) {
                logger.error("{}传入的参数posId, operatorId, status, startDateStr, endDateStr不能为空", serviceName);
                return Result.paramNotNullError("posId, operatorId, status, startDateStr, endDateStr");
            }

            try {
                Date startDate = DateUtil.parseDate(startDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                Date endDate = DateUtil.parseDate(endDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                long diff = endDate.getTime() - startDate.getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days > 7) {
                    logger.error("{}[{}-{}]时间范围不能超过7天", serviceName, startDateStr, endDateStr);
                    return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:startDate-endDate时间范围不能超过7天");
                }
            } catch (Exception e) {
                logger.error("{}输入[{}-{}]时间格式异常:{}", serviceName, startDateStr, endDateStr, e);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:startDate-endDate时间时间格式是否为yyyy-MM-ddTHH:mm:ss");
            }

            StringBuilder condition = new StringBuilder();
            List<Object> param = new ArrayList<>();
            DbUtil.initParam("posId", posId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("operatorId", operatorId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("status", status, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("orderType", type, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("orderStatus", orderStatus, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("postStatus", postStatus, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("updateTime", DateUtil.parseDate(startDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.GREATER_THAN_EQUAL);
            DbUtil.initParam("updateTime", DateUtil.parseDate(endDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.LESS_THAN_EQUAL);
            DbUtil.initParam("plateNum", plateNum, condition, param, DbUtil.OPERATE.FULL_LIKE);
            DbUtil.initParam("plateColor", plateColor, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("accountName", accountName, condition, param, DbUtil.OPERATE.FULL_LIKE);
            DbUtil.initParam("mobile", mobile, condition, param, DbUtil.OPERATE.EQUAL);
            //查询数据库数据
            SqlPara sqlPara = DbUtil.getSqlPara("queryOrderInfo", Kv.by("condition", condition.toString().replaceFirst("and", " ")));
            for (Object paramNode : param) {
                sqlPara.addPara(paramNode);
            }

            datas = Db.find(sqlPara);
            logger.info("{}[posId={},operatorId={}]查询到的订单数据有{}条", serviceName, posId, operatorId, datas.size());
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        return Result.success(datas);
    }


}

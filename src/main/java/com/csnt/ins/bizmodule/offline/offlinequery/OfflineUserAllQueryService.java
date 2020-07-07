package com.csnt.ins.bizmodule.offline.offlinequery;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseInfoReceiveService;
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
 * @author cml
 * @Description: 客户列表信息查询
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/27.
 */
public class OfflineUserAllQueryService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);

    private String serviceName = "[1015用户列表查询]";

    @Override
    public Result entry(Map dataMap) {
        List<Record> datas = new ArrayList<>();
        Map outMap = new HashMap<>();
        try {
            Record record = new Record().setColumns(dataMap);
            Integer userType = null == record.get("userType") ? null : Integer.parseInt(record.get("userType") + "");
            Integer userIdType = null == record.get("userIdType") ? null : Integer.parseInt(record.get("userIdType") + "");
            String userIdNum = null == record.get("userIdNum") ? null : record.get("userIdNum").toString();
            String channelId = null == record.get("channelId") ? null : record.get("channelId").toString();
            String operatorId = null == record.get("operatorId") ? null : record.get("operatorId").toString();
            Integer pageNo = null == record.get("pageNo") ? null : Integer.parseInt(record.get("pageNo") + "");
            Integer pageSize = null == record.get("pageSize") ? null : Integer.parseInt(record.get("pageSize") + "");
            String startDateStr = null == record.get("startTime") ? null : record.get("startTime").toString();
            String endDateStr = null == record.get("endTime") ? null : record.get("endTime").toString();

            if (SysConfig.getEncryptionFlag()) {
                // 客户证件号码加密
                if (StringUtil.isNotEmpty(userIdNum)) {
                    userIdNum = MyAESUtil.Encrypt( record.get("userIdNum"));
                }
            }

            if (StringUtil.isEmpty(pageNo, pageSize)) {
                logger.error("{}传入的参数pageNo, pageSize不能为空", serviceName);
                return Result.paramNotNullError("pageNo, pageSize");
            }

            try {
                if (startDateStr != null && endDateStr != null) {
                    Date startDate = DateUtil.parseDate(startDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                    Date endDate = DateUtil.parseDate(endDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
                    if (DateUtil.diffDays(startDate,endDate) > 31) {
                        logger.error("{}[{}-{}]时间范围不能超过31天", serviceName, startDateStr, endDateStr);
                        return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:时间不能超过31天");
                    }
                }

            } catch (Exception e) {
                logger.error("{}输入[{}-{}]时间格式异常:{}", serviceName, startDateStr, endDateStr, e);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:startDate-endDate时间时间格式是否为yyyy-MM-ddTHH:mm:ss");
            }

            if (pageNo <=0) {
                logger.error("{}页码小于0[pageNo={}]", serviceName, pageNo);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:页码小于0");
            }
            if (pageSize <=0 || pageSize > 100) {
                logger.error("{}页大小不合规[pageSize={}]", serviceName, pageSize);
                return Result.bizError(ResponseStatusEnum.SYS_INVALID_PARAM.getCode(), "参数异常:页大小不合规");
            }
            int stNum = (pageNo -1) * pageSize;
//            int endNum = (pageNo -1) * pageSize + pageSize;


            StringBuilder condition = new StringBuilder();
            List<Object> param = new ArrayList<>();
            DbUtil.initParam("userType", userType, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("userIdType", userIdType, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("userIdNum", userIdNum, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("channelId", channelId, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("operatorId", operatorId, condition, param, DbUtil.OPERATE.EQUAL);
             DbUtil.initParam("registeredTime", DateUtil.parseDate(startDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.GREATER_THAN_EQUAL);
            DbUtil.initParam("registeredTime", DateUtil.parseDate(endDateStr, DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS), condition, param, DbUtil.OPERATE.LESS_THAN_EQUAL);
             //查询数据库数据
            Kv kv = new Kv();
            Kv ctkv = new Kv();
            kv.set("condition", condition.toString().replaceFirst("and", " "));
            ctkv.set("condition", condition.toString().replaceFirst("and", " "));

            kv.set("limitsize", stNum + "," +  pageSize);
            SqlPara sqlPara = DbUtil.getSqlPara("QueryOfflineListUserInfoAll",kv);
            SqlPara sqlCtPara = DbUtil.getSqlPara("QueryOfflineListUserInfoCtAll",ctkv);

            for (Object paramNode : param) {
                sqlPara.addPara(paramNode);
                sqlCtPara.addPara(paramNode);

            }
            datas = Db.find(sqlPara);

            //解密输出
            if (datas != null && SysConfig.getEncryptionFlag()) {

                for (int i=0; i < datas.size();i++) {
                    Record map  = datas.get(i);
                    map.set("userName",map.get("userName")== null?null:MyAESUtil.Decrypt( map.get("userName")));
                    map.set("userIdNum",map.get("userIdNum")== null?null:MyAESUtil.Decrypt( map.get("userIdNum")));
                    map.set("tel",map.get("tel")== null?null:MyAESUtil.Decrypt( map.get("tel")));
                    map.set("address",map.get("address")== null?null:MyAESUtil.Decrypt( map.get("address")));
                    map.set("agentName",map.get("agentName")== null?null:MyAESUtil.Decrypt( map.get("agentName")));
                    map.set("agentIdNum",map.get("agentIdNum")== null?null:MyAESUtil.Decrypt( map.get("agentIdNum")));
                    map.set("bankAccount",map.get("bankAccount")== null?null:MyAESUtil.Decrypt( map.get("bankAccount")));
                }
            }

            outMap.put("rows",datas);
            // 取汇总数据
            Record rc = Db.findFirst(sqlCtPara);
            outMap.put("count",rc.get("count"));

            logger.info("{}查询到数据有{}条", serviceName,  datas.size());
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        return Result.success(outMap);
    }


}

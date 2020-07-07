package com.csnt.ins.bizmodule.order.obuactivatenotice;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1104OBU二次激活申请查询
 *
 * @ClassName ObuActivateApplyQueryService
 * @Description TODO
 * @Author tanxing
 * @Date 2019/7/01 14:55
 * Version 1.0
 **/
public class ObuActivateApplyQueryService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(ObuActivateApplyQueryService.class);

    private final String serverName = "[1104OBU二次激活申请查询]";


    /**
     * 1、查询obu二次激活申请单状态
     * 2、响应客户端
     *
     * @param dataMap json数据
     * @return
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            String obuId = record.getStr("obuId");
            String cardId = record.getStr("cardId");

            if (StringUtil.isEmpty(obuId, cardId)) {
                logger.error("{}传入参数obuId, cardId不能为空", serverName);
                return Result.paramNotNullError("obuId, cardId");
            }

            //1、查询obu二次激活申请单状态
            Record applyRecord = Db.findFirst(DbUtil.getSql("queryObuApplyResult"), obuId, cardId);
            if (applyRecord == null) {
                logger.error("{}未查询到obu二次激活申请单数据,obuId:{},cardId={}", serverName, obuId, cardId);
                return Result.bizError(798, "未查询到obu二次激活申请单数据");
            }

            //2、响应客户端
            return Result.success(applyRecord);
        } catch (Exception e) {
            logger.error("{}查询二次激活申请异常:{}", serverName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "查询二次激活申请异常");
        }
    }
}
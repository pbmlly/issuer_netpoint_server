package com.csnt.ins.bizmodule.params;


import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 2201优惠参数接口
 *
 * @author duwanjiang
 */
@SuppressWarnings("Duplicates")
public class OfficeCardParamDicService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(OfficeCardParamDicService.class);

    private String serviceName = "[2201优惠参数接口]";

    private final String TABLE_TBL_OFFICECARDPARAMDIC = "tbl_officeCardParamDic";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record cardParam = new Record().setColumns(dataMap);
            logger.info("{}接收到参数:{}", serviceName, cardParam);
            String cardId = cardParam.get("cardId");
            String notNullParamNames = "posId,userId,cardId,cardNetId,useTime,expireTime,freeType,freeAreaNum," +
                    "freeArea,rebate,version,startTime";
            if (StringUtil.isEmptyArg(cardParam, notNullParamNames)) {
                logger.error("{}传入参数{}不能为空", serviceName, notNullParamNames);
                return Result.paramNotNullError(notNullParamNames);
            }


            Db.tx(() -> {
                Db.save(TABLE_TBL_OFFICECARDPARAMDIC, cardParam);
                return true;
            });

            logger.info("{}[cardId={}]接收卡参数信息成功", serviceName, cardId);
        } catch (Exception e) {
            logger.error("{}数据库入库异常:{}", serviceName, e.toString(), e);
            //主键冲突
            if (e.toString().contains("Duplicate entry")) {
                return Result.byEnum(ResponseStatusEnum.SYS_DB_PRIMARY_ERROR);
            }
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }

        return Result.success(null);
    }


}

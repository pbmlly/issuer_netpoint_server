package com.csnt.ins.bizmodule.order.obuactivatenotice;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.order.commonquery.QueryDictionaryService;
import com.csnt.ins.enumobj.ObuActivateApplyResultEnum;
import com.csnt.ins.enumobj.OrderTypeEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.enumobj.ServiceTypeEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1102检查OBU状态是否激活
 *
 * @ClassName CheckObuStatusService
 * @Description TODO
 * @Author tanxing
 * @Date 2019/7/01 14:55
 * Version 1.0
 **/
public class CheckObuStatusService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(CheckObuStatusService.class);

    private final String serverName = "[1102检查OBU状态是否激活]";

    private QueryDictionaryService queryDictionaryService = new QueryDictionaryService();

    private final String KEY_ISACTIVE = "isActive";

    private final int NON_EXISTENT_CODE = 701;
    private final String NON_EXISTENT_MSG = "非本发行方发行的OBU，不能激活";

    private final int NOT_ACTIVATED = 0;
    private final int ACTIVATED = 1;

    /**
     * 1、非本发行方发行的OBU，不能激活
     * 2、判断订单类型
     * 3、判断是否激活
     * 4、检验OBU二次激活申请是否审核通过
     * 5、判断卡签是否一致
     * 6、返回当前激活车辆信息
     *
     * @param dataMap json数据
     * @return
     */
    @Override
    public Result entry(Map dataMap) {
        Record obuRecord = new Record();
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            String obuId = record.getStr("obuId");
            String cardId = record.getStr("cardId");

            logger.info("{}接收到参数:{}", serverName, record);

            if (StringUtil.isEmpty(obuId)) {
                logger.error("{}参数obuId不能为空", serverName);
                return Result.bizError(851, "obuId不能为空");
            }
            if (StringUtil.isEmpty(cardId)) {
                logger.error("{}参数cardId不能为空", serverName);
                return Result.bizError(851, "cardId不能为空");
            }
            Kv kv = new Kv().set("id", obuId);
            SqlPara sqlPara = DbUtil.getSqlPara("findObuInfoCardInfoById", kv);


            //1、非本发行方发行的OBU，不能激活
            obuRecord = Db.findFirst(sqlPara);
            if (obuRecord == null) {
                logger.error("{}[obuId={}]非本发行方发行的OBU，不能激活", serverName, obuId);
                return Result.bizError(852, NON_EXISTENT_MSG);
            }

            logger.info("{}查询到当前OBU在发行系统中的数据情况:{}", serverName, obuRecord);

            //2、判断订单类型
            int orderType = obuRecord.getInt("orderType");
            if (orderType == OrderTypeEnum.CHANGE_CARD.getValue()) {
                logger.error("{}当前订单类型为[换卡]订单,无需进行OBU激活", serverName);
                return Result.bizError(853, "当前订单类型为[换卡]订单,无需进行OBU激活");
            } else {
                //判断订单是否已经撤单或退货
                Integer serviceType = obuRecord.getInt("serviceType");
                if (serviceType != null
                        && (serviceType == ServiceTypeEnum.CANCEL.getValue()
                        || serviceType == ServiceTypeEnum.RETURN.getValue())) {
                    logger.error("{}当前订单已撤单或退货,无法进行激活", serviceType);
                    return Result.bizError(854, "当前订单已撤单或退货,无法进行激活");
                }
            }

            //判断订单邮寄状态
            int postStatus = obuRecord.getInt("postStatus");
            if(postStatus < 2){
                logger.error("{}当前订单还未邮寄,无法激活",serverName);
                return Result.bizError(853, "当前订单还未邮寄,无法激活");
            }

            //3、判断是否激活
            Integer isActive = obuRecord.getInt(KEY_ISACTIVE);
            if (isActive != null && isActive == ACTIVATED) {
                //4、检验OBU二次激活申请是否审核通过
                Record applyRecord = Db.findFirst(DbUtil.getSql("findActivateApplyByObuId"), obuId);
                if (applyRecord == null) {
                    logger.error("{}[obuId={}]此OBU已经激活，若需二次激活，请点击二次激活按钮申请二次激活", serverName, obuId);
                    return Result.bizError(855, "此OBU已经激活，若需二次激活，请点击二次激活按钮申请二次激活");
                } else {
                    int result = applyRecord.getInt("result");
                    if (!ObuActivateApplyResultEnum.PASSED.equals(result)) {
                        logger.error("{}[obuId={}]此OBU二次激活申请审核结果为[{}]，暂不能二次激活",
                                serverName, obuId, ObuActivateApplyResultEnum.getName(result));
                        return Result.bizError(855, String.format("此OBU二次激活申请审核结果为[%s]，暂不能二次激活",
                                ObuActivateApplyResultEnum.getName(result)));
                    }
                }
            } else {
                obuRecord.set(KEY_ISACTIVE, NOT_ACTIVATED);
            }


            //5、判断卡签是否一致
            String rcCardId = obuRecord.getStr("cardId");
            if (!cardId.equals(rcCardId)) {
                logger.error("{}[obuId={},cardId={},oldCardId={}]卡签不一致", serverName, obuId, cardId, rcCardId);
                return Result.bizError(856, "卡签不一致");
            }

            //6、返回当前激活车辆信息
            String vehicleId = obuRecord.getStr("vehicleId");
            if (StringUtil.isNotEmpty(vehicleId)) {
                String[] vehicleArr = vehicleId.split("_");
                String plateNum = vehicleArr[0];
                String plateColor = queryDictionaryService.findDictionaryNameByCode("VehicleColor", vehicleArr[1]);
                obuRecord.set("plateNum", plateNum);
                obuRecord.set("plateColor", plateColor);
                obuRecord.remove("vehicleId");
            }

            //==========移除返回的多余参数=====
            obuRecord.remove("orderType", "serviceType");

        } catch (Exception e) {
            logger.error("{}查询OBU信息异常:{}", serverName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "查询OBU信息异常");
        }
        logger.info("{}OBU激活检查成功:{}", serverName, obuRecord);
        return Result.success(obuRecord);
    }

}
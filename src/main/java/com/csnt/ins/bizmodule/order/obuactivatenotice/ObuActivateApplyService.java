package com.csnt.ins.bizmodule.order.obuactivatenotice;

import com.alibaba.druid.util.Base64;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ObuActivateApplyResultEnum;
import com.csnt.ins.enumobj.OnlinePictureBusinessTypeEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 1103OBU二次激活申请
 *
 * @ClassName ObuActivateApplyService
 * @Description TODO
 * @Author tanxing
 * @Date 2019/7/01 14:55
 * Version 1.0
 **/
public class ObuActivateApplyService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(ObuActivateApplyService.class);

    private final String serverName = "[1103OBU二次激活申请]";

    private final String TABLE_ETC_OBUACTIVATE_APPLY = "etc_obuactivate_apply";
    private final String TABLE_ONLINEPICTURE = "onlinepicture";

    /**
     * 1、检查车头图片是否超过200K
     * 2、判断当前OBU是否已经存在“未审核或审核通过”的申请单
     * --1、存在：不允许申请
     * 3、判断当前cardId是否是当前obu车辆的cardId
     * 4、组装申请表数据对象
     * 5、组装车头照片表数据对象
     * 6、存储数据
     * 7、响应客户端
     *
     * @param dataMap json数据
     * @return
     */
    @Override
    public Result entry(Map dataMap) {
        boolean flag = false;
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            String obuId = record.getStr("obuId");
            String cardId = record.getStr("cardId");
            String imgHeadstock = record.getStr("imgHeadstock");

            if (StringUtil.isEmpty(obuId, cardId, imgHeadstock)) {
                logger.error("{}传入参数obuId, cardId, imgHeadstock不能为空", serverName);
                return Result.paramNotNullError("obuId, cardId, imgHeadstock");
            }

            //1、检查车头图片是否超过200K
            if (!isLess200KCheckImgSize(imgHeadstock)) {
                logger.error("{}传入车头照片的大小超过200K", serverName);
                return Result.sysError("传入车头照片的大小超过200K");
            }
            // 增加判断是否为互联网发行订单
            Record intRc = Db.findFirst(DbUtil.getSql("queryInternetIssuerObu"), obuId);
            if (intRc == null) {
                logger.error("{}未查询到该订单信息，orderId={}", serverName,obuId);
                return Result.bizError(704,"该OBU标签不是互联网发行") ;
            }


            //2、判断当前OBU是否已经存在“未审核或审核通过”的申请单
            Record countObuActivateApply = Db.findFirst(DbUtil.getSql("countObuActivateApply"), obuId);
            if (countObuActivateApply != null && countObuActivateApply.getInt("num") > 0) {
                logger.error("{}当前obu还有未处理完成的二次激活申请单,obuId:{}", serverName, obuId);
                return Result.sysError("当前obu还有未处理完成的二次激活申请单");
            }
            //3、判断当前cardId是否是当前obu车辆的cardId
            Record cardInfoRecord = Db.findFirst(DbUtil.getSql("findCardInfoByObuId"), obuId);
            if (cardInfoRecord != null) {
                if (!cardId.equals(cardInfoRecord.getStr("cardId"))) {
                    logger.error("{}传入cardId与当前OBU所属车辆的cardId不匹配,incardId={},cardId={}",
                            serverName, cardId, cardInfoRecord.getStr("cardId"));
                    return Result.sysError("传入cardId与当前OBU所属车辆的cardId不匹配");
                }
            } else {
                logger.error("{}当前OBU对应车辆没有正常的卡,obuId:{}", serverName, obuId);
                return Result.sysError("当前OBU对应车辆没有正常的卡");
            }


            //4、组装申请表数据对象
            Record applyRerocd = dataToApplyRerocd(record, cardInfoRecord);

            //5、组装车头照片表数据对象
            Record pictureRerocd = dataToApplyPictureRerocd(record, applyRerocd);
            //6、存储数据
            flag = Db.tx(() -> {
                if (!Db.save(TABLE_ETC_OBUACTIVATE_APPLY, applyRerocd)) {
                    logger.error("{}保存TABLE_ETC_OBUACTIVATE_APPLY表失败", serverName);
                    return false;
                }
                if (!Db.save(TABLE_ONLINEPICTURE, pictureRerocd)) {
                    logger.error("{}保存TABLE_ONLINEPICTURE表失败", serverName);
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}二次激活申请成功:{}", serverName, record);
            } else {
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "二次激活申请失败:数据入库失败");
            }
        } catch (Exception e) {
            logger.error("{}二次激活申请异常:{}", serverName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.BIZ_ISSUER_ERROR, "二次激活申请异常");
        }

        return Result.success(null);
    }

    /**
     * 检查图片的大小是否小于200K
     *
     * @param imgBase64Str
     * @return
     */
    private boolean isLess200KCheckImgSize(String imgBase64Str) {
        Integer size = StringUtil.getBase64ImgSize(imgBase64Str);
        logger.info("{}当前传入图片大小为[{}]字节", serverName, size);
        if (size / 1024 > SysConfig.getPictureSizeLimit()) {
            return false;
        }
        return true;
    }

    /**
     * 组装申请单的record对象
     *
     * @param record
     * @param cardInfoRecord
     * @return
     */
    private Record dataToApplyRerocd(Record record, Record cardInfoRecord) {
        Record applyRecord = new Record();
        String id = StringUtil.getUUID();
        applyRecord.set("id", id);
        applyRecord.set("cardId", record.getStr("cardId"));
        applyRecord.set("obuId", record.getStr("obuId"));
        applyRecord.set("vehicleId", cardInfoRecord.getStr("vehicleId"));
        applyRecord.set("userId", cardInfoRecord.getStr("userId"));
        applyRecord.set("bankPost", cardInfoRecord.getStr("bankPost"));
        applyRecord.set("oldActiveTime", cardInfoRecord.get("activeTime"));
        applyRecord.set("oldActiveType", cardInfoRecord.get("activeType"));
        applyRecord.set("oldActiveChannel", cardInfoRecord.get("activeChannel"));
        applyRecord.set("result", ObuActivateApplyResultEnum.UNCHECK.getValue());
        applyRecord.set("imgHeadstock", id);
        applyRecord.set("createTime", new Date());
        applyRecord.set("updateTime", new Date());

        return applyRecord;
    }

    /**
     * 组装申请单的record对象
     *
     * @param record
     * @param applyRecord
     * @return
     */
    private Record dataToApplyPictureRerocd(Record record, Record applyRecord) {
        Record pictureRecord = new Record();
        String vehicleId = applyRecord.getStr("vehicleId");
        String carNumber = vehicleId.split("_")[0];
        String calcolor = vehicleId.split("_")[1];
        String id = StringUtil.getUUID();
        pictureRecord.set("id", id);
        pictureRecord.set("userId", applyRecord.getStr("id"));
        pictureRecord.set("bankCode", applyRecord.getStr("bankPost"));
        pictureRecord.set("businessType", OnlinePictureBusinessTypeEnum.OBU_ACITVATE.getValue());
        pictureRecord.set("imgHeadstock", Base64.base64ToByteArray(record.getStr("imgHeadstock")));
        pictureRecord.set("CarNumber", carNumber);
        pictureRecord.set("Calcolor", calcolor);
        pictureRecord.set("CreateTime", new Date());

        return pictureRecord;
    }
}
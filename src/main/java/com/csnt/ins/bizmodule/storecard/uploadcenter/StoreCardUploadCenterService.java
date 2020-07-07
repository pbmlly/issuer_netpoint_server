package com.csnt.ins.bizmodule.storecard.uploadcenter;

import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.enumobj.OperationEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 8712 卡营改增信息上传
 *
 * @author source
 **/
public class StoreCardUploadCenterService implements IReceiveService,BaseUploadService {
    private Logger logger = LoggerFactory.getLogger(StoreCardUploadCenterService.class);

    private final String serverName = "[8712储蓄卡信息上传部中心]";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

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
            String cardId = record.getStr("cardId");

            if (StringUtil.isEmpty(cardId)) {
                Result.bizError(766,"卡信息必须输入");
            }
            // 读取卡信息
            Record cardInfo = Db.findFirst(DbUtil.getSql("queryEtcCardInfoById"), cardId);
            if (cardInfo == null) {
                logger.error("{}发行系统未找到当前卡:{}", serverName, cardId);
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR, "发行系统未找到当前卡信息");
            }
            //7、营改增平台上传卡信息上传及变更
            BaseUploadResponse response = uploadBasicCardInfo(cardInfo);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传卡营改增信息失败:{}", serverName, response);
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }


            return Result.success(null, "卡信息上传成功");
        } catch (Throwable t) {
            logger.error("{}卡信息上传营改增失败失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 上传卡信息到部中心
     *
     * @param cardInfo
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record cardInfo) {
        cardInfo.set("operation", OperationEnum.ADD.getValue());

        EtcCardinfoJson etcCardinfoJson = new EtcCardinfoJson();
        etcCardinfoJson._setOrPut(cardInfo.getColumns());
        // 时间需要转换为字符串
        etcCardinfoJson.setEnableTime(DateUtil.formatDate(cardInfo.get("enableTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setExpireTime(DateUtil.formatDate(cardInfo.get("expireTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setIssuedTime(DateUtil.formatDate(cardInfo.get("issuedTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));
        etcCardinfoJson.setStatusChangeTime(DateUtil.formatDate(cardInfo.get("statusChangeTime"), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS));

        logger.info("{}上传卡的内容为:{}", serverName, etcCardinfoJson);
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传卡响应信息:{}", serverName, response);
        return response;
    }
}

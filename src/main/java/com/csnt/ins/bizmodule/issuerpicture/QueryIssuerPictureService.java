package com.csnt.ins.bizmodule.issuerpicture;


import com.alibaba.druid.util.Base64;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseDownService;
import com.csnt.ins.bizmodule.base.BaseDwResponse;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.json.Jackson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 恒通邮寄信息接收接口
 **/
public class QueryIssuerPictureService  implements IReceiveService, BaseDownService {
    Logger logger = LoggerFactory.getLogger(QueryIssuerPictureService.class);

    private String serviceName = "[8941查询发行图片]";


    private final String TABLE_ONLINEPICTURE = "onlinepicture";

    @Override
    public Result entry(Map dataMap) {
        try {
            Map outMap = new HashMap<>();
            outMap.put("plateNum",null);
            outMap.put("plateColor",null);
            outMap.put("imgPositive",null);
            outMap.put("imgBack",null);
            outMap.put("imgHome",null);
            outMap.put("imgInfo",null);
            outMap.put("imgHeadstock",null);
            outMap.put("imgHold",null);


            Record inMap = new Record().setColumns(dataMap);
            // 车牌号码
            String plateNum = inMap.get("plateNum");
            // 车牌颜色
            Integer plateColor = inMap.get("plateColor");
            if (StringUtil.isEmpty(plateNum, plateColor)) {
                logger.error("{}参数plateNum, plateColor不能为空", serviceName);
                return Result.paramNotNullError("plateNum, plateColor");
            }
            outMap.put("plateNum",inMap.getStr("plateNum"));
            outMap.put("plateColor",inMap.getInt("plateColor"));

            // 查询线下图片表
            Record picRc = Db.findFirst(DbUtil.getSql("queryIssuerPicture"),
                     inMap.getStr("plateNum"),inMap.getInt("plateColor"));

            if (picRc != null) {
                outMap.put("plateNum",picRc.getStr("CarNumber"));
                outMap.put("plateColor",picRc.getInt("Calcolor"));
                outMap.put("imgPositive",picRc.get("ImgPositive")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("ImgPositive"))));
                outMap.put("imgBack",picRc.get("imgBack")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("imgBack"))));
                outMap.put("imgHome",picRc.get("imgHome")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("imgHome"))));
                outMap.put("imgInfo",picRc.get("imgInfo")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("imgInfo"))));
                outMap.put("imgHeadstock",picRc.get("imgHeadstock")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("imgHeadstock"))));
                outMap.put("imgHold",picRc.get("imgHold")==null?null:new String(org.apache.commons.codec.binary.Base64.encodeBase64(picRc.get("imgHold"))));
            } else {
                // 判断是否为总对总订单
                String vehicleCode = inMap.getStr("plateNum") + "_" + inMap.getInt("plateColor");
                Record onRC = Db.findFirst(DbUtil.getSql("queryCenterOrderIssuer"), vehicleCode);
                if (onRC != null) {
                    // 车辆编号
                    String  vehicleId = onRC.getStr("vehicleId");
                    // 账号编号
                    String  accountId = onRC.getStr("accountId");

                    // 账户证件信息
//                    Result result = null;
                    Result result = getAccoutImg(accountId);
                    if (result.getSuccess()) {
                        Map  map = (Map) result.getData();
                        outMap.put("imgPositive",map.get("positiveImageStr"));
                        outMap.put("imgBack",map.get("negativeImageStr"));
                    }
                    // 行驶证信息
                    result = getVehicleImg(accountId,vehicleId);
                    if (result.getSuccess()) {
                        Map  map = (Map) result.getData();
                        outMap.put("ImgHome",map.get("positiveImageStr"));
                        outMap.put("ImgInfo",map.get("negativeImageStr"));
                    }

                    // 车主身份证图像
                    result = getOwnerImg(accountId,vehicleId);
                    if (result.getSuccess()) {
                        Map  map = (Map) result.getData();
                        outMap.put("ImgHeadstock",map.get("positiveImageStr"));
                        outMap.put("ImgHold",map.get("negativeImageStr"));
                    }
                }

            }

            return Result.success(outMap);
        } catch (ClassCastException c) {
            logger.error("{}参数类型异常:{}", serviceName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Exception e) {
            logger.error("{}查询图片信息异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    /**
     *   取客户证件信息
     * @param accountId
     * @return
     */
    private Result getAccoutImg(String accountId) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String time = dateFormat.format(date);
        String fileName =  "CERTIFY_ACCOUNTIMG_REQ_" + CommonAttribute.ISSUER_ISS_SENDER + "_" + time + ".json";
        Map sedMsg = new HashMap<>();
        sedMsg.put("accountId",accountId);
        String json = Jackson.getJson().toJson(sedMsg);
        Result response = upload(json, fileName);

        return response;
    }
    /**
     *   取行驶证信息信息
     * @param accountId
     * @return
     */
    private Result getVehicleImg(String accountId,String vehicleId) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String time = dateFormat.format(date);
        String fileName =  "CERTIFY_VEHICLEIMG_REQ_" + CommonAttribute.ISSUER_ISS_SENDER + "_" + time + ".json";
        Map sedMsg = new HashMap<>();
        sedMsg.put("accountId",accountId);
        sedMsg.put("vehicleId",vehicleId);

        String json = Jackson.getJson().toJson(sedMsg);
        Result response = upload(json, fileName);

        return response;
    }

    /**
     *   车主身份证图像
     * @param accountId
     * @return
     */
    private Result getOwnerImg(String accountId,String vehicleId) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String time = dateFormat.format(date);
        String fileName =  "CERTIFY_OWNERIMG_REQ_" + CommonAttribute.ISSUER_ISS_SENDER + "_" + time + ".json";
        Map sedMsg = new HashMap<>();
        sedMsg.put("accountId",accountId);
        sedMsg.put("vehicleId",vehicleId);

        String json = Jackson.getJson().toJson(sedMsg);
        Result response = upload(json, fileName);

        return response;
    }
}

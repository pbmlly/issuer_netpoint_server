package com.csnt.ins.bizmodule.issuerpicture;


import com.alibaba.druid.util.Base64;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ExpressStatusEnum;
import com.csnt.ins.enumobj.OrderStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 恒通邮寄信息接收接口
 **/
public class IssuerPictureReceiveService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(IssuerPictureReceiveService.class);

    private String serviceName = "[8940发行图片接收]";


    private final String TABLE_ONLINEPICTURE = "onlinepicture";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record inMap = new Record().setColumns(dataMap);

            Map inCheckMap = checkInput(inMap);
            if (!(boolean) inCheckMap.get("bool")) {
                return (Result) inCheckMap.get("result");
            }

            Record picRc = onlinePicture(inMap);
            // 判断 车辆编号，用户编号是否存在图片记录，如果存在删除该记录
            Record picDt = Db.findFirst(DbUtil.getSql("queryPictureExist"),
                       inMap.getStr("userId"),inMap.getStr("plateNum"),inMap.getInt("plateColor"));
            boolean flag = Db.tx(() -> {

                if (!Db.save(TABLE_ONLINEPICTURE, "id", picRc)) {
                    logger.error("{}保存TABLE_ONLINEPICTURE表失败", serviceName);
                    return false;
                };
                if (picDt != null) {
                    if (!Db.delete(TABLE_ONLINEPICTURE, "id", picDt)) {
                        logger.error("{}删除TABLE_ONLINEPICTURE表失败", serviceName);
                        return false;
                    };
                }
                return true;
            });

            if (flag) {
                logger.info("{}发行图片信息接收成功", serviceName);
                return Result.success(null);
            } else {
                logger.error("{}数据库入库失败", serviceName);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }


        } catch (ClassCastException c) {
            logger.error("{}参数类型异常:{}", serviceName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Exception e) {
            logger.error("{}接收图片信息异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
     }

    private Map checkInput(Record inMap) {
        //打印输入日志
        Map logMap = new HashMap<>();
        logMap.put("userId", inMap.get("userId"));
        logMap.put("businessType", inMap.get("businessType"));
        logMap.put("plateNum", inMap.get("plateNum"));
        logMap.put("plateColor", inMap.get("plateColor"));

        logger.info("{}[msgtype=8940]当前请求参数为:{}", serviceName, logMap);

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);


        // 客户编号
        String userId = inMap.get("userId");
        // 业务类型
        String businessType = inMap.get("businessType");
        // 车牌号码
        String plateNum = inMap.get("plateNum");
        // 车牌颜色
        Integer plateColor = inMap.get("plateColor");
        if (StringUtil.isEmpty(userId, businessType, plateNum, plateColor)) {
            logger.error("{}参数userId, businessType, plateNum, plateColor不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError("userId, businessType, plateNum, plateColor"));
            outMap.put("bool", false);
            return outMap;
        }

        //身份证正面照
        String imgPositive = inMap.get("imgPositive");
        //身份证反面照
        String imgBack = inMap.get("imgBack");
        //行驶证首页照
        String imgHome = inMap.get("imgHome");
        //行驶证信息照
        String imgInfo = inMap.get("imgInfo");
        //车头照
        String imgHeadstock = inMap.get("imgHeadstock");
        //手持身份证照
        String imgHold = inMap.get("imgHold");

        if (StringUtil.isEmpty(imgPositive, imgBack, imgHome, imgInfo, imgHeadstock)) {
            logger.error("{}参数imgPositive, imgBack,imgHome,imgInfo,imgHeadstock不能为空", serviceName);
            outMap.put("result", Result.paramNotNullError(" imgPositive, imgBack,imgHome,imgInfo,imgHeadstock"));
            outMap.put("bool", false);
            return outMap;
        }

        if (!isLess200KCheckImgSize(imgPositive)) {
            logger.error("{}身份证正面照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "身份证正面照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgBack)) {
            logger.error("{}身份证反面照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "身份证反面照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgHome)) {
            logger.error("{}行驶证首页照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "行驶证首页照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgInfo)) {
            logger.error("{}行驶证信息照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "行驶证信息照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (!isLess200KCheckImgSize(imgHeadstock)) {
            logger.error("{}车头照文件过大", serviceName);
            outMap.put("result", Result.bizError(704, "车头照文件过大"));
            outMap.put("bool", false);
            return outMap;
        }
        if (StringUtil.isNotEmpty(imgHold)) {
            if (!isLess200KCheckImgSize(imgHold)) {
                logger.error("{}手持身份证照文件过大，", serviceName);
                outMap.put("result", Result.bizError(704, "手持身份证照文件过大"));
                outMap.put("bool", false);
                return outMap;
            }
        }
        return outMap;
    }

    /**
     * 检查图片的大小是否小于200K
     *
     * @param imgBase64Str
     * @return
     */
    private boolean isLess200KCheckImgSize(String imgBase64Str) {
        Integer size = StringUtil.getBase64ImgSize(imgBase64Str);
        logger.info("{}当前传入图片大小为[{}]字节", serviceName, size);
        if (size / 1024 > SysConfig.getPictureSizeLimit()) {
            return false;
        }
        return true;
    }

    /**
     * 保存在线订单图片,并保存订单的操作用户
     *
     * @param onlineOrder
     */
    private Record onlinePicture(Record onlineOrder) {
        Record onlinePicture = new Record();
        onlinePicture.set("id", StringUtil.getUUID());
        onlinePicture.set("userId", onlineOrder.getStr("userId"));
        onlinePicture.set("bankCode", null);
        onlinePicture.set("businessType", onlineOrder.getStr("businessType"));
        onlinePicture.set("carNumber", onlineOrder.getStr("plateNum"));
        onlinePicture.set("calColor", onlineOrder.getStr("plateColor"));
        //将base64转为二进制流
        //身份证正面照
        byte[] imgPositive = Base64.base64ToByteArray(onlineOrder.get("imgPositive"));
        onlinePicture.set("imgPositive", imgPositive);

        // 身份证反面照
        byte[] imgBack = Base64.base64ToByteArray(onlineOrder.get("imgBack"));
        onlinePicture.set("imgBack", imgBack);

        // 行驶证首页照
        byte[] imgHome = Base64.base64ToByteArray(onlineOrder.get("imgHome"));
        onlinePicture.set("imgHome", imgHome);

        //行驶证信息照
        byte[] imgInfo = Base64.base64ToByteArray(onlineOrder.get("imgInfo"));
        onlinePicture.set("imgInfo", imgInfo);

        //车头照
        byte[] imgHeadstock = Base64.base64ToByteArray(onlineOrder.get("imgHeadstock"));
        onlinePicture.set("imgHeadstock", imgHeadstock);

        //手持身份证照
        if (StringUtil.isNotEmpty(onlineOrder.getStr("imgHold"))) {
            byte[] imgHold = Base64.base64ToByteArray(onlineOrder.get("imgHold"));
            onlinePicture.set("imgHold", imgHold);
        } else {
            onlinePicture.set("imgHold", null);
        }


        onlinePicture.set("createTime", new Date());

        return onlinePicture;
    }
}

package com.csnt.ins.bizmodule.order.postsale;


import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserSignoflUploadService;
import com.csnt.ins.enumobj.BankIdEnum;
import com.csnt.ins.enumobj.OrderStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.issuer.OnlineOrders;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单撤销
 **/
public class CancelOrderReceiveService implements IReceiveService, BaseUploadService {
    Logger logger = LoggerFactory.getLogger(CancelOrderReceiveService.class);

    private String serviceName = "[8934订单撤销]";

    private final String REPEATSING_MSG = "车辆无绑定的签约渠道";


    /**
     * 4.1  车辆支付渠道绑定/ 解绑通知
     */
    UserSignoflUploadService userSignoflUploadService = new UserSignoflUploadService();
    /**
     * 获取上传对象
     */
    public IUpload upload = CsntUpload.getInstance();

    @Override
    public Result entry(Map dataMap) {
        try {
            Record appRc = new Record().setColumns(dataMap);
            // 接收信息检查
            String orderId = appRc.getStr("orderId");
            // 车辆营改增编号
            String vehicleCode = appRc.getStr("vehicleCode");

            if (StringUtil.isEmpty(orderId, vehicleCode)) {
                logger.error("{}参数orderId, vehicleCode不能为空", serviceName);
                return Result.paramNotNullError("orderId, vehicleCode");
            }

            // 判断该申请单是否存在（onlineorders表是否存在）
            OnlineOrders onlineOrders = OnlineOrders.dao.findById(orderId);
            if (onlineOrders == null) {
                logger.error("{}未找到该订单信息，订单编号：{}", serviceName, orderId);
                return Result.bizError(621, "未找到该订单信息");
            }

            if (!vehicleCode.equals(onlineOrders.getVehicleCode())) {
                logger.error("{}上送车辆信息不一致：上送：{},订单：{}", serviceName, vehicleCode, onlineOrders.getVehicleCode());
                return Result.bizError(622, "上送车辆信息不一致");
            }

            if (onlineOrders.getOrderStatus() ==4) {
                logger.error("{}该订单已经撤销订单：{}", serviceName,onlineOrders.getOrderId());
                return Result.bizError(627, "该订单已经撤销订单");
            }

            if (onlineOrders.getObuId() != null) {
                Record obuRc = Db.findFirst(DbUtil.getSql("queryObuInfoByObuId"), onlineOrders.getObuId().toString());
                if (obuRc != null ) {
                    int status = obuRc.getInt("status");
                    if (status != 4 && status != 5 ) {
                        logger.error("{}该订单已经发行了OBU不能撤单：OBU：{}", serviceName, onlineOrders.getObuId().toString());
                        return Result.bizError(624, "该订单已经发行了OBU不能撤单");
                    }
                }
            }
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleCode);

            if ( onlineOrders.getCardId() != null && etcOflVehicleinfo.getBankPost() !=null) {
                logger.error("{}该订单已经发行了ETC卡不能撤单：ETC卡：{}", serviceName, onlineOrders.getCardId().toString());
                return Result.bizError(623, "该订单已经发行了ETC卡不能撤单");
            }

            // 线下渠道解除绑定
            Integer userIdType = onlineOrders.getUserIdType();
            String userIdNum = onlineOrders.getUserIdNum();
            if (onlineOrders.getOrderType() == 1) {
                Map  oflMap = callOflInterface(userIdType,userIdNum,vehicleCode,onlineOrders,etcOflVehicleinfo);
                boolean obuBl = (boolean) oflMap.get("bool");
                if (!obuBl) {
                    return (Result) oflMap.get("result");
                }
            }


            boolean flag = Db.tx(() -> {

                //更新订单信息表
                onlineOrders.setOrderStatus(OrderStatusEnum.CANCELED.getValue());
                onlineOrders.setUpdateTime(new Date());
                if (!onlineOrders.update()) {
                    logger.error("{}保存onlineOrders表失败", serviceName);
                    return false;
                }

                if (onlineOrders.getOrderType() == 1 &&
                        etcOflVehicleinfo != null && onlineOrders.getChannelType().equals(etcOflVehicleinfo.getChannelType())) {
                    etcOflVehicleinfo.setBankPost(null);
                    if (!etcOflVehicleinfo.update()) {
                        logger.error("{}保存etcOflVehicleinfo表失败", serviceName);
                        return false;
                    }
                }

                return true;
            });

            if (flag) {
                logger.info("{}订单撤销成功，订单编号{}", serviceName,onlineOrders.getOrderId());
                return Result.success(null, "订单撤销成功");
            } else {
                logger.error("{}客服申请订单失败,入库失败,orderid：{}", serviceName,onlineOrders.getOrderId());
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "订单撤销失败");
            }

        } catch (ClassCastException c) {
            logger.error("{}订单撤销异常:{}", serviceName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Throwable e) {
            logger.error("{}订单撤销接收异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }

    private Map callOflInterface(Integer userIdType, String userIdNum, String vehicleId, OnlineOrders onlineOrders,EtcOflVehicleinfo etcOflVehicleinfo) {
        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);
        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findFirstByUserIdNumAndType(userIdNum, userIdType);
//        EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
        if (etcOflUserinfo != null
                && etcOflVehicleinfo != null
                && etcOflVehicleinfo.getDepVehicleId() != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", true);
                outMap.put("result", result);
                return outMap;
            }

            String vehiclePlate = vehicleId.split("_")[0];
            Integer vehicleColor = MathUtil.asInteger(vehicleId.split("_")[1]);

            //4.1  车辆支付渠道绑定/ 解绑通知
            if (etcOflVehicleinfo.getDepVehicleId() != null) {
                result = userSignoflUploadService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                        .set("plateNum", vehiclePlate)
                        .set("plateColor", vehicleColor)
                        //签约方式 1-线上 2-线下
                        .set("signType", 1)
                        .set("issueChannelId", onlineOrders.getChannelId())
//                        .set("channelType", "001")
                        .set("channelType", getChannelType(onlineOrders.getBankCode()))
                        //绑定的卡类型 1-信用卡 2-借记卡
                        .set("cardType", 3)
                        .set("account", null)
                        .set("enableTime", DateUtil.formatDate(onlineOrders.getCreateTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                        .set("closeTime", getCloseTime(onlineOrders.getCreateTime()))
                        //绑定状态 1:绑定 2:解绑
                        .set("status", 2));
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()
                        && !result.getMsg().contains(REPEATSING_MSG)) {
                    logger.error("{}解绑/绑定银行卡失败:{}", serviceName, result);
                    outMap.put("bool", false);
                    outMap.put("result", result);
                    return outMap;
                }
            }
        }

        return outMap;
    }

    /**
     * 更加开启时间获取关闭时间
     *
     * @param genTime
     * @return
     */
    private String getCloseTime(Date genTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(genTime);
        calendar.add(Calendar.YEAR, 10);
        return DateUtil.formatDate(calendar.getTime(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS);
    }

    /**
     * 获取对应的ChannelType
     *
     * @param bankPost
     * @return
     */
    private String getChannelType(String bankPost) {

        String channelType = "103";
        Record rc = Db.findFirst(DbUtil.getSql("queryTblAgency"), bankPost);

        if (rc != null) {
            channelType = rc.getStr("SIGNCHANNEL");
        }
        return channelType;
    }
}

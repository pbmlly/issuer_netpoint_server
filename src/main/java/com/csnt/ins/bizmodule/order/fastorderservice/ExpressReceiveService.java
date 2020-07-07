package com.csnt.ins.bizmodule.order.fastorderservice;


import com.alibaba.fastjson.JSONObject;
import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ExpressStatusEnum;
import com.csnt.ins.enumobj.OrderProcessStatusEnum;
import com.csnt.ins.enumobj.OrderStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.OnlineOrders;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮寄信息接收接口8903
 **/
public class ExpressReceiveService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(ExpressReceiveService.class);

    private String serviceName = "[8903邮寄信息接收]";

    public IUpload upload = CsntUpload.getInstance();

    private final String ISSORDER_SEND_REQ = "ISSORDER_SEND_REQ_";

    private final String TABLE_ONLINE_ORDERS = "online_orders";

    /**
     * 邮寄信息重复上传的msg
     */
    private final String UPLOAD_REPEAT_MSG = "订单不是待发货状态";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record express = new Record().setColumns(dataMap);
            String orderId = express.get("orderId");
            int delivery = express.getInt("delivery");
            int expressType = express.getInt("expressType");
            String expressId = express.get("expressId");
            if (StringUtil.isEmpty(orderId)) {
                logger.error("{}订单编号不能为空", serviceName);
                return Result.sysError("订单编号不能为空");
            }
            if (StringUtil.isEmpty(delivery)) {
                logger.error("{}邮寄类型不能为空", serviceName);
                return Result.sysError("邮寄类型不能为空");
            }
            if (StringUtil.isEmpty(expressType)) {
                logger.error("{}快递渠道不能为空", serviceName);
                return Result.sysError("快递渠道不能为空");
            }
            if (StringUtil.isEmpty(expressId)) {
                logger.error("{}快递编号不能为空", serviceName);
                return Result.sysError("快递编号不能为空");
            }

            Record order = Db.findFirst(DbUtil.getSql("queryOnlineOrderExpress"), orderId);

            if (order == null) {
                logger.error("{}[orderId={}]未找到订单信息", serviceName, orderId);
                return Result.sysError("未找到订单信息");
            }

            if (OrderProcessStatusEnum.PROCESSED.getValue() != MathUtil.asInteger(order.get("status"))) {
                logger.error("{}[orderId={}]订单还未处理完成,不能填写邮寄信息[status={}]",
                        serviceName, orderId, order.get("status"));
                return Result.sysError("订单还未处理完成,不能填写邮寄信息");
            }

            if (ExpressStatusEnum.EXPRESSED.getValue() <= MathUtil.asInteger(order.get("postStatus"))) {
                logger.error("{}[orderId={}]当前订单已填写邮寄信息[poststatus={}]",
                        serviceName, orderId, order.get("postStatus"));
                return Result.sysError("当前订单已填写邮寄信息");
            }
            if (OrderStatusEnum.CANCELED.getValue() <= MathUtil.asInteger(order.get("orderStatus") == null ? 0 : order.get("orderStatus"))) {
                logger.error("{}[orderId={}]当前订单已撤单或退货,无需填写邮寄信息[orderStatus={}]",
                        serviceName, orderId, order.get("orderStatus"));
                return Result.sysError("当前订单已撤单或退货,无需填写邮寄信息");
            }

            // 省内订单不上传邮寄信息
            String channelType = order.getStr("channelType");
            if (!CommonAttribute.ISSUER_CHANNEL_PRE_TOTAL_BANK.equals(channelType)) {
                logger.error("{}[orderId={}]当前订单非总行发行订单,无需填写订单信息[channelTypePre={}]",
                        serviceName, orderId, channelType);
                return Result.sysError("当前订单非总行发行订单,无需填写订单信息");
            }

            //上传3.2OBU发货信息
            BaseUploadResponse response = uploadOrderSend(order, delivery, expressType, expressId);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()
                    && !isRepeatUpload(response)) {
                logger.error("{}[orderId={}]邮寄信息上传部中心失败:{}", serviceName, orderId, response);
                return Result.sysError(response.getErrorMsg());
            }

            Record onlineOrders = new Record();
            onlineOrders.set("orderId", orderId);
            //设置已发货
            onlineOrders.set("postStatus", ExpressStatusEnum.EXPRESSED.getValue());
            //发货类型
            onlineOrders.set("delivery", delivery);
            //快递渠道
            onlineOrders.set("expressType", expressType);
            //快递单号
            onlineOrders.set("expressId", expressId);
            //是否已发送邮寄
            onlineOrders.set("syncPostStatus", 1);
            onlineOrders.set("syncPostTime", new Date());


            Db.update(TABLE_ONLINE_ORDERS, "orderId", onlineOrders);
        } catch (ClassCastException c) {
            logger.error("{}参数类型异常:{}", serviceName, c.toString(), c);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "参数类型异常");
        } catch (Exception e) {
            logger.error("{}更新订单邮寄信息异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
        return Result.success(null);
    }

    /**
     * 是否重复上传邮寄信息
     *
     * @param baseUploadResponse
     * @return
     */
    private boolean isRepeatUpload(BaseUploadResponse baseUploadResponse) {
        if (baseUploadResponse.getErrorMsg().contains(UPLOAD_REPEAT_MSG)) {
            return true;
        }
        return false;
    }

    /**
     * 上传订单邮寄信息到部中心
     * 3.2 OBU  发货 （修订）
     *
     * @param order
     * @param delivery
     * @param expressType
     * @param expressId
     * @return
     */
    private BaseUploadResponse uploadOrderSend(Record order, int delivery, int expressType, String expressId) {
        Map sedMsg = new HashMap<>();
        sedMsg.put("orderId", order.get("orderid"));
        sedMsg.put("orderType", order.get("ordertype"));
        sedMsg.put("accountId", order.get("accountid"));
        sedMsg.put("delivery", delivery);
        sedMsg.put("expressType", expressType);
        sedMsg.put("expressId", expressId);
        sedMsg.put("obuId", order.get("obuid"));
        sedMsg.put("brand", order.get("obubrand"));
        sedMsg.put("obuModel", order.get("obumodel"));
        sedMsg.put("obuSign", order.get("obusign"));
        sedMsg.put("cardId", order.get("cardid"));
        sedMsg.put("cardType", order.get("cardtype"));
        sedMsg.put("cardBrand", order.get("brand"));
        sedMsg.put("cardModel", order.get("model"));

        String json = JSONObject.toJSON(sedMsg).toString();
        BaseUploadRequest request = new BaseUploadRequest(json.getBytes(Charset.forName(StringUtil.UTF8_STR)), getRequestJsonName());
        BaseUploadResponse response = upload.uploadRequestSender(request, SysConfig.getRequestTimeout());
        logger.info("{}请求[3.2 OBU发货接口]响应信息为:{}", serviceName, response);
        return response;
    }

    /**
     * 获取请求文件名
     *
     * @return
     */
    public String getRequestJsonName() {
        return ISSORDER_SEND_REQ + CommonAttribute.ISSUER_ISS_SENDER + "_" + DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + ".json";
    }

}

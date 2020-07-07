package com.csnt.ins.bizmodule.order.handset;


import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ExpressStatusEnum;
import com.csnt.ins.enumobj.OrderProcessStatusEnum;
import com.csnt.ins.enumobj.OrderStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 8907订单确认接口
 *
 * @author source
 */
public class OrderConfirmService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(OrderConfirmService.class);

    private String serviceName = "[8907订单确认接口]";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record orderInfo = new Record().setColumns(dataMap);
            String orderId = orderInfo.getStr("orderId");
            String posId = orderInfo.get("posId");
            String userId = orderInfo.get("userId");
            Integer type = orderInfo.get("type");
            if (StringUtil.isEmpty(orderId, posId, userId, type)) {
                logger.error("{}传入参数orderId,posId,userId,type不能为空", serviceName);
                return Result.paramNotNullError("orderId,posId,userId,type");
            }
            logger.info("{}接收订单确认接口:{}", serviceName, orderInfo);

            Record orderRecord = Db.findFirst(DbUtil.getSql("queryOrderStatus"), orderId);
            //判断订单是否存在
            if (orderRecord == null) {
                logger.error("{}订单不存在:orderId={}", serviceName, orderId);
                return Result.sysError("确认订单不存在");
            }

            //判断订单是否已处理完成
            int status = orderRecord.getInt("status");
            if (status == OrderProcessStatusEnum.PROCESSED.getValue()) {
                logger.error("{}订单已处理完成:orderId={}", serviceName, orderId);
                return Result.byEnum(ResponseStatusEnum.BIZ_ORDER_PROCESSED);
            }

            //判断订单是否在处理中
            String operatorId = orderRecord.getStr("operatorId");
            String myPosId = orderRecord.getStr("posId");
            if (status == OrderProcessStatusEnum.PROCESSING.getValue()) {
                if (userId.equals(operatorId) && posId.equals(myPosId)) {
                    logger.error("{}当前订单已确认:orderId={}", serviceName, orderId);
                    return Result.byEnum(ResponseStatusEnum.SUCCESS, "当前订单已确认");
                } else {
                    logger.error("{}订单确认失败,当前订单已由[posId={},operatorId={}]确认:orderId={}", serviceName, myPosId, operatorId, orderId);
                    return Result.sysError(String.format("订单确认失败,当前订单已由[posId=%s,operatorId=%s]确认", myPosId, operatorId));
                }
            }

            //判断订单的状态
            int orderStatus = orderRecord.get("orderStatus")==null?0:orderRecord.getInt("orderStatus");
            if (orderStatus >= OrderStatusEnum.CANCELED.getValue()) {
                logger.error("{}[orderId={},orderStatus={}]当前订单已撤单或退货,不能进行发行", serviceName, orderId, orderStatus);
                return Result.sysError("当前订单已撤单或退货,不能进行发行");
            }

            //判断订单的邮寄状态
            int postStatus = orderRecord.getInt("postStatus");
            if (postStatus >= ExpressStatusEnum.EXPRESSED.getValue()) {
                logger.error("{}[orderId={},postStatus={}]当前订单已发货或激活,不能进行发行", serviceName, orderId, postStatus);
                return Result.sysError("当前订单已发货或激活,不能进行发行");
            }

            //更新订单确认状态
            int updateNum = Db.update(DbUtil.getSql("updateOrderStatus"),
                    posId, userId, new Date(), orderId);
            if (updateNum == 0) {
                logger.error("{}确认订单失败，当前订单也被其他人确认");
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "确认订单失败，当前订单也被其他人确认");
            }
            logger.info("{}[orderId={}]订单确认成功", serviceName, orderId);
            return Result.success(null);
        } catch (Throwable t) {
            logger.error("{}订单确认异常:{}", serviceName, t.toString(), t);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
    }


}

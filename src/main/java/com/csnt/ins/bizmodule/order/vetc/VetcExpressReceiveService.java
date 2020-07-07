package com.csnt.ins.bizmodule.order.vetc;


import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ExpressStatusEnum;
import com.csnt.ins.enumobj.OrderStatusEnum;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 恒通邮寄信息接收接口
 **/
public class VetcExpressReceiveService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(VetcExpressReceiveService.class);

    private String serviceName = "[8931邮寄信息接收]";


    private final String TABLE_ONLINE_ORDERS = "online_orders";

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
                return Result.bizError(871,"订单编号不能为空");
            }
            if (StringUtil.isEmpty(delivery)) {
                logger.error("{}邮寄类型不能为空", serviceName);
                return Result.bizError(871,"邮寄类型不能为空");
            }
            if (StringUtil.isEmpty(expressType)) {
                logger.error("{}快递渠道不能为空", serviceName);
                return Result.bizError(871,"快递渠道不能为空");
            }
            if (StringUtil.isEmpty(expressId)) {
                logger.error("{}快递编号不能为空", serviceName);
                return Result.bizError(871,"快递编号不能为空");
            }

            Record order = Db.findFirst(DbUtil.getSql("queryOnlineOrderExpress"), orderId);

            if (order == null) {
                logger.error("{}[orderId={}]未找到订单信息", serviceName, orderId);
                return Result.bizError(872,"未找到订单信息");
            }

            if (ExpressStatusEnum.EXPRESSED.getValue() <= MathUtil.asInteger(order.get("postStatus"))) {
                logger.error("{}[orderId={}]当前订单已填写邮寄信息[poststatus={}]",
                        serviceName, orderId, order.get("postStatus"));
                return Result.bizError(873,"当前订单已填写邮寄信息");
            }
            if (OrderStatusEnum.CANCELED.getValue() <= MathUtil.asInteger(order.get("orderStatus") == null ? 0 : order.get("orderStatus"))) {
                logger.error("{}[orderId={}]当前订单已撤单或退货,无需填写邮寄信息[orderStatus={}]",
                        serviceName, orderId, order.get("orderStatus"));
                return Result.bizError(874,"当前订单已撤单或退货,无需填写邮寄信息");
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


}

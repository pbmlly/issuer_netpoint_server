package com.csnt.ins.bizmodule.order.littleprogram;


import com.alibaba.druid.util.Base64;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.OrderProcessStatusEnum;
import com.csnt.ins.enumobj.OrderTypeEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 8911微信小程序二发订单查询接口
 *
 * @author source
 */
public class OrderQueryByCardIdAndObuIdService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(OrderQueryByCardIdAndObuIdService.class);

    private String serviceName = "[8911微信小程序二发订单查询接口]";

    private final String TABLE_ONLINE_PICTURE = "onlinepicture";
    private final String TABLE_ONLINE_ORDERS = "online_orders";

    @Override
    public Result entry(Map dataMap) {
        //用于保存预绑定的订单数据
        Record orderRecord = new Record();
        try {
            Record vehicleInfo = new Record().setColumns(dataMap);
            logger.info("{}接收参数:{}", serviceName, vehicleInfo);

            String cardId = vehicleInfo.get("cardId");
            String obuId = vehicleInfo.get("obuId");
            String posId = vehicleInfo.get("posId");
            String userId = vehicleInfo.get("userId");
            String imgHeadstock = vehicleInfo.getStr("imgHeadstock");
            String type = vehicleInfo.getStr("type");
            if (StringUtil.isEmpty(cardId, obuId, posId, userId)) {
                logger.error("{}传入参数cardId, obuId, posId, userId不能为空", serviceName);
                return Result.paramNotNullError("cardId, obuId, posId, userId");
            }


            //查询绑定卡表
            Record bindOrder = Db.findFirst(DbUtil.getSql("queryWXLittleProBindOrder"), cardId, obuId);
            if (bindOrder == null) {
                logger.error("{}未查询到[cardId={},obuId={}]的预绑定订单", serviceName, cardId, obuId);
                return Result.sysError("当前卡、签已发行,请点击[一键激活按钮]进行激活！");
            }

            //根据卡号和OBU号判断订单的有效性
            String orderId = bindOrder.getStr("orderId");
            int orderType = bindOrder.getInt("orderType");
            String bindObuId = bindOrder.getStr("obuId");
            String bindCardId = bindOrder.getStr("cardId");
            int status = bindOrder.getInt("status");
            if (status == OrderProcessStatusEnum.PROCESSED.getValue()) {
                logger.error("{}当前卡、签已发行,无需二次发行", serviceName);
                return Result.sysError("当前卡、签已发行,请点击[一键激活按钮]进行激活！");
            }
            if (OrderTypeEnum.NEW.getValue() == orderType
                    || OrderTypeEnum.CHANGE_ALL.getValue() == orderType) {
                if (!cardId.equals(bindCardId) || !obuId.equals(bindObuId)) {
                    logger.error("{}输入卡号、OBU号与绑定的卡号、OBU号不匹配,[obuId={},cardId={},bindObuId={},bindCardId={}]",
                            serviceName, obuId, cardId, bindObuId, bindCardId);
                    return Result.sysError("输入卡号、OBU号与绑定的卡号、OBU号不匹配");
                }
            } else if (OrderTypeEnum.CHANGE_OBU.getValue() == orderType) {
                if (!obuId.equals(bindObuId)) {
                    logger.error("{}输入OBU号与绑定的OBU号不匹配,[obuId={},bindObuId={}]",
                            serviceName, obuId, bindObuId);
                    return Result.sysError("输入OBU号与绑定的OBU号不匹配");
                }
            } else if (OrderTypeEnum.CHANGE_CARD.getValue() == orderType) {
                if (!cardId.equals(bindCardId)) {
                    logger.error("{}输入卡号与绑定的卡号不匹配,[cardId={},bindCardId={}]",
                            serviceName, cardId, bindCardId);
                    return Result.sysError("输入卡号与绑定的卡号不匹配");
                }
            }

            //判断订单图片是否存在
            if (!isExistPicture(orderId)) {
                //判断车头图片是否存在
                if (StringUtil.isEmpty(imgHeadstock)) {
                    logger.error("{}[orderId={}]用户为上传车头图片", serviceName, orderId);
                    return Result.sysError("获取订单前,请先上传车头图片");
                } else {
                    //判断图片大小是否超过200K
                    if (!isLess200KCheckImgSize(imgHeadstock)) {
                        logger.error("{}[orderId={}]用户传入图片过大,请小于200KB", serviceName, orderId);
                        return Result.sysError("用户传入图片过大,请小于200KB");
                    } else {
                        //保存图片数据
                        saveOnlinePicture(bindOrder, imgHeadstock);
                    }
                }
            }


            //查询订单数据
            String condition = " 1=1 ";
            if (StringUtil.isNotEmpty(type)) {
                condition += " and orderType=? ";
            }
            SqlPara sqlPara = DbUtil.getSqlPara("queryWXLittleProgram2OrderInfo", Kv.by("condition", condition));
            sqlPara.addPara(orderId);
            if (StringUtil.isNotEmpty(type)) {
                sqlPara.addPara(type);
            }
            orderRecord = Db.findFirst(sqlPara);
            if (orderRecord == null) {
                logger.error("{}[orderId={},cardId={},obuId={}]未查询到订单数据", serviceName, orderId, cardId, obuId);
                return Result.sysError("未检查到可操作订单");
            }

            //更新订单操作员信息
            updateOnlineOrdersOperator(orderId, posId, userId);
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        return Result.success(orderRecord);
    }

    /**
     * 判断订单图片是否存在
     *
     * @param orderId
     * @return
     */
    private boolean isExistPicture(String orderId) {
        Record record = Db.findFirst(DbUtil.getSql("isExistOnlineImgByUserId"), orderId);
        if (record.getInt("num") == 0) {
            return false;
        }
        return true;
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
     * @param bindOrder
     * @param imgHeadstock
     */
    private void saveOnlinePicture(Record bindOrder, String imgHeadstock) {
        Record onlinePicture = new Record();
        onlinePicture.set("id", StringUtil.getUUID());
        onlinePicture.set("userId", bindOrder.getStr("orderId"));
        onlinePicture.set("bankCode", bindOrder.getStr("bankCode"));
        onlinePicture.set("carNumber", bindOrder.getStr("plateNum"));
        onlinePicture.set("calColor", bindOrder.getStr("plateColor"));
        //将base64转为二进制流
        byte[] bt = Base64.base64ToByteArray(imgHeadstock);
        onlinePicture.set("imgHeadstock", bt);
        onlinePicture.set("createTime", new Date());
        //保存在线图片
        Db.save(TABLE_ONLINE_PICTURE, "id", onlinePicture);
    }

    /**
     * 更新订单的操作员信息
     *
     * @param orderId
     * @param posId
     * @param userId
     */
    private void updateOnlineOrdersOperator(String orderId, String posId, String userId) {
        Record onlineOrders = new Record();
        onlineOrders.set("orderId", orderId);
        onlineOrders.set("posId", posId);
        onlineOrders.set("operatorId", userId);
        onlineOrders.set("updateTime", new Date());
        //更新订单表的网点和操作员
        Db.update(TABLE_ONLINE_ORDERS, "orderId", onlineOrders);
        logger.info("{}更新订单的操作员信息完成[orderId={},posId={},operatorId={}]",
                serviceName, orderId, posId, userId);
    }
}

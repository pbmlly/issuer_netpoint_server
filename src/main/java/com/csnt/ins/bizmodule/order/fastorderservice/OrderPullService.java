package com.csnt.ins.bizmodule.order.fastorderservice;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseInfoReceiveService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
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
 * @author luoxiaojian
 * @Description: 订单拉取服务类 8901
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/27.
 */
public class OrderPullService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);

    private String serviceName = "[8901订单拉取]";

    /**
     * 默认订单状态 0-全部订单类型
     */
    private final int DEFAULT_ORDER_TYPE = 0;

    @Override
    public Result entry(Map dataMap) {
        Record order = new Record();
        try {
            Record record = new Record().setColumns(dataMap);
            String posId = record.get("posId", "").toString();
            String operatorId = record.get("userId", "").toString();

            //订单类型 默认0-全部订单类型
            Integer type = StringUtil.isEmpty(dataMap.get("type")) ? DEFAULT_ORDER_TYPE : Integer.parseInt(dataMap.get("type") + "");
            if (StringUtil.isEmpty(posId) || StringUtil.isEmpty(operatorId)) {
                logger.error("{}请求的posId,operatorId不能为空", serviceName);
                return Result.paramNotNullError("posId,operatorId");
            }

            //orderStatus=2- 未支付 3- 已支付 postStatus=1- 未发货
            String conditionSql = "  posId=? AND operatorId=?  AND `status`=1 and (orderStatus in (2,3) or orderStatus is null) and postStatus = 1 ";
            //判断是否查询所有类型订单
            if (!isDefaultOrderType(type)) {
                conditionSql = " orderType=? and " + conditionSql;
            }
            //查询数据库数据
            SqlPara sqlPara = DbUtil.getSqlPara("queryOrderInfo", Kv.by("condition", conditionSql));
            if (!isDefaultOrderType(type)) {
                sqlPara.addPara(type);
            }
            sqlPara.addPara(posId);
            sqlPara.addPara(operatorId);
            order = Db.findFirst(sqlPara);
            if (null == order) {
                order = getRecord(posId, operatorId, type, 0);
            }
            logger.info("{}[posId={},operatorId={}]获取到订单:{}", serviceName, posId, operatorId, order);
            if (null == order) {
                logger.error("{}[posId={},operatorId={}]未拉取到订单信息", serviceName, posId, operatorId);
                //更新数据
                return Result.byEnum(ResponseStatusEnum.NO_DATA_ERROR);
            }
        } catch (Exception e) {
            logger.error("{}查询数据库异常:{}", serviceName, e.toString(), e);
            Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }
        return Result.success(order);
    }

    /**
     * 抓取新的订单记录
     *
     * @param offset 偏移量，指在数据资源同时竞争的时候，自动偏移次数
     * @return
     */
    private Record getRecord(String posId, String operatorId, Integer type, Integer offset) {
        //orderStatus=2- 未支付 3- 已支付 postStatus=1- 未发货
        String conditionSql = " ( orderStatus in (2,3)  or orderStatus is null   ) and postStatus = 1 and `status`=0 ORDER BY createTime ASC limit 1 ";
        if (!isDefaultOrderType(type)) {
            conditionSql = " orderType=? and " + conditionSql;
        }
        //使用逻辑乐观锁 乐观锁锁住数据
        SqlPara queryPara = DbUtil.getSqlPara("queryOrderInfo", Kv.by("condition", conditionSql));
        if (!isDefaultOrderType(type)) {
            queryPara.addPara(type);
        }
        Record order = Db.findFirst(queryPara);
        if (null != order) {
            int updateNum = Db.update(DbUtil.getSql("updateOrderStatus", true), posId, operatorId, new Date(), order.getStr("orderId"));
            //如果数据被其他人获取 最多自动处理10次
            offset++;
            if (SysConfig.UseOptimisticLock() && updateNum < 1 && offset < 10) {
                logger.error("发生同步数据竞争(多终端请求了同一条数据)，自动进行偏移，目标数据id[{}],数据明细：postId[{}],operateId[{}],type[{}]当前偏移次数[{}]",
                        order.getStr("orderId"), posId, operatorId, type, offset);
                order = getRecord(posId, operatorId, type, offset);
            }
        }
        return order;
    }

    /**
     * 判断是否查询所有类型订单
     *
     * @param type
     * @return
     */
    private boolean isDefaultOrderType(int type) {
        return DEFAULT_ORDER_TYPE == type;
    }
}

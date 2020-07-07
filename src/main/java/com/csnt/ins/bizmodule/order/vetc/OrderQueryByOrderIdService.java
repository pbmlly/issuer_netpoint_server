package com.csnt.ins.bizmodule.order.vetc;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseInfoReceiveService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cml
 * @Description: 订单查询服务类 8932
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/27.
 */
public class OrderQueryByOrderIdService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);

    private String serviceName = "[8932根据订单编号查询订单信息]";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record record = new Record().setColumns(dataMap);
            String orderId = record.get("orderId");

            if (StringUtil.isEmpty(orderId)) {
                logger.error("{}参数orderId不能为空", serviceName);
                return Result.sysError("orderId不能为空");
            }

            //查询数据库数据
            Record onlineRecord = Db.findFirst(DbUtil.getSql("queryOrderByOrderId"), orderId);
            if (onlineRecord == null) {
                logger.error("{}未查询到该订单信息，orderId={}", serviceName,orderId);
                return Result.bizError(704,"未查询到该订单信息") ;
            } else {
                logger.info("{}[orderid={}]查询到订单信息", serviceName,orderId);

                if (SysConfig.getEncryptionFlag()) {
                    try {
                        onlineRecord.set("accountName", MyAESUtil.Decrypt( onlineRecord.getStr("accountName")));
                        onlineRecord.set("mobile", MyAESUtil.Decrypt( onlineRecord.getStr("mobile")));
                        onlineRecord.set("userIdNum", MyAESUtil.Decrypt( onlineRecord.getStr("userIdNum")));
                        onlineRecord.set("postName", MyAESUtil.Decrypt( onlineRecord.getStr("postName")));
                        onlineRecord.set("postPhone", MyAESUtil.Decrypt( onlineRecord.getStr("postPhone")));
                        onlineRecord.set("postAddr", MyAESUtil.Decrypt( onlineRecord.getStr("postAddr")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return Result.success(onlineRecord);
            }
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
    }
}

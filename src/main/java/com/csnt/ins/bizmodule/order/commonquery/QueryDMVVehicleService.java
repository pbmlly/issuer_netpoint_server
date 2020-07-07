package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1002车管所车辆信息查询
 *
 * @author source
 **/
public class QueryDMVVehicleService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryDMVVehicleService.class);

    private final String serverName = "[1002车管所车辆信息查询]";

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
            String vehicleId = record.getStr("vehicleId");
            vehicleId = vehicleId.split("_")[0].replaceFirst("青", "");
            return Result.success(Db.use(CommonAttribute.DB_SESSION_GGSJTS).find(Db.getSql("mysql.findGgsjtsVehicle"), vehicleId));
        } catch (Throwable t) {
            logger.error("{}车牌信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }


}

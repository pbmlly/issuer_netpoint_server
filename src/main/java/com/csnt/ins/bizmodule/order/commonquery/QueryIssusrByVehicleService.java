package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 1006车辆发行卡、OBU信息查询
 *
 * @author source
 **/
public class QueryIssusrByVehicleService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryIssusrByVehicleService.class);

    private final String serverName = "[1006车辆发行卡、OBU信息查询]";

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
            String plateNum = record.getStr("plateNum");
            Integer plateColor = record.get("plateColor");
            String vehicleId = plateNum + "_" + plateColor;
            Record rd = Db.findFirst(DbUtil.getSql("queryIssuerByVehicle"), vehicleId, vehicleId, vehicleId);

            if (rd == null) {
                return Result.success(null, "未查询到车辆信息");
            }

            return Result.success(rd);
        } catch (Throwable t) {
            logger.error("{}车牌信息查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }


}

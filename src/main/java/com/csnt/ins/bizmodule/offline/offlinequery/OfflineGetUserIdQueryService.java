package com.csnt.ins.bizmodule.offline.offlinequery;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseInfoReceiveService;
import com.csnt.ins.bizmodule.order.queryuserid.GenerateUserIdService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author cml
 * @Description: 用于查询客户编号信息
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/27.
 */
public class OfflineGetUserIdQueryService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);
    GenerateUserIdService generateUserIdService = new GenerateUserIdService();

    private String serviceName = "[1012用于查询客户编号信息]";

    @Override
    public Result entry(Map dataMap) {
        List<Record> datas = new ArrayList<>();
        Map outMap = new HashMap<>();
        try {
            Record record = new Record().setColumns(dataMap);
            String userIdNum = record.get("userIdNum");
            String userIdType = record.get("userIdType");
            if (StringUtil.isEmpty(userIdNum, userIdType)) {
                logger.error("{}参数userIdNum, userIdType不能为空", serviceName);
                return Result.paramNotNullError("userIdNum, userIdType");
            }
            if (SysConfig.getEncryptionFlag()) {
                // 客户证件号码加密密
                if (StringUtil.isNotEmpty(userIdNum)) {
                    userIdNum = MyAESUtil.Encrypt( record.get("userIdNum"));
                }
            }
            outMap = generateUserIdService.postQueryUserIdServer(userIdType, userIdNum);
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("获取客户编号异常");
        }
        return Result.success(outMap);
    }


}

package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1006用户信息查询
 *
 * @Author: cym
 * @Date: 2019/8/12 15:17
 */
public class QueryUserInfoService implements IReceiveService {

    private Logger logger = LoggerFactory.getLogger(QueryVehicleService.class);

    private final String serviceName = "[1006用户信息查询]";

    @Override
    public Result entry(Map dataMap) {
        Record data;
        try {
            Record record = new Record().setColumns(dataMap);
            //证件类型
            Integer userIdType = record.get("userIdType") == null ? null : Integer.parseInt(record.get("userIdType").toString());
            //证件号码
            String userIdNum = record.get("userIdNum") == null ? null : record.get("userIdNum").toString();
            //参数校验
            if (StringUtil.isEmpty(userIdType, userIdNum)) {
                logger.error("{}传入的参数userIdType, userIdNum不能为空", serviceName);
                return Result.paramNotNullError("userIdType, userIdNum不能为空");
            }
            if (SysConfig.getEncryptionFlag()) {
                //加密
                try {
                    userIdNum  = MyAESUtil.Encrypt( record.getStr("userIdNum"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return  Result.bizError(799, "证件信息加密失败") ;
                }

            }

            //sql构造
            StringBuilder condition = new StringBuilder();
            List<Object> param = new ArrayList<>();
            DbUtil.initParam("userIdType", userIdType, condition, param, DbUtil.OPERATE.EQUAL);
            DbUtil.initParam("userIdNum", userIdNum, condition, param, DbUtil.OPERATE.EQUAL);

            //查询数据库数据
            SqlPara sqlPara = DbUtil.getSqlPara("queryUserInfo", Kv.by("condition", condition.toString().replaceFirst("and", " ")));
            for (Object paramNode : param) {
                sqlPara.addPara(paramNode);
            }

            data = Db.findFirst(sqlPara);
            if (data == null) {
                return Result.success(null, "未查询到用户信息");
            }
            if (SysConfig.getEncryptionFlag()) {
                //数据解密
                data.set("userName", MyAESUtil.Decrypt( data.getStr("userName")));
                data.set("userIdNum", MyAESUtil.Decrypt( data.getStr("userIdNum")));
                data.set("tel", MyAESUtil.Decrypt( data.getStr("tel")));
                data.set("address", MyAESUtil.Decrypt( data.getStr("address")));
                data.set("agentName", MyAESUtil.Decrypt( data.getStr("agentName")));
                data.set("agentIdNum", MyAESUtil.Decrypt( data.getStr("agentIdNum")));
                data.set("bankAccount", MyAESUtil.Decrypt( data.getStr("bankAccount")));
            }
        } catch (Exception e) {
            logger.error("{}数据查询异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询数据异常");
        }
        return Result.success(data);
    }
}

package com.csnt.ins.bizmodule.order.handset;


import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.MyAESUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 8908车牌查询在线申请接口
 *
 * @author source
 */
public class OnlineApplyQueryByVehicleIdService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(OnlineApplyQueryByVehicleIdService.class);

    private String serviceName = "[8908车牌查询在线申请接口]";

    @Override
    public Result entry(Map dataMap) {
        try {
            Record vehicleInfo = new Record().setColumns(dataMap);
            String plateNum = vehicleInfo.get("plateNum");
            String plateColor = vehicleInfo.get("plateColor");
            Integer userIdType = vehicleInfo.get("userIdType");
            String userIdNum = vehicleInfo.get("userIdNum");
            logger.info("{}接收车牌查询申请信息接口:{}", serviceName, vehicleInfo);

            if (StringUtil.isEmpty(plateNum, plateColor, userIdType, userIdNum)) {
                logger.error("{}参数plateNum, plateColor, userIdType, userIdNum不能为空", serviceName);
                return Result.paramNotNullError("plateNum, plateColor, userIdType, userIdNum");
            }
            if (SysConfig.getEncryptionFlag()) {
                // 证件号码加密
                if (StringUtil.isNotEmpty(userIdNum)) {
                    userIdNum = MyAESUtil.Encrypt(userIdNum);
                }
            }

            List<Record> datas;
            try {
                Object[] param = new Object[2];
                param[0] = plateNum;
                param[1] = plateColor;
                datas = Db.find(DbUtil.getSql("queryOnlineApplyBycarNumber"), param);
                if (datas.size() == 0) {
                    logger.error("{}未检查到该车牌信息的申请信息", serviceName);
                    return Result.sysError("未检查到该车牌信息的申请信息！");
                }
                logger.info("{}[{}]查询到的申请数据有{}条", serviceName, vehicleInfo, datas.size());
            } catch (Exception e) {
                logger.error("{}[plateNum={},plateColor={}]数据查询异常:{}", serviceName, plateNum, plateColor, e.toString(), e);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "查询数据异常");
            }

            Map rsMap = getData(datas.get(0));
            logger.info("{}查询申请订单成功:{}", serviceName, rsMap);
            return Result.success(rsMap);
        } catch (IOException e) {
            logger.error("{}查询订单异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询订单异常");
        } catch (SQLException e) {
            logger.error("{}查询数据库异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR, "查询数据异常");
        } catch (ClassCastException e) {
            logger.error("{}传入参数类型异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM, "传入参数类型异常");
        } catch (Exception e) {
            logger.error("{}查询申请信息异常:{}", serviceName, e.toString(), e);
            return Result.sysError("查询申请信息异常");
        }
    }

    /**
     * 订单信息组装
     *
     * @param rc
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private Map getData(Record rc) throws IOException, SQLException {

        Map map = new HashMap<>();

        // 订单编号
        map.put("orderId", rc.get("bookId"));
        // id  uuid唯一主键
        map.put("id", rc.get("id"));
        // 订单生成时间
        map.put("createTime", rc.get("bookDate"));
        // 车辆号码
        map.put("carNumber", rc.get("carNumber"));
        // 车辆颜色
        map.put("calcolor", rc.get("calcolor"));
        // 车辆颜色描述
        map.put("calcolorDesc", rc.get("calcolorDesc"));
        // 账户类型
        map.put("customerType", rc.get("customerType"));
        // 账户类型名称
        map.put("customerTypeDesc", rc.get("customerTypeDesc"));
        // 车辆载重/座位数
        map.put("seats", rc.get("seats"));
        // 所属银行
        map.put("bankCodeDesc", rc.get("bankCodeDesc"));
        // 办理类型
        map.put("type", rc.get("type"));
        // 办理类型描述
        map.put("typeDesc", rc.get("typeDesc"));
        // 支付卡类型
        map.put("cardType", rc.get("cardType"));
        // 支付卡类型名称
        map.put("cardTypeDesc", rc.get("cardTypeDesc"));
        // 车型（描述）
        // 取车型
        int vehtype = getVehicleType(rc.getStr("velchel"), rc.getInt("seats"));
        Record vehtypeRc = Db.findFirst(Db.getSql("mysql.queryVehicleTypeDesc"), vehtype);

        map.put("velchelDesc", vehtypeRc != null ? vehtypeRc.get("name") : null);
        // 开户证件类型
        map.put("passPortType", rc.get("passPortType"));
        // 开户证件类型名称
        map.put("passPortTypeDesc", rc.get("passPortTypeDesc"));
        // 用户证件号码
        map.put("passPortId", rc.get("passPortId"));
        // 用户姓名
        map.put("customerName", rc.get("customerName"));
        // 收货人
        map.put("postName", rc.get("postName"));
        // 收货人电话
        map.put("postTel", rc.get("postTel"));
        // 收货地址
        map.put("postAddr", rc.getStr("postArea") + rc.getStr("postAddres"));
        // 收货邮编
        map.put("postId", rc.get("postId"));
        // 品牌型号
        map.put("model", rc.get("model"));
        // 车辆识别码
        map.put("vin", rc.get("vin"));
        // 发动机号
        map.put("engineNo", rc.get("engineNo"));
        // 车辆尺寸
        map.put("dimension", rc.get("dimension"));
        // 审核状态
        map.put("examineResult", rc.get("examineResult") == null ? 0 : rc.get("examineResult"));
        // 审核说明
        map.put("examineDescription", rc.get("examineDescription"));

        if (SysConfig.getEncryptionFlag()) {

            try {
                // 用户证件号码
                map.put("passPortId", MyAESUtil.Decrypt(rc.getStr("passPortId")));
                // 用户姓名
                map.put("customerName",MyAESUtil.Decrypt(rc.get("customerName")));
                // 收货人
                map.put("postName",MyAESUtil.Decrypt(rc.get("postName")));
                // 收货人电话
                map.put("postTel", MyAESUtil.Decrypt(rc.get("postTel")));
                // 收货地址
                map.put("postAddr", map.get("postAddr")==null?null:MyAESUtil.Decrypt(map.get("postAddr").toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 读取图片信息
        List<Record> imgs = Db.find(Db.getSql("mysql.queryOnlineImgByUserId"), rc.getStr("bookId"));
        logger.info("{}[orderId={}]读取到图片数据[{}]条", serviceName, rc.get("bookId"), imgs.size());
        // 身份证正面照
        map.put("imgPositive", null);
        // 身份证反面照
        map.put("imgBack", null);
        // 行驶证首页照
        map.put("imgHome", null);
        // 行驶证信息照
        map.put("imgInfo", null);
        // 车头照
        map.put("imgHeadstock", null);
        // 手持身份证照
        map.put("imgHold", null);

        if (imgs.size() > 0) {
            Record imgRC = imgs.get(0);

            byte[] imgPositiveByte = imgRC.getBytes("imgPositive");
            byte[] imgbackByte = imgRC.getBytes("imgBack");
            byte[] imghomeByte = imgRC.getBytes("imgHome");
            byte[] imginfoByte = imgRC.getBytes("imgInfo");
            byte[] imgheadstockByte = imgRC.getBytes("imgHeadstock");
            byte[] imgholdByte = imgRC.getBytes("imgHold");

            if (imgPositiveByte != null) {
                map.put("imgPositive", StringUtil.getBase64StrByByteArr(imgPositiveByte));
            }
            if (imgbackByte != null) {
                map.put("imgBack", StringUtil.getBase64StrByByteArr(imgbackByte));
            }
            if (imghomeByte != null) {
                map.put("imgHome", StringUtil.getBase64StrByByteArr(imghomeByte));
            }
            if (imginfoByte != null) {
                map.put("imgInfo", StringUtil.getBase64StrByByteArr(imginfoByte));
            }
            if (imgheadstockByte != null) {
                map.put("imgHeadstock", StringUtil.getBase64StrByByteArr(imgheadstockByte));
            }
            if (imgholdByte != null) {
                map.put("imgHold", StringUtil.getBase64StrByByteArr(imgholdByte));
            }
        }

        return map;
    }

    /**
     * 获取收费车型
     *
     * @param velchel
     * @param seats
     * @return
     */
    public int getVehicleType(String velchel, int seats) {

        if (velchel == null) {
            return 0;
        }
        if (CommonAttribute.VEHCILE_TYPE_TRUCK.equals(velchel)) {
            // 货车
            if (seats <= 2) {
                return 11;
            }
            if (seats <= 5) {
                return 12;
            }
            if (seats <= 10) {
                return 13;
            }
            if (seats <= 15) {
                return 14;
            } else {
                return 15;
            }
        } else {
            // 客车
            if (seats <= 7) {
                return 1;
            }
            if (seats <= 19) {
                return 2;
            }
            if (seats <= 39) {
                return 3;
            }
            if (seats >= 39) {
                return 4;
            }
        }

        return 0;
    }
}

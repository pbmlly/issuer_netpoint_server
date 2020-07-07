package com.csnt.ins.bizmodule.order.orderbusiness;


import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuinfoUploadService;
import com.csnt.ins.bizmodule.offline.service.UserObuoflService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.issuer.OnlineOrders;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OBU信息接收接口
 *
 * @author source
 */
@SuppressWarnings("Duplicates")
public class ObuReceiveService implements IReceiveService, BaseUploadService {
    Logger logger = LoggerFactory.getLogger(ObuReceiveService.class);

    private String serviceName = "[8904在线发行OBU信息接收]";

    private final String TABLE_OBUINFO = "etc_obuinfo";
    private final String TABLE_IM_ORDER = "im_order";
    private final String TABLE_ONLINE_ORDERS = "online_orders";
    private final String TABLE_OBUINFO_HISTORY = "etc_obuinfo_history";
    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String MSG_NORMAL = "车辆存在非核销挂起的OBU";
    private final String OFFLINE_CHANNEL = "040";

    /**
     * 异常响应编码
     */
    private final int ERROR_RESPONSE_STATUS = 801;

    /**
     * 8842卡信息新增及变更通知
     */
    UserObuinfoUploadService userObuinfoUploadService = new UserObuinfoUploadService();

    /**
     * 获取OBU信息
     */
    UserObuoflService userObuoflService = new UserObuoflService();

    @Override
    public Result entry(Map dataMap) {
        try {
            Record obuInfo = new Record().setColumns(dataMap);
            logger.info("{}接收到网点OBU信息:{}", serviceName, obuInfo);
            String orderId = obuInfo.get("orderId");
            String notNullParamNames = "orderId,obuId,obuSign,enableTime,expireTime," +
                    "registeredType,registeredChannelId,registeredTime,installTime,installType,activeChannel,status,statusChangeTime";
            if (StringUtil.isEmptyArg(obuInfo, notNullParamNames)) {
                logger.error("{}传入参数{}不能为空", serviceName, notNullParamNames);
                return Result.paramNotNullError(notNullParamNames);
            }

            if (!SysConfig.CONNECT.get("gather.id").equals(obuInfo.getStr("obuId").toString().substring(0,2))) {
                logger.error("{}参数OBU错误：{}", serviceName,obuInfo.getStr("obuId").toString());
                return Result.bizError(795, "参数OBU错误，不属于该发行方！");
            }



            OnlineOrders onlineOrders = OnlineOrders.dao.findById(obuInfo.get("orderId"));

            if (onlineOrders == null) {
                logger.error("{}[orderId={}未找到该订单信息", serviceName, orderId);
                return Result.bizError(831, "未找到该订单信息！");
            }
            //判断订单是否在进行中
            if (onlineOrders.getStatus() != OrderProcessStatusEnum.PROCESSING.getValue()) {
                logger.error("{}[orderId={}]当前订单状态不在处理中[status={}]", serviceName, orderId, onlineOrders.getStatus());
                if (onlineOrders.getStatus() == OrderProcessStatusEnum.UNPROCESS.getValue()) {
                    logger.error("{}[orderId={}]当前订单已过期,请重新拉取订单", serviceName, orderId);
                    return Result.bizError(834, "当前订单已过期,请重新拉取订单！");
                } else {
                    logger.error("{}[orderId={}]当前订单已处理完成,无需重新处理", serviceName, orderId);
                    return Result.bizError(835, "当前订单已处理完成,无需重新处理！");
                }
            }

            //判断订单OBU是否已经接收
            if (StringUtil.isNotEmpty(onlineOrders.getObuId())) {
                return Result.bizError(836, "当前订单OBU已上传,无需重复处理！ OBU号:" + onlineOrders.getObuId());
            }
            // 判断OBU 编号是否已经发行
//            Record obuRc =  Db.findFirst(DbUtil.getSql("CheckCenterObuInfoById"), obuInfo.get("obuId").toString());
//            if (obuRc != null) {
//                return Result.sysError("当前OBU已经发行。");
//            }

            // 判断OBU是否已经发行
            Record cdRc = Db.findFirst(DbUtil.getSql("CheckCenterObuInfoById"), obuInfo.get("obuId").toString());
            if (cdRc != null) {
                return Result.bizError(869, "当前OBU标签已经发行。");
            }


            //检查OBU品牌
            String brand = obuInfo.getStr("brand");
            if (StringUtil.isEmpty(brand)) {
                obuInfo.set("brand", getObuBrand(obuInfo.getStr("obuId")));
            }
            //检查OBU型号
            String model = obuInfo.getStr("model");
            if (StringUtil.isEmpty(model)) {
                obuInfo.set("model", getObuModel(obuInfo.getStr("obuId")));
            }

            //判断订单类型 新增订单需要校验当前卡对应的车辆是否已经发行
            int orderType = onlineOrders.getOrderType();
            if (OrderTypeEnum.NEW.getValue() == orderType) {
                //判断当前OBU对应车辆是否已经发行,需要排除上传部中心成功，本地入库失败的情况
                String obuId = findObuIdByVehicleId(onlineOrders.getVehicleCode());
                if (StringUtil.isNotEmpty(obuId) && (!obuInfo.getStr("obuId").equals(obuId))) {
                    logger.error("{}[orderId={}]当前订单车辆已发行了OBU[obuId={}],不能重复发OBU", serviceName, orderId, obuId);
                    String msg = String.format("当前订单车辆已发行了OBU,不能重复发OBU,OBU号:%s", obuId);
                    //将发行失败的OBU信息保存到Im_order表中，用于人工撤单
                    saveImOrderResponseMsg(orderId, msg);
                    return Result.bizError(837, msg);
                }
            } else if (OrderTypeEnum.CHANGE_CARD.equals(orderType)
                    || OrderTypeEnum.REISSUE_CARD.equals(orderType)) {
                //签的售后订单不允许使用卡的处理流程
                logger.error("{}[orderId={}]当前订单为[{}]订单,不允许接收OBU信息", serviceName, orderId, OrderTypeEnum.getName(orderType));
                return Result.bizError(839, "当前订单为[" + OrderTypeEnum.getName(orderType) + "]订单,不允许接收OBU信息");
            }

            Date currentDate = new Date();
            obuInfo.set("id", obuInfo.get("obuId"));
            //installType=1时 installChannelId =0
            if (1 == MathUtil.asInteger(obuInfo.get("installType"))) {
                obuInfo.set("installChannelId", 0);
            }
            obuInfo.set("userId", onlineOrders.getUserId());
            obuInfo.set("vehicleId", onlineOrders.getVehicleCode());
            obuInfo.set("channelType", onlineOrders.getChannelType());
            obuInfo.set("operation", OperationEnum.ADD.getValue());
            obuInfo.set("opTime", currentDate);
            obuInfo.set("createTime", currentDate);
            obuInfo.set("updateTime", currentDate);
            obuInfo.remove("orderId", "obuId", "activeChannel", "plateColor", "plateNum");

            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            String channelType = onlineOrders.getStr("channelType").substring(0, 3);
            if (bl && channelType.equals(OFFLINE_CHANNEL)) {
                Map offMap = callOffineObuInfo(obuInfo, onlineOrders.get("userId"));
                if (!(boolean) offMap.get("bool")) {
                    return (Result) offMap.get("result");
                }
            }

            //向部中心上传OBU信息
            BaseUploadResponse response = uploadBasicObuInfo(obuInfo, onlineOrders);

            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传部中心数据异常:{}", serviceName, response);
                return Result.bizError(840, "上传部中心数据异常,stateCode:" + response.getStateCode() + " errorMsg:" + response.getErrorMsg());
            }


            boolean flag = false;

            String ids = "id,opTime";
            flag = Db.tx(() -> {
                //保存至卡信息表
                Db.delete(TABLE_OBUINFO, obuInfo);

                if (!Db.save(TABLE_OBUINFO, obuInfo)) {
                    logger.error("{}保存TABLE_OBUINFO表失败", serviceName);
                    return false;
                }
                if (!Db.save(TABLE_OBUINFO_HISTORY, ids, obuInfo)) {
                    logger.error("{}保存TABLE_OBUINFO_HISTORY表失败", serviceName);
                    return false;
                }
//                Db.save(TABLE_OBUINFO, obuInfo);
//                Db.save(TABLE_OBUINFO_HISTORY, ids, obuInfo);

                //更新CardId
                onlineOrders.setObuId(obuInfo.get("id"));

                //判断订单是否完成
                if (isComplete(onlineOrders)) {
                    onlineOrders.setStatus(OrderProcessStatusEnum.PROCESSED.getValue());
                }
//                onlineOrders.update();
                if (!onlineOrders.update()) {
                    logger.error("{}保存onlineOrders-update表失败", serviceName);
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}[orderId={}]订单接收OBU信息成功", serviceName, orderId);
                return Result.success(null);
            } else {
                logger.error("{}[orderId={}]数据库入库失败", serviceName, orderId);
                return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }
        } catch (Throwable e) {
            logger.error("{}数据库入库异常:{}", serviceName, e.toString(), e);
            return Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }

    }

    /**
     * 获取OBU品牌
     * <p>
     * #埃特斯
     * obu00=1!!!UTTAG-1
     * #金溢
     * obu01=2!!!Sophia-v60c+
     * #成谷
     * obu03=11!!!adfdd
     * #聚利
     * obu04=3!!!JLCZ-06S
     * #万集
     * obu08=7!!!W-115B+
     *
     * @param obuId
     * @return
     */
    private int getObuBrand(String obuId) {
        int result = 0;
        if (StringUtil.isEmpty(obuId)) {
            return result;
        }
        String brand = obuId.substring(9, 11);
        String brandStr = SysConfig.getObuBrand(brand);
        if (StringUtil.isEmpty(brandStr)) {
            result = 1;
        } else {
            result = MathUtil.asInteger(brandStr.split(";;")[0]);
        }
        /*switch (brand) {
            case "00":
                result = 1;
                break;
            case "01":
                result = 2;
                break;
            case "03":
                result = 11;
                break;
            case "04":
                result = 3;
                break;
            case "08":
                result = 7;
                break;
            default:
                result = 1;
        }*/
        return result;
    }

    /**
     * 获取OBU型号
     * <p>
     * #埃特斯
     * obu00=1!!!UTTAG-1
     * #金溢
     * obu01=2!!!Sophia-v60c+
     * #成谷
     * obu03=11!!!adfdd
     * #聚利
     * obu04=3!!!JLCZ-06S
     * #万集
     * obu08=7!!!W-115B+
     *
     * @param obuId
     * @return
     */
    private String getObuModel(String obuId) {
        if (StringUtil.isEmpty(obuId)) {
            return "";
        }
        String brand = obuId.substring(9, 11);
        String brandStr = SysConfig.getObuBrand(brand);
        if (StringUtil.isEmpty(brandStr)) {
            brand = "UTTAG-1";
        } else {
            brand = brandStr.split(";;")[1];
        }
        /*switch (brand) {
            case "00":
                brand = "UTTAG-1";
                break;
            case "01":
                brand = "Sophia-v60c+";
                break;
            case "03":
                brand = "adfdd";
                break;
            case "04":
                brand = "JLCZ-06S";
                break;
            case "08":
                brand = "W-115B+";
                break;
            default:
                brand = "UTTAG-1";
        }*/
        return brand;
    }

    /**
     * 判断订单是否处理完成
     * <p>
     * 1、如果是新增订单，
     * 则判断卡是否已发行，如果已发行则订单处理完成
     * 2、如果是换签订单
     * 则直接判断当前订单处理完成
     * 3、如果是换卡、换签全套订单
     * 则判断卡是否已发行，如果已发行则订单处理完成
     * </p>
     *
     * @param onlineOrders
     * @return
     */
    private boolean isComplete(OnlineOrders onlineOrders) {
        boolean isComplete = false;
        //判断订单类型
        if (OrderTypeEnum.NEW.getValue() == onlineOrders.getOrderType()
                || OrderTypeEnum.CHANGE_ALL.getValue() == onlineOrders.getOrderType()) {

            isComplete = StringUtil.isNotEmpty(onlineOrders.getCardId(), onlineOrders.getObuId());
        } else if (OrderTypeEnum.CHANGE_OBU.getValue() == onlineOrders.getOrderType()) {
            isComplete = true;
        }
        return isComplete;
    }

    /**
     * 判断当前车辆在OBU表中 是否存在operation !== 3 的数据
     *
     * @param vehicleId
     * @return
     */
    private String findObuIdByVehicleId(String vehicleId) {
        //查询发行系统
        Record cardRecord = queryIssueObuinfoByVehicleId(vehicleId);
        if (cardRecord == null) {
            //查询易构
            cardRecord = queryYGObuinfoByVehicleId(vehicleId);
        }
        if (cardRecord == null) {
            //由于易构注销车辆方式，会导致汇聚平台中的卡签异常，无法为该车发行新卡签
//            cardRecord = queryHJPTObuinfoByVehicleId(vehicleId);
        }
        if (cardRecord == null) {
            return null;
        }
        return cardRecord.getStr("id");
    }

    /**
     * 根据车牌查询汇聚平台OBU信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryHJPTObuinfoByVehicleId(String vehicleId) {
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(DbUtil.getSql("queryHJPTObuinfoByVehicleId"), vehicleId);
    }

    /**
     * 根据车牌查询发行OBU信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryIssueObuinfoByVehicleId(String vehicleId) {
        return Db.findFirst(DbUtil.getSql("queryIssueObuinfoByVehicleId"), vehicleId);
    }

    /**
     * 根据车牌查询汇聚平台OBU信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryYGObuinfoByVehicleId(String vehicleId) {
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(DbUtil.getSql("queryYGObuinfoByVehicleId"), vehicleId);
    }

    /**
     * 上传OBU信息到部中心
     *
     * @param obuInfo
     * @param onlineOrders
     * @return
     */
    private BaseUploadResponse uploadBasicObuInfo(Record obuInfo, OnlineOrders onlineOrders) {
        Date currentDate = new Date();
        //处理obuInfo
//        String plateColor = String.valueOf(obuInfo.getInt("plateColor"));
//        String plateNum = obuInfo.get("plateNum");
//        obuInfo.set("vehicleId", plateNum + "_" + plateColor);


        EtcObuinfoJson etcObuinfoJson = new EtcObuinfoJson();
        etcObuinfoJson._setOrPut(obuInfo.getColumns());
        BaseUploadResponse response = upload(etcObuinfoJson, BASIC_OBUUPLOAD_REQ);
        logger.info("{}上传OBU响应信息:{}", serviceName, response);
        return response;
    }

    /**
     * 保存im_order表的异常响应信息，用于人工撤单
     *
     * @param orderId
     * @param msg
     */
    private void saveImOrderResponseMsg(String orderId, String msg) {
        Record imOrder = new Record();
        imOrder.set("orderId", orderId);
        imOrder.set("responsestatus", ERROR_RESPONSE_STATUS);
        imOrder.set("responsemsg", msg);
        imOrder.set("responsetime", new Date());
        //更新online_orders表的orderStatus=3撤单，防止再次拉取订单
        Record onlineOrders = new Record();
        onlineOrders.set("orderId", orderId);
        //4- 已撤单
        onlineOrders.set("orderStatus", OrderStatusEnum.CANCELED.getValue());
        Db.tx(() -> {
            if (!Db.update(TABLE_IM_ORDER, "orderid", imOrder)) {
                logger.error("{}更新TABLE_IM_ORDER表失败", serviceName);
                return false;
            }
            if (!Db.update(TABLE_ONLINE_ORDERS, "orderid", onlineOrders)) {
                logger.error("{}更新TABLE_ONLINE_ORDERS表失败", serviceName);
                return false;
            }

            return true;
        });
        logger.info("{}保存发行OBU异常信息到im_order表成功", serviceName);
    }

    /**
     * 线下相关接口上传部中心
     *
     * @param dataRC
     * @return
     */
    private Map callOffineObuInfo(Record dataRC, String userId) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(userId);
        if (etcOflUserinfo != null) {
            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", false);
                outMap.put("result", result);
                return outMap;
            }
            // 获取车辆信息
            String vehicleId = dataRC.get("vehicleId");
            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);
            if (etcOflVehicleinfo == null) {
                logger.error("{}未找到OBU对应的车辆信息vehid={}", serviceName, vehicleId);
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(704, "未找到OBU对应的车辆信息"));
            }


            String[] sp = vehicleId.split("_");
            if (sp.length != 2) {
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(704, "车牌异常"));
                logger.error("{}[vehicleId={}]车牌异常:",
                        serviceName, vehicleId);
                return outMap;
            }
            if (etcOflVehicleinfo.getDepVehicleId() != null) {
                // 调用4.2obu信息新增及变更接口
                result = userObuinfoUploadService.entry(Kv.by("obuId", dataRC.get("id"))
                        .set("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        .set("issuerId", CommonAttribute.ISSUER_CODE)
                        .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                        .set("type", 1)
                        .set("brand", dataRC.get("brand"))
                        .set("model", dataRC.get("model"))
                        .set("obuSign", Integer.parseInt(dataRC.get("obuSign").toString()))
                        .set("plateNum", sp[0])
                        .set("plateColor", Integer.parseInt(sp[1]))
                        .set("enableTime", dataRC.get("enableTime"))
                        .set("expireTime", dataRC.get("expireTime"))
                        .set("issueChannelType", 2)
                        .set("issueChannelId", dataRC.get("registeredChannelId"))
                        .set("activeTime", DateUtil.formatDate(new Date(), DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS))
                        .set("activeType", 1)
                );
//                && !result.getMsg().contains(MSG_NORMAL)
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    logger.error("{}调用OBU信息新增及变更通知失败:{}", serviceName, result);
//                    outMap.put("bool", false);
                    outMap.put("result", result);
                    //调用查询接口判断是否重复上传
                    result = userObuoflService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                            .set("openId", etcOflUserinfo.getOpenId())
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("vehicleId", etcOflVehicleinfo.getDepVehicleId()));
                    if (!checkUserCardoflService(result, dataRC.get("id"))) {
                        logger.error("{}调用OBU信息查询失败:{}", serviceName, result);
                        outMap.put("bool", false);
                    }
                    return outMap;
                }
            }

        }


        return outMap;
    }

    /**
     * 检查OBU查询接口的结果
     *
     * @param result
     * @return
     */
    private boolean checkUserCardoflService(Result result, String obuId) {
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            return false;
        } else {
            Map<String, Object> map = (Map<String, Object>) result.getData();
            logger.info("{}查询当前车辆的卡信息为:{}", serviceName, map);
            //1- 存在 2- 不存在
            if (map != null && 1 == MathUtil.asInteger(map.get("result"))) {
                List<Map> encryptedData = (List<Map>) map.get("encryptedData");
                for (Map data : encryptedData) {
                    String id = (String) data.get("obuId");
                    if (obuId.equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

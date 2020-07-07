package com.csnt.ins.bizmodule.order.orderbusiness;


import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardinfoUploadService;
import com.csnt.ins.bizmodule.offline.service.UserCardoflService;
import com.csnt.ins.enumobj.*;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.issuer.EtcOflVehicleinfo;
import com.csnt.ins.model.issuer.OnlineOrders;
import com.csnt.ins.model.json.EtcCardinfoJson;
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
 * 卡信息接收接口8905
 *
 * @author source
 */
@SuppressWarnings("Duplicates")
public class CardReceiveService implements IReceiveService, BaseUploadService {
    Logger logger = LoggerFactory.getLogger(CardReceiveService.class);

    private String serviceName = "[8905在线发行卡信息接收]";

    private final String TABLE_CARDINFO = "etc_cardinfo";
    private final String TABLE_IM_ORDER = "im_order";
    private final String TABLE_ONLINE_ORDERS = "online_orders";
    private final String TABLE_CARDINFO_HISTORY = "etc_cardinfo_history";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";
    private final String OFFLINE_CHANNEL = "040";
    private final String MSG_NORMAL = "或者正常";

    /**
     * 8842卡信息新增及变更通知
     */
    UserCardinfoUploadService userCardinfoUploadService = new UserCardinfoUploadService();
    /**
     * 异常响应编码
     */
    private final int ERROR_RESPONSE_STATUS = 801;

    /**
     * 获取卡信息
     */
    UserCardoflService userCardoflService = new UserCardoflService();

    @Override
    public Result entry(Map dataMap) {
        try {
            Record cardInfo = new Record().setColumns(dataMap);
            logger.info("{}接收到网点卡信息:{}", serviceName, cardInfo);
            String orderId = cardInfo.get("orderId");
            String notNullParamNames = "orderId,cardId,cardType,agencyId,enableTime," +
                    "expireTime,issuedType,channelId,issuedTime,status,statusChangeTime";
            if (StringUtil.isEmptyArg(cardInfo, notNullParamNames)) {
                logger.error("{}传入参数{}不能为空", serviceName, notNullParamNames);
                return Result.paramNotNullError(notNullParamNames);
            }

            if (!SysConfig.CONNECT.get("gather.id").equals(cardInfo.get("cardId").toString().substring(0,2))) {
                logger.error("{}参数cardId错误：{}", serviceName,cardInfo.get("cardId").toString());
                return Result.bizError(795, "参数cardId错误，不属于该发行方！");
            }


            OnlineOrders onlineOrders = OnlineOrders.dao.findById(orderId);

            if (onlineOrders == null) {
                logger.error("{}[orderId={}]未找到订单信息", serviceName, orderId);
                return Result.bizError(815, "未找到订单信息！");
            }
            //判断订单是否在进行中
            if (onlineOrders.getStatus() != OrderProcessStatusEnum.PROCESSING.getValue()) {
                logger.error("{}[orderId={}]当前订单状态不在处理中[status={}]", serviceName, orderId, onlineOrders.getStatus());
                if (onlineOrders.getStatus() == OrderProcessStatusEnum.UNPROCESS.getValue()) {
                    logger.error("{}[orderId={}]当前订单已过期,请重新拉取订单", serviceName, orderId);
                    return Result.bizError(816, "当前订单已过期,请重新拉取订单！");
                } else {
                    logger.error("{}[orderId={}]当前订单已处理完成,无需重新处理", serviceName, orderId);
                    return Result.bizError(817, "当前订单已处理完成,无需重新处理！");
                }
            }

            //判断订单卡是否已经接收
            if (StringUtil.isNotEmpty(onlineOrders.getCardId())) {
                return Result.bizError(818, "当前订单卡已上传,无需重复处理！ ETC卡号:" + onlineOrders.getCardId());
            }

            // 判断卡是否已经发行
            Record cdRc = Db.findFirst(DbUtil.getSql("CheckCenterCardInfoById"), cardInfo.get("cardId").toString());
            if (cdRc != null) {
                return Result.bizError(819, "当前ETC卡号已经发行。");
            }


            //处理卡品牌brand
            String brand = cardInfo.getStr("brand");
            if (StringUtil.isEmpty(brand)) {
                cardInfo.set("brand", getCardBrand(cardInfo.getStr("cardId")));
            }
            //检查卡型号
            String model = cardInfo.getStr("model");
            if (StringUtil.isEmpty(model)) {
                cardInfo.set("model", getCardModel(cardInfo.getStr("cardId")));
            }


            //判断订单类型 新增订单需要校验当前卡对应的车辆是否已经发行
            int orderType = onlineOrders.getOrderType();
            if (OrderTypeEnum.NEW.equals(orderType)) {
                //判断当前卡对应车辆是否已经发行，需要排除上传部中心成功，本地入库失败的情况
                String cardid = findCardIdByVehicleId(onlineOrders.getVehicleCode());
                if (StringUtil.isNotEmpty(cardid) && (!cardInfo.getStr("cardId").equals(cardid))) {
                    logger.error("{}[orderId={}]当前订单车辆已发行了卡[cardId={}],不能重复发卡", serviceName, orderId, cardid);
                    String msg = String.format("当前订单车辆已发行了卡,不能重复发卡,ETC卡号:%s", cardid);
                    //向im_order表写入异常信息，用于用户进行手动撤单
                    saveImOrderResponseMsg(orderId, msg);
                    return Result.bizError(820, msg);
                }
            } else if (OrderTypeEnum.CHANGE_OBU.equals(orderType)
                    || OrderTypeEnum.REISSUE_OBU.equals(orderType)) {
                //签的售后订单不允许使用卡的处理流程
                logger.error("{}[orderId={}]当前订单为[{}]订单,不允许接收卡信息", serviceName, orderId, OrderTypeEnum.getName(orderType));
                return Result.bizError(821, "当前订单为[" + OrderTypeEnum.getName(orderType) + "]订单,不允许接收卡信息");
            }

            Date currentDate = new Date();
            //处理cardInfo
//        String plateColor = String.valueOf(cardInfo.getInt("plateColor"));
//        String plateNum = cardInfo.get("plateNum");
//        cardInfo.set("vehicleId", plateNum + "_" + plateColor);
            cardInfo.set("id", cardInfo.get("cardId"));
            cardInfo.set("userId", onlineOrders.getUserId());
            cardInfo.set("userId", onlineOrders.getUserId());
            cardInfo.set("vehicleId", onlineOrders.getVehicleCode());
            cardInfo.set("bankPost", onlineOrders.getBankCode());
            cardInfo.set("channelType", onlineOrders.getChannelType());
            cardInfo.set("operation", OperationEnum.ADD.getValue());
            cardInfo.set("opTime", currentDate);
            cardInfo.set("createTime", currentDate);
            cardInfo.set("updateTime", currentDate);
            cardInfo.remove("orderId", "cardId", "plateColor", "plateNum");


            // 判断数据是否上传线下部省平台
            boolean bl = Boolean.parseBoolean(SysConfig.CONFIG.get("open.userauth"));
            String channelType = onlineOrders.getStr("channelType").substring(0, 3);

            if (bl && channelType.equals(OFFLINE_CHANNEL)) {
                Map offMap = callOffineCardInfo(cardInfo, onlineOrders.get("userId"));
                if (!(boolean) offMap.get("bool")) {
                    return (Result) offMap.get("result");
                }
            }

            //上传部中心用户卡信息
            BaseUploadResponse response = uploadBasicCardInfo(cardInfo, onlineOrders);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传部中心数据异常:{}", serviceName, response);
                return Result.bizError(822, "上传部中心数据异常,stateCode:" + response.getStateCode() + " errorMsg:" + response.getErrorMsg());
            }

            boolean flag = false;

            String ids = "id,opTime";
            // 世纪恒通增加返回入库时间
            String crTime = DateUtil.formatDate(cardInfo.get("createTime"),DateUtil.FORMAT_YYYY_MM_DDTH_HMMSS) ;
            Map rtMap =new HashMap<>();
            rtMap.put("createDate",crTime);
            flag = Db.tx(() -> {
                //保存至卡信息表
                Db.delete(TABLE_CARDINFO, cardInfo);
                if (!Db.save(TABLE_CARDINFO, cardInfo)) {
                    logger.error("{}保存TABLE_CARDINFO表失败", serviceName);
                    return false;
                }
                if (!Db.save(TABLE_CARDINFO_HISTORY, ids, cardInfo)) {
                    logger.error("{}保存TABLE_CARDINFO_HISTORY表失败", serviceName);
                    return false;
                }

                //更新CardId
                onlineOrders.setCardId(cardInfo.get("id"));

                //判断订单是否处理完成
                if (isComplete(onlineOrders)) {
                    onlineOrders.setStatus(OrderProcessStatusEnum.PROCESSED.getValue());
                }
                if (!onlineOrders.update()) {
                    logger.error("{}保存onlineOrders表失败", serviceName);
                    return false;
                }
                return true;
            });

            if (flag) {
                logger.info("{}[orderId={}]订单接收卡信息成功", serviceName, orderId);
                return Result.success(rtMap);
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
     * 获取卡品牌
     * #捷德
     * card02=1!!!SLE77CLFX2407PM
     * #天喻
     * card01=3!!!P5CD041(天喻非接触式)
     *
     * @param cardId
     * @return
     */
    private int getCardBrand(String cardId) {
        int result = 0;
        if (StringUtil.isEmpty(cardId)) {
            return result;
        }
        String brand = cardId.substring(10, 12);

        String brandStr = SysConfig.getCardBrand(brand);
        if (StringUtil.isEmpty(brandStr)) {
            result = 3;
        } else {
            result = MathUtil.asInteger(brandStr.split(";;")[0]);
        }
       /* switch (brand) {
            //天喻
            case "01":
                result = 3;
                break;
            //捷德
            case "02":
                result = 1;
                break;
            //握奇
            case "03":
                result = 2;
                break;
            //航天信息
            case "04":
                result = 7;
                break;
            default:
                result = 3;
        }*/
        return result;
    }

    /**
     * 获取OBU型号
     * <p>
     * #捷德
     * card02=1!!!SLE77CLFX2407PM
     * #天喻
     * card01=3!!!P5CD041(天喻非接触式)
     *
     * @param obuId
     * @return
     */
    private String getCardModel(String obuId) {
        if (StringUtil.isEmpty(obuId)) {
            return "";
        }
        String brand = obuId.substring(10, 12);
        String brandStr = SysConfig.getCardBrand(brand);
        if (StringUtil.isEmpty(brandStr)) {
            brand = "P5CD041(天喻非接触式)";
        } else {
            brand = brandStr.split(";;")[1];
        }
      /*  switch (brand) {
            case "01":
                brand = "P5CD041(天喻非接触式)";
                break;
            case "02":
                brand = "SLE77CLFX2407PM";
                break;
            default:
                brand = "P5CD041(天喻非接触式)";
        }*/
        return brand;
    }

    /**
     * 判断订单是否处理完成
     * <p>
     * 1、如果是新增订单，
     * 则判断OBU是否已发行，如果已发行则订单处理完成
     * 2、如果是换卡订单
     * 则直接判断当前订单处理完成
     * 3、如果是换卡、换签全套订单
     * 则判断OBU是否已发行，如果已发行则订单处理完成
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
        } else if (OrderTypeEnum.CHANGE_CARD.getValue() == onlineOrders.getOrderType()) {
            isComplete = true;
        }
        return isComplete;
    }

    /**
     * 判断当前车辆在卡表中 是否存在operation !== 3 的数据
     *
     * @param vehicleId
     * @return
     */
    private String findCardIdByVehicleId(String vehicleId) {
        //查询发行系统
        Record cardRecord = queryIssueCardinfoByVehicleId(vehicleId);
        if (cardRecord == null) {
            //查询易构库
            cardRecord = queryYGCardinfoByVehicleId(vehicleId);
        }
        if (cardRecord == null) {

            //由于易构注销车辆方式，会导致汇聚平台中的卡签异常，无法为该车发行新卡签
//            cardRecord = queryHJPTCardinfoByVehicleId(vehicleId);
        }
        if (cardRecord == null) {
            return null;
        }
        return cardRecord.getStr("id");
    }

    /**
     * 根据车牌查询汇聚平台卡信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryHJPTCardinfoByVehicleId(String vehicleId) {
        return Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(DbUtil.getSql("queryHJPTCardinfoByVehicleId"), vehicleId);
    }

    /**
     * 根据车牌查询发行系统卡信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryIssueCardinfoByVehicleId(String vehicleId) {
        return Db.findFirst(DbUtil.getSql("queryIssueCardinfoByVehicleId"), vehicleId);
    }

    /**
     * 根据车牌查询汇聚平台卡信息
     *
     * @param vehicleId
     * @return
     */
    private Record queryYGCardinfoByVehicleId(String vehicleId) {
        return Db.use(CommonAttribute.DB_SESSION_YG).findFirst(DbUtil.getSql("queryYGCardinfoByVehicleId"), vehicleId);
    }

    /**
     * 上传用户卡信息到部中心
     *
     * @param cardInfo
     * @param onlineOrders
     * @return
     */
    private BaseUploadResponse uploadBasicCardInfo(Record cardInfo, OnlineOrders onlineOrders) {


        EtcCardinfoJson etcCardinfoJson = new EtcCardinfoJson();
        etcCardinfoJson._setOrPut(cardInfo.getColumns());
        BaseUploadResponse response = upload(etcCardinfoJson, BASIC_CARDUPLOAD_REQ);
        logger.info("{}上传用户卡响应信息:{}", serviceName, response);
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

        logger.info("{}保存发行卡异常信息到im_order表成功", serviceName);
    }

    /**
     * 线下相关接口上传部中心
     *
     * @param dataRC
     * @return
     */
    private Map callOffineCardInfo(Record dataRC, String userId) {

        Map outMap = new HashMap<>();
        outMap.put("bool", true);
        outMap.put("result", null);

        // 检查客户是否在部中心线下渠道开户
        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(userId);
        if (etcOflUserinfo != null) {

            // 获取车辆信息
            String vehicleId = dataRC.get("vehicleId");
            //查询当前车辆是否已经发行了OBU
            Record obuRecord = Db.findFirst(DbUtil.getSql("queryOBUidByVeh"), vehicleId);
            if (obuRecord == null) {
                logger.error("{}当前车辆还未发行OBU,请先发行OBU", serviceName);
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(ResponseStatusEnum.BIZ_ISSUER_ERROR.getCode(), "当前车辆还未发行OBU,请先发行OBU"));
                return outMap;
            }

            //刷新用户凭证
            Result result = oflAuthTouch(etcOflUserinfo);
            //判断刷新凭证是否成功，失败则直接退出
            if (!result.getSuccess()) {
                logger.error("{}[userId={}]刷新凭证异常:{}", serviceName, etcOflUserinfo.getUserId(), result);
                outMap.put("bool", true);
                outMap.put("result", result);
                return outMap;
            }

            EtcOflVehicleinfo etcOflVehicleinfo = EtcOflVehicleinfo.dao.findById(vehicleId);

            String[] sp = vehicleId.split("_");
            if (sp.length != 2) {
                outMap.put("bool", false);
                outMap.put("result", Result.bizError(704, "车牌异常"));
                logger.error("{}[vehicleId={}]车牌异常:",
                        serviceName, vehicleId);
                return outMap;
            }
            // 调用4.3卡信息新增及变更接口
            if (etcOflVehicleinfo.getDepVehicleId() != null) {
                // 保存issuerChannelId，世纪恒通的绑卡渠道
                dataRC.set("issuerChannelId",etcOflVehicleinfo.getIssuerChannelId());

                result = userCardinfoUploadService.entry(Kv.by("cardId", dataRC.get("id"))
                        .set("accessToken", etcOflUserinfo.getAccessToken())
                        .set("openId", etcOflUserinfo.getOpenId())
                        .set("accountId", etcOflUserinfo.getDepUserId())
                        //1- 新办
                        //3- 换卡
                        //4- 换卡签全套
                        //6- 卡补办
                        .set("type", 1)
                        .set("issuerId", CommonAttribute.ISSUER_CODE)
                        .set("vehicleId", etcOflVehicleinfo.getDepVehicleId())
                        .set("cardType", dataRC.get("cardType"))
                        .set("brand", dataRC.get("brand"))
                        .set("model", dataRC.get("model"))
                        .set("plateNum", sp[0])
                        .set("plateColor", Integer.parseInt(sp[1]))
                        .set("enableTime", dataRC.get("enableTime"))
                        .set("expireTime", dataRC.get("expireTime"))
                        .set("issueChannelType", 2)
                        .set("issueChannelId", dataRC.get("channelId")));


//                && !result.getMsg().contains(MSG_NORMAL) )
                if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                    logger.error("{}调用卡信息新增及变更通知失败:{}", serviceName, result);
//                    outMap.put("bool", false);
                    outMap.put("result", result);
                    //调用查询接口判断是否重复上传
                    result = userCardoflService.entry(Kv.by("accessToken", etcOflUserinfo.getAccessToken())
                            .set("openId", etcOflUserinfo.getOpenId())
                            .set("accountId", etcOflUserinfo.getDepUserId())
                            .set("obuId", obuRecord.get("id")));
                    if (!checkUserCardoflService(result, dataRC.get("id"))) {
                        logger.error("{}调用卡信息查询失败:{}", serviceName, result);
                        outMap.put("bool", false);
                    }

                    return outMap;
                }
            }

        }


        return outMap;
    }

    /**
     * 检查卡查询接口的结果
     *
     * @param result
     * @return
     */
    private boolean checkUserCardoflService(Result result, String cardId) {
        if (result.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            return false;
        } else {
            Map<String, Object> map = (Map<String, Object>) result.getData();
            logger.info("{}查询当前车辆的卡信息为:{}", serviceName, map);
            //1- 存在 2- 不存在
            if (map != null && 1 == MathUtil.asInteger(map.get("result"))) {
                List<Map> encryptedData = (List<Map>) map.get("encryptedData");
                for (Map data : encryptedData) {
                    String id = (String) data.get("id");
                    if (cardId.equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

package com.csnt.ins.bizmodule.base;


import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName BaseInfoReceiveService
 * @Description 4类基础信息接收
 * @Author duwanjiang
 * @Date 2019/6/20 0:19
 * Version 1.0
 **/
public class BaseInfoReceiveService implements IReceiveService {
    Logger logger = LoggerFactory.getLogger(BaseInfoReceiveService.class);
    private final String serviceName = "[4类基础信息接收]";

    private final String KEY_USERINFO = "userInfo";
    private final String KEY_VEHICLEINFO = "vehicleInfo";
    private final String KEY_CARDINFO = "cardInfo";
    private final String KEY_OBUINFO = "obuInfo";

    private final String MAP_KEY_PRIMARY_KEY = "primaryKey";
    private final String MAP_KEY_RECORD_LIST = "recordList";

    /**
     * ============================
     * 业务处理入口
     * ============================
     *
     * @param dataMap
     * @return
     */
    @Override
    public Result entry(Map dataMap) {
        logger.info("{}开始接收基础数据");
        long startTime = System.currentTimeMillis();

        Result finalResult;
        try {
            List<Map> users = (List) dataMap.get(KEY_USERINFO);
            List<Map> vehs = (List) dataMap.get(KEY_VEHICLEINFO);
            List<Map> cards = (List) dataMap.get(KEY_CARDINFO);
            List<Map> obus = (List) dataMap.get(KEY_OBUINFO);

            if (users == null || vehs == null || cards == null || obus == null) {
                logger.error("{}请求参数格式错误", serviceName);
                finalResult = Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM);
                return finalResult;
            }

            //对列表进行排序
            List<Map> userList = sortListACE(users);
            List<Map> vehList = sortListACE(vehs);
            List<Map> cardList = sortListACE(cards);
            List<Map> obuList = sortListACE(obus);

            // 取相应的组装历史信息
            Map userMap = getUserHistoryRecord(userList);
            Map vehMap = getVehicleHistoryRecord(vehList);
            Map cardMap = getCardHistoryRecord(cardList);
            Map obuMap = getObuHistoryRecord(obuList);

            boolean result = Db.tx(() -> {

                // 用户表相关操作
                List<Record> userLs = (List) userMap.get(MAP_KEY_RECORD_LIST);
                List<Object[]> userPs = (List) userMap.get(MAP_KEY_PRIMARY_KEY);

                if (userLs.size() > 0) {
                    //最终状态表
                    for (int i = 0; i < userLs.size(); i++) {
                        Record record = userLs.get(i);

                        Object[] param = new Object[1];
                        param[0] = record.get("id");
                        Db.delete(Db.getSql("mysql.deleteUserInfoByPK"), param);
                        Db.save(CommonAttribute.ETC_USERINFO, record);
                        logger.info("{}接收数据,user最终表新增{}", serviceName, param[0]);
                    }

                    // 插入历史表前先删除
                    Db.batch(Db.getSql("mysql.deleteUserHistoryInfoByPK"), userPs.toArray(new Object[][]{}), SysConfig.getCommitLimit());
                    int userCount = MathUtil.sumResultCount(Db.batchSave(CommonAttribute.ETC_USERINFO_HISTORY, userLs, SysConfig.getCommitLimit()));
                    logger.info("{}接收数据,user历史表新增条数{}，记录条数{}", serviceName, userCount, userLs.size());
                    if (userCount != userLs.size()) {
                        logger.error("{}接收数据,错误,user历史表新增条数{}，记录条数{}", serviceName, userCount, userLs.size());
                        return false;
                    }
                }

                // 车辆信息相关操作
                List<Record> vehLs = (List) vehMap.get(MAP_KEY_RECORD_LIST);
                List<Object[]> vehPs = (List) vehMap.get(MAP_KEY_PRIMARY_KEY);

                if (vehLs.size() > 0) {
                    //最终状态表
                    for (int i = 0; i < vehLs.size(); i++) {
                        Record record = vehLs.get(i);
                        Object[] param = new Object[1];
                        param[0] = record.get("id");
                        Db.delete(Db.getSql("mysql.deleteVehicleInfoByPK"), param);
                        Db.save(CommonAttribute.ETC_VEHICLEINFO, record);
                    }

                    // 插入历史表前先删除
                    Db.batch(Db.getSql("mysql.deleteVehicleHistoryInfoByPK"), vehPs.toArray(new Object[][]{}), SysConfig.getCommitLimit());
                    int userCount = MathUtil.sumResultCount(Db.batchSave(CommonAttribute.ETC_VEHICLEINFO_HISTORY, vehLs, SysConfig.getCommitLimit()));
                    logger.info("{}接收数据,veh历史表新增条数{}，记录条数{}", serviceName, userCount, vehLs.size());

                    if (userCount != vehList.size()) {
                        logger.error("{}接收数据,错误,veh历史表新增条数{}，记录条数{}", serviceName, userCount, vehLs.size());
                        return false;
                    }
                }

                // card信息相关操作
                List<Record> cardLs = (List) cardMap.get(MAP_KEY_RECORD_LIST);
                List<Object[]> cardPs = (List) cardMap.get(MAP_KEY_PRIMARY_KEY);

                if (cardLs.size() > 0) {
                    //最终状态表
                    for (int i = 0; i < cardLs.size(); i++) {
                        Record record = cardLs.get(i);
                        Object[] param = new Object[1];
                        param[0] = record.get("id");
                        Db.delete(Db.getSql("mysql.deleteCardInfoByPk"), param);
                        // 取卡的区域信息
                        Map mp = getPosType(record.get("id"), record.get("channelId"));
                        record.set("posType", mp.get("posType"));
                        record.set("areaType", mp.get("areaType"));
                        Db.save(CommonAttribute.ETC_CARDINFO, record);
                    }

                    // 插入历史表前先删除
                    Db.batch(Db.getSql("mysql.deleteCardHistoryInfoByPk"), cardPs.toArray(new Object[][]{}), SysConfig.getCommitLimit());
                    int userCount = MathUtil.sumResultCount(Db.batchSave(CommonAttribute.ETC_CARDINFO_HISTORY, cardLs, SysConfig.getCommitLimit()));
                    logger.info("{}接收数据,card历史表新增条数{}，记录条数{}", serviceName, userCount, cardLs.size());

                    if (userCount != cardList.size()) {
                        logger.error("{}接收数据,错误,card历史表新增条数{}，记录条数{}", serviceName, userCount, cardLs.size());
                        return false;
                    }
                }

                // obu信息相关操作
                List<Record> obuLs = (List) obuMap.get(MAP_KEY_RECORD_LIST);
                List<Object[]> obuPs = (List) obuMap.get(MAP_KEY_PRIMARY_KEY);

                if (obuLs.size() > 0) {
                    //最终状态表
                    for (int i = 0; i < obuLs.size(); i++) {
                        Record record = obuLs.get(i);
                        Object[] param = new Object[1];
                        param[0] = record.get("id");
                        Db.delete(Db.getSql("mysql.deleteObuInfoByPk"), param);
                        Db.save(CommonAttribute.ETC_OBUINFO, record);
                    }

                    // 插入历史表前先删除
                    Db.batch(Db.getSql("mysql.deleteObuHistoryInfoByPk"), obuPs.toArray(new Object[][]{}), SysConfig.getCommitLimit());
                    int userCount = MathUtil.sumResultCount(Db.batchSave(CommonAttribute.ETC_OBUINFO_HISTORY, obuLs, SysConfig.getCommitLimit()));
                    logger.info("{}接收数据,obu历史表新增条数{}，记录条数{}", serviceName, userCount, obuLs.size());

                    if (userCount != obuList.size()) {
                        logger.error("{}接收数据,错误,obu历史表新增条数{}，记录条数{}", serviceName, userCount, obuLs.size());
                        return false;
                    }
                }
                return true;
            });
            logger.info("{}接收数据,是否成功：{}", serviceName, result);

            if (result) {
                finalResult = Result.success(null);
            } else {
                finalResult = Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
            }
        } catch (Exception e) {
            logger.error("{}数据入库失败:{}", serviceName, e.toString(), e);
            finalResult = Result.byEnum(ResponseStatusEnum.SYS_DB_SAVE_ERROR);
        }

        logger.info("{}接收基础数据完成,耗时[{}]", serviceName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return finalResult;
    }

    /**
     * 对类表根据操作时间进行排序
     *
     * @param list
     * @return
     */
    private List<Map> sortListACE(List<Map> list) {
        return list.stream().sorted((x, y) -> {
            if (x.get("opTime") != null && y.get("opTime") != null) {
                Date xDate = DateUtil.parseDate(String.valueOf(x.get("opTime")), DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS);
                Date yDate = DateUtil.parseDate(String.valueOf(y.get("opTime")), DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS);
                return xDate.compareTo(yDate);
            } else {
                return 1;
            }
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户recordlist
     *
     * @return
     */
    private Map getUserHistoryRecord(List<Map> userList) {

        Map outMap = new HashMap<>();
        List<Object[]> userParams = new ArrayList<>();
        List<Record> recordList = new ArrayList<>();

        if (userList.size() > 0) {
            for (Map map : userList) {
                Object[] param = new Object[2];
                param[0] = map.get("id");
                param[1] = map.get("opTime");
                userParams.add(param);
                recordList.add(userMapToRecord(map));
            }
        }
        outMap.put(MAP_KEY_PRIMARY_KEY, userParams);
        outMap.put(MAP_KEY_RECORD_LIST, recordList);
        return outMap;
    }

    ;

    /**
     * 组装插入的车辆历史信息及插入前删除相应的历史信息
     *
     * @return
     */
    private Map getVehicleHistoryRecord(List<Map> vehList) {

        Map outMap = new HashMap<>();
        List<Object[]> userParams = new ArrayList<>();
        List<Record> recordList = new ArrayList<>();

        if (vehList.size() > 0) {
            for (Map map : vehList) {
                Object[] param = new Object[2];
                param[0] = map.get("id");
                param[1] = map.get("opTime");
                userParams.add(param);
                recordList.add(vehMapToRecord(map));
            }
        }
        outMap.put(MAP_KEY_PRIMARY_KEY, userParams);
        outMap.put(MAP_KEY_RECORD_LIST, recordList);
        return outMap;
    }

    ;

    /**
     * 组装插入的卡历史信息及插入前删除相应的历史信息
     *
     * @return
     */
    private Map getCardHistoryRecord(List<Map> cardList) {

        Map outMap = new HashMap<>();
        List<Object[]> userParams = new ArrayList<>();
        List<Record> recordList = new ArrayList<>();

        if (cardList.size() > 0) {
            for (Map map : cardList) {
                Object[] param = new Object[2];
                param[0] = map.get("id");
                param[1] = map.get("opTime");
                userParams.add(param);
                recordList.add(cardMapToRecord(map));
            }
        }
        outMap.put(MAP_KEY_PRIMARY_KEY, userParams);
        outMap.put(MAP_KEY_RECORD_LIST, recordList);
        return outMap;
    }

    ;

    /**
     * 组装插入的obu历史信息及插入前删除相应的历史信息
     *
     * @return
     */
    private Map getObuHistoryRecord(List<Map> obuList) {

        Map outMap = new HashMap<>();
        List<Object[]> userParams = new ArrayList<>();
        List<Record> recordList = new ArrayList<>();

        if (obuList.size() > 0) {
            for (Map map : obuList) {
                Object[] param = new Object[2];
                param[0] = map.get("id");
                param[1] = map.get("opTime");
                userParams.add(param);
                recordList.add(obuMapToRecord(map));
            }
        }
        outMap.put(MAP_KEY_PRIMARY_KEY, userParams);
        outMap.put(MAP_KEY_RECORD_LIST, recordList);
        return outMap;
    }

    ;

    /**
     * map 转换为用户信息
     *
     * @param map
     * @return
     */
    private Record userMapToRecord(Map map) {

        Record record = new Record();
        //客户编号
        record.set("id", map.get("id"));
        //客户类型1-个人，2-单位
        record.set("userType", map.get("userType"));
        //开户人名称
        record.set("userName", map.get("userName"));
        //开户人证件类型
        record.set("userIdType", map.get("userIdType"));
        //开户人证件号
        record.set("userIdNum", map.get("userIdNum"));
        //开户人/指定经办人电号码
        record.set("tel", map.get("tel"));
        //开户人地址
        record.set("address", map.get("address"));
        //开户方式
        record.set("registeredType", map.get("registeredType"));
        //开户渠道编号
        record.set("channelId", map.get("channelId"));
        //开户渠道编号
        record.set("registeredTime", map.get("registeredTime"));
        //部门/分支机构名称
        record.set("department", map.get("department"));
        //指定经办人姓名
        record.set("agentName", map.get("agentName"));
        //指定经办人证件类型
        record.set("agentIdType", map.get("agentIdType"));
        //指定经办人证件号
        record.set("agentIdNum", map.get("agentIdNum"));
        //单位开户行
        record.set("bank", map.get("bank"));
        //单位开户行地址
        record.set("bankAddr", map.get("bankAddr"));
        //单位开户行账号
        record.set("bankAccount", map.get("bankAccount"));
        //单位纳税人识别号
        record.set("taxpayerCode", map.get("taxpayerCode"));
        //客户状态
        record.set("status", map.get("status"));
        //客户状态变更时间YYYY-MM-DDTHH:mm:ss
        record.set("statusChangeTime", map.get("statusChangeTime"));
        //人脸特征版本号
        record.set("faceFeatureVersion", map.get("faceFeatureVersion"));
        //人脸特征码
        record.set("faceFeatureCode", map.get("faceFeatureCode"));
        //操作1- 新增2- 变更 3- 删除
        record.set("operation", map.get("operation"));
        //渠道类型010001快发网点 020001手持机网点
        record.set("channelType", map.get("channelType"));
        //是否需要上传部中心 0-不需要，1-需要
        record.set("needupload", map.get("needupload"));
        //信息录入网点id
        record.set("orgId", map.get("orgId"));
        //信息录入人工号
        record.set("operatorId", map.get("operatorId"));
        //操作时间
        record.set("opTime", map.get("opTime"));

        //数据是否可上传状态 2-可上传部中心
        record.set("uploadStatus", 2);
        //入库时间
        record.set("createTime", new Date());
        //更新时间
        record.set("updateTime", new Date());

        return record;
    }

    /**
     * map 转换为车辆信息
     *
     * @return
     */
    private Record vehMapToRecord(Map map) {
        Record record = new Record();
        //车辆编号
        record.set("id", map.get("id"));
        //收费车型
        record.set("type", map.get("type"));
        //所属客户编号
        record.set("userId", map.get("userId"));
        //机动车所有人名称
        record.set("ownerName", map.get("ownerName"));
        //机动车所有人证件类型
        record.set("ownerIdType", map.get("ownerIdType"));
        //机动车所有人证件号码
        record.set("ownerIdNum", map.get("ownerIdNum"));
        //所有人联系
        record.set("ownerTel", map.get("ownerTel"));
        //所有人联系地址
        record.set("address", map.get("address"));
        //指定联系人姓名
        record.set("contact", map.get("contact"));
        //录入方式 1-线上，2-线下
        record.set("registeredType", map.get("registeredType"));
        //录入渠道编号
        record.set("channelId", map.get("channelId"));
        //录入时间YYYY-MM-DDTHH:mm:ss
        record.set("registeredTime", map.get("registeredTime"));
        //行驶证车辆类型
        record.set("vehicleType", map.get("vehicleType"));
        //行驶证品牌型号
        record.set("vehicleModel", map.get("vehicleModel"));
        //车辆使用性质
        record.set("useCharacter", map.get("useCharacter"));
        //车辆识别代号
        record.set("VIN", map.get("VIN"));
        //车辆发动机号
        record.set("engineNum", map.get("engineNum"));
        //注册日期YYYY-MM-DD
        record.set("registerDate", map.get("registerDate"));
        //发证日期YYYY-MM-DD
        record.set("issueDate", map.get("issueDate"));
        //档案编号
        record.set("fileNum", map.get("fileNum"));
        //核定载人数
        record.set("approvedCount", map.get("approvedCount"));
        //总质量
        record.set("totalMass", map.get("totalMass"));
        //整备质量
        record.set("maintenanceMass", map.get("maintenanceMass"));
        //核定载质量
        record.set("permittedWeight", map.get("permittedWeight"));
        //外廓尺寸
        record.set("outsideDimensions", map.get("outsideDimensions"));
        //准牵引总质量
        record.set("permittedTowWeight", map.get("permittedTowWeight"));
        //检验记录
        record.set("testRecord", map.get("testRecord"));
        //车轮数
        record.set("wheelCount", map.get("wheelCount"));
        //车轴数
        record.set("axleCount", map.get("axleCount"));
        //轴距
        record.set("axleDistance", map.get("axleDistance"));
        //轴型
        record.set("axisType", map.get("axisType"));
        //车脸识别特征版本号
        record.set("vehicleFeatureVersion", map.get("vehicleFeatureVersion"));
        //车脸识别特征码
        record.set("vehicleFeatureCode", map.get("vehicleFeatureCode"));
        //预付费/代扣账户编码
        record.set("payAccountNum", map.get("payAccountNum"));
        //操作1- 新增2- 变更3- 删除
        record.set("operation", map.get("operation"));
        //渠道类型 010001快发网点 020001手持机网点
        record.set("channelType", map.get("channelType"));
        //信息录入网点id
        record.set("orgId", map.get("orgId"));
        //信息录入人工号
        record.set("operatorId", map.get("operatorId"));
        //操作时间
        record.set("opTime", map.get("opTime"));
        //数据是否可上传状态2-可上传部中心
        record.set("uploadStatus", 2);
        //入库时间
        record.set("createTime", new Date());
        //更新时间
        record.set("updateTime", new Date());

        return record;
    }

    /**
     * map 转换为card信息
     *
     * @return
     */
    private Record cardMapToRecord(Map map) {
        Record record = new Record();
        //用户卡编号
        record.set("id", map.get("id"));
        //卡类型
        record.set("cardType", map.get("cardType"));
        //卡品牌
        record.set("brand", map.get("brand"));
        //卡型号
        record.set("model", map.get("model"));
        //客服合作机构编号
        record.set("agencyId", map.get("agencyId"));
        //客户编号
        record.set("userId", map.get("userId"));
        //车辆编号
        record.set("vehicleId", map.get("vehicleId"));
        //卡启用时间YYYY-MM-DDTHH:mm:ss
        record.set("enableTime", map.get("enableTime"));
        //卡到期时间YYYY-MM-DDTHH:mm:ss
        record.set("expireTime", map.get("expireTime"));
        //开卡方式1-线上，2-线下
        record.set("issuedType", map.get("issuedType"));
        //开卡渠道编号
        record.set("channelId", map.get("channelId"));
        //开卡时间YYYY-MM-DDTHH:mm:ss
        record.set("issuedTime", map.get("issuedTime"));
        //用户卡状态
        record.set("status", map.get("status"));
        //用户卡状态变更时间
        record.set("statusChangeTime", map.get("statusChangeTime"));
        //操作1- 新增 2- 变更 3- 删除
        record.set("operation", map.get("operation"));
        //个人或单位银行卡号或账号
        record.set("accountid", map.get("accountid"));
        //银行预留手机号
        record.set("linkmobile", map.get("linkmobile"));
        //银行账户名称
        record.set("bankusername", map.get("bankusername"));
        //银行卡绑定用户身份证号
        record.set("certsn", map.get("certsn"));
        //网点编号
        record.set("posid", map.get("posid"));
        //银行绑卡请求日期时间
        record.set("gentime", map.get("gentime"));
        //银行绑卡9902请求流水号
        record.set("trx_serno", map.get("trx_serno"));
        //员工推荐人工号
        record.set("employeeid", map.get("employeeid"));
        //银行验证9901请求流水号
        record.set("org_trx_serno", map.get("org_trx_serno"));
        //绑定银行账户类型
        record.set("acc_type", map.get("acc_type"));
        //银行编码63010102001	中国工商银行
        //63010102002	中国建设银行
        //63010102003	中国银行青海省分行
        //63010102004	中国农业银行
        //63010102006	中国邮政储蓄银行
        //63010102033	青海银行
        record.set("bankPost", map.get("bankPost"));
        //渠道类型010001快发网点 020001手持机网点
        record.set("channelType", map.get("channelType"));
        //信息录入网点id
        record.set("orgId", map.get("orgId"));
        //信息录入人工号
        record.set("operatorId", map.get("operatorId"));
        //操作时间
        record.set("opTime", map.get("opTime"));
        //数据是否可上传状态
        record.set("uploadStatus", 2);
        //入库时间
        record.set("createTime", new Date());
        //更新时间
        record.set("updateTime", new Date());

        // 取卡的区域信息
        Map mp = getPosType(record.get("id"), record.get("channelId"));
        record.set("posType", mp.get("posType"));
        record.set("areaType", mp.get("areaType"));


        return record;
    }

    /**
     * map 转换为OBU信息
     *
     * @return
     */
    private Record obuMapToRecord(Map map) {
        Record record = new Record();
        //OBU 序号编码
        record.set("id", map.get("id"));
        //OBU 品牌
        record.set("brand", map.get("brand"));
        //OBU 型号
        record.set("model", map.get("model"));
        //OBU 单/双片标识
        record.set("obuSign", map.get("obuSign"));
        //客户编号
        record.set("userId", map.get("userId"));
        //车辆编号
        record.set("vehicleId", map.get("vehicleId"));
        //OBU 启用时间YYYY-MM-DDTHH:mm:ss
        record.set("enableTime", map.get("enableTime"));
        //OBU 到期时间YYYY-MM-DD
        record.set("expireTime", map.get("expireTime"));
        //OBU 注册方式1-线上，2-线下
        record.set("registeredType", map.get("registeredType"));
        //OBU 注册渠道编号
        record.set("registeredChannelId", map.get("registeredChannelId"));
        //OBU 注册时间
        record.set("registeredTime", map.get("registeredTime"));
        //OBU 安装方式
        record.set("installType", map.get("installType"));
        //OBU安装/激活地点
        record.set("installChannelId", map.get("installChannelId"));
        //OBU安装/激活时间
        record.set("installTime", map.get("installTime"));
        //OBU 状态
        record.set("status", map.get("status"));
        //OBU 状态变更时间
        record.set("statusChangeTime", map.get("statusChangeTime"));
        //操作1-新增 2-变更 3-删除
        record.set("operation", map.get("operation"));
        //渠道类型010001快发网点 020001手持机网点
        record.set("channelType", map.get("channelType"));
        //信息录入网点id
        record.set("orgId", map.get("orgId"));
        //信息录入人工号
        record.set("operatorId", map.get("operatorId"));
        //操作时间
        record.set("opTime", map.get("opTime"));
        //数据是否可上传状态
        record.set("uploadStatus", 2);
        //入库时间
        record.set("createTime", new Date());
        //更新时间
        record.set("updateTime", new Date());

        return record;
    }

    // 根据卡号取postype,areatype
    public Map getPosType(String cardId, String channlId) {

        Map outMap = new HashMap<>();
        outMap.put("posType", null);
        outMap.put("areaType", null);
        // 判断卡是否存在
        Object[] param = new Object[1];
        param[0] = cardId;
        Record rc = Db.findFirst(Db.getSql("mysql.selectCardInfoByPk"), param);
        if (rc != null) {
            // 该卡已经存在
            outMap.put("posType", rc.get("posType"));
            outMap.put("areaType", rc.get("areaType"));
            return outMap;
        }
        // 如果不存在，则到网点信息表查找区域信息
        Object[] param1 = new Object[1];
        param1[0] = channlId;
        Record posRc = Db.findFirst(Db.getSql("mysql.selectPositioninfoByCode"), param1);
        if (posRc != null) {
            // 该卡已经存在
            outMap.put("posType", posRc.get("posType"));
            outMap.put("areaType", posRc.get("areaType"));
            return outMap;
        }

        return outMap;
    }
}

package com.csnt.ins.bizmodule.order.queryuserid;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @ClassName GenerateUserIdService
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/28 15:30
 * Version 1.0
 **/
public class GenerateUserIdService {
    Logger logger = LoggerFactory.getLogger(GenerateUserIdService.class);

    private final String serverName = "[用户编码生成]";

    private final String MSG_CODE = "1010";

    private final String TABLE_ETC_USERIDTEMP = "etc_userid_temp";


    private final String KEY_USERIDTYPE = "useridtype";
    private final String KEY_USERIDNUM = "useridnum";
    private final String KEY_USERID = "id";
    private final String KEY_ISNEW = "isnew";
    private final String KEY_NEEDUPLOAD = "needupload";
    private final String KEY_MSG = "msg";


    /**
     * ============================
     * 创建用户编码
     * ============================
     * 返回示例：
     * {
     * "msg": "成功",
     * "useridnum": "630103197612110812",
     * "userid": "63010119062952296",
     * "useridtype": "113",
     * "needupload": 0,
     * "isnew": 0
     * }
     */
    public synchronized Map createUserId(String userIdType, String userIdNum) {
        logger.info("{}开始创建用户编码[userIdType={},userIdNum={}]", serverName, userIdType, userIdNum);
        long startTime = System.currentTimeMillis();
        Map<String, Object> userMap = new HashMap<>(16);
        //1-是新增客编 0-老客编
        int isNew = 0;
        //证件类型
        userMap.put(KEY_USERIDTYPE, userIdType);
        //证件号码
        userMap.put(KEY_USERIDNUM, userIdNum);
        String userId = "";
        //判断userIdType和userIdNum
        if (StringUtil.isEmpty(userIdType) || StringUtil.isEmpty(userIdNum)) {
            userMap.put(KEY_MSG, "证件号码和类型信息不能为空");
            return userMap;
        }

        try {
            //1.查询客户编码是否存在
            userId = queryUserIdByUserIdNum(userIdType, userIdNum);
            if (StringUtil.isEmpty(userId)) {
                //查询营改增之前的数据是否存在
                userId = queryUserIdByUserIdNum(matchOldUserIdType(userIdType), userIdNum);
                if (StringUtil.isEmpty(userId)) {
                    userId = queryYGUserIdByUserIdNum(userIdType, userIdNum);
                    if (StringUtil.isEmpty(userId)) {
                        //查询营改增之前的数据是否存在
                        userId = queryYGUserIdByUserIdNum(matchOldUserIdType(userIdType), userIdNum);
                        if (StringUtil.isEmpty(userId)) {
                            //当易构和中海的正式用户表都查询不到表示新用户
                            //1-是新增客编 2-老客编
                            isNew = 1;
                            userId = queryUserIdTempByUserIdNum(userIdType, userIdNum);
                            if (StringUtil.isEmpty(userId)) {
                                //2.生成客户编码
                                userId = generateUserId();
                                //将userId存入userIdTemp表
                                saveUserIdToTemp(userId, userIdType, userIdNum);
                            }
                        }

                    }
                }
            }

            userMap.put(KEY_USERID, userId);

            userMap.put(KEY_ISNEW, isNew);
            //1-需要上传部中心 2-不需要上传部中心
            userMap.put(KEY_NEEDUPLOAD, isNew);
        } catch (Exception e) {
            logger.error("{}创建用户编码失败:{}", serverName, e.toString(), e);
            userMap.put(KEY_MSG, "证件号码和类型信息异常");
            return userMap;
        }
        userMap.put(KEY_MSG, "成功");
        logger.info("{}创建用户编码完成,耗时[{}]", serverName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return userMap;
    }

    /**
     * 查询用户编码
     *
     * @return
     */
    private String generateUserId() {
        String userId = "";
        boolean flag = true;
        int count = 0;
        while (flag) {
            count++;
            userId = generateNewUserId();
            //检查当前userId是否已经存在,不存在则返回，如果存在就一直产生id
            if (StringUtil.isEmpty(queryUserIdById(userId))
                    && StringUtil.isEmpty(queryYGUserIdById(userId))
                    && StringUtil.isEmpty(queryUserIdTempById(userId))) {
                flag = false;
            }
        }
        logger.info("{}创建用户编码循环执行次数[{}]", serverName, count);

        return userId;
    }

    /**
     * 生成新的用户id
     *
     * @return
     */
    private String generateNewUserId() {
        //产生5位随机数
        int random = new Random().nextInt(99999);
        return String.format("%s%s%05d", CommonAttribute.ISSUER_CODE, DateKit.toStr(new Date(), DateUtil.FORMAT_YYM_MDD), random);
    }

    /**
     * 将新生产的用户编码存入临时表
     *
     * @param userId
     * @param userIdType
     * @param userIdNum
     */
    private void saveUserIdToTemp(String userId, String userIdType, String userIdNum) {
        Record record = new Record();
        record.set("id", userId);
        record.set("userIdType", userIdType);
        record.set("userIdNum", userIdNum);
        Db.save(TABLE_ETC_USERIDTEMP, record);
    }

    /**
     * 根据证件号查用户编码
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    public String queryUserIdByUserIdNum(String userIdType, String userIdNum) {
        Kv kv = new Kv().set("userIdType", userIdType).set("userIdNum", userIdNum);
        SqlPara sqlPara = Db.getSqlPara("mysql.findUserIdByUserIdNum", kv);
        Record record = Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
        //对operator=3进行判断，如果是3标识不存在
        if (record != null && !"3".equals(record.getStr("operation"))) {
            return record.get("id");
        }
        return null;
    }

    /**
     * 根据证件号查用户编码temp
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    private String queryUserIdTempByUserIdNum(String userIdType, String userIdNum) {
        Kv kv = new Kv().set("userIdType", userIdType).set("userIdNum", userIdNum);
        SqlPara sqlPara = Db.getSqlPara("mysql.findUserIdTempByUserIdNum", kv);
        Record record = Db.findFirst(sqlPara);
        return record == null ? null : record.get("id");
    }

    /**
     * 根据证件号查易构用户编码
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    private String queryYGUserIdByUserIdNum(String userIdType, String userIdNum) {
        Kv kv = new Kv().set("userIdType", userIdType).set("userIdNum", userIdNum);
        SqlPara sqlPara = Db.getSqlPara("mysql.findYGUserUserIdByUserIdNum", kv);
        Record record = Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
        return record == null ? null : record.get("id");
    }

    /**
     * 根据用户id查用户编码
     *
     * @param id
     * @return
     */
    private String queryUserIdById(String id) {
        Kv kv = new Kv().set("id", id);
        SqlPara sqlPara = Db.getSqlPara("mysql.findUserIdById", kv);
        Record record = Db.use(CommonAttribute.DB_SESSION_HJPT).findFirst(sqlPara);
        return record == null ? null : record.get("id");
    }

    /**
     * 根据用户id查用户编码temp
     *
     * @param id
     * @return
     */
    private String queryUserIdTempById(String id) {
        Kv kv = new Kv().set("id", id);
        SqlPara sqlPara = Db.getSqlPara("mysql.findUserIdTempById", kv);
        Record record = Db.findFirst(sqlPara);
        return record == null ? null : record.get("id");
    }

    /**
     * 根据用户id查易构用户编码
     *
     * @param id
     * @return
     */
    private String queryYGUserIdById(String id) {
        Kv kv = new Kv().set("id", id);
        SqlPara sqlPara = Db.getSqlPara("mysql.findYGUserIdById", kv);
        Record record = Db.use(CommonAttribute.DB_SESSION_YG).findFirst(sqlPara);
        return record == null ? null : record.get("id");
    }

    /**
     * 将用户证件类型转为营改增之前的类型
     *
     * @param userIdType
     * @return
     */
    private String matchOldUserIdType(String userIdType) {
        String oldUserIdType;
        switch (userIdType) {
            //身份证
            case "101":
                oldUserIdType = "199";
                break;
            case "102":
                oldUserIdType = "199";
                break;
            case "103":
                oldUserIdType = "199";
                break;
            case "104":
                oldUserIdType = "199";
                break;
            case "105":
                oldUserIdType = "199";
                break;
            case "106":
                oldUserIdType = "199";
                break;
            case "113":
                oldUserIdType = "199";
                break;
            case "114":
                oldUserIdType = "199";
                break;
            // 统一社会信用代码证书
            case "201":
                oldUserIdType = "299";
                break;
            case "202":
                oldUserIdType = "299";
                break;
            case "203":
                oldUserIdType = "299";
                break;
            case "204":
                oldUserIdType = "299";
                break;
            case "205":
                oldUserIdType = "299";
                break;
            case "206":
                oldUserIdType = "299";
                break;
            case "217":
                oldUserIdType = "299";
                break;
            case "218":
                oldUserIdType = "299";
                break;
            case "219":
                oldUserIdType = "299";
                break;
            default:
                oldUserIdType = "199";
        }
        return oldUserIdType;
    }

    /**
     * 向查客编服务查询客编
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    public Map postQueryUserIdServer(String userIdType, String userIdNum) {
        Map map = new HashMap();
        map = createUserId(userIdType, userIdNum);
//        try {
//            Map<String, String> headers = new HashMap();
//            headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1010");
//            map.put("userIdType", userIdType);
//            map.put("userIdNum", userIdNum);
//            String data = FastJson.getJson().toJson(map);
//            headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
//
//            String outData = HttpKit.post(SysConfig.getQueryUserIdUrl(), data, headers);
//            map = FastJson.getJson().parse(outData, Map.class);
//            map = (Map) map.get("data");
//        } catch (Exception e) {
//            logger.error("{}查询客编异常:{}", serverName, e.toString(), e);
//        }
        return map;
    }



    /**
     * 根据证件号查用户编码
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    public String queryUserIdCenterByUserIdNum(String userIdType, String userIdNum) {
        Kv kv = new Kv().set("userIdType", userIdType).set("userIdNum", userIdNum);
//        SqlPara sqlPara = Db.getSqlPara("mysql.findUserIdByUserIdNum", kv);
        Record record =Db.findFirst(Db.getSqlPara("mysql.findUserIdCenterByUserIdNum", kv));
//        if (record != null && !"3".equals(record.getStr("operation"))) {
//            return record.get("id");
//        }
        return record == null ? null : record.get("id");
    }

}

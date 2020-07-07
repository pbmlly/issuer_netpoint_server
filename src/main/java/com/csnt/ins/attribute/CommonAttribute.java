package com.csnt.ins.attribute;

import com.csnt.ins.utils.SysConfig;

/**
 * @ClassName CommonAttribute
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/15 21:07
 * Version 1.0
 **/
public class CommonAttribute {

    /**
     * 连接发行系统的数据库session名
     */
    public static final String DB_SESSION_ISSUER = "issuer";
    /**
     * 连接汇聚平台系统的数据库session名
     */
    public static final String DB_SESSION_HJPT = "hjpt";
    /**
     * 连接易构系统的数据库session名
     */
    public static final String DB_SESSION_YG = "yg";
    /**
     * 连接省中心车管所数据库session名
     */
    public static final String DB_SESSION_GGSJTS = "ggsjts";
    /**
     * 查询数据条数限制
     */
    public static final String QUERY_LIMIT = "limit";

    /**
     * 发行方类型 中海
     */
    public static final int ISSUER_TYPE_CSNT = 1;
    /**
     * 发行方类型 易构
     */
    public static final int ISSUER_TYPE_EG = 0;
    /**
     * 发行方类型 即不是易构也不是中海
     */
    public static final int ISSUER_TYPE_NULL = -1;

    /**
     * 用户历史表名
     */
    public static final String ETC_USERINFO_HISTORY = "Etc_UserInfo_History";
    /**
     * 用户表名
     */
    public static final String ETC_USERINFO = "Etc_UserInfo";

    /**
     * 车辆历史表名
     */
    public static final String ETC_VEHICLEINFO_HISTORY = "Etc_VehicleInfo_History";
    /**
     * 车辆表名
     */
    public static final String ETC_VEHICLEINFO = "Etc_VehicleInfo";
    /**
     * 订单表
     */
    public static final String ONLINE_ORDERS = "online_orders";
    /**
     * card历史表名
     */
    public static final String ETC_CARDINFO_HISTORY = "Etc_CardInfo_History";
    /**
     * card表名
     */
    public static final String ETC_CARDINFO = "Etc_CardInfo";

    /**
     * obu历史表名
     */
    public static final String ETC_OBUINFO_HISTORY = "Etc_ObuInfo_History";
    /**
     * obu表名
     */
    public static final String ETC_OBUINFO = "Etc_ObuInfo";

    /**
     * http PARAM username
     */
    public static final String HTTP_PARAM_USERNAME = "username";
    /**
     * http PARAM password
     */
    public static final String HTTP_PARAM_PASSWORD = "password";
    /**
     * http header username
     */
    public static final String HTTP_HEADER_USERID = "userId";
    /**
     * http header password
     */
    public static final String HTTP_HEADER_AUTHORIZATION = "authorization";
    /**
     * http header md5
     */
    public static final String HTTP_HEADER_MD5 = "md5";
    /**
     * http header msgtype
     */
    public static final String HTTP_HEADER_MSGTYPE = "msgtype";

    public static final String VEHCILE_TYPE_TRUCK = "11";

    // 正常
    public static final int CUSTOMER_STATUS_NORMAL = 1;

    // 线上
    public static final int REGISTEREDTYPE_ONLINE = 1;

    // 订单类型 OBU
    public static final int ORDERTYPE_OBU = 1;

    // 邮寄类型
    public static final int POSTSTATUS_UNMAILED = 1;

    /**
     * 请求发行监控的sender
     */
    public static final String ISSUER_ISS_SENDER = SysConfig.getGatherCode() + "010101";
    /**
     * 发行方编码
     */
    public static final String ISSUER_CODE = SysConfig.getGatherCode() + "0101";
    /**
     * 发行方网点编码
     */
    public static final String ISSUER_NETID = SysConfig.getGatherCode() + "01";

    /**
     * 互联网总行发行渠道前缀
     */
    public static final String ISSUER_CHANNEL_PRE_TOTAL_BANK = "041";

    //==========================缓存名============================//
    /**
     * 缓存名24Hour
     */
    public static final String CACHE_NAME_24HOUR = "24Hour";
    /**
     * 缓存名twoHour
     */
    public static final String CACHE_NAME_TWOHOUR = "twoHour";
    /**
     * 缓存名halfHour
     */
    public static final String CACHE_NAME_HALFHOUR = "halfHour";

}

package com.csnt.ins.utils;

import com.jfinal.kit.Prop;

/**
 * 系统配置对象
 *
 * @author source
 * @date 2019/6/15
 */
public class SysConfig {
    public final static Prop CONFIG = new Prop("config.properties");
    public final static Prop CONNECT = new Prop("connect.properties");
    public final static Prop SDK = new Prop("sdk.properties");
    public final static Prop BRAND = new Prop("brand.properties");
    /**
     * 查询流水条数限制
     *
     * @return
     */
    public static int getQueryLimit(){
        return CONFIG.getInt("query.limit");
    }
    /**
     * 获取图片大小限制(单位：KB)
     *
     * @return
     */
    public static int getPictureSizeLimit(){
        return CONFIG.getInt("picture.size.limit",200);
    }
    /**
     * 提交流水条数限制
     *
     * @return
     */
    public static int getCommitLimit(){
        return CONFIG.getInt("commit.limit");
    }

    /**
     * 获取 jwt的请求头
     * @return
     */
    public static String JwtSecret() {
        return CONFIG.get("jwt.secret","csnt6p");
    }

    /**
     * 获取jwt超时时间
     * @return
     */
    public static Long getJwtExpiration() {
        return CONFIG.getLong("jwt.expiration",86400L);
    }

    /**
     * 是否使用乐观锁
     * @return
     */
    public static boolean UseOptimisticLock() {
        return CONFIG.getBoolean("db.optimisticlock",true);
    }


    //================================= connect.properties =================================//
    /**
     * 获取查客编地址
     *
     * @return
     */
    public static String getQueryUserIdUrl() {
        return CONNECT.get("query.userid.url","");
    }

    /**
     * 获取请求省中心编码
     *
     * @return
     */
    public static String getGatherCode() {
        return CONNECT.get("gather.id","63");
    }

    /**
     * 获取汇聚平台用户名
     *
     * @return
     */
    public static String getAccount() {
        return CONNECT.get("account","root");
    }

    /**
     * 获取汇聚平台密码
     *
     * @return
     */
    public static String getPassword() {
        return CONNECT.get("password","202cb962ac59075b964b07152d234b70");
    }

    /**
     * 获取汇聚平台ip
     *
     * @return
     */
    public static String getConnectIp() {
        return CONNECT.get("connect.ip","");
    }
    /**
     * 获取汇聚平台端口
     *
     * @return
     */
    public static String getConnectPort() {
        return CONNECT.get("connect.port","");
    }

    /**
     * 获取请求汇聚平台超时时间
     *
     * @return
     */
    public static int getRequestTimeout() {
        return CONNECT.getInt("request.timeout",60000);
    }

    //================================= sdk.properties =================================//

    /**
     * 获取本省私钥
     *
     * @return
     */
    public static String getPrivateKeyBase64() {
        return SDK.get("privateKeyBase64","");
    }

    /**
     * 获取SDK的 aesKey
     */
    public static String getSdkAesKey() {
        return SDK.get("aesKey");
    }

    /**
     * 获取SDK的 appId
     */
    public static String getSdkAppId() {
        return SDK.get("appId");
    }
    /**
     * 获取SDK的 appSecret
     */
    public static String getSdkAppSecret() {
        return SDK.get("appSecret");
    }

    //================================= brand.properties =================================//
    /**
     * 获取卡的品牌号信息
     */
    public static String getCardBrand(String id) {
        return BRAND.get("card"+id);
    }
    /**
     * 获取OBU的品牌号信息
     */
    public static String getObuBrand(String id) {
        return BRAND.get("obu"+id);
    }

    /**
     * 获取密钥
     *
     * @return
     */
    public static String getAesKey(){
        return CONFIG.get("aes.key");
    }
    /**
     * 是否加密
     *
     * @return
     */
    public static boolean getEncryptionFlag(){
        return CONFIG.getBoolean("encryption.flag",true);
    }

}

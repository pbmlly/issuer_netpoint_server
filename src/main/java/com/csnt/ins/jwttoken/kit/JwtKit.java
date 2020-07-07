package com.csnt.ins.jwttoken.kit;

import com.csnt.ins.bean.auth.UserAuth;
import com.csnt.ins.bean.auth.UserInfo;
import com.csnt.ins.jwttoken.Bean.IJwtAble;
import com.csnt.ins.jwttoken.exception.ConfigException;
import com.csnt.ins.jwttoken.service.IJwtUserService;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.kit.PathKit;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian 2017/7/22
 * FOR : Jwt操作工具类
 */
public class JwtKit {

    private static final Logger logger = LoggerFactory.getLogger(JwtKit.class);

    private static final String serverName = "[Jwt操作工具类]";
    // 默认请求头标识符
    public static String header = "Authorization";
    // 默认token前缀
    public static String tokenPrefix = "";
    // 默认私钥
    public static String secret = "csnt6p";
    // 默认失效时间(秒)
    public static Long expiration = 86400L;


    //  需要注入的服务参数
    public static IJwtUserService userService = null;
    public static ConcurrentHashMap jwtStore = new ConcurrentHashMap();

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    static {    // token的反序列化
        readFile();
        secret = SysConfig.JwtSecret();
        expiration = SysConfig.getJwtExpiration();
    }

    /**
     * 序列化
     */
    private static void writeFile() {
        FileOutputStream fos = null;
        ObjectOutputStream store = null;
        try {
            File file = new File(PathKit.getRootClassPath() + "/jwt_token.store");
            file.createNewFile();
            fos = new FileOutputStream(file);
            store = new ObjectOutputStream(fos);
            store.writeObject(jwtStore);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}持久化token到文件异常:{}", serverName, e.toString(), e);
            jwtStore = new ConcurrentHashMap();
        } finally {
            try {
                if (store != null) {
                    store.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                logger.error("{}关闭流异常:{}", serverName, e.toString(), e);
            }
        }
    }

    /**
     * 反序列化
     */
    private static void readFile() {
        FileInputStream fis = null;
        ObjectInput store = null;
        try {
            fis = new FileInputStream(new File(PathKit.getRootClassPath() + "/jwt_token.store"));
            store = new ObjectInputStream(fis);
            jwtStore = (ConcurrentHashMap) store.readObject();
        } catch (Exception e) {
            logger.error("{}从文件中读token异常:{}", serverName, e.toString(), e);
            jwtStore = new ConcurrentHashMap();
        } finally {
            try {
                if (store != null) {
                    store.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                logger.error("{}关闭流异常:{}", serverName, e.toString(), e);
            }
        }
    }

    /**
     * 重构缓存
     */
    public static void storeReset() {
        jwtStore = new ConcurrentHashMap();
    }

    /**
     * 通过 用户名密码 获取 token 如果为null，那么用户非法
     *
     * @param userName
     * @param password
     * @return
     */
    public static <T> UserAuth getToken(String userName, String password, T extendData) {
        if (userService == null) {
            logger.error("{}获取token失败,userServie对象未被初始化", serverName);
            throw new ConfigException("userService", "空/null");
        }
        IJwtAble user = userService.login(userName, password, extendData);
        if (user == null) {
            return null;
        }
        // 构建服务器端储存对象
        // 在服务器端储存jwtBean
        jwtStore.put(userName, user);
        writeFile();
        // 用userName创建token
        String trueToken = generateToken(userName);
        Long expireIn = getTokenExpiredTimeStamp(trueToken, true) / 1000;
        UserAuth userAuth = new UserAuth(user, tokenPrefix + trueToken, expireIn);
        return userAuth;
    }

    /**
     * 通过 旧的token来交换新的token
     *
     * @param token
     * @return
     */
    public static IJwtAble refreshToken(String token) {
        if (userService == null) {
            logger.error("{}刷新token失败,userServie对象未被初始化", serverName);
            throw new ConfigException("userService", "空/null");
        }
        String trueToken = getTrueToken(token);
        // 如果已经过期
        if (!isTokenExpired(trueToken, true)) {
            // 解析出用户名
            String userName = getJwtUser(trueToken, true);
            IJwtAble jwtBean = getJwtBean(userName, getCreatedDateFormToken(trueToken, true));
            //如果用户修改了密码，则让其重新登录
            if (jwtBean == null) {
                return null;
            }
            // 在此匹配生成token
            ((UserInfo) jwtBean).setTocken(generateToken(userName));
            return jwtBean;
            //更新用户的过期时间

        } else {
            //如果tokon过期则返回空
            return null;
        }
    }

    /**
     * 获取 TrueToken
     *
     * @param token
     * @return
     * @throws ConfigException
     */
    private static String getTrueToken(String token) throws ConfigException {
        if (token == null || token.length() < tokenPrefix.length()) {
            logger.error("{}获取trueToken失败,token格式异常", serverName);
            throw new ConfigException("token", "被解析");
        }
        return token.substring(tokenPrefix.length(), token.length());
    }

    /**
     * 从用户Token中获取用户名信息
     *
     * @param authToken
     * @return
     */
    public static String getJwtUser(String authToken, boolean isTrueToken) {
        String jwtUser = null;
        try {
            if (!isTrueToken) {
                authToken = getTrueToken(authToken);
            }

            final Claims claims = getClaimsFromToken(authToken);
            jwtUser = claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}从token中获取用户对象异常:{}", serverName, e.toString(), e);
        } finally {
            return jwtUser;
        }
    }

    /**
     * 获取 getJwtBean 对象
     *
     * @param jwtUser
     * @param created
     * @return
     */
    public static IJwtAble getJwtBean(String jwtUser, Date created) {
        IJwtAble jwtBean = null;
        try {
            jwtBean = (IJwtAble) jwtStore.get(jwtUser);
            /* 如果创建时间在修改密码之前 **/

            if (created == null || jwtBean == null
                    || (null != jwtBean.getLastModifyPasswordTime() && created.before(jwtBean.getLastModifyPasswordTime()))) {
                jwtBean = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}通过jwtUser获取token对象异常:{}", serverName, e.toString(), e);
        }
        return jwtBean;
    }

    /**
     * 获取Token的过期日期
     *
     * @param token
     * @return
     */
    public static Date getExpirationDateFromToken(String token, boolean isTrueToken) {
        Date expiration;
        try {
            if (!isTrueToken) {
                token = getTrueToken(token);
            }

            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}通过token获取过期日期异常:{}", serverName, e.toString(), e);
            expiration = null;
        }
        return expiration;
    }

    /**
     * 获取用户Token的创建日期
     *
     * @param authToken
     * @return
     */
    public static Date getCreatedDateFormToken(String authToken, boolean isTrueToken) {
        Date creatd;
        try {
            if (!isTrueToken) {
                authToken = getTrueToken(authToken);
            }
            final Claims claims = getClaimsFromToken(authToken);
            // 把时间戳转化为日期类型
            creatd = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("{}通过token获取token创建日期异常:{}", serverName, e.toString(), e);
            creatd = null;
        }

        return creatd;
    }

    /**
     * 判断Token是否已经过期
     *
     * @param token
     * @return
     */
    public static Boolean isTokenExpired(String token, boolean isTrueToken) {
        final Date expiration = getExpirationDateFromToken(token, isTrueToken);
        if (expiration == null) {
            return true;
        }
        return new Date().after(expiration);
    }

    /**
     * 获取Token的expireIn (单位：毫秒)
     *
     * @param token
     * @param isTrueToken
     * @return 毫秒
     */
    public static long getTokenExpiredTimeStamp(String token, boolean isTrueToken) {
        final Date expiration = getExpirationDateFromToken(token, isTrueToken);
        if (expiration == null) {
            return 0;
        }
        return expiration.getTime() - System.currentTimeMillis();
    }


    /**
     * 将Token信息解析成Claims
     *
     * @param token
     * @return
     */
    private static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("{}将Token信息解析成Claims异常:{}", serverName, e.toString(), e);
            claims = null;
        }
        return claims;
    }

    /**
     * 根据用户信息生成Token
     *
     * @param userName
     * @return
     */
    private static String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(CLAIM_KEY_USERNAME, userName);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 根据Claims信息来创建Token
     *
     * @param claims
     * @returns
     */
    private static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成令牌的过期日期
     *
     * @return
     */
    private static Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }


}

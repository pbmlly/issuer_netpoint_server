package com.csnt.ins.jwttoken.plugin;

import com.csnt.ins.jwttoken.exception.ConfigException;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.csnt.ins.jwttoken.service.IJwtUserService;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian 2017/7/22
 * FOR : 配置插件的接口
 */
public class JwtTokenPlugin implements IPlugin {
    /**
     * 默认构造器
     *
     * @param userService       登录方法的接口实现
     */
    public JwtTokenPlugin(IJwtUserService userService) {
        this(JwtKit.secret,JwtKit.expiration,userService);
    }

    /**
     * 简单构造器--提供私钥和Token有效时长
     *
     * @param secret             秘钥
     * @param expiration        失效时长（秒）
     * @param userService       登录方法的接口实现
     */
    public JwtTokenPlugin(String secret, Long expiration, IJwtUserService userService) {
        this(JwtKit.header,JwtKit.tokenPrefix,secret,expiration,userService);
    }

    /**
     * 全参构造器
     *
     * @param header            请求头中的Header
     * @param tokenPrefix      Token的前缀
     * @param secret            秘钥
     * @param expiration       失效时长（秒）
     * @param userService      登录方法的接口实现
     */
    public JwtTokenPlugin(String header, String tokenPrefix, String secret, Long expiration, IJwtUserService userService) {
        if (StrKit.isBlank(header)) {
            throw new ConfigException("Header", "空/null");
        }
        JwtKit.header = header;
        if (null == tokenPrefix) {
            throw new ConfigException("TokenPrefix", "null");
        }
        JwtKit.tokenPrefix = tokenPrefix;
        if (StrKit.isBlank(secret)) {
            throw new ConfigException("私钥", "空/null");
        }
        JwtKit.secret = secret;
        if (!StrKit.notNull(expiration) || StrKit.isBlank(expiration.toString())) {
            throw new ConfigException("失效时间", "空/null");
        }
        JwtKit.expiration = expiration;
        if (userService == null) {
            throw new ConfigException("userService", "空/null");
        }
        JwtKit.userService = userService;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}

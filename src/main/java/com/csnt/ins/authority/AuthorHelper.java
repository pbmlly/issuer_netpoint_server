package com.csnt.ins.authority;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.jwttoken.Bean.IJwtAble;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName AuthorInterceptor
 * @Description 用户授权拦截器
 * @Author duwanjiang
 * @Date 2019/6/20 0:15
 * Version 1.0
 **/
public class AuthorHelper {

    private static final Logger logger = LoggerFactory.getLogger(AuthorHelper.class);


    /**
     * 是否忽略业务msgType
     *
     * @param msgType
     * @return
     */
    public static boolean isIgnoreMsgType(String msgType) {
        if (StringUtil.isEmpty(msgType)) {
            logger.error("msgType is empty!");
            return false;
        }
        String msgTypes = SysConfig.CONFIG.get("ignore.msgtype");
        if (StringUtil.isEmpty(msgTypes)) {
            return false;
        }
        String[] msgTypeList = msgTypes.split(",");
        for (String item : msgTypeList) {
            if (item.equals(msgType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 从请求头解析出me
     *
     * @param request
     * @return
     */
    public static boolean checkUser(HttpServletRequest request) {
        String userId = request.getHeader(CommonAttribute.HTTP_HEADER_USERID);
        if (StringUtil.isEmpty(userId)) {
            logger.error("userId is empty!");
            return false;
        }
        String authToken = request.getHeader(CommonAttribute.HTTP_HEADER_AUTHORIZATION);
        if (StringUtil.isEmpty(authToken)) {
            logger.error("authToken is empty!");
            return false;
        }
        //校验用户的userId是否和token匹配
        if(!userId.equals(JwtKit.getJwtUser(authToken,false))){
            logger.error("userId 和 authToken中的userId 不匹配!");
            return false;
        }

        //检查token有效期
        IJwtAble me = JwtKit.refreshToken(authToken);
        if (me == null) {
            return false;
        }
        return true;
    }


    /**
     * 解析token校验用户
     *
     * @param dataInfo
     * @return
     */
    public static boolean checkUser(DataInfo dataInfo) {
        String userId = dataInfo.getUserId();
        if (StringUtil.isEmpty(userId)) {
            logger.error("userId is empty!");
            return false;
        }
        String authToken = dataInfo.getAuthorization();
        if (StringUtil.isEmpty(authToken)) {
            logger.error("authToken is empty!");
            return false;
        }
        //校验用户的userId是否和token匹配
        if(!userId.equals(JwtKit.getJwtUser(authToken,false))){
            logger.error("userId 和 authToken中的userId 不匹配!"+ JwtKit.getJwtUser(authToken,false));
            return false;
        }

        //检查token有效期
        IJwtAble me = JwtKit.refreshToken(authToken);
        if (me == null) {
            return false;
        }
        return true;
    }

}

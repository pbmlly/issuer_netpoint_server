package com.csnt.ins.bizmodule.login;


import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.auth.UserAuth;
import com.csnt.ins.bean.auth.UserInfo;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.expection.BizException;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.MathUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 0109登录接口
 *
 * @author source
 */
public class LoginService implements IReceiveService {

    Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final String serviceName = "[0109登录接口]";

    /**
     * 经度
     */
    private final String LONGITUDE = "longitude";
    /**
     * 纬度
     */
    private final String LATITUDE = "latitude";

    @Override
    public Result entry(Map dataMap) {
        UserAuth<UserInfo> result = null;
        try {
            Record login = new Record().setColumns(dataMap);
            logger.info("{}当前登录信息为:{}", login, serviceName);
            UserInfo user = new UserInfo();
            user.setHost(login.getStr("clientIp"));
            String userName = login.getStr(CommonAttribute.HTTP_PARAM_USERNAME);
            String pwd = login.getStr(CommonAttribute.HTTP_PARAM_PASSWORD);
            String longitude = login.getStr(LONGITUDE);
            String latitude = login.getStr(LATITUDE);
            String channelType = login.getStr("channelType");
            if (StringUtil.isEmpty(userName, pwd)) {
                logger.error("{}[userName={}]登录失败,用户名或密码不能为空", serviceName, userName);
                return Result.sysError("登录失败,用户名或密码不能为空!");
            }
            if (StringUtil.isNotEmpty(longitude, latitude)) {
                try {
                    user.setLongitude(MathUtil.asDouble(longitude));
                    user.setLatitude(MathUtil.asDouble(latitude));
                } catch (Exception e) {
                    logger.error("{}[{}]登录失败，经度和纬度格式异常", serviceName, user);
                    return Result.sysError("登录失败，经度和纬度格式异常");
                }
            }

            result = JwtKit.getToken(userName, pwd, user);
            logger.info("{}{}获取登录token信息:{}", serviceName, userName, result);
            if (StringUtil.isNotEmpty(result, result.getUser(), result.getToken())) {
                logger.info("{}[user={}]登录成功:result={}", serviceName, user, result);

                //判断类型检查
                if (StringUtil.isNotEmpty(channelType) && !channelType.equals(result.getUser().getChannelType())) {
                    logger.error("{}当前用户与实际渠道类型不匹配,不允许登录:[InputChannelType={},channelType={}]",
                            serviceName, channelType, result.getUser().getChannelType());
                    return Result.sysError("当前用户与实际渠道类型不匹配,不允许登录");
                }

                return Result.success(Kv.by("posid", result.getUser().getPosId())
                        .set("token", result.getToken())
                        .set("expiresIn", result.getExpiresIn())
                        .set("posName", result.getUser().getPosName())
                        .set("agencyId", result.getUser().getAgencyId())
                        .set("type", result.getUser().getType())
                        .set("channelType", result.getUser().getChannelType())
                        .set("rolesList", result.getUser().getRoles())
                );
            } else {
                logger.error("{}[{}]登录失败", serviceName, user);
                return Result.loginFail();
            }
        } catch (BizException b) {
            logger.error("{}登录异常", serviceName, b);
            return Result.sysError(b.getExceptionMsg());
        } catch (Throwable t) {
            logger.error("{}登录异常", serviceName, t);
            return Result.sysError(t.getMessage());
        }
    }

}

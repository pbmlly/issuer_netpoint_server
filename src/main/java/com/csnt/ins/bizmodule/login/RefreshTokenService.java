package com.csnt.ins.bizmodule.login;


import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.auth.UserAuth;
import com.csnt.ins.bean.auth.UserInfo;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.expection.BizException;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 0110刷新接口调用凭证
 *
 * @author duwanjiang
 */
public class RefreshTokenService implements IReceiveService {

    Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final String serviceName = "[0110刷新接口调用凭证]";

    @Override
    public Result entry(Map dataMap) {
        UserAuth<UserInfo> result = null;
        try {
            Record login = new Record().setColumns(dataMap);
            logger.info("{}接收到参数为:{}", login, serviceName);
            UserInfo user = new UserInfo();
            user.setHost(login.getStr("clientIp"));

            String userName = login.getStr(CommonAttribute.HTTP_PARAM_USERNAME);
            String pwd = login.getStr(CommonAttribute.HTTP_PARAM_PASSWORD);
            String token = login.getStr("token");
            if (StringUtil.isEmpty(userName, pwd, token)) {
                logger.error("{}传入参数 username, password,token不能为空", serviceName);
                return Result.paramNotNullError("username, password, token");
            }

            //校验用户的userId是否和token匹配
            if (!userName.equals(JwtKit.getJwtUser(token, false))) {
                logger.error("{}userId 和 authToken中的userId 不匹配!", serviceName);
                return Result.sysError("传入的username和token不匹配");
            }
            result = JwtKit.getToken(userName, pwd, user);
            logger.info("{}{}获取登录token信息:{}", serviceName, userName, result);
            if (StringUtil.isNotEmpty(result, result.getUser(), result.getToken())) {
                logger.info("{}[user={}]刷新token成功:result={}", serviceName, user, result);

                return Result.success(Kv.by("posid", result.getUser().getPosId())
                        .set("token", result.getToken())
                        .set("expiresIn", result.getExpiresIn()));
            } else {
                logger.error("{}[{}]刷新凭证失败", serviceName, user);
                return Result.loginFail();
            }
        } catch (BizException b) {
            logger.error("{}刷新凭证异常", serviceName, b);
            return Result.sysError(b.getExceptionMsg());
        } catch (Throwable t) {
            logger.error("{}刷新凭证异常", serviceName, t);
            return Result.sysError(t.getMessage());
        }
    }

}

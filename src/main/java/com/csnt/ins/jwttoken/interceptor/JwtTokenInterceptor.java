package com.csnt.ins.jwttoken.interceptor;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.authority.AuthorHelper;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.controller.LoginController;
import com.csnt.ins.jwttoken.Bean.IJwtAble;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 古楼城外折戟沉沙，谁与我共驰骋天涯
 * luoxiaojian
 * FOR : Jwt插件核心拦截方法
 * @author cloud
 */
public class JwtTokenInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        //跳过验证登录方法
        Controller controller = inv.getController();
        if (controller instanceof LoginController) {
            inv.invoke();
            return;
        }
        HttpServletRequest request = inv.getController().getRequest();
        String msgType = request.getHeader(CommonAttribute.HTTP_HEADER_MSGTYPE);
        //过滤业务类型
        if (!AuthorHelper.isIgnoreMsgType(msgType)) {
            //验证用户
            if (!AuthorHelper.checkUser(request)) {
                inv.getController().renderJson(Result.authFail("鉴权失败,请重新登录!"));
            }

        }
        inv.invoke();

        // 移除避免暴露当前角色信息
        inv.getController().getRequest().removeAttribute("me");
    }


    /**
     * 从请求头解析出me，用于传递token参数
     *
     * @param request
     * @return
     */
    protected static IJwtAble getMe(HttpServletRequest request) {
        IJwtAble me = (IJwtAble) request.getAttribute("me");
        if (null != me) {
            return me;
        }
        String authHeader = request.getHeader(JwtKit.header);
        // 从token中解析出jwtAble
        String jwtUser = JwtKit.getJwtUser(authHeader, false);
        if (jwtUser != null) {
            Date created = JwtKit.getCreatedDateFormToken(authHeader, false);
            me = JwtKit.getJwtBean(jwtUser, created);
            request.setAttribute("me", me);
        }
        return me;
    }
}

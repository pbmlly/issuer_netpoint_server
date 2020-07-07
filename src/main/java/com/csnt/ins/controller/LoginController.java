package com.csnt.ins.controller;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.login.LoginService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.HttpUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.core.Controller;
import com.jfinal.json.FastJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luoxiaojian
 * @Description: 登录的控制类
 * @Copyright cstc. 交通信息化部
 * @date 2019/6/26.
 */
public class LoginController extends Controller {

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    LoginService service = new LoginService();

    /**
     * 经度
     */
    private final String LONGITUDE = "longitude";
    /**
     * 纬度
     */
    private final String LATITUDE = "latitude";

    /**
     * 登录方法 get请求
     */
    public void index() {
        Result<Object> result;
        try {
            //获取请求参数
            Map paraMap = new HashMap();
            try {
                //转换参数为map
                paraMap = HttpUtil.getParams(getRequest());
            } catch (Exception e) {
                logger.error("参数转换异常:{}", e.toString(), e);
                result = Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM);
                responseMsg(result, "0109");
                return;
            }
            renderJson(service.entry(paraMap));
        } catch (Exception e) {
            logger.error("{}用户登录异常", e);
            renderJson(Result.sysError(e.getMessage()));
        }
    }

    /**
     * 向客户端响应消息
     *
     * @param result
     * @param msgType
     */
    private void responseMsg(Result result, String msgType) {
        //设置响应头
        HttpServletResponse response = getResponse();
        setResponseHead(response, FastJson.getJson().toJson(result), msgType);
        //设置响应状态
        response.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        //返回响应信息
        renderJson(result);
    }

    /**
     * 设置响应消息头部
     *
     * @param response
     * @param content
     */
    private void setResponseHead(HttpServletResponse response, String content, String msgType) {
        response.setHeader(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(content));
        response.setHeader(CommonAttribute.HTTP_HEADER_MSGTYPE, msgType);
        response.setHeader("Content-Encoding", "json");
    }
}

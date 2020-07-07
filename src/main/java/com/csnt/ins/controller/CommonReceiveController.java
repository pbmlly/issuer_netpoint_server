package com.csnt.ins.controller;

import com.alibaba.druid.util.StringUtils;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.factory.TransServiceFactory;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.core.Controller;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName BaseInfoController
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/20 0:07
 * Version 1.0
 **/
public class CommonReceiveController extends Controller {

    Logger logger = LoggerFactory.getLogger(CommonReceiveController.class);

    private final String serviceName = "[http协议接口]";
    /**
     * <code, service>
     */

    private Map<String, IReceiveService> map = TransServiceFactory.getInstance();

    /**
     * =====================================
     * 服务接口入口
     * =====================================
     */
    public void index() {
        long startTime = System.currentTimeMillis();
        Result<Object> result;
        //获取业务类型
        String msgtype = getHeader(CommonAttribute.HTTP_HEADER_MSGTYPE);
        String userId = getHeader(CommonAttribute.HTTP_HEADER_USERID);
        logger.info("{}接收到[msgtype={}]请求", serviceName, msgtype);
        try {
            if (StringUtil.isEmpty(msgtype)) {
                logger.error("{}业务类型msgtype为空", serviceName);
                result = Result.byEnum(ResponseStatusEnum.SYS_API_ERROR);
            } else {
                //获取请求参数
                String data = HttpKit.readData(getRequest());
                if (StringUtils.isEmpty(data)) {
                    logger.error("{}请求参数为空", serviceName);
                    result = Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM);
                } else {
                    //进行md5校验
                    String md5 = getHeader(CommonAttribute.HTTP_HEADER_MD5);
                    if (StringUtil.isEmpty(md5) || !UtilMd5.checkMd5(data, md5)) {
                        logger.error("{}[msgtype={},userId={}]md5校验失败,实际MD5:{},传入MD5:{}",
                                serviceName, msgtype, userId, UtilMd5.EncoderByMd5(data), md5);
                        result = Result.byEnum(ResponseStatusEnum.SYS_INVALID_MD5);
                    } else {
                        Map paraMap = new HashMap();
                        try {
                            //转换参数为map
                            paraMap = FastJson.getJson().parse(data, Map.class);
                        } catch (Exception e) {
                            logger.error("{}[msgtype={},userId={}]参数转换异常:{}",
                                    serviceName, msgtype, userId, e.toString(), e);
                            result = Result.byEnum(ResponseStatusEnum.SYS_INVALID_PARAM);
                            responseMsg(result, msgtype);
                            return;
                        }
                        //调用业务功能
                        //根据code调用相应的service

                        if (!msgtype.equals("8920") && !msgtype.equals("8940") ) {
                            logger.info("{}[msgtype={},userId={}]当前请求参数为:{}", serviceName, msgtype, userId, paraMap);
                        }

                        IReceiveService transService = map.get(msgtype);
                        if (transService != null) {
                            result = transService.entry(paraMap);
                        } else {
                            //接口未授权
                            logger.error("{}未找到对应的业务API[msgtype={}]", serviceName, msgtype);
                            result = Result.byEnum(ResponseStatusEnum.SYS_API_ERROR);
                            responseMsg(result, msgtype);
                            return;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("{}接收请求[msgtype={},userId={}]异常:{}", serviceName, msgtype, userId, e.toString(), e);
            result = Result.byEnum(ResponseStatusEnum.SYS_INTERVAL_ERROR);
        }
        responseMsg(result, msgtype);
        logger.info("{}接收到[msgtype={},userId={}]请求完成,耗时[{}],响应内容:{}",
                serviceName, msgtype, userId, DateUtil.diffTime(startTime, System.currentTimeMillis()), result);
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

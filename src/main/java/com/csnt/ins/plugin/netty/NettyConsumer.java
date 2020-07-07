package com.csnt.ins.plugin.netty;

import com.csnt.ins.authority.AuthorHelper;
import com.csnt.ins.bean.netty.DataInfo;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.factory.TransServiceFactory;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author source
 */
public class NettyConsumer implements Consumer<DataInfo> {
    Logger logger = LoggerFactory.getLogger(NettyConsumer.class);

    private final String serviceName = "[socket协议接口]";

    /**
     * <code, service>
     */

    private Map<String, IReceiveService> map = TransServiceFactory.getInstance();

    @Override
    public void accept(DataInfo dataInfo) {
        Result result;
        long startTime = System.currentTimeMillis();
        String msgType = dataInfo.getMsgType();
        if (dataInfo.isError()) {
            return;
        }

        if (UtilMd5.checkMd5(dataInfo.getRequestJson(), dataInfo.getRequestJsonMd5())) {

            logger.info("{}接收到[msgtype={}]请求", serviceName, msgType);
            Map paraMap = FastJson.getJson().parse(StringUtil.validJson(dataInfo.getRequestJson()), Map.class);
            if ("0109".equals(msgType)) {
                paraMap.put("clientIp", dataInfo.getClientIp());
            }

            //过滤业务类型
            if (!AuthorHelper.isIgnoreMsgType(msgType)) {
                //验证用户
                boolean check = AuthorHelper.checkUser(dataInfo);
                if (!check) {
                    logger.error("{}接收的[msgtype={},userId={}]用户权限校验失败", serviceName, msgType, dataInfo.getUserId());
                    dataInfo.setResponseJson(FastJson.getJson().toJson(Result.authFail("鉴权失败,请重新登录!")));
                    logger.info("{}接收到[msgtype={}]请求完成,耗时[{}]",
                            serviceName, msgType, DateUtil.diffTime(startTime, System.currentTimeMillis()));
                    return;
                }
            }
            //调用业务功能
            //根据code调用相应的service
            IReceiveService transService = map.get(msgType);
            if (transService != null) {
                try {
                    if (!msgType.equals("8920") && !msgType.equals("8940") ) {
                        logger.info("{}[msgtype={},userId={}]当前请求参数为:{}", serviceName, msgType, dataInfo.getUserId(), paraMap);
                    }

                    result = transService.entry(paraMap);
                } catch (Exception e) {
                    logger.error("{}[msgtype={}]业务调用异常:{}", serviceName, msgType, e.toString(), e);
                    result = Result.sysError("系统调用异常");
                }
            } else {
                //接口未授权
                logger.error("{}未找到对应的业务API[msgType={}]", serviceName, msgType);
                result = Result.byEnum(ResponseStatusEnum.SYS_API_ERROR);
            }
        } else {
            result = Result.byEnum(ResponseStatusEnum.SYS_INVALID_MD5);
        }

        dataInfo.setResponseJson(FastJson.getJson().toJson(result));
        logger.info("{}接收到[msgtype={},userId={}]请求完成,耗时[{}],响应内容:{}",
                serviceName, msgType, dataInfo.getUserId(), DateUtil.diffTime(startTime, System.currentTimeMillis()), dataInfo.getResponseJson());
    }

}

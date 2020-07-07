package com.csnt.ins.factory;

import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 传输服务工厂类
 *
 * @author duwanjiang
 */
public class TransServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(TransServiceFactory.class);

    /**
     * <code, service>
     */
    private static Map<String, IReceiveService> map = null;

    /**
     * 获取传输服务api的实体map
     *
     * @return
     * @throws Exception
     */
    public static Map<String, IReceiveService> getInstance() {
        if (map == null) {
            try {
                init();
            } catch (Exception e) {
                logger.error("初始化传输服务api实体异常:{}", e.toString());
            }
        }
        return map;
    }

    /**
     * 初始化传输服务实体map
     *
     * @throws Exception
     */
    public static void init() throws Exception {
        map = new HashMap<>(16);
        //加载配置文件
        logger.info("开始初始化服务对外接口类");
        Prop config = PropKit.use("transservice.properties");
        Properties prop = config.getProperties();

        //将TransService映射关系注入map
        for (Map.Entry entry : prop.entrySet()) {

            String code = (String) entry.getKey();
            Object value = entry.getValue();

            if (StringUtil.isNotEmpty(value)) {

                String className = (String) value;
                Class<?> clazz = Class.forName(className);

                Object obj = clazz.newInstance();
                if (!(obj instanceof IReceiveService)) {
                    throw new RuntimeException(String.format("配置文件[transservice.properties]错误：%s必须实现%s",
                            className, IReceiveService.class.getName()));
                }

                IReceiveService transService = (IReceiveService) obj;
                map.put(code, transService);
                logger.info("接口:{}-->{}",code,transService.getClass().getName());
            }
        }
        logger.info("完成初始化服务对外接口类");
    }
}

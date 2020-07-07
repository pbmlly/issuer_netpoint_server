package com.csnt.ins.factory;

import com.csnt.ins.bean.account.Account;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName AccountFactory
 * @Description 账号工厂类
 * @Author duwanjiang
 * @Date 2019/6/22 14:21
 * Version 1.0
 **/
public class AccountFactory {

    private static final Logger logger = LoggerFactory.getLogger(AccountFactory.class);
    /**
     * 存储账号信息
     */
    private static Map<String, Account> map = null;

    /**
     * 获取传输服务api的实体map
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Account> getInstance() {
        if (map == null) {
            try {
                init();
            } catch (Exception e) {
                logger.error("初始账号信息异常:{}", e.toString(), e);
            }
        }
        return map;
    }

    /**
     * 账号初始化
     */
    public static void init() {
        logger.info("开始初始化账号信息");
        Prop config = PropKit.use("account.properties");
        Properties prop = config.getProperties();
        map = new HashMap<>();

        //将用户映射关系注入map
        for (Map.Entry entry : prop.entrySet()) {
            String code = (String) entry.getKey();
            Object value = entry.getValue();
            if (StringUtil.isNotEmpty(value)) {
                Account account = new Account(code, (String) value);
                map.put(code, account);
            } else {
                throw new RuntimeException("账号的密码信息不能为空");
            }
        }
        logger.info("完成初始化账号信息");
    }
}

package com.csnt.ins.web;

import com.csnt.ins.controller.*;
import com.csnt.ins.factory.AccountFactory;
import com.csnt.ins.jwttoken.interceptor.JwtTokenInterceptor;
import com.csnt.ins.jwttoken.plugin.JwtTokenPlugin;
import com.csnt.ins.jwttoken.service.impl.AuthService;
import com.csnt.ins.model.issuer._MappingKit;
import com.csnt.ins.plugin.atomikos.AtomikosPlugin;
import com.csnt.ins.plugin.netty.NettyConsumer;
import com.csnt.ins.plugin.netty.NettyServerPlugin;
import com.csnt.ins.plugin.scheduledthread.ScheduledThreadPlugin;
import com.csnt.ins.plugin.sdk.SdkPlugin;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.config.*;
import com.jfinal.json.MixedJsonFactory;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author source
 */
@SuppressWarnings("Duplicates")
public class WebConfig extends JFinalConfig {
    private static Logger logger = LoggerFactory.getLogger(WebConfig.class);

    /**
     * 配置常量
     */
    @Override
    public void configConstant(Constants me) {
        me.setDevMode(true);
        me.setEncoding("utf-8");
        me.setJsonFactory(new MixedJsonFactory());
    }

    /**
     * 配置路由
     */
    @Override
    public void configRoute(Routes me) {
        //网点http协议请求接口
        me.add("/pos", CommonReceiveController.class);
        //登录请求地址
        me.add("/login", LoginController.class);
        //登录请求地址
        me.add("/issuerChange", IssuerChangeUserIdController.class);
        //4类基础信息上传
        me.add("/baseupload", IssuerBaseUploadController.class);
        //查询客户的车辆列表
        me.add("/vpofl", IssuerGetVPOFLController.class);
        //查询客户的车辆列表
        me.add("/payquery", IssuerPayQueryController.class);
    }


    @Override
    public void configEngine(Engine me) {

    }

    /**
     * 配置插件
     */
    @Override
    public void configPlugin(Plugins me) {
        //初始化账号
        AccountFactory.init();

        //初始化数据库
        configDbForDruid(me);

        //添加缓存插件
        me.add(new EhCachePlugin());

        /**配置权限拦截插件*/
        me.add(new JwtTokenPlugin(AuthService.me));

        //初始化netty转发
        NettyServerPlugin nettyServerPlugin = new NettyServerPlugin(SysConfig.CONFIG.getInt("netty.inetPort"), new NettyConsumer());
        me.add(nettyServerPlugin);


        //定时线程控件
        ScheduledThreadPlugin scheduledThreadPlugin = ScheduledThreadPlugin.builder()
                .enableConfigFile("job.properties")
                .build();
        me.add(scheduledThreadPlugin);

        //汇聚平台插件
        me.add(new SdkPlugin());
    }


    /**
     * 配置拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new JwtTokenInterceptor());
    }

    /**
     * 配置处理器
     */
    @Override
    public void configHandler(Handlers me) {

    }


    /**
     * 配置数据库 Druid
     */
    private void configDbForDruid(Plugins me) {
        loadPropertyFile("database.properties");
        String dbConfig = getProperty("database.register", "main");
        String[] dataBases = dbConfig.split(",");
        for (String dbKey : dataBases) {
            DruidPlugin dp = new DruidPlugin(getProperty(dbKey + ".jdbcUrl"), getProperty(dbKey + ".user"), getProperty(dbKey + ".password"), getProperty(dbKey + ".driver"));
            //设置连接池数量
            if (StringUtil.isNotEmpty(getPropertyToInt(dbKey + ".initialSize"), getPropertyToInt(dbKey + ".minIdle"), getPropertyToInt(dbKey + ".maxActive"))) {
                dp.set(getPropertyToInt(dbKey + ".initialSize"), getPropertyToInt(dbKey + ".minIdle"), getPropertyToInt(dbKey + ".maxActive"));
            }
            ActiveRecordPlugin arp;
            if ("main".equals(dbKey)) {
                arp = new ActiveRecordPlugin(dp);
                _MappingKit.mapping(arp);
                //添加sql模板文件
                arp.addSqlTemplate("sqltemplate/main.sql");
            } else {
                arp = new ActiveRecordPlugin(dbKey, dp);
            }
            arp.setDialect(getDbDialectByDriver(getProperty(dbKey + ".driver")));
            arp.setContainerFactory(new CaseInsensitiveContainerFactory());
            arp.setShowSql(true);
            me.add(dp);
            me.add(arp);
        }
    }

    /**
     * 配置数据库 Atomikos
     */
    private void configDbForAtomikos(Plugins me) {
        loadPropertyFile("database.properties");
        String dbConfig = getProperty("database.register", "main");
        String[] dataBases = dbConfig.split(",");
        for (String dbKey : dataBases) {
            AtomikosPlugin ap = new AtomikosPlugin(getProperty(dbKey + ".jdbcUrl"), getProperty(dbKey + ".user"), getProperty(dbKey + ".password"), getProperty(dbKey + ".xa.datasource"), getProperty(dbKey + ".driver"));
            ap.setName(dbKey);
            //设置连接池数量
            if (StringUtil.isNotEmpty(getPropertyToInt(dbKey + ".minPoolSize"), getPropertyToInt(dbKey + ".maxPoolSize"))) {
                ap.set(getPropertyToInt(dbKey + ".minPoolSize"), getPropertyToInt(dbKey + ".maxPoolSize"));
            }
            ActiveRecordPlugin arp;
            if ("main".equals(dbKey)) {
                arp = new ActiveRecordPlugin(ap);
                //添加sql模板文件
                arp.addSqlTemplate("sqltemplate/main.sql");
            } else {
                arp = new ActiveRecordPlugin(dbKey, ap);
            }

            arp.setDialect(getDbDialectByDriver(getProperty(dbKey + ".driver")));
            arp.setContainerFactory(new CaseInsensitiveContainerFactory());
            arp.setShowSql(true);
            me.add(ap);
            me.add(arp);
        }
    }


    private static Dialect getDbDialectByDriver(String driverUrl) {
        Dialect dialect = null;
        if (StringUtil.isEmpty(driverUrl)) {
            return null;
        }
        if (driverUrl.toLowerCase().contains("mysql")) {
            dialect = new MysqlDialect();
        } else if (driverUrl.toLowerCase().contains("sqlserver")) {
            dialect = new SqlServerDialect();
        } else if (driverUrl.toLowerCase().contains("oracle")) {
            dialect = new OracleDialect();
        }
        return dialect;
    }


}

package com.csnt.netpoint.generator;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;

import javax.sql.DataSource;

/**
 * Created by 62350 on 2018/4/17.
 */
public class _JFinalGenerator {

    public static DataSource getDataSource() {
        Prop prop = PropKit.use("database.properties");
        //加载配置文件
        //创建c3p0连接
        DruidPlugin c3p0Plugin = new DruidPlugin(prop.get("main.jdbcUrl"),prop.get("main.user"),prop.get("main.password"));

        c3p0Plugin.start();
        return c3p0Plugin.getDataSource();
    }
    public static DataSource getYGDataSource() {
        Prop prop = PropKit.use("database.properties");
        //加载配置文件
        //创建c3p0连接
        DruidPlugin c3p0Plugin = new DruidPlugin(prop.get("yg.jdbcUrl"),prop.get("yg.user"),prop.get("yg.password"));

        c3p0Plugin.start();
        return c3p0Plugin.getDataSource();
    }
    public static DataSource getHJPTDataSource() {
        Prop prop = PropKit.use("database.properties");
        //加载配置文件
        //创建c3p0连接
        DruidPlugin c3p0Plugin = new DruidPlugin(prop.get("hjpt.jdbcUrl"),prop.get("hjpt.user"),prop.get("hjpt.password"));

        c3p0Plugin.start();
        return c3p0Plugin.getDataSource();
    }

    public static void main(String[] args) {
        generatorIssuer();
    }

    private static void generatorIssuer(){
        // base model 所使用的包名
        String baseModelPackageName = "com.csnt.ins.model.issuer.base";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/csnt/ins/model/issuer/base";

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "com.csnt.ins.model.issuer";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        // 创建生成器
        Generator gernerator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        // 设置数据库方言
        //gernerator.setDialect(new MysqlDialect());
        // 添加不需要生成的表名
        gernerator.addExcludedTable("");
        // 设置是否在 Model 中生成 dao 对象
        gernerator.setGenerateDaoInModel(true);
        gernerator.setDialect(new MysqlDialect());
        // 设置是否生成字典文件
        gernerator.setGenerateDataDictionary(false);
        ModelBuild modelBuild = new ModelBuild(getDataSource(),
                new String[]{"etc_issued_record","etc_ofl_userinfo","etc_ofl_vehicleinfo"} ,
                "2");
        gernerator.setMetaBuilder(modelBuild);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
        //gernerator.setRemovedTableNamePrefixes("t_");
        // 生成
        gernerator.generate();
    }

    private static void generatorYG(){
        // base model 所使用的包名
        String baseModelPackageName = "com.csnt.ins.model.yg.base";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/csnt/ins/model/yg/base";

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "com.csnt.ins.model.yg";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        // 创建生成器
        Generator gernerator = new Generator(getYGDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        // 设置数据库方言
        //gernerator.setDialect(new MysqlDialect());
        // 添加不需要生成的表名
        gernerator.addExcludedTable("");
        // 设置是否在 Model 中生成 dao 对象
        gernerator.setGenerateDaoInModel(true);
        gernerator.setDialect(new OracleDialect());
        // 设置是否生成字典文件
        gernerator.setGenerateDataDictionary(false);
        ModelBuild modelBuild = new ModelBuild(getYGDataSource(),
                new String[]{"TBL_VEHICLE","TBL_USERCARD","TBL_OBU"} ,
                "3");
        gernerator.setMetaBuilder(modelBuild);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
        //gernerator.setRemovedTableNamePrefixes("t_");
        // 生成
        gernerator.generate();
    }

    private static void generatorHJPT(){
        // base model 所使用的包名
        String baseModelPackageName = "com.csnt.ins.model.hjpt.base";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/csnt/ins/model/hjpt/base";

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "com.csnt.ins.model.hjpt";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        // 创建生成器
        Generator gernerator = new Generator(getHJPTDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        // 设置数据库方言
        //gernerator.setDialect(new MysqlDialect());
        // 添加不需要生成的表名
        gernerator.addExcludedTable("");
        // 设置是否在 Model 中生成 dao 对象
        gernerator.setGenerateDaoInModel(true);
        gernerator.setDialect(new MysqlDialect());
        // 设置是否生成字典文件
        gernerator.setGenerateDataDictionary(false);
        ModelBuild modelBuild = new ModelBuild(getHJPTDataSource(),
                new String[]{"vehicleupload","cardupload","obuupload"} ,
                "3");
        gernerator.setMetaBuilder(modelBuild);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
        //gernerator.setRemovedTableNamePrefixes("t_");
        // 生成
        gernerator.generate();
    }

}

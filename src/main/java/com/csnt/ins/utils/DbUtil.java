package com.csnt.ins.utils;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author duwanjiang
 * @date 2018/3/19
 */
public class DbUtil{
    public enum OPERATE{
        EQUAL("="),
        LESS_THAN("<"),
        LESS_THAN_EQUAL("<="),
        GREATER_THAN_EQUAL(">="),
        NOT_EQUAL("<>"),
        IS_NULL("is null"),
        IS_NOT_NULL("is not null"),
        RIGHT_LIKE("like %"),
        LEFT_LIKE("%"),
        FULL_LIKE("%"),
        DEFAULT_LIKE("="),
        NOT_LIKE("="),
        IN("=");
        private String flag;
        OPERATE(String flag) {
            this.flag = flag;
        }
        public String get(){
            return flag;
        }
    }


    private static Logger logger = LoggerFactory.getLogger(DbUtil.class);

    /**
     * int[]数组求和
     */
    public static int sumResultCount(int[] result) {
        int resultSum = 0;
        for (int temp : result) {
            resultSum += temp;
        }
        return resultSum;
    }

    /**
     * 通过jdbc驱动名来获取数据库方言
     *
     * @param driverUrl
     * @return
     */
    public static Dialect getDbDialectByDriver(String driverUrl) {
        Dialect dialect = null;
        if (StringUtil.isEmpty(driverUrl)) {
            return dialect;
        }
        if (driverUrl.toLowerCase().indexOf("mysql") > -1) {
            dialect = new MysqlDialect();
        } else if (driverUrl.toLowerCase().indexOf("sqlserver") > -1) {
            dialect = new SqlServerDialect();
        } else if (driverUrl.toLowerCase().indexOf("oracle") > -1) {
            dialect = new OracleDialect();
        }
        return dialect;
    }


    /**
     * 根据sql模板获取sqlPara
     * @param key
     * @param kv
     * @return
     */
    public static SqlPara getSqlPara(String key, Map kv) {
        return getSqlPara(key, kv, true);
    }

    /**
     * 根据sql模板获取sqlPara
     * @param key
     * @param kv
     * @param isExistNameSpace
     * @return
     */
    public static SqlPara getSqlPara(String key, Map kv, boolean isExistNameSpace) {
        if (isExistNameSpace) {
            if (StringUtil.isEmpty(getCurrentDialectName())) {
                return null;
            }
            return Db.getSqlPara(getCurrentDialectName() + "." + key, kv);
        } else {
            return Db.getSqlPara(key, kv);
        }
    }

    /**
     * 获取模板中的sql语句
     *
     * @param key
     * @return
     */
    public static String getSql(String key) {
        return getSql(key, true);
    }

    /**
     * 获取模板sql语句
     *
     * @param key
     * @param isExistNameSpace
     * @return
     */
    public static String getSql(String key, boolean isExistNameSpace) {
        if (isExistNameSpace) {
            if (StringUtil.isEmpty(getCurrentDialectName())) {
                return null;
            }
            return Db.getSql(getCurrentDialectName() + "." + key);
        } else {
            return Db.getSql(key);
        }
    }
    /**
     * 获取模板sql语句
     *
     * @param key
     * @param param
     * @return
     */
    public static SqlPara getSqlParam(String key,Map param) {
        if (StringUtil.isEmpty(getCurrentDialectName())) {
            return null;
        }
        return Db.getSqlPara(getCurrentDialectName() + "." + key, param);

    }

    /**
     * 获取当前的方言名称
     *
     * @return
     */
    public static String getCurrentDialectName() {
        Dialect dialect = DbKit.getConfig().getDialect();
        if (dialect != null) {
            if (dialect instanceof MysqlDialect) {
                return "mysql";
            } else if (dialect instanceof OracleDialect) {
                return "oracle";
            } else if (dialect instanceof SqlServerDialect) {
                return "sqlserver";
            }
        }
        return null;
    }

    /**
     * 从record对象转化为插入sql语句
     *
     * @param tableName
     * @param records
     * @param batchCount
     * @param getDbCurrentDateFields 需要获取数据库当前时间的字段，格式如:[createTime,insertTime]
     * @return
     */
    public static List<StringBuffer> getInsertSqlFromRecord(String tableName, List<Record> records, int batchCount, String[] getDbCurrentDateFields) {
        List result = new ArrayList();
        StringBuffer insert = new StringBuffer().append("insert into `").append(tableName).append("`("),
                sql = new StringBuffer().append(insert);
        for (int i = 0, count = 1; i < records.size(); i++, count++) {
            StringBuffer value = new StringBuffer(); //存储值
            Record record = records.get(i);
            //判断是否大于最大批量处理数
            if (count > batchCount) {
                count = 1;
                result.add(sql);    //存储上一个sql
                sql = new StringBuffer().append(insert); //重置sql语句
            }
            //遍历每个字段，拼接sql语句
            Iterator iterator = record.getColumns().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry e = (Map.Entry) iterator.next();
                //表示不是第一个字段了
                if (value.length() > 0) {
                    if (count == 1) {
                        sql.append(", ");
                    }
                    value.append(", ");
                }
                //拼接需要插入的字段
                if (count == 1) {
                    sql.append('`').append((String) e.getKey()).append('`');
                }
                //如果是日期类型  需要格式化
                Object keyValue = e.getValue();
                if (keyValue instanceof Date) {
                    keyValue = DateUtil.formatDate((Date) keyValue, "yyyy-MM-dd HH:mm:ss");
                }
                //判断当前字段是否是获取数据库当前时间
                if (getDbCurrentDateFields.length > 0) {
                    for (String field : getDbCurrentDateFields) {
                        if (e.getKey().equals(field)) {
                            keyValue = getDbCurrentDateSql();
                            break;
                        }
                    }
                    //拼接到sql中去
                    value.append(keyValue);
                } else {
                    //将值包装为字符串
                    value.append("'").append(keyValue).append("'");
                }

            }

            //最终组装sql
            if (count == 1) {
                sql.append(") values(").append(value).append(")");
            } else {
                sql.append(",(").append(value).append(")");
            }
        }
        //如果result为空，则表示当前所有记录为超过最大提交数
        result.add(sql);

        return result;
    }

    /**
     * 从record对象转化为插入sql语句
     *
     * @param tableName
     * @param primarykeys            主键数组
     * @param records
     * @param batchCount
     * @param getDbCurrentDateFields 需要获取数据库当前时间的字段，格式如:[createTime,insertTime]
     * @return
     */
    public static List<StringBuffer> getUpdateSqlFromRecord(String tableName, String[] primarykeys, List<Record> records, int batchCount, String[] getDbCurrentDateFields) {
        List result = new ArrayList();
        StringBuffer update = new StringBuffer().append("update `").append(tableName).append("` set ");
        StringBuffer sql = new StringBuffer();  //初始化sql语句
        for (int i = 0, count = 1; i < records.size(); i++, count++) {
            StringBuffer value = new StringBuffer(); //存储值的字符串
            StringBuffer primaryKeyValue = new StringBuffer(); //主键值的字符串


            Record record = records.get(i);

            //判断是否大于最大批量处理数
            if (count > batchCount) {
                count = 1;
                result.add(sql);    //存储上一个sql
                sql = new StringBuffer(); //重置一下sql
            }
            //遍历每个字段，拼接sql语句
            Iterator iterator = record.getColumns().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry e = (Map.Entry) iterator.next();
                //表示不是第一个字段了
                if (value.length() > 0) {
                    value.append(", ");
                }

                //当前字段名
                String keyField = (String) e.getKey();
                //如果是日期类型  需要格式化
                Object keyValue = e.getValue();
                if (keyValue instanceof Date) {
                    keyValue = DateUtil.formatDate((Date) keyValue, "yyyy-MM-dd HH:mm:ss");
                }

                //判断当前字段是否是获取数据库当前时间
                if (StringUtil.isContained(getDbCurrentDateFields, keyField)) {
                    keyValue = getDbCurrentDateSql();
                } else {
                    keyValue = new StringBuffer("'").append(keyValue).append("'").toString();
                }

                //判断当前字段是否为主键
                if (StringUtil.isContained(primarykeys, keyField)) {
                    primaryKeyValue.append(keyField).append("=").append(keyValue);
                } else {
                    //将值包装为字符串
                    value.append(keyField).append("=").append(keyValue);
                }
            }

            //最终组装sql
            sql.append(update).append(value).append(" where ").append(primaryKeyValue).append(";");
        }
        //如果result为空，则表示当前所有记录未超过最大提交数
        if (StringUtil.isNotEmpty(sql)) {
            result.add(sql);
        }

        return result;
    }

    /**
     * 根据数据库方言获取数据的当前时间函数
     *
     * @return
     */
    public static String getDbCurrentDateSql() {
        String sql = "";
        //获取当前的方言对象
        Dialect dialect = DbKit.getConfig().getDialect();
        if (dialect != null) {
            if (dialect instanceof MysqlDialect) {
                sql = " SYSDATE() ";
            } else if (dialect instanceof OracleDialect) {
                sql = " sysdate ";
            } else if (dialect instanceof SqlServerDialect) {
                sql = " GETDATE() ";
            }
        }
        return sql;
    }

    /**
     * 获取主键uuid
     *
     * @return
     */
    public static String getUUid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 判断错误是否是主键冲突
     * @param e 错误异常
     * @return 是否是主键冲突
     */
    public static boolean isPrimarkKeyExpection(Exception e){
        if (null != e.getMessage() && e.getMessage().contains("Unique constraint")) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 拼接查询条件
     * @param column 查询列
     * @param value 目标值
     * @param condition 条件支付查
     * @param params
     * @param <T>
     */
    public static  <T>  void initParam(String column, T value, StringBuilder condition, List<Object> params, OPERATE operator){
        if(null!=value){
            if(operator==OPERATE.EQUAL){
                condition.append(" and ").append(column).append(OPERATE.EQUAL.get()).append("? ");
                params.add(value);
            }else if(operator==OPERATE.LESS_THAN){
                condition.append(" and ").append(column).append(OPERATE.LESS_THAN.get()).append("? ");
                params.add(value);
            }else if(operator==OPERATE. LESS_THAN_EQUAL){
                condition.append(" and ").append(column).append(OPERATE.LESS_THAN_EQUAL.get()).append("? ");
                params.add(value);
            }else if(operator==OPERATE.GREATER_THAN_EQUAL){
                condition.append(" and ").append(column).append(OPERATE.GREATER_THAN_EQUAL.get()).append("? ");
                params.add(value);
            }else if(operator==OPERATE.NOT_EQUAL){
                condition.append(" and ").append(column).append(OPERATE.NOT_EQUAL.get()).append("? ");
                params.add(value);
            }else if(operator==OPERATE.IS_NULL){
                condition.append(" and ").append(column).append(OPERATE.IS_NULL.get());
                params.add(value);
            }else if(operator==OPERATE.IS_NOT_NULL){
                condition.append(" and ").append(column).append(OPERATE.IS_NOT_NULL.get());
                params.add(value);
            }else if(operator==OPERATE.RIGHT_LIKE){
                condition.append(" and ").append(column).append(" like '%").append(value).append("'");
            }else if(operator==OPERATE.LEFT_LIKE){
                condition.append(" and ").append(column).append(" like '").append(value).append("%'");
            }else if(operator==OPERATE.FULL_LIKE){
                condition.append(" and ").append(column).append(" like '%").append(value).append("%'");
            }else if(operator==OPERATE.NOT_LIKE){
                condition.append(" and ").append(column).append("not like ? ");
                params.add(value);
            }else if(operator==OPERATE.DEFAULT_LIKE){
                condition.append(" and ").append(column).append(" like ? ");
                params.add(value);
            }else if(operator==OPERATE.IN){
                condition.append(" and ").append(column).append(" like int (?) ");
            }
        }
    }

}

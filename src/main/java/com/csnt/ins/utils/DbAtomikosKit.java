package com.csnt.ins.utils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.dialect.Dialect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@SuppressWarnings("Duplicates")
public class DbAtomikosKit {

    private static int[] batch(Connection conn, String sql, Object[][] paras) throws SQLException {
        if (paras == null || paras.length == 0) {
            return new int[0];
        }

        int counter = 0;
        int pointer = 0;
        int[] result = new int[paras.length];
        PreparedStatement pst = conn.prepareStatement(sql);
        for (int i = 0; i < paras.length; i++) {
            for (int j = 0; j < paras[i].length; j++) {
                Object value = paras[i][j];
                if (value instanceof Date) {
                    if (value instanceof java.sql.Date) {
                        pst.setDate(j + 1, (java.sql.Date) value);
                    } else if (value instanceof java.sql.Timestamp) {
                        pst.setTimestamp(j + 1, (java.sql.Timestamp) value);
                    } else {
                        // Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
                        Date d = (Date) value;
                        pst.setTimestamp(j + 1, new java.sql.Timestamp(d.getTime()));
                    }
                } else {
                    pst.setObject(j + 1, value);
                }
            }
            pst.addBatch();

        }
        int[] r = pst.executeBatch();
        for (int k = 0; k < r.length; k++) {
            result[pointer++] = r[k];
        }
        close(pst);
        return result;
    }

    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.use().batch(sql, new Object[][]{{"James", 888}, {"zhanjin", 888}});
     * </pre>
     *
     * @param sql   The SQL to execute.
     * @param paras An array of query replacement parameters.  Each row in this array is one set of batch replacement values.
     * @return The number of rows updated per statement
     */
    public static int[] batch(DbPro dbPro, String sql, Object[][] paras) {
        Connection conn = null;
        Config config = dbPro.getConfig();
        try {
            conn = config.getConnection();
            return batch(conn, sql, paras);
        } catch (Exception e) {
            throw new ActiveRecordException(e);
        } finally {
            config.close(conn);
        }
    }

    private static  int[] batch(Connection conn, String sql, String columns, List list) throws SQLException {
        if (list == null || list.size() == 0) {
            return new int[0];
        }
        Object element = list.get(0);
        if (!(element instanceof Record) && !(element instanceof Model)) {
            throw new IllegalArgumentException("The element in list must be Model or Record.");
        }
        boolean isModel = element instanceof Model;

        String[] columnArray = columns.split(",");
        for (int i = 0; i < columnArray.length; i++) {
            columnArray[i] = columnArray[i].trim();
        }

        int pointer = 0;
        int size = list.size();
        int[] result = new int[size];
        PreparedStatement pst = conn.prepareStatement(sql);
        for (int i = 0; i < size; i++) {
            Map map = isModel ? getAttrs((Model)list.get(i)): ((Record)list.get(i)).getColumns();
            for (int j = 0; j < columnArray.length; j++) {
                Object value = map.get(columnArray[j]);
                if (value instanceof Date) {
                    if (value instanceof java.sql.Date) {
                        pst.setDate(j + 1, (java.sql.Date) value);
                    } else if (value instanceof java.sql.Timestamp) {
                        pst.setTimestamp(j + 1, (java.sql.Timestamp) value);
                    } else {
                        // Oracle、SqlServer 中的 TIMESTAMP、DATE 支持 new Date() 给值
                        Date d = (Date) value;
                        pst.setTimestamp(j + 1, new java.sql.Timestamp(d.getTime()));
                    }
                } else {
                    pst.setObject(j + 1, value);
                }
            }
            pst.addBatch();
        }
        int[] r = pst.executeBatch();
        for (int k = 0; k < r.length; k++) {
            result[pointer++] = r[k];
        }
        close(pst);
        return result;
    }

    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * String sql = "insert into user(name, cash) values(?, ?)";
     * int[] result = Db.use().batch(sql, "name, cash", modelList, 500);
     * </pre>
     *
     * @param sql               The SQL to execute.
     * @param columns           the columns need be processed by sql.
     * @param modelOrRecordList model or record object list.
     * @return The number of rows updated per statement
     */
    public static int[] batch(DbPro dbPro, String sql, String columns, List modelOrRecordList) {
        Connection conn = null;
        Config config = dbPro.getConfig();
        try {
            conn = config.getConnection();
            return batch(conn, sql, columns, modelOrRecordList);
        } catch (Exception e) {
            throw new ActiveRecordException(e);
        } finally {
            config.close(conn);
        }
    }

    private static int[] batch( Connection conn, List<String> sqlList) throws SQLException {
        if (sqlList == null || sqlList.size() == 0) {
            return new int[0];
        }

        int pointer = 0;
        int size = sqlList.size();
        int[] result = new int[size];
        Statement st = conn.createStatement();
        for (int i = 0; i < size; i++) {
            st.addBatch(sqlList.get(i));

        }
        int[] r = st.executeBatch();
        for (int k = 0; k < r.length; k++) {
            result[pointer++] = r[k];
        }
        close(st);
        return result;
    }

    /**
     * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
     * <pre>
     * Example:
     * int[] result = Db.use().batch(sqlList, 500);
     * </pre>
     *
     * @param sqlList   The SQL list to execute.
     * @return The number of rows updated per statement
     */
    public static int[] batch(DbPro dbPro, List<String> sqlList) {
        Connection conn = null;
        Config config = dbPro.getConfig();
        try {
            conn = config.getConnection();
            return batch(conn, sqlList);
        } catch (Exception e) {
            throw new ActiveRecordException(e);
        } finally {
            config.close(conn);
        }
    }

    /**
     * Batch save models using the "insert into ..." sql generated by the first model in modelList.
     * Ensure all the models can use the same sql as the first model.
     */
    public static int[] batchSave(DbPro dbPro,List<? extends Model> modelList) {
        if (modelList == null || modelList.size() == 0){
            return new int[0];}

        Dialect dialect = dbPro.getConfig().getDialect();

        Model model = modelList.get(0);
        Map<String, Object> attrs = getAttrs(model);
        int index = 0;
        StringBuilder columns = new StringBuilder();
        // the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            if (dialect.isOracle()) {	// 支持 oracle 自增主键
                Object value = e.getValue();
                if (value instanceof String && ((String)value).endsWith(".nextval")) {
                    continue ;
                }
            }

            if (index++ > 0) {
                columns.append(',');
            }
            columns.append(e.getKey());
        }

        StringBuilder sql = new StringBuilder();
        List<Object> parasNoUse = new ArrayList<Object>();
        dialect.forModelSave(TableMapping.me().getTable(model.getClass()), attrs, sql, parasNoUse);
        return batch(dbPro,sql.toString(), columns.toString(), modelList);
    }

    /**
     * Batch save records using the "insert into ..." sql generated by the first record in recordList.
     * Ensure all the record can use the same sql as the first record.
     *
     * @param tableName the table name
     */
    public static int[] batchSave(DbPro dbPro, String tableName, List<Record> recordList) {
        if (recordList == null || recordList.size() == 0) {
            return new int[0];
        }
        Dialect dialect = dbPro.getConfig().getDialect();

        Record record = recordList.get(0);
        Map<String, Object> cols = record.getColumns();
        int index = 0;
        StringBuilder columns = new StringBuilder();
        // the same as the iterator in Dialect.forDbSave() to ensure the order of the columns
        for (Map.Entry<String, Object> e : cols.entrySet()) {
            if (dialect.isOracle()) {    // 支持 oracle 自增主键
                Object value = e.getValue();
                if (value instanceof String && ((String) value).endsWith(".nextval")) {
                    continue;
                }
            }

            if (index++ > 0) {
                columns.append(',');
            }
            columns.append(e.getKey());
        }

        String[] pKeysNoUse = new String[0];
        StringBuilder sql = new StringBuilder();
        List<Object> parasNoUse = new ArrayList<Object>();
        dialect.forDbSave(tableName, pKeysNoUse, record, sql, parasNoUse);
        return batch(dbPro, sql.toString(), columns.toString(), recordList);
    }

    /**
     * Batch update models using the attrs names of the first model in modelList.
     * Ensure all the models can use the same sql as the first model.
     */
    public static int[] batchUpdate(DbPro dbPro, List<? extends Model> modelList) {
        if (modelList == null || modelList.size() == 0){
            return new int[0];
        }
        Dialect dialect = dbPro.getConfig().getDialect();

        Model model = modelList.get(0);
        Table table = TableMapping.me().getTable(model.getClass());
        String[] pKeys = table.getPrimaryKey();

        Map<String, Object> attrs = getAttrs(model);
        List<String> attrNames = new ArrayList<String>();
        // the same as the iterator in Dialect.forModelSave() to ensure the order of the attrs
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            String attr = e.getKey();
            if (!dialect.isPrimaryKey(attr, pKeys) && table.hasColumnLabel(attr)) {
                attrNames.add(attr);
            }
        }
        attrNames.addAll(Arrays.asList(pKeys));
        String columns = StrKit.join(attrNames.toArray(new String[0]), ",");

        // update all attrs of the model not use the midifyFlag of every single model
        Set<String> modifyFlag = attrs.keySet();	// model.getModifyFlag();

        StringBuilder sql = new StringBuilder();
        List<Object> parasNoUse = new ArrayList<Object>();
        dialect.forModelUpdate(TableMapping.me().getTable(model.getClass()), attrs, modifyFlag, sql, parasNoUse);
        return batch(dbPro,sql.toString(), columns, modelList);
    }

    /**
     * Batch update records using the columns names of the first record in recordList.
     * Ensure all the records can use the same sql as the first record.
     *
     * @param tableName  the table name
     * @param primaryKey the primary key of the table, composite primary key is separated by comma character: ","
     */
    public static int[] batchUpdate(DbPro dbPro, String tableName, String primaryKey, List<Record> recordList) {
        if (recordList == null || recordList.size() == 0) {
            return new int[0];
        }

        String[] pKeys = primaryKey.split(",");
        Dialect dialect = dbPro.getConfig().getDialect();
        dialect.trimPrimaryKeys(pKeys);

        Record record = recordList.get(0);
        Map<String, Object> cols = record.getColumns();
        List<String> colNames = new ArrayList<String>();
        // the same as the iterator in Dialect.forDbUpdate() to ensure the order of the columns
        for (Map.Entry<String, Object> e : cols.entrySet()) {
            String col = e.getKey();
            if (!dialect.isPrimaryKey(col, pKeys)) {
                colNames.add(col);
            }
        }
        colNames.addAll(Arrays.asList(pKeys));
        String columns = StrKit.join(colNames.toArray(new String[0]), ",");

        Object[] idsNoUse = new Object[pKeys.length];
        StringBuilder sql = new StringBuilder();
        List<Object> parasNoUse = new ArrayList<Object>();
        dialect.forDbUpdate(tableName, pKeys, idsNoUse, record, sql, parasNoUse);
        return batch(dbPro, sql.toString(), columns, recordList);
    }

    /**
     * Batch update records with default primary key, using the columns names of the first record in recordList.
     * Ensure all the records can use the same sql as the first record.
     *
     * @param tableName the table name
     */
    public static int[] batchUpdate(DbPro dbPro, String tableName, List<Record> recordList) {
        return batchUpdate(dbPro, tableName, dbPro.getConfig().getDialect().getDefaultPrimaryKey(), recordList);
    }

    private static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                throw new ActiveRecordException(e);
            }
        }
    }

    private static  Map<String, Object> getAttrs(Model model){
        Set<Map.Entry<String, Object>> set = model._getAttrsEntrySet();
        Map<String, Object> mapFromSet = new HashMap<String, Object>(16);
        for(Map.Entry<String, Object> entry : set) {
            mapFromSet.put(entry.getKey(), entry.getValue());
        }
        return mapFromSet;
    }
}

package com.csnt.netpoint.generator;

import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;

import javax.sql.DataSource;

public class ModelBuild extends MetaBuilder {

    private String[] createTableName;

    public String[] getCreateTableName() {
        return createTableName;
    }

    public void setCreateTableName(String[] createTableName) {
        this.createTableName = createTableName;
    }

    public ModelBuild(DataSource dataSource) {
        super(dataSource);
        // TODO Auto-generated constructor stub
    }

    public ModelBuild(DataSource dataSource, String[] createTableName, String dataType) {
        super(dataSource);
        if(dataType.equals("1")){
            setDialect(new SqlServerDialect());
        }else if(dataType.equals("2")){
            setDialect(new MysqlDialect());
        }else if(dataType.equals("3")){
            setDialect(new OracleDialect());
        }
        setCreateTableName(createTableName);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected boolean isSkipTable(String tableName) {
        for (String table: createTableName){
            if (tableName.equals(table))
                return false;
        }
        return true;
    }
}
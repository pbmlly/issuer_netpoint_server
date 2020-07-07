package com.csnt.ins.plugin.atomikos;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import com.atomikos.jdbc.internal.AbstractDataSourceBean;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.IDataSourceProvider;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * @author source
 */
public class AtomikosPlugin implements IPlugin, IDataSourceProvider {
    private String name = null;
    private String url;
    private String username;
    private String password;
    private String xaDataSourceStr;
    private String driverClassName;

    private int minPoolSize = 3;
    private int maxPoolSize = 32;

    private AbstractDataSourceBean ds;
    private boolean localTransactionMode = true;
    private volatile boolean isStarted = false;

    public AtomikosPlugin(String url, String username, String password, String xaDataSourceStr, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.xaDataSourceStr = xaDataSourceStr;
        this.driverClassName = driverClassName;
    }

    public AtomikosPlugin set(int minPoolSize, int maxPoolSize) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public final AtomikosPlugin setLocalTransactionMode(boolean localTransactionMode) {
        this.localTransactionMode = localTransactionMode;
        return this;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public static boolean tx(IAtom atom) {
        UserTransactionImp utx = new UserTransactionImp();
        try {
            utx.begin();
            boolean result = atom.run();
            if (result) {
                // 提交事务
                utx.commit();
            } else {
                //回滚事务
                utx.rollback();
            }
            return result;
        } catch (Throwable t) {
            try {
                utx.rollback();
            } catch (Exception e) {
                LogKit.error(e.getMessage(), e);
            }
            throw t instanceof RuntimeException ? (RuntimeException) t : new ActiveRecordException(t);
        }
    }

    @Override
    public boolean start() {
        if (isStarted) {
            return true;
        } else {
            if (StrKit.notBlank(xaDataSourceStr)) {
                AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
                atomikosDataSourceBean.setXaDataSourceClassName(xaDataSourceStr);

                //是否开启本地事务与jta事务混合
                atomikosDataSourceBean.setLocalTransactionMode(localTransactionMode);
                Properties properties = new Properties();
                properties.put("URL", url);
                properties.put("user", username);
                properties.put("password", password);
                atomikosDataSourceBean.setXaProperties(properties);
                ds = atomikosDataSourceBean;
            } else {
                AtomikosNonXADataSourceBean atomikosNonXADataSourceBean = new AtomikosNonXADataSourceBean();
                atomikosNonXADataSourceBean.setLocalTransactionMode(localTransactionMode);
                atomikosNonXADataSourceBean.setUrl(url);
                atomikosNonXADataSourceBean.setUser(username);
                atomikosNonXADataSourceBean.setPassword(password);
                atomikosNonXADataSourceBean.setDriverClassName(driverClassName);
                ds = atomikosNonXADataSourceBean;
            }
            if (StrKit.notBlank(name)) {
                ds.setUniqueResourceName(name);
            }
            ds.setMinPoolSize(minPoolSize);
            ds.setMaxPoolSize(maxPoolSize);
            this.isStarted = true;
            return true;
        }
    }

    @Override
    public boolean stop() {
        if (ds != null) {
            ds.close();
        }

        ds = null;
        isStarted = false;
        return true;
    }

    @Override
    public DataSource getDataSource() {
        return ds;
    }

}

package com.side.springtestbed.utils.data;

import com.side.springtestbed.utils.utils.JDBCUtil;

import javax.sql.CommonDataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class JDBCCommonDataSource implements CommonDataSource, Serializable {
    protected Properties connectionProps = new Properties();
    protected String description = null;
    protected String dataSourceName = null;
    protected String serverName = null;
    protected String networkProtocol = null;
    protected int loginTimeout = 0;
    protected transient PrintWriter logWriter;
    protected String user = null;
    protected String password = null;
    protected String url = null;

    public JDBCCommonDataSource() {
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    public void setLogWriter(PrintWriter var1) throws SQLException {
        this.logWriter = var1;
    }

    public void setLoginTimeout(int var1) throws SQLException {
        this.loginTimeout = var1;
        this.connectionProps.setProperty("loginTimeout", Integer.toString(this.loginTimeout));
    }

    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDataSourceName() {
        return this.dataSourceName;
    }

    public String getNetworkProtocol() {
        return this.networkProtocol;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getDatabaseName() {
        return this.url;
    }

    public String getDatabase() {
        return this.url;
    }

    public String getUrl() {
        return this.url;
    }

    public String getURL() {
        return this.url;
    }

    public String getUser() {
        return this.user;
    }

    public void setDatabaseName(String var1) {
        this.url = var1;
    }

    public void setDatabase(String var1) {
        this.url = var1;
    }

    public void setUrl(String var1) {
        this.url = var1;
    }

    public void setURL(String var1) {
        this.url = var1;
    }

    public void setPassword(String var1) {
        this.password = var1;
        this.connectionProps.setProperty("password", var1);
    }

    public void setUser(String var1) {
        this.user = var1;
        this.connectionProps.setProperty("user", var1);
    }

    public void setProperties(Properties var1) {
        this.connectionProps = var1 == null ? new Properties() : (Properties)var1.clone();
        if (this.user != null) {
            this.connectionProps.setProperty("user", this.user);
        }

        if (this.password != null) {
            this.connectionProps.setProperty("password", this.password);
        }

        if (this.loginTimeout != 0) {
            this.connectionProps.setProperty("loginTimeout", Integer.toString(this.loginTimeout));
        }

    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw (SQLFeatureNotSupportedException) JDBCUtil.notSupported();
    }
}


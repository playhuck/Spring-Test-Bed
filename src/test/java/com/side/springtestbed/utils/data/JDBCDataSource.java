package com.side.springtestbed.utils.data;

import com.side.springtestbed.utils.utils.JDBCUtil;
import org.hsqldb.jdbc.JDBCDriver;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.Properties;

public class JDBCDataSource extends JDBCCommonDataSource implements DataSource, Serializable, Referenceable, Wrapper {
    public Connection getConnection() throws SQLException {
        if (this.url == null) {
            throw JDBCUtil.nullArgument("url");
        } else if (this.connectionProps == null) {
            if (this.user == null) {
                throw JDBCUtil.invalidArgument("user");
            } else if (this.password == null) {
                throw JDBCUtil.invalidArgument("password");
            } else {
                return this.getConnection(this.user, this.password);
            }
        } else {
            return this.getConnection(this.url, this.connectionProps);
        }
    }

    public Connection getConnection(String var1, String var2) throws SQLException {
        if (var1 == null) {
            throw JDBCUtil.invalidArgument("user");
        } else if (var2 == null) {
            throw JDBCUtil.invalidArgument("password");
        } else {
            Properties var3 = new Properties();
            var3.setProperty("user", var1);
            var3.setProperty("password", var2);
            var3.setProperty("loginTimeout", Integer.toString(this.loginTimeout));
            return this.getConnection(this.url, var3);
        }
    }

    private Connection getConnection(String var1, Properties var2) throws SQLException {
        if (!var1.startsWith("jdbc:hsqldb:")) {
            var1 = "jdbc:hsqldb:" + var1;
        }

        return JDBCDriver.getConnection(var1, var2);
    }

    public <T> T unwrap(Class<T> var1) throws SQLException {
        if (this.isWrapperFor(var1)) {
            return (T) this;
        } else {
            throw JDBCUtil.invalidArgument("iface: " + var1);
        }
    }

    public boolean isWrapperFor(Class<?> var1) throws SQLException {
        return var1 != null && var1.isAssignableFrom(this.getClass());
    }

    public Reference getReference() throws NamingException {
        String var1 = "org.hsqldb.jdbc.JDBCDataSourceFactory";
        Reference var2 = new Reference(this.getClass().getName(), var1, (String)null);
        var2.add(new StringRefAddr("database", this.getDatabase()));
        var2.add(new StringRefAddr("user", this.getUser()));
        var2.add(new StringRefAddr("password", this.password));
        var2.add(new StringRefAddr("loginTimeout", Integer.toString(this.loginTimeout)));
        return var2;
    }

    public JDBCDataSource() {
    }
}


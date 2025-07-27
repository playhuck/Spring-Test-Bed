package com.side.springtestbed.utils.data;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.P6LogQuery;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.*;
import com.p6spy.engine.wrapper.ConnectionWrapper;
import com.side.springtestbed.utils.conn.ConnectionDecoratorFactoryService;

import javax.naming.*;
import javax.sql.*;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;
import java.util.*;
import java.util.logging.Logger;

public class P6DataSource implements DataSource, ConnectionPoolDataSource, XADataSource, Referenceable, Serializable {
    protected transient CommonDataSource realDataSource;
    protected String rdsName;
    protected transient JdbcEventListenerFactory jdbcEventListenerFactory;
    private ConnectionDecoratorFactoryService JdbcEventListenerFactoryLoader;

    public P6DataSource() {
    }

    public P6DataSource(DataSource delegate) {
        this.realDataSource = delegate;
    }

    public String getRealDataSource() {
        return this.rdsName;
    }

    public void setRealDataSource(String jndiName) {
        this.rdsName = jndiName;
    }

    protected synchronized void bindDataSource() throws SQLException {
        if (null == this.realDataSource) {
            P6SpyLoadableOptions options = P6SpyOptions.getActiveInstance();
            if (this.rdsName == null) {
                this.rdsName = options.getRealDataSource();
            }

            if (this.rdsName == null) {
                throw new SQLException("P6DataSource: no value for Real Data Source Name, cannot perform jndi lookup");
            } else {
                Hashtable<String, String> env = null;
                String factory;
                if ((factory = options.getJNDIContextFactory()) != null) {
                    env = new Hashtable();
                    env.put("java.naming.factory.initial", factory);
                    String url = options.getJNDIContextProviderURL();
                    if (url != null) {
                        env.put("java.naming.provider.url", url);
                    }

                    String custom = options.getJNDIContextCustom();
                    if (custom != null) {
                        env.putAll(this.parseDelimitedString(custom));
                    }
                }

                try {
                    InitialContext ctx;
                    if (env != null) {
                        ctx = new InitialContext(env);
                    } else {
                        ctx = new InitialContext();
                    }

                    this.realDataSource = (CommonDataSource)ctx.lookup(this.rdsName);
                } catch (NamingException var6) {
                    NamingException e = var6;
                    throw new SQLException("P6DataSource: naming exception during jndi lookup of Real Data Source Name of '" + this.rdsName + "'. " + e.getMessage(), e);
                }

                Map<String, String> props = this.parseDelimitedString(options.getRealDataSourceProperties());
                if (props != null) {
                    this.setDataSourceProperties(props);
                }

                if (this.realDataSource == null) {
                    throw new SQLException("P6DataSource: jndi lookup for Real Data Source Name of '" + this.rdsName + "' failed, cannot bind named data source.");
                }
            }
        }
    }

    private void setDataSourceProperties(Map<String, String> props) throws SQLException {
        Map<String, String> matchedProps = new HashMap();
        Class<?> klass = this.realDataSource.getClass();
        Method[] var4 = klass.getMethods();
        int var5 = var4.length;

        label56:
        for(int var6 = 0; var6 < var5; ++var6) {
            Method method = var4[var6];
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String propertyName = methodName.substring(3).toLowerCase();
                Iterator var10 = props.entrySet().iterator();

                while(true) {
                    Map.Entry entry;
                    String key;
                    do {
                        if (!var10.hasNext()) {
                            continue label56;
                        }

                        entry = (Map.Entry)var10.next();
                        key = (String)entry.getKey();
                    } while(!key.toLowerCase().equals(propertyName));

                    try {
                        String value = (String)entry.getValue();
                        Class<?>[] types = method.getParameterTypes();
                        Class<?> paramType = types[0];
                        if (paramType.isAssignableFrom(String.class)) {
                            P6LogQuery.debug("calling " + methodName + " on DataSource " + this.rdsName + " with " + value);
                            method.invoke(this.realDataSource, value);
                            matchedProps.put(key, value);
                        } else if (paramType.isPrimitive() && Integer.TYPE.equals(paramType)) {
                            P6LogQuery.debug("calling " + methodName + " on DataSource " + this.rdsName + " with " + value);
                            method.invoke(this.realDataSource, Integer.valueOf(value));
                            matchedProps.put(key, value);
                        } else {
                            P6LogQuery.debug("method " + methodName + " on DataSource " + this.rdsName + " matches property " + propertyName + " but expects unsupported type " + paramType.getName());
                            matchedProps.put(key, value);
                        }
                    } catch (IllegalAccessException var16) {
                        throw new SQLException("spy.properties file includes datasource property " + key + " for datasource " + this.rdsName + " but access is denied to method " + methodName, var16);
                    } catch (InvocationTargetException var17) {
                        InvocationTargetException e = var17;
                        throw new SQLException("spy.properties file includes datasource property " + key + " for datasource " + this.rdsName + " but call method " + methodName + " fails", e);
                    }
                }
            }
        }

        Iterator var18 = props.keySet().iterator();

        while(var18.hasNext()) {
            String key = (String)var18.next();
            if (!matchedProps.containsKey(key)) {
                P6LogQuery.debug("spy.properties file includes datasource property " + key + " for datasource " + this.rdsName + " but class " + klass.getName() + " has no method by that name");
            }
        }

    }

    private HashMap<String, String> parseDelimitedString(String delimitedString) {
        if (delimitedString == null) {
            return null;
        } else {
            HashMap<String, String> result = new HashMap();
            StringTokenizer st = new StringTokenizer(delimitedString, ",", false);

            while(st.hasMoreElements()) {
                String pair = st.nextToken();
                StringTokenizer pst = new StringTokenizer(pair, ";", false);
                if (pst.hasMoreElements()) {
                    String name = pst.nextToken();
                    if (pst.hasMoreElements()) {
                        String value = pst.nextToken();
                        result.put(name, value);
                    }
                }
            }

            return result;
        }
    }

    public Reference getReference() throws NamingException {
        Reference reference = new Reference(this.getClass().getName(), P6DataSourceFactory.class.getName(), (String)null);
        reference.add(new StringRefAddr("dataSourceName", this.getRealDataSource()));
        return reference;
    }

    public int getLoginTimeout() throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        return this.realDataSource.getLoginTimeout();
    }

    public void setLoginTimeout(int inVar) throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        this.realDataSource.setLoginTimeout(inVar);
    }

    public PrintWriter getLogWriter() throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        return this.realDataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter inVar) throws SQLException {
        this.realDataSource.setLogWriter(inVar);
    }

    public Connection getConnection() throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        long start = System.nanoTime();
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        JdbcEventListener jdbcEventListener = this.jdbcEventListenerFactory.createJdbcEventListener();
        ConnectionInformation connectionInformation = ConnectionInformation.fromDataSource(this.realDataSource);
        jdbcEventListener.onBeforeGetConnection(connectionInformation);

        Connection conn;
        try {
            conn = ((DataSource)this.realDataSource).getConnection();
            connectionInformation.setConnection(conn);
            if (conn.getMetaData() != null) {
                connectionInformation.setUrl(conn.getMetaData().getURL());
            }

            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, (SQLException)null);
        } catch (SQLException var7) {
            SQLException e = var7;
            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, e);
            throw e;
        }

        return ConnectionWrapper.wrap(conn, jdbcEventListener, connectionInformation);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        long start = System.nanoTime();
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        JdbcEventListener jdbcEventListener = this.jdbcEventListenerFactory.createJdbcEventListener();
        ConnectionInformation connectionInformation = ConnectionInformation.fromDataSource(this.realDataSource);
        jdbcEventListener.onBeforeGetConnection(connectionInformation);

        Connection conn;
        try {
            conn = ((DataSource)this.realDataSource).getConnection(username, password);
            connectionInformation.setConnection(conn);
            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, (SQLException)null);
        } catch (SQLException var9) {
            SQLException e = var9;
            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, e);
            throw e;
        }

        return ConnectionWrapper.wrap(conn, jdbcEventListener, connectionInformation);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ((Wrapper)this.realDataSource).isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ((DataSource)this.realDataSource).unwrap(iface);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.realDataSource.getParentLogger();
    }

    public PooledConnection getPooledConnection() throws SQLException {
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        return new P6XAConnection(((ConnectionPoolDataSource)this.castRealDS(ConnectionPoolDataSource.class)).getPooledConnection(), this.jdbcEventListenerFactory);
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        return new P6XAConnection(((ConnectionPoolDataSource)this.castRealDS(ConnectionPoolDataSource.class)).getPooledConnection(user, password), this.jdbcEventListenerFactory);
    }

    public XAConnection getXAConnection() throws SQLException {
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        return new P6XAConnection(((XADataSource)this.castRealDS(XADataSource.class)).getXAConnection(), this.jdbcEventListenerFactory);
    }

    public XAConnection getXAConnection(String user, String password) throws SQLException {
        if (this.jdbcEventListenerFactory == null) {
            this.jdbcEventListenerFactory = (JdbcEventListenerFactory) JdbcEventListenerFactoryLoader.load();
        }

        return new P6XAConnection(((XADataSource)this.castRealDS(XADataSource.class)).getXAConnection(user, password), this.jdbcEventListenerFactory);
    }

    <T> T castRealDS(Class<T> iface) throws SQLException {
        if (this.realDataSource == null) {
            this.bindDataSource();
        }

        if (iface.isInstance(this.realDataSource)) {
            return (T) this.realDataSource;
        } else if (this.isWrapperFor(iface)) {
            return this.unwrap(iface);
        } else {
            throw new IllegalStateException("realdatasource type not supported: " + this.realDataSource);
        }
    }

    public void setJdbcEventListenerFactory(JdbcEventListenerFactory jdbcEventListenerFactory) {
        this.jdbcEventListenerFactory = jdbcEventListenerFactory;
    }
}


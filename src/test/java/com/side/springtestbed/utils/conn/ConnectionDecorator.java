package com.side.springtestbed.utils.conn;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionDecorator implements Connection {
    private final Connection target;
    private final ConnectionCallback callback;

    public ConnectionDecorator(Connection target, ConnectionCallback callback) {
        this.target = target;
        this.callback = callback;
    }

    public Connection getTarget() {
        return this.target;
    }

    public ConnectionCallback getCallback() {
        return this.callback;
    }

    public Statement createStatement() throws SQLException {
        return this.target.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.target.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.target.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        return this.target.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.target.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return this.target.getAutoCommit();
    }

    public void commit() throws SQLException {
        this.target.commit();
    }

    public void rollback() throws SQLException {
        this.target.rollback();
    }

    public void close() throws SQLException {
        this.callback.close();
        this.target.close();
    }

    public boolean isClosed() throws SQLException {
        return this.target.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return this.target.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        this.target.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return this.target.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        this.target.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return this.target.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        this.target.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return this.target.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return this.target.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        this.target.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.target.createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.target.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.target.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.target.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        this.target.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        this.target.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        return this.target.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        return this.target.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return this.target.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        this.target.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.target.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.target.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.target.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return this.target.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return this.target.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return this.target.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return this.target.prepareStatement(sql, columnNames);
    }

    public Clob createClob() throws SQLException {
        return this.target.createClob();
    }

    public Blob createBlob() throws SQLException {
        return this.target.createBlob();
    }

    public NClob createNClob() throws SQLException {
        return this.target.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return this.target.createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return this.target.isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        this.target.setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        this.target.setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return this.target.getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return this.target.getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return this.target.createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return this.target.createStruct(typeName, attributes);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.target.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.target.isWrapperFor(iface);
    }

    public void setSchema(String schema) throws SQLException {
        this.getTarget().setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return this.getTarget().getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        this.getTarget().abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        this.getTarget().setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return this.getTarget().getNetworkTimeout();
    }

    public void beginRequest() throws SQLException {
        this.getTarget().beginRequest();
    }

    public void endRequest() throws SQLException {
        this.getTarget().endRequest();
    }

    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
        return this.getTarget().setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return this.getTarget().setShardingKeyIfValid(shardingKey, timeout);
    }

    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
        this.getTarget().setShardingKey(shardingKey, superShardingKey);
    }

    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        this.getTarget().setShardingKey(shardingKey);
    }
}


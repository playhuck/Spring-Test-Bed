package com.side.springtestbed.utils.conn;

import java.sql.Connection;

public abstract class ConnectionProxyFactory {
    public ConnectionProxyFactory() {
    }

    public Connection newInstance(Connection target, ConnectionPoolCallback connectionPoolCallback) {
        return this.proxyConnection(target, new ConnectionCallback(connectionPoolCallback));
    }

    protected abstract Connection proxyConnection(Connection var1, ConnectionCallback var2);
}

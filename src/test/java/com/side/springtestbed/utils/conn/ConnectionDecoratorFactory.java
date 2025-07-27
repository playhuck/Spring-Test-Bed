package com.side.springtestbed.utils.conn;

import java.sql.Connection;

public class ConnectionDecoratorFactory extends ConnectionProxyFactory {
    public static final ConnectionProxyFactory INSTANCE = new ConnectionDecoratorFactory();

    public ConnectionDecoratorFactory() {
    }

    protected Connection proxyConnection(Connection target, ConnectionCallback callback) {
        return new ConnectionDecorator(target, callback);
    }
}

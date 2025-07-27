package com.side.springtestbed.utils.conn;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallable<T> {
    T execute(Connection connection) throws SQLException;
}


package com.side.springtestbed.utils.conn;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {
    Connection getConnection(ConnectionRequestContext var1) throws SQLException;
}


package com.side.springtestbed.utils;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionVoidCallable {
    void execute(Connection connection) throws SQLException;
}

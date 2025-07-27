package com.side.springtestbed.utils.conn;

public interface ConnectionPoolCallback {
    void acquireConnection();

    void releaseConnection(long var1);
}

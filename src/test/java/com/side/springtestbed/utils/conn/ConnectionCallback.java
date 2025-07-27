package com.side.springtestbed.utils.conn;

public class ConnectionCallback {
    private final ConnectionPoolCallback connectionPoolCallback;
    private final long startNanos = System.nanoTime();

    public ConnectionCallback(ConnectionPoolCallback connectionPoolCallback) {
        this.connectionPoolCallback = connectionPoolCallback;
        this.connectionPoolCallback.acquireConnection();
    }

    public void close() {
        long endNanos = System.nanoTime();
        long durationNanos = endNanos - this.startNanos;
        this.connectionPoolCallback.releaseConnection(durationNanos);
    }
}

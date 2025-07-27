package com.side.springtestbed.utils.event;

public class ConnectionLeaseTimeThresholdExceededEvent extends TimeThresholdExceededEvent {
    private static final long serialVersionUID = -2107982228572130887L;

    public ConnectionLeaseTimeThresholdExceededEvent(String uniqueName, long timeThresholdMillis, long actualTimeMillis) {
        super(uniqueName, timeThresholdMillis, actualTimeMillis);
    }
}

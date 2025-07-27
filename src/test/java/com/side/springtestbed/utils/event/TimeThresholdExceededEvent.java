package com.side.springtestbed.utils.event;

public abstract class TimeThresholdExceededEvent extends Event {
    private static final long serialVersionUID = 8983594872506186227L;
    private final long timeThresholdMillis;
    private final long actualTimeMillis;

    public TimeThresholdExceededEvent(String uniqueName, long timeThresholdMillis, long actualTimeMillis) {
        super(uniqueName);
        this.timeThresholdMillis = timeThresholdMillis;
        this.actualTimeMillis = actualTimeMillis;
    }

    public long getTimeThresholdMillis() {
        return this.timeThresholdMillis;
    }

    public long getActualTimeMillis() {
        return this.actualTimeMillis;
    }
}

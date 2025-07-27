package com.side.springtestbed.utils.event;

public class ConnectionAcquisitionTimeoutEvent extends Event {
    private static final long serialVersionUID = -1769599416259900943L;

    public ConnectionAcquisitionTimeoutEvent(String uniqueName) {
        super(uniqueName);
    }
}

package com.side.springtestbed.common.exception;

import java.sql.SQLException;

public class ConnectionAcquisitionTimeoutException extends SQLException {
    private static final long serialVersionUID = -5632855297822130922L;

    public ConnectionAcquisitionTimeoutException(Throwable cause) {
        super(cause);
    }
}


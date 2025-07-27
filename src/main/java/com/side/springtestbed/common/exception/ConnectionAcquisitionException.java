package com.side.springtestbed.common.exception;

import java.sql.SQLException;

public class ConnectionAcquisitionException extends SQLException {
    private static final long serialVersionUID = 2752173976156070744L;

    public ConnectionAcquisitionException(String reason) {
        super(reason);
    }
}
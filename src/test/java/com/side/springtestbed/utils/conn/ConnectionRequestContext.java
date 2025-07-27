package com.side.springtestbed.utils.conn;

import com.side.springtestbed.utils.utils.Credentials;

public final class ConnectionRequestContext {
    private final Credentials credentials;
    private int retryAttempts;

    private ConnectionRequestContext(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    public int getRetryAttempts() {
        return this.retryAttempts;
    }

    public void incrementAttempts() {
        ++this.retryAttempts;
    }

    public String toString() {
        return "ConnectionRequestContext{credentials=" + this.credentials + ", retryAttempts=" + this.retryAttempts + "}";
    }

    public static class Builder {
        private Credentials credentials;

        public Builder() {
        }

        public Builder setCredentials(Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public ConnectionRequestContext build() {
            return new ConnectionRequestContext(this.credentials);
        }
    }
}

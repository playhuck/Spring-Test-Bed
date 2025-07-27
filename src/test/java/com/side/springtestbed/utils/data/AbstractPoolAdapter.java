package com.side.springtestbed.utils.data;

import com.side.springtestbed.utils.event.ConnectionAcquisitionTimeoutEvent;
import com.side.springtestbed.common.exception.ConnectionAcquisitionTimeoutException;
import com.side.springtestbed.utils.ConfigurationProperties;
import com.side.springtestbed.utils.conn.ConnectionRequestContext;
import com.side.springtestbed.utils.conn.PoolAdapter;
import com.side.springtestbed.utils.event.EventPublisher;
import com.side.springtestbed.utils.metrics.Metrics;
import com.side.springtestbed.utils.utils.Credentials;
import com.side.springtestbed.utils.utils.Timer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPoolAdapter<T extends DataSource> implements PoolAdapter<T> {
    public static final String CONNECTION_ACQUISITION_MILLIS = "connectionAcquisitionMillis";
    private final ConfigurationProperties<T, Metrics, PoolAdapter<T>> configurationProperties;
    private final T targetDataSource;
    private final Timer connectionAcquisitionTimer;
    private final EventPublisher eventPublisher;

    public AbstractPoolAdapter(ConfigurationProperties<T, Metrics, PoolAdapter<T>> configurationProperties) {
        this.configurationProperties = configurationProperties;
        this.targetDataSource = configurationProperties.getTargetDataSource();
        this.connectionAcquisitionTimer = ((Metrics)configurationProperties.getMetrics()).timer("connectionAcquisitionMillis");
        this.eventPublisher = configurationProperties.getEventPublisher();
    }

    public T getTargetDataSource() {
        return this.targetDataSource;
    }

    public Connection getConnection(ConnectionRequestContext requestContext) throws SQLException {
        long startNanos = System.nanoTime();
        boolean var14 = false;

        Connection var5;
        try {
            var14 = true;
            Credentials credentials = requestContext.getCredentials();
            var5 = credentials == null ? this.targetDataSource.getConnection() : this.targetDataSource.getConnection(credentials.getUsername(), credentials.getPassword());
            var14 = false;
        } catch (SQLException var15) {
            SQLException e = var15;
            throw this.translateException(e);
        } catch (RuntimeException var16) {
            RuntimeException e = var16;
            throw this.translateException(e);
        } finally {
            if (var14) {
                long endNanos = System.nanoTime();
                this.connectionAcquisitionTimer.update(TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos), TimeUnit.MILLISECONDS);
            }
        }

        long endNanos = System.nanoTime();
        this.connectionAcquisitionTimer.update(TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos), TimeUnit.MILLISECONDS);
        return var5;
    }

    protected SQLException translateException(Exception e) {
        if (this.isTimeoutAcquisitionException(e)) {
            this.eventPublisher.publish(new ConnectionAcquisitionTimeoutEvent(this.configurationProperties.getUniqueName()));
            return new ConnectionAcquisitionTimeoutException(e);
        } else {
            return e instanceof SQLException ? (SQLException)e : new SQLException(e);
        }
    }

    protected abstract boolean isTimeoutAcquisitionException(Exception var1);
}


package com.side.springtestbed.utils;

import com.side.springtestbed.utils.conn.ConnectionProxyFactory;
import com.side.springtestbed.utils.event.EventPublisher;
import com.side.springtestbed.utils.metrics.MetricNamingStrategy;

import javax.sql.DataSource;

public abstract class ConfigurationProperties<T extends DataSource, M, P> {
    private final String uniqueName;
    private final EventPublisher eventPublisher;
    private boolean jmxEnabled;
    private boolean jmxAutoStart;
    private long metricLogReporterMillis;
    private MetricNamingStrategy metricNamingStrategy;
    private long connectionAcquisitionTimeThresholdMillis = Long.MAX_VALUE;
    private long connectionLeaseTimeThresholdMillis = Long.MAX_VALUE;

    public ConfigurationProperties(String uniqueName, EventPublisher eventPublisher) {
        this.uniqueName = uniqueName;
        this.eventPublisher = eventPublisher;
    }

    public String getUniqueName() {
        return this.uniqueName;
    }

    public EventPublisher getEventPublisher() {
        return this.eventPublisher;
    }

    public boolean isJmxEnabled() {
        return this.jmxEnabled;
    }

    protected void setJmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

    public boolean isJmxAutoStart() {
        return this.jmxAutoStart;
    }

    public void setJmxAutoStart(boolean jmxAutoStart) {
        this.jmxAutoStart = jmxAutoStart;
    }

    public long getMetricLogReporterMillis() {
        return this.metricLogReporterMillis;
    }

    protected void setMetricLogReporterMillis(long metricLogReporterMillis) {
        this.metricLogReporterMillis = metricLogReporterMillis;
    }

    public MetricNamingStrategy getMetricNamingStrategy() {
        return this.metricNamingStrategy;
    }

    public void setMetricNamingStrategy(MetricNamingStrategy metricNamingStrategy) {
        this.metricNamingStrategy = metricNamingStrategy;
    }

    public long getConnectionAcquisitionTimeThresholdMillis() {
        return this.connectionAcquisitionTimeThresholdMillis;
    }

    public void setConnectionAcquisitionTimeThresholdMillis(long connectionAcquisitionTimeThresholdMillis) {
        this.connectionAcquisitionTimeThresholdMillis = connectionAcquisitionTimeThresholdMillis;
    }

    public long getConnectionLeaseTimeThresholdMillis() {
        return this.connectionLeaseTimeThresholdMillis;
    }

    public void setConnectionLeaseTimeThresholdMillis(long connectionLeaseTimeThresholdMillis) {
        this.connectionLeaseTimeThresholdMillis = connectionLeaseTimeThresholdMillis;
    }

    public abstract T getTargetDataSource();

    public abstract M getMetrics();

    public abstract P getPoolAdapter();

    public abstract ConnectionProxyFactory getConnectionProxyFactory();
}


package com.side.springtestbed.utils.conn;

import com.side.springtestbed.utils.*;
import com.side.springtestbed.utils.event.EventListenerResolver;
import com.side.springtestbed.utils.event.EventPublisher;
import com.side.springtestbed.utils.metrics.MetricNamingStrategy;
import com.side.springtestbed.utils.metrics.Metrics;
import com.side.springtestbed.utils.metrics.MetricsFactory;
import com.side.springtestbed.utils.metrics.MetricsFactoryResolver;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

public final class FlexyPoolConfiguration<T extends DataSource> extends ConfigurationProperties<T, Metrics, PoolAdapter<T>> {
    public static final long DEFAULT_METRIC_LOG_REPORTER_MILLIS;
    private final T targetDataSource;
    private Metrics metrics;
    private PoolAdapter<T> poolAdapter;
    private ConnectionProxyFactory connectionProxyFactory;

    private FlexyPoolConfiguration(String uniqueName, T targetDataSource, EventPublisher eventPublisher) {
        super(uniqueName, eventPublisher);
        this.targetDataSource = targetDataSource;
    }

    public T getTargetDataSource() {
        return this.targetDataSource;
    }

    public Metrics getMetrics() {
        return this.metrics;
    }

    public PoolAdapter<T> getPoolAdapter() {
        return this.poolAdapter;
    }

    public ConnectionProxyFactory getConnectionProxyFactory() {
        return this.connectionProxyFactory;
    }

    static {
        DEFAULT_METRIC_LOG_REPORTER_MILLIS = TimeUnit.MINUTES.toMillis(5L);
    }

    public static class Builder<T extends DataSource> {
        private final String uniqueName;
        private final T targetDataSource;
        private final PoolAdapterFactory<T> poolAdapterFactory;
        private MetricsFactory metricsFactory;
        private ConnectionProxyFactory connectionProxyFactory;
        private boolean jmxEnabled;
        private boolean jmxAutoStart;
        private long metricLogReporterMillis;
        private MetricNamingStrategy metricNamingStrategy;
        private EventListenerResolver eventListenerResolver;
        private long connectionAcquisitionTimeThresholdMillis;
        private long connectionLeaseTimeThresholdMillis;

        public Builder(String uniqueName, T targetDataSource, PoolAdapterFactory<T> poolAdapterFactory) {
            this.connectionProxyFactory = ConnectionDecoratorFactoryResolver.INSTANCE.resolve();
            this.jmxEnabled = true;
            this.jmxAutoStart = false;
            this.metricLogReporterMillis = FlexyPoolConfiguration.DEFAULT_METRIC_LOG_REPORTER_MILLIS;
            this.metricNamingStrategy = new DefaultNamingStrategy();
            this.connectionAcquisitionTimeThresholdMillis = Long.MAX_VALUE;
            this.connectionLeaseTimeThresholdMillis = Long.MAX_VALUE;
            this.uniqueName = uniqueName;
            this.targetDataSource = targetDataSource;
            this.poolAdapterFactory = poolAdapterFactory;
        }

        public Builder<T> setMetricsFactory(MetricsFactory metricsFactory) {
            this.metricsFactory = metricsFactory;
            return this;
        }

        public Builder<T> setConnectionProxyFactory(ConnectionProxyFactory connectionProxyFactory) {
            this.connectionProxyFactory = connectionProxyFactory;
            return this;
        }

        public Builder<T> setJmxEnabled(boolean enableJmx) {
            this.jmxEnabled = enableJmx;
            return this;
        }

        public Builder<T> setJmxAutoStart(boolean jmxAutoStart) {
            this.jmxAutoStart = jmxAutoStart;
            return this;
        }

        public Builder<T> setMetricLogReporterMillis(long metricLogReporterMillis) {
            this.metricLogReporterMillis = metricLogReporterMillis;
            return this;
        }

        public Builder<T> setMetricNamingUniqueName(MetricNamingStrategy metricNamingStrategy) {
            this.metricNamingStrategy = metricNamingStrategy;
            return this;
        }

        public Builder<T> setEventListenerResolver(EventListenerResolver eventListenerResolver) {
            this.eventListenerResolver = eventListenerResolver;
            return this;
        }

        public Builder<T> setConnectionAcquisitionTimeThresholdMillis(Long connectionAcquisitionTimeThresholdMillis) {
            if (connectionAcquisitionTimeThresholdMillis != null) {
                this.connectionAcquisitionTimeThresholdMillis = connectionAcquisitionTimeThresholdMillis;
            }

            return this;
        }

        public Builder<T> setConnectionLeaseTimeThresholdMillis(Long connectionLeaseTimeThresholdMillis) {
            if (connectionLeaseTimeThresholdMillis != null) {
                this.connectionLeaseTimeThresholdMillis = connectionLeaseTimeThresholdMillis;
            }

            return this;
        }

        public FlexyPoolConfiguration<T> build() {
            EventPublisher eventPublisher = EventPublisher.newInstance(this.eventListenerResolver);
            FlexyPoolConfiguration<T> configuration = new FlexyPoolConfiguration(this.uniqueName, this.targetDataSource, eventPublisher);
            configuration.setJmxEnabled(this.jmxEnabled);
            configuration.setJmxAutoStart(this.jmxAutoStart);
            configuration.setMetricLogReporterMillis(this.metricLogReporterMillis);
            configuration.setMetricNamingStrategy(this.metricNamingStrategy);
            configuration.setConnectionAcquisitionTimeThresholdMillis(this.connectionAcquisitionTimeThresholdMillis);
            configuration.setConnectionLeaseTimeThresholdMillis(this.connectionLeaseTimeThresholdMillis);
            if (this.metricsFactory == null) {
                this.metricsFactory = MetricsFactoryResolver.INSTANCE.resolve();
            }

            configuration.metrics = this.metricsFactory.newInstance(configuration);
            configuration.poolAdapter = this.poolAdapterFactory.newInstance(configuration);
            configuration.connectionProxyFactory = this.connectionProxyFactory;
            return configuration;
        }
    }
}

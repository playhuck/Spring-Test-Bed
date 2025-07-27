package com.side.springtestbed.utils.conn;

import com.side.springtestbed.common.exception.ConnectionAcquisitionException;
import com.side.springtestbed.common.exception.ConnectionAcquisitionTimeoutException;
import com.side.springtestbed.utils.PropertyLoader;
import com.side.springtestbed.utils.data.DataSourcePoolAdapter;
import com.side.springtestbed.utils.event.ConnectionAcquisitionTimeThresholdExceededEvent;
import com.side.springtestbed.utils.event.ConnectionLeaseTimeThresholdExceededEvent;
import com.side.springtestbed.utils.utils.Timer;
import com.side.springtestbed.utils.event.EventListenerResolver;
import com.side.springtestbed.utils.event.EventPublisher;
import com.side.springtestbed.utils.metrics.Metrics;
import com.side.springtestbed.utils.metrics.MetricsFactory;
import com.side.springtestbed.utils.utils.Credentials;
import com.side.springtestbed.utils.utils.Histogram;
import com.side.springtestbed.utils.utils.LifeCycleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class FlexyPoolDataSource<T extends DataSource> implements DataSource, LifeCycleCallback, ConnectionPoolCallback, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexyPoolDataSource.class);
    public static final String OVERALL_CONNECTION_ACQUISITION_MILLIS = "overallConnectionAcquisitionMillis";
    public static final String CONCURRENT_CONNECTIONS_HISTOGRAM = "concurrentConnectionsHistogram";
    public static final String CONCURRENT_CONNECTION_REQUESTS_HISTOGRAM = "concurrentConnectionRequestsHistogram";
    public static final String CONNECTION_LEASE_MILLIS = "connectionLeaseMillis";
    private final String uniqueName;
    private final PoolAdapter<T> poolAdapter;
    private final T targetDataSource;
    private final Metrics metrics;
    private final Timer connectionAcquisitionTotalTimer;
    private final Histogram concurrentConnectionCountHistogram;
    private final Histogram concurrentConnectionRequestCountHistogram;
    private final Timer connectionLeaseTimer;
    private final ConnectionProxyFactory connectionProxyFactory;
    private final Collection<ConnectionAcquisitionStrategy> connectionAcquiringStrategies;
    private AtomicLong concurrentConnectionCount;
    private AtomicLong concurrentConnectionRequestCount;
    private final EventPublisher eventPublisher;
    private final long connectionAcquisitionTimeThresholdMillis;
    private final long connectionLeaseTimeThresholdMillis;

    public FlexyPoolDataSource(FlexyPoolConfiguration<T> configuration, ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, T>... connectionAcquiringStrategyFactories) {
        this(configuration, Arrays.asList(connectionAcquiringStrategyFactories));
    }

    public FlexyPoolDataSource() {
        this((new ConfigurationLoader()).getFlexyPoolDataSourceConfiguration());
    }

    public FlexyPoolDataSource(T targetDataSource) {
        this((new ConfigurationLoader(targetDataSource)).getFlexyPoolDataSourceConfiguration());
    }

    public FlexyPoolDataSource(T targetDataSource, Properties overridingProperties) {
        this((new ConfigurationLoader(targetDataSource, overridingProperties)).getFlexyPoolDataSourceConfiguration());
    }

    private FlexyPoolDataSource(FlexyPoolConfiguration<T> configuration, List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, T>> connectionAcquiringStrategyFactories) {
        this.connectionAcquiringStrategies = new LinkedHashSet();
        this.concurrentConnectionCount = new AtomicLong();
        this.concurrentConnectionRequestCount = new AtomicLong();
        this.uniqueName = configuration.getUniqueName();
        this.poolAdapter = configuration.getPoolAdapter();
        this.targetDataSource = this.poolAdapter.getTargetDataSource();
        this.metrics = configuration.getMetrics();
        this.connectionAcquisitionTotalTimer = this.metrics.timer("overallConnectionAcquisitionMillis");
        this.concurrentConnectionCountHistogram = this.metrics.histogram("concurrentConnectionsHistogram");
        this.concurrentConnectionRequestCountHistogram = this.metrics.histogram("concurrentConnectionRequestsHistogram");
        this.connectionLeaseTimer = this.metrics.timer("connectionLeaseMillis");
        this.connectionProxyFactory = configuration.getConnectionProxyFactory();
        if (connectionAcquiringStrategyFactories.isEmpty()) {
            LOGGER.info("FlexyPool is not using any strategy!");
        }

        Iterator var3 = connectionAcquiringStrategyFactories.iterator();

        while(var3.hasNext()) {
            ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, T> connectionAcquiringStrategyFactory = (ConnectionAcquisitionStrategyFactory)var3.next();
            this.connectionAcquiringStrategies.add(connectionAcquiringStrategyFactory.newInstance(configuration));
        }

        this.eventPublisher = configuration.getEventPublisher();
        this.connectionAcquisitionTimeThresholdMillis = configuration.getConnectionAcquisitionTimeThresholdMillis();
        this.connectionLeaseTimeThresholdMillis = configuration.getConnectionLeaseTimeThresholdMillis();
    }

    private FlexyPoolDataSource(FlexyPoolDataSourceConfiguration<T> flexyPoolDataSourceConfiguration) {
        this(flexyPoolDataSourceConfiguration.getConfiguration(), flexyPoolDataSourceConfiguration.getConnectionAcquiringStrategyFactories());
    }

    public Connection getConnection() throws SQLException {
        return this.getConnection((new ConnectionRequestContext.Builder()).build());
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return this.getConnection((new ConnectionRequestContext.Builder()).setCredentials(new Credentials(username, password)).build());
    }

    public T getTargetDataSource() {
        return this.targetDataSource;
    }

    private Connection getConnection(ConnectionRequestContext context) throws SQLException {
        this.concurrentConnectionRequestCountHistogram.update(this.concurrentConnectionRequestCount.incrementAndGet());
        long startNanos = System.nanoTime();
        boolean var17 = false;

        Connection var20;
        try {
            var17 = true;
            Connection connection = null;
            if (this.connectionAcquiringStrategies.isEmpty()) {
                connection = this.poolAdapter.getConnection(context);
            } else {
                Iterator var5 = this.connectionAcquiringStrategies.iterator();

                while(var5.hasNext()) {
                    ConnectionAcquisitionStrategy strategy = (ConnectionAcquisitionStrategy)var5.next();

                    try {
                        connection = strategy.getConnection(context);
                        break;
                    } catch (ConnectionAcquisitionTimeoutException var18) {
                        LOGGER.warn("Couldn't retrieve connection from strategy {} with context {}", strategy, context);
                    }
                }
            }

            if (connection == null) {
                throw new ConnectionAcquisitionException("Couldn't acquire connection for current strategies: " + this.connectionAcquiringStrategies);
            }

            var20 = this.connectionProxyFactory.newInstance(connection, this);
            var17 = false;
        } finally {
            if (var17) {
                long endNanos = System.nanoTime();
                long acquisitionDurationMillis = TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos);
                this.connectionAcquisitionTotalTimer.update(acquisitionDurationMillis, TimeUnit.MILLISECONDS);
                this.concurrentConnectionRequestCountHistogram.update(this.concurrentConnectionRequestCount.decrementAndGet());
                if (acquisitionDurationMillis > this.connectionAcquisitionTimeThresholdMillis) {
                    this.eventPublisher.publish(new ConnectionAcquisitionTimeThresholdExceededEvent(this.uniqueName, this.connectionAcquisitionTimeThresholdMillis, acquisitionDurationMillis));
                    LOGGER.info("Connection acquired in {} millis, while threshold is set to {} in {} FlexyPoolDataSource", new Object[]{acquisitionDurationMillis, this.connectionAcquisitionTimeThresholdMillis, this.uniqueName});
                }

            }
        }

        long endNanos = System.nanoTime();
        long acquisitionDurationMillis = TimeUnit.NANOSECONDS.toMillis(endNanos - startNanos);
        this.connectionAcquisitionTotalTimer.update(acquisitionDurationMillis, TimeUnit.MILLISECONDS);
        this.concurrentConnectionRequestCountHistogram.update(this.concurrentConnectionRequestCount.decrementAndGet());
        if (acquisitionDurationMillis > this.connectionAcquisitionTimeThresholdMillis) {
            this.eventPublisher.publish(new ConnectionAcquisitionTimeThresholdExceededEvent(this.uniqueName, this.connectionAcquisitionTimeThresholdMillis, acquisitionDurationMillis));
            LOGGER.info("Connection acquired in {} millis, while threshold is set to {} in {} FlexyPoolDataSource", new Object[]{acquisitionDurationMillis, this.connectionAcquisitionTimeThresholdMillis, this.uniqueName});
        }

        return var20;
    }

    public void acquireConnection() {
        this.concurrentConnectionCountHistogram.update(this.concurrentConnectionCount.incrementAndGet());
    }

    public void releaseConnection(long leaseDurationNanos) {
        this.concurrentConnectionCountHistogram.update(this.concurrentConnectionCount.decrementAndGet());
        long leaseDurationMillis = TimeUnit.NANOSECONDS.toMillis(leaseDurationNanos);
        this.connectionLeaseTimer.update(leaseDurationMillis, TimeUnit.MILLISECONDS);
        if (leaseDurationMillis > this.connectionLeaseTimeThresholdMillis) {
            this.eventPublisher.publish(new ConnectionLeaseTimeThresholdExceededEvent(this.uniqueName, this.connectionLeaseTimeThresholdMillis, leaseDurationMillis));
            LOGGER.info("Connection leased for {} millis, while threshold is set to {} in {} FlexyPoolDataSource", new Object[]{leaseDurationMillis, this.connectionLeaseTimeThresholdMillis, this.uniqueName});
        }

    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.targetDataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        this.targetDataSource.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return this.targetDataSource.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        this.targetDataSource.setLoginTimeout(seconds);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.targetDataSource.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.targetDataSource.isWrapperFor(iface);
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return java.util.logging.Logger.getLogger("global");
    }

    public void start() {
        this.metrics.start();
    }

    public void stop() {
        this.metrics.stop();
    }

    public void close() throws IOException {
        this.metrics.stop();
        if (this.targetDataSource instanceof Closeable) {
            ((Closeable)this.targetDataSource).close();
        }

    }

    private static class ConfigurationLoader<D extends DataSource> {
        private final PropertyLoader propertyLoader;
        private final FlexyPoolDataSourceConfiguration<D> flexyPoolDataSourceConfiguration;

        public ConfigurationLoader() {
            this.propertyLoader = new PropertyLoader();
            D dataSource = this.propertyLoader.getDataSource();
            this.flexyPoolDataSourceConfiguration = this.init(dataSource);
        }

        public ConfigurationLoader(D dataSource) {
            this.propertyLoader = new PropertyLoader();
            this.flexyPoolDataSourceConfiguration = this.init(dataSource);
        }

        public ConfigurationLoader(D dataSource, Properties overridingProperties) {
            this.propertyLoader = new PropertyLoader(overridingProperties);
            this.flexyPoolDataSourceConfiguration = this.init(dataSource);
        }

        private FlexyPoolDataSourceConfiguration<D> init(D dataSource) {
            return new FlexyPoolDataSourceConfiguration(this.configuration(dataSource), this.connectionAcquiringStrategyFactories());
        }

        private FlexyPoolConfiguration<D> configuration(D dataSource) {
            String uniqueName = this.propertyLoader.getUniqueName();
            PoolAdapterFactory<D> poolAdapterFactory = this.propertyLoader.getPoolAdapterFactory();
            MetricsFactory metricsFactory = this.propertyLoader.getMetricsFactory();
            ConnectionProxyFactory connectionProxyFactory = this.propertyLoader.getConnectionProxyFactory();
            Integer metricLogReporterMillis = this.propertyLoader.getMetricLogReporterMillis();
            Boolean jmxEnabled = this.propertyLoader.isJmxEnabled();
            Boolean jmxAutoStart = this.propertyLoader.isJmxAutoStart();
            EventListenerResolver eventListenerResolver = this.propertyLoader.getEventListenerResolver();
            Long connectionAcquisitionTimeThresholdMillis = this.propertyLoader.getConnectionAcquisitionTimeThresholdMillis();
            Long connectionLeaseTimeThresholdMillis = this.propertyLoader.getConnectionLeaseTimeThresholdMillis();
            if (poolAdapterFactory == null) {
                poolAdapterFactory = (PoolAdapterFactory<D>) DataSourcePoolAdapter.FACTORY;
            }

            FlexyPoolConfiguration.Builder<D> configurationBuilder = new FlexyPoolConfiguration.Builder(uniqueName, dataSource, poolAdapterFactory);
            if (metricsFactory != null) {
                configurationBuilder.setMetricsFactory(metricsFactory);
            }

            if (connectionProxyFactory != null) {
                configurationBuilder.setConnectionProxyFactory(connectionProxyFactory);
            }

            if (metricLogReporterMillis != null) {
                configurationBuilder.setMetricLogReporterMillis((long)metricLogReporterMillis);
            }

            if (jmxEnabled != null) {
                configurationBuilder.setJmxEnabled(jmxEnabled);
            }

            if (jmxAutoStart != null) {
                configurationBuilder.setJmxAutoStart(jmxAutoStart);
            }

            if (eventListenerResolver != null) {
                configurationBuilder.setEventListenerResolver(eventListenerResolver);
            }

            if (connectionAcquisitionTimeThresholdMillis != null) {
                configurationBuilder.setConnectionAcquisitionTimeThresholdMillis(connectionAcquisitionTimeThresholdMillis);
            }

            if (connectionLeaseTimeThresholdMillis != null) {
                configurationBuilder.setConnectionLeaseTimeThresholdMillis(connectionLeaseTimeThresholdMillis);
            }

            return configurationBuilder.build();
        }

        private List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, D>> connectionAcquiringStrategyFactories() {
            return this.propertyLoader.getConnectionAcquiringStrategyFactories();
        }

        public FlexyPoolDataSourceConfiguration<D> getFlexyPoolDataSourceConfiguration() {
            return this.flexyPoolDataSourceConfiguration;
        }
    }

    private static class FlexyPoolDataSourceConfiguration<D extends DataSource> {
        private final FlexyPoolConfiguration<D> configuration;
        private final List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, D>> connectionAcquiringStrategyFactories;

        public FlexyPoolDataSourceConfiguration(FlexyPoolConfiguration<D> configuration, List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, D>> connectionAcquiringStrategyFactories) {
            this.configuration = configuration;
            this.connectionAcquiringStrategyFactories = connectionAcquiringStrategyFactories;
        }

        public FlexyPoolConfiguration<D> getConfiguration() {
            return this.configuration;
        }

        public List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, D>> getConnectionAcquiringStrategyFactories() {
            return this.connectionAcquiringStrategyFactories;
        }
    }
}

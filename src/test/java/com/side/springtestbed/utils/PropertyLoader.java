package com.side.springtestbed.utils;

import com.side.springtestbed.utils.conn.*;
import com.side.springtestbed.utils.event.EventListenerResolver;
import com.side.springtestbed.utils.metrics.MetricNamingStrategy;
import com.side.springtestbed.utils.metrics.MetricsFactory;
import com.side.springtestbed.utils.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class PropertyLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyLoader.class);
    public static final String PROPERTIES_FILE_PATH = "flexy.pool.properties.path";
    public static final String PROPERTIES_FILE_NAME = "flexy-pool.properties";
    private final Properties properties;

    public PropertyLoader() {
        this.properties = new Properties();
        this.load();
    }

    public PropertyLoader(Properties overridingProperties) {
        this();
        this.properties.putAll(overridingProperties);
    }

    private void load() {
        InputStream propertiesInputStream = null;

        try {
            propertiesInputStream = this.propertiesInputStream();
            if (propertiesInputStream == null) {
                throw new IllegalArgumentException("The properties file could not be loaded!");
            }

            this.properties.load(propertiesInputStream);
        } catch (IOException var11) {
            IOException e = var11;
            LOGGER.error("Can't load properties", e);
        } finally {
            try {
                if (propertiesInputStream != null) {
                    propertiesInputStream.close();
                }
            } catch (IOException var10) {
                IOException e = var10;
                LOGGER.error("Can't close the properties InputStream", e);
            }

        }

    }

    private InputStream propertiesInputStream() throws IOException {
        String propertiesFilePath = System.getProperty("flexy.pool.properties.path");
        URL propertiesFileUrl = null;
        if (propertiesFilePath != null) {
            try {
                propertiesFileUrl = new URL(propertiesFilePath);
            } catch (MalformedURLException var7) {
                propertiesFileUrl = ClassLoaderUtils.getResource(propertiesFilePath);
                if (propertiesFileUrl == null) {
                    File f = new File(propertiesFilePath);
                    if (f.exists() && f.isFile()) {
                        try {
                            propertiesFileUrl = f.toURI().toURL();
                        } catch (MalformedURLException var6) {
                            LOGGER.error("The property " + propertiesFilePath + " can't be resolved to either a URL/a Classpath resource or a File");
                        }
                    }
                }
            }

            if (propertiesFileUrl != null) {
                return propertiesFileUrl.openStream();
            }
        }

        return ClassLoaderUtils.getResourceAsStream("flexy-pool.properties");
    }

    public String getUniqueName() {
        return this.properties.getProperty(PropertyLoader.PropertyKey.DATA_SOURCE_UNIQUE_NAME.getKey());
    }

    public <T extends DataSource> T getDataSource() {
        T dataSource = (T) this.jndiLookup(PropertyKey.DATA_SOURCE_JNDI_NAME);
        if (dataSource != null) {
            return dataSource;
        } else {
            dataSource = (T) this.instantiateClass(PropertyKey.DATA_SOURCE_CLASS_NAME);
            if (dataSource == null) {
                throw new IllegalArgumentException("The " + PropertyLoader.PropertyKey.DATA_SOURCE_CLASS_NAME + " property is mandatory!");
            } else {
                return this.applyDataSourceProperties(dataSource);
            }
        }
    }

    private <T extends DataSource> T applyDataSourceProperties(T dataSource) {
        Iterator var2 = this.properties.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Object, Object> entry = (Map.Entry)var2.next();
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            String propertyKey = PropertyLoader.PropertyKey.DATA_SOURCE_PROPERTY.getKey();
            if (key.startsWith(propertyKey)) {
                String dataSourceProperty = key.substring(propertyKey.length());
                ReflectionUtils.invokeSetter(dataSource, dataSourceProperty, value);
            }
        }

        return dataSource;
    }

    public <T extends DataSource> PoolAdapterFactory<T> getPoolAdapterFactory() {
        return (PoolAdapterFactory)this.instantiateClass(PropertyLoader.PropertyKey.POOL_ADAPTER_FACTORY);
    }

    public MetricsFactory getMetricsFactory() {
        return (MetricsFactory)this.instantiateClass(PropertyLoader.PropertyKey.POOL_METRICS_FACTORY);
    }

    public ConnectionProxyFactory getConnectionProxyFactory() {
        return (ConnectionProxyFactory)this.instantiateClass(PropertyLoader.PropertyKey.POOL_CONNECTION_PROXY_FACTORY);
    }

    public Integer getMetricLogReporterMillis() {
        return this.integerProperty(PropertyLoader.PropertyKey.POOL_METRICS_REPORTER_LOG_MILLIS);
    }

    public Boolean isJmxEnabled() {
        return this.booleanProperty(PropertyLoader.PropertyKey.POOL_METRICS_REPORTER_JMX_ENABLE);
    }

    public Boolean isJmxAutoStart() {
        return this.booleanProperty(PropertyLoader.PropertyKey.POOL_METRICS_REPORTER_JMX_AUTO_START);
    }

    public MetricNamingStrategy getMetricNamingStrategy() {
        return (MetricNamingStrategy)this.instantiateClass(PropertyLoader.PropertyKey.POOL_METRICS_NAMING_STRATEGY);
    }

    public boolean isJndiLazyLookup() {
        return Boolean.TRUE.equals(this.booleanProperty(PropertyLoader.PropertyKey.DATA_SOURCE_JNDI_LAZY_LOOKUP));
    }

    public <T extends DataSource> List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, T>> getConnectionAcquiringStrategyFactories() {
        ConnectionAcquisitionFactoryResolver<T> connectionAcquiringStrategyFactoryResolver = (ConnectionAcquisitionFactoryResolver)this.instantiateClass(PropertyLoader.PropertyKey.POOL_STRATEGIES_FACTORY_RESOLVER);
        return connectionAcquiringStrategyFactoryResolver != null ? connectionAcquiringStrategyFactoryResolver.resolveFactories() : Collections.emptyList();
    }

    public EventListenerResolver getEventListenerResolver() {
        return (EventListenerResolver)this.instantiateClass(PropertyLoader.PropertyKey.POOL_EVENT_LISTENER_RESOLVER);
    }

    public Long getConnectionAcquisitionTimeThresholdMillis() {
        return this.longProperty(PropertyLoader.PropertyKey.POOL_TIME_THRESHOLD_CONNECTION_ACQUISITION);
    }

    public Long getConnectionLeaseTimeThresholdMillis() {
        return this.longProperty(PropertyLoader.PropertyKey.POOL_TIME_THRESHOLD_CONNECTION_LEASE);
    }

    private <T> T instantiateClass(PropertyKey propertyKey) {
        T object = null;
        String property = this.properties.getProperty(propertyKey.getKey());
        if (property != null) {
            try {
                Class<T> clazz = ClassLoaderUtils.loadClass(property);
                LOGGER.debug("Instantiate {}", clazz);
                object = clazz.newInstance();
            } catch (ClassNotFoundException var5) {
                ClassNotFoundException e = var5;
                LOGGER.error("Couldn't load the " + property + " class given by the " + propertyKey + " property", e);
            } catch (InstantiationException var6) {
                InstantiationException e = var6;
                LOGGER.error("Couldn't instantiate the " + property + " class given by the " + propertyKey + " property", e);
            } catch (IllegalAccessException var7) {
                IllegalAccessException e = var7;
                LOGGER.error("Couldn't access the " + property + " class given by the " + propertyKey + " property", e);
            }
        }

        return object;
    }

    private Integer integerProperty(PropertyKey propertyKey) {
        Integer value = null;
        String property = this.properties.getProperty(propertyKey.getKey());
        if (property != null) {
            value = Integer.valueOf(property);
        }

        return value;
    }

    private Long longProperty(PropertyKey propertyKey) {
        Long value = null;
        String property = this.properties.getProperty(propertyKey.getKey());
        if (property != null) {
            value = Long.valueOf(property);
        }

        return value;
    }

    private Boolean booleanProperty(PropertyKey propertyKey) {
        Boolean value = null;
        String property = this.properties.getProperty(propertyKey.getKey());
        if (property != null) {
            value = Boolean.valueOf(property);
        }

        return value;
    }

    private <T> T jndiLookup(PropertyKey propertyKey) {
        String property = this.properties.getProperty(propertyKey.getKey());
        if (property != null) {
            return this.isJndiLazyLookup() ? LazyJndiResolver.newInstance(property, DataSource.class) : JndiUtils.lookup(property);
        } else {
            return null;
        }
    }

    public static enum PropertyKey {
        DATA_SOURCE_UNIQUE_NAME("flexy.pool.data.source.unique.name"),
        DATA_SOURCE_JNDI_NAME("flexy.pool.data.source.jndi.name"),
        DATA_SOURCE_JNDI_LAZY_LOOKUP("flexy.pool.data.source.jndi.lazy.lookup"),
        DATA_SOURCE_CLASS_NAME("flexy.pool.data.source.class.name"),
        DATA_SOURCE_PROPERTY("flexy.pool.data.source.property."),
        POOL_ADAPTER_FACTORY("flexy.pool.adapter.factory"),
        POOL_METRICS_FACTORY("flexy.pool.metrics.factory"),
        POOL_CONNECTION_PROXY_FACTORY("flexy.pool.connection.proxy.factory"),
        POOL_METRICS_REPORTER_LOG_MILLIS("flexy.pool.metrics.reporter.log.millis"),
        POOL_METRICS_REPORTER_JMX_ENABLE("flexy.pool.metrics.reporter.jmx.enable"),
        POOL_METRICS_REPORTER_JMX_AUTO_START("flexy.pool.metrics.reporter.jmx.auto.start"),
        POOL_METRICS_NAMING_STRATEGY("flexy.pool.metrics.naming.strategy"),
        POOL_STRATEGIES_FACTORY_RESOLVER("flexy.pool.strategies.factory.resolver"),
        POOL_EVENT_LISTENER_RESOLVER("flexy.pool.event.listener.resolver"),
        POOL_TIME_THRESHOLD_CONNECTION_ACQUISITION("flexy.pool.time.threshold.connection.acquisition"),
        POOL_TIME_THRESHOLD_CONNECTION_LEASE("flexy.pool.time.threshold.connection.lease");

        private final String key;

        private PropertyKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}


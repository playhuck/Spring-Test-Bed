package com.side.springtestbed.utils.conn;

import com.side.springtestbed.utils.ConfigurationProperties;
import com.side.springtestbed.utils.metrics.Metrics;

import javax.sql.DataSource;

public interface ConnectionAcquisitionStrategyFactory<S extends ConnectionAcquisitionStrategy, T extends DataSource> {
    S newInstance(ConfigurationProperties<T, Metrics, PoolAdapter<T>> var1);

    S newInstance(FlexyPoolConfiguration<T> configuration);
}

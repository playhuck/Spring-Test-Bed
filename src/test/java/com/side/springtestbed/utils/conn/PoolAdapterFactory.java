package com.side.springtestbed.utils.conn;

import com.side.springtestbed.utils.ConfigurationProperties;
import com.side.springtestbed.utils.metrics.Metrics;

import javax.sql.DataSource;

public interface PoolAdapterFactory<T extends DataSource> {
    PoolAdapter<T> newInstance(ConfigurationProperties<T, Metrics, PoolAdapter<T>> var1);
}


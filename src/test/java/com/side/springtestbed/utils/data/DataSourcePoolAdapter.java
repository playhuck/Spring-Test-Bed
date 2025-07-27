package com.side.springtestbed.utils.data;

import com.side.springtestbed.utils.ConfigurationProperties;
import com.side.springtestbed.utils.conn.PoolAdapter;
import com.side.springtestbed.utils.conn.PoolAdapterFactory;
import com.side.springtestbed.utils.metrics.Metrics;

import javax.sql.DataSource;

public class DataSourcePoolAdapter extends AbstractPoolAdapter<DataSource> {
    public static final PoolAdapterFactory<DataSource> FACTORY = new PoolAdapterFactory<DataSource>() {
        public PoolAdapter<DataSource> newInstance(ConfigurationProperties<DataSource, Metrics, PoolAdapter<DataSource>> configurationProperties) {
            return new DataSourcePoolAdapter(configurationProperties);
        }
    };

    public DataSourcePoolAdapter(ConfigurationProperties<DataSource, Metrics, PoolAdapter<DataSource>> configurationProperties) {
        super(configurationProperties);
    }

    public int getMaxPoolSize() {
        throw new UnsupportedOperationException("The DataSourcePoolAdapter cannot read the max pool size");
    }

    public void setMaxPoolSize(int maxPoolSize) {
        throw new UnsupportedOperationException("The DataSourcePoolAdapter cannot write the max pool size");
    }

    protected boolean isTimeoutAcquisitionException(Exception e) {
        return false;
    }
}


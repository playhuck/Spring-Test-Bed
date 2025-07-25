package com.side.springtestbed.utils;

import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.DataSourceQueryCountListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

import javax.sql.DataSource;

public enum DataSourceProxyType {
    DATA_SOURCE_PROXY {
        @Override
        public DataSource dataSource(DataSource dataSource) {
            ChainListener listener = new ChainListener();
            SLF4JQueryLoggingListener loggingListener = new SLF4JQueryLoggingListener();
            loggingListener.setQueryLogEntryCreator(new InlineQueryLogEntryCreator());
            listener.addListener(loggingListener);
            listener.addListener(new DataSourceQueryCountListener());
            return ProxyDataSourceBuilder
                    .create(dataSource)
                    .name(name())
                    .listener(listener)
                    .build();
        }
    },
    P6SPY {
        @Override
        public DataSource dataSource(DataSource dataSource) {
            return new P6DataSource(dataSource);
        }
    };

    public abstract DataSource dataSource(DataSource dataSource);
}

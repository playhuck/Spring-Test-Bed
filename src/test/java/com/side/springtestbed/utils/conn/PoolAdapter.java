package com.side.springtestbed.utils.conn;

import javax.sql.DataSource;

public interface PoolAdapter<T extends DataSource> extends ConnectionFactory {
    T getTargetDataSource();

    int getMaxPoolSize();

    void setMaxPoolSize(int var1);
}

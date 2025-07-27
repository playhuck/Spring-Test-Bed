package com.side.springtestbed.utils.conn;

import javax.sql.DataSource;
import java.util.List;

public interface ConnectionAcquisitionFactoryResolver<T extends DataSource> {
    List<ConnectionAcquisitionStrategyFactory<? extends ConnectionAcquisitionStrategy, T>> resolveFactories();
}
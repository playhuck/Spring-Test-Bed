package com.side.springtestbed.utils;

import org.hibernate.dialect.Dialect;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceProvider {

    enum IdentifierStrategy {
        IDENTITY,
        SEQUENCE
    }

    String hibernateDialect();

    DataSource dataSource();

    Class driverClassName();

    Class<? extends DataSource> dataSourceClassName();

    Properties dataSourceProperties();

    String url();

    String username();

    String password();

    Database database();

    Queries queries();

    default Class<? extends Dialect> hibernateDialectClass() {
        return ReflectionUtils.getClass(hibernateDialect());
    }
}

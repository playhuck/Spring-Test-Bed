package com.side.springtestbed.common.config;

import com.side.springtestbed.common.listener.FilterQueryListener;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
public class DataSourceProxyConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        DataSource dataSource = properties.initializeDataSourceBuilder().build();

        return ProxyDataSourceBuilder
                .create(dataSource)
                .name("DataSourceProxy")
                .listener(new FilterQueryListener())
                .multiline()
                .countQuery()
                .build();
    }
}

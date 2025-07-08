package com.side.springtestbed.hibernate.postgresql.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableJpaRepositories(basePackages = "com.side.springtestbed.hibernate.postgresql.repository")
@EntityScan(basePackages = "com.side.springtestbed.hibernate.postgresql.entity")
@EnableTransactionManagement
@Slf4j
public class PostgreSQLHibernateConfig {
    
    @PostConstruct
    public void init() {
        log.info("🚀 PostgreSQL Hibernate Configuration initialized");
        log.info("📁 Entity Package: com.side.springtestbed.hibernate.postgresql.entity");
        log.info("📁 Repository Package: com.side.springtestbed.hibernate.postgresql.repository");
        log.info("🔄 Transaction Management: Enabled");
    }
}

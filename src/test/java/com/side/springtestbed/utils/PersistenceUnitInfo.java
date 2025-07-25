package com.side.springtestbed.utils;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public interface PersistenceUnitInfo {
    String getPersistenceUnitName();

    String getPersistenceProviderClassName();

    PersistenceUnitTransactionType getTransactionType();

    DataSource getJtaDataSource();

    DataSource getNonJtaDataSource();

    List<String> getMappingFileNames();

    List<URL> getJarFileUrls();

    URL getPersistenceUnitRootUrl();

    List<String> getManagedClassNames();

    boolean excludeUnlistedClasses();

    SharedCacheMode getSharedCacheMode();

    ValidationMode getValidationMode();

    Properties getProperties();

    String getPersistenceXMLSchemaVersion();

    ClassLoader getClassLoader();

    void addTransformer(ClassTransformer var1);

    ClassLoader getNewTempClassLoader();
}

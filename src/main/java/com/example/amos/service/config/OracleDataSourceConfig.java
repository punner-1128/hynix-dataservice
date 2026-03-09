package com.example.amos.service.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.amos.service.common.exception.ServiceException;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties(OracleProperties.class)
public class OracleDataSourceConfig {

    private final OracleProperties oracleProperties;
    private final Environment environment;

    public OracleDataSourceConfig(OracleProperties oracleProperties, Environment environment) {
        this.oracleProperties = oracleProperties;
        this.environment = environment;
    }

    @Bean(name = "oracleDataSourceMap")
    public Map<String, DataSource> oracleDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
        for (String datasourceKey : oracleProperties.getDatabases()) {
            String prefix = "spring.datasource." + datasourceKey;
            DataSourceProperties properties = buildDataSourceProperties(prefix, datasourceKey);
            dataSourceMap.put(datasourceKey, buildHikariDataSource(prefix, properties));
        }
        return Collections.unmodifiableMap(dataSourceMap);
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource primaryDataSource(@Qualifier("oracleDataSourceMap") Map<String, DataSource> oracleDataSourceMap) {
        if (oracleProperties.getDatabases().isEmpty()) {
            throw new ServiceException("5001", "spring.oracle.databases is empty");
        }

        String primaryKey = oracleProperties.getDatabases().get(0);
        DataSource dataSource = oracleDataSourceMap.get(primaryKey);
        if (dataSource == null) {
            throw new ServiceException("5001", "Primary Oracle DataSource not found for key: " + primaryKey);
        }
        return dataSource;
    }

    @Bean(name = "oracleTransactionManagerMap")
    public Map<String, PlatformTransactionManager> oracleTransactionManagerMap(
            @Qualifier("oracleDataSourceMap") Map<String, DataSource> oracleDataSourceMap) {
        Map<String, PlatformTransactionManager> transactionManagerMap = new LinkedHashMap<>();
        for (String datasourceKey : oracleProperties.getDatabases()) {
            DataSource dataSource = oracleDataSourceMap.get(datasourceKey);
            if (dataSource == null) {
                throw new ServiceException("5001", "Oracle DataSource not found for key: " + datasourceKey);
            }
            transactionManagerMap.put(datasourceKey, new DataSourceTransactionManager(dataSource));
        }
        return Collections.unmodifiableMap(transactionManagerMap);
    }

    private DataSourceProperties buildDataSourceProperties(String prefix, String datasourceKey) {
        DataSourceProperties properties = new DataSourceProperties();
        properties.setUrl(requiredProperty(prefix + ".jdbc-url", datasourceKey));
        properties.setUsername(requiredProperty(prefix + ".username", datasourceKey));
        properties.setPassword(requiredProperty(prefix + ".password", datasourceKey));
        properties.setDriverClassName(requiredProperty(prefix + ".driver-class-name", datasourceKey));
        return properties;
    }

    private HikariDataSource buildHikariDataSource(String prefix, DataSourceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setDriverClassName(properties.getDriverClassName());
        Binder.get(environment).bind(prefix + ".hikari", Bindable.ofInstance(dataSource));
        return dataSource;
    }

    private String requiredProperty(String key, String datasourceKey) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ServiceException("5001", "Missing Oracle datasource property: " + key + " (" + datasourceKey + ")");
        }
        return value;
    }
}

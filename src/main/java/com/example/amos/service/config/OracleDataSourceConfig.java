package com.example.amos.service.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OracleDataSourceConfig {

    @Bean
    @ConfigurationProperties("oracle.oracledb1.datasource")
    public DataSourceProperties oracleDb1DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "oracleDataSource1")
    public DataSource oracleDataSource1(@Qualifier("oracleDb1DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "oracleJdbcTemplate1")
    public JdbcTemplate oracleJdbcTemplate1(@Qualifier("oracleDataSource1") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "oracleTransactionManager1")
    public PlatformTransactionManager oracleTransactionManager1(@Qualifier("oracleDataSource1") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @ConfigurationProperties("oracle.oracledb2.datasource")
    public DataSourceProperties oracleDb2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "oracleDataSource2")
    public DataSource oracleDataSource2(@Qualifier("oracleDb2DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "oracleJdbcTemplate2")
    public JdbcTemplate oracleJdbcTemplate2(@Qualifier("oracleDataSource2") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "oracleTransactionManager2")
    public PlatformTransactionManager oracleTransactionManager2(@Qualifier("oracleDataSource2") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @ConfigurationProperties("oracle.oracledb3.datasource")
    public DataSourceProperties oracleDb3DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "oracleDataSource3")
    public DataSource oracleDataSource3(@Qualifier("oracleDb3DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "oracleJdbcTemplate3")
    public JdbcTemplate oracleJdbcTemplate3(@Qualifier("oracleDataSource3") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "oracleTransactionManager3")
    public PlatformTransactionManager oracleTransactionManager3(@Qualifier("oracleDataSource3") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

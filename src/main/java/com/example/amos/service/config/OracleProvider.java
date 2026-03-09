package com.example.amos.service.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.amos.service.common.exception.ServiceException;

@Component
public class OracleProvider {

    private final Map<String, DataSource> oracleDataSourceMap;

    public OracleProvider(@Qualifier("oracleDataSourceMap") Map<String, DataSource> oracleDataSourceMap) {
        this.oracleDataSourceMap = oracleDataSourceMap;
    }

    public DataSource getOracle(String dbName) {
        DataSource dataSource = oracleDataSourceMap.get(dbName);
        if (dataSource == null) {
            throw new ServiceException("4001", "Unknown Oracle datasource key: " + dbName);
        }
        return dataSource;
    }
}

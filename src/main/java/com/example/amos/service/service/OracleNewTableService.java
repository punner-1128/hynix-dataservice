package com.example.amos.service.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.amos.service.common.exception.ServiceException;
import com.example.amos.service.config.OracleProperties;
import com.example.amos.service.repository.oracle.mapper.NewTableMapper;

@Service
public class OracleNewTableService {

    private final OracleProperties oracleProperties;
    private final Map<String, SqlSessionTemplate> sqlSessionTemplateMap;

    public OracleNewTableService(
            OracleProperties oracleProperties,
            @Qualifier("oracleSqlSessionTemplateMap") Map<String, SqlSessionTemplate> sqlSessionTemplateMap) {
        this.oracleProperties = oracleProperties;
        this.sqlSessionTemplateMap = sqlSessionTemplateMap;
    }

    public List<Map<String, Object>> getNewTable() {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String datasourceKey : oracleProperties.getDatabases()) {
            merged.put(datasourceKey, getMapper(datasourceKey).selectAll());
        }
        return List.of(merged);
    }

    public List<Map<String, Object>> getNewTable(String instanceId) {
        return getMapper(instanceId).selectAll();
    }

    public List<Map<String, Object>> searchNewTable(String instanceId, String column1, BigDecimal column2) {
        return getMapper(instanceId).searchNewTable(column1, column2);
    }

    private NewTableMapper getMapper(String instanceId) {
        SqlSessionTemplate sqlSessionTemplate = sqlSessionTemplateMap.get(instanceId);
        if (sqlSessionTemplate == null) {
            throw new ServiceException("4001", "Unknown Oracle datasource key: " + instanceId);
        }
        return sqlSessionTemplate.getMapper(NewTableMapper.class);
    }
}

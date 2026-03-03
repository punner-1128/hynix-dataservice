package com.example.amos.service.repository.oracle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OracleRepository {

    private final Map<String, JdbcTemplate> jdbcTemplateMap;

    public OracleRepository(@Qualifier("oracleJdbcTemplateMap") Map<String, JdbcTemplate> jdbcTemplateMap) {
        this.jdbcTemplateMap = jdbcTemplateMap;
    }

    public String currentTimestamp(String instanceId) {
        JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(instanceId);
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("Invalid datasourceKey: " + instanceId);
        }
        return jdbcTemplate.queryForObject(
                "SELECT TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3 TZH:TZM') FROM dual",
                String.class);
    }

    public List<Map<String, Object>> findAllNewTable(String instanceId) {
        JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(instanceId);
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("Invalid datasourceKey: " + instanceId);
        }
        return jdbcTemplate.queryForList("SELECT * FROM NEWTABLE");
    }

    public List<Map<String, Object>> searchNewTable(String instanceId, String column1, BigDecimal column2) {
        JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(instanceId);
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("Invalid datasourceKey: " + instanceId);
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM APPUSER.NEWTABLE WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (column1 != null) {
            sql.append(" AND COLUMN1 = ?");
            params.add(column1);
        }
        if (column2 != null) {
            sql.append(" AND COLUMN2 = ?");
            params.add(column2);
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}

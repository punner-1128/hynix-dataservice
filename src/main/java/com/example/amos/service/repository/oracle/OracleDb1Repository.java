package com.example.amos.service.repository.oracle;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OracleDb1Repository {

    private final JdbcTemplate jdbcTemplate;

    public OracleDb1Repository(@Qualifier("oracleJdbcTemplate1") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String currentTimestamp() {
        return jdbcTemplate.queryForObject(
                "SELECT TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3 TZH:TZM') FROM dual",
                String.class);
    }

    public List<Map<String, Object>> findAllNewTable() {
        return jdbcTemplate.queryForList("SELECT * FROM NEWTABLE");
    }
}

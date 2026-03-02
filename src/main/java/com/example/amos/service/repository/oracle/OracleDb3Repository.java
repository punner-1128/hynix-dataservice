package com.example.amos.service.repository.oracle;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OracleDb3Repository {

    private final JdbcTemplate jdbcTemplate;
    
    public OracleDb3Repository(@Qualifier("oracleJdbcTemplate3") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String currentTimestamp() {
        return jdbcTemplate.queryForObject(
                "SELECT TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3 TZH:TZM') FROM dual",
                String.class);
    }
}

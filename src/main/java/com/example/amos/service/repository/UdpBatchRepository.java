package com.example.amos.service.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.amos.service.dto.UdpData;

@Repository
public class UdpBatchRepository {

    private static final String INSERT_SQL = """
            INSERT INTO tb_udt_inout
            (FIELD1, FIELD2, FIELD3, FIELD4, FIELD5)
            VALUES
            (?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UdpBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void batchInsert(List<UdpData> udpDataList) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UdpData udpData = udpDataList.get(i);

                ps.setString(1, udpData.getField1());

                if (udpData.getField2() == null) {
                    ps.setNull(2, Types.DOUBLE);
                } else {
                    ps.setDouble(2, udpData.getField2());
                }

                if (udpData.getField3() == null) {
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(3, udpData.getField3() ? 1 : 0);
                }

                ps.setBigDecimal(4, udpData.getField4());

                if (udpData.getField5() == null) {
                    ps.setNull(5, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(udpData.getField5()));
                }
            }

            @Override
            public int getBatchSize() {
                return udpDataList.size();
            }
        });
    }
}

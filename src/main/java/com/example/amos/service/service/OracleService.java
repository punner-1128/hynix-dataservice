package com.example.amos.service.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import com.example.amos.service.config.OracleMyBatisConfig;
import com.example.amos.service.config.OracleProperties;
import com.example.amos.service.config.OracleProvider;
import com.example.amos.service.mapper.LogMapper;
import com.example.amos.service.mapper.NewTableMapper;
import com.example.amos.service.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OracleService {

    private final OracleProperties oracleProperties;
    private final OracleProvider oracleProvider;
    private final OracleMyBatisConfig oracleMyBatisConfig;

    public Map<String, Object> selectNewTable(String column1, Long column2) {
        return executeForAllDatabases(session -> {
            NewTableMapper mapper = session.getMapper(NewTableMapper.class);
            return mapper.selectNewTable(column1, column2);
        });
    }

    public Map<String, Object> selectAllNewTable() {
        return executeForAllDatabases(session -> {
            NewTableMapper mapper = session.getMapper(NewTableMapper.class);
            return mapper.selectAllNewTable();
        });
    }

    public Map<String, Object> selectUserTable(String userId) {
        return executeForAllDatabases(session -> {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.selectUserTable(userId);
        });
    }

    public Map<String, Object> selectLogTable(String logLevel) {
        return executeForAllDatabases(session -> {
            LogMapper mapper = session.getMapper(LogMapper.class);
            return mapper.selectLogTable(logLevel);
        });
    }

    private Map<String, Object> executeForAllDatabases(Function<SqlSession, List<Map<String, Object>>> queryFunction) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (String dbName : oracleProperties.getDatabases()) {
            try {
                DataSource dataSource = oracleProvider.getOracle(dbName);
                SqlSessionFactory sqlSessionFactory = oracleMyBatisConfig.createSqlSessionFactory(dataSource);

                try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                    result.put(dbName, queryFunction.apply(sqlSession));
                }
            } catch (Exception e) {
                log.error("Failed to query Oracle database: {}", dbName, e);
                result.put(dbName, List.of());
            }
        }

        return result;
    }
}

package com.example.amos.service.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.example.amos.service.common.exception.ServiceException;

@Configuration
public class OracleMyBatisConfig {

    private final OracleProperties oracleProperties;

    public OracleMyBatisConfig(OracleProperties oracleProperties) {
        this.oracleProperties = oracleProperties;
    }

    @Bean(name = "oracleSqlSessionFactoryMap")
    public Map<String, SqlSessionFactory> oracleSqlSessionFactoryMap(
            @Qualifier("oracleDataSourceMap") Map<String, DataSource> oracleDataSourceMap) throws Exception {
        Map<String, SqlSessionFactory> sqlSessionFactoryMap = new LinkedHashMap<>();
        for (String datasourceKey : oracleProperties.getDatabases()) {
            DataSource dataSource = oracleDataSourceMap.get(datasourceKey);
            if (dataSource == null) {
                throw new ServiceException("5001", "Oracle DataSource not found for key: " + datasourceKey);
            }
            sqlSessionFactoryMap.put(datasourceKey, buildSqlSessionFactory(dataSource));
        }
        return Collections.unmodifiableMap(sqlSessionFactoryMap);
    }

    @Bean(name = "oracleSqlSessionTemplateMap")
    public Map<String, SqlSessionTemplate> oracleSqlSessionTemplateMap(
            @Qualifier("oracleSqlSessionFactoryMap") Map<String, SqlSessionFactory> oracleSqlSessionFactoryMap) {
        Map<String, SqlSessionTemplate> sqlSessionTemplateMap = new LinkedHashMap<>();
        for (String datasourceKey : oracleProperties.getDatabases()) {
            SqlSessionFactory sqlSessionFactory = oracleSqlSessionFactoryMap.get(datasourceKey);
            if (sqlSessionFactory == null) {
                throw new ServiceException("5001", "Oracle SqlSessionFactory not found for key: " + datasourceKey);
            }
            sqlSessionTemplateMap.put(datasourceKey, new SqlSessionTemplate(sqlSessionFactory));
        }
        return Collections.unmodifiableMap(sqlSessionTemplateMap);
    }

    private SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/oracle/NewTableMapper.xml"));
        return bean.getObject();
    }
}

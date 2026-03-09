package com.example.amos.service.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
public class OracleMyBatisConfig {

    private static final String ORACLE_MAPPER_PATTERN = "classpath*:mapper/oracle/*.xml";

    @Bean(name = "oracleMapperLocations")
    public Resource[] oracleMapperLocations() throws Exception {
        return new PathMatchingResourcePatternResolver().getResources(ORACLE_MAPPER_PATTERN);
    }

    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(oracleMapperLocations());
        return factoryBean.getObject();
    }
}

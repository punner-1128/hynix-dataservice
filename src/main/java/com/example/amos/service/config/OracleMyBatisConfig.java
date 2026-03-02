package com.example.amos.service.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.example.amos.service.repository.oracle.NewTableMapper;

@Configuration
public class OracleMyBatisConfig {

    @Bean(name = "oracleSqlSessionFactory1")
    public SqlSessionFactory oracleSqlSessionFactory1(@Qualifier("oracleDataSource1") DataSource dataSource)
            throws Exception {
        return buildSqlSessionFactory(dataSource);
    }

    @Bean(name = "oracleSqlSessionFactory2")
    public SqlSessionFactory oracleSqlSessionFactory2(@Qualifier("oracleDataSource2") DataSource dataSource)
            throws Exception {
        return buildSqlSessionFactory(dataSource);
    }

    @Bean(name = "oracleSqlSessionFactory3")
    public SqlSessionFactory oracleSqlSessionFactory3(@Qualifier("oracleDataSource3") DataSource dataSource)
            throws Exception {
        return buildSqlSessionFactory(dataSource);
    }

    @Bean(name = "oracleNewTableMapper1")
    public MapperFactoryBean<NewTableMapper> oracleNewTableMapper1(
            @Qualifier("oracleSqlSessionFactory1") SqlSessionFactory sqlSessionFactory) {
        return buildMapperFactory(sqlSessionFactory);
    }

    @Bean(name = "oracleNewTableMapper2")
    public MapperFactoryBean<NewTableMapper> oracleNewTableMapper2(
            @Qualifier("oracleSqlSessionFactory2") SqlSessionFactory sqlSessionFactory) {
        return buildMapperFactory(sqlSessionFactory);
    }

    @Bean(name = "oracleNewTableMapper3")
    public MapperFactoryBean<NewTableMapper> oracleNewTableMapper3(
            @Qualifier("oracleSqlSessionFactory3") SqlSessionFactory sqlSessionFactory) {
        return buildMapperFactory(sqlSessionFactory);
    }

    private SqlSessionFactory buildSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/oracle/NewTableMapper.xml"));
        return bean.getObject();
    }

    private MapperFactoryBean<NewTableMapper> buildMapperFactory(SqlSessionFactory sqlSessionFactory) {
        MapperFactoryBean<NewTableMapper> factoryBean = new MapperFactoryBean<>(NewTableMapper.class);
        factoryBean.setSqlSessionFactory(sqlSessionFactory);
        return factoryBean;
    }
}

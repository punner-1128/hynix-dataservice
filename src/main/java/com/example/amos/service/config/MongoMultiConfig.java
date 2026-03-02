package com.example.amos.service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@EnableConfigurationProperties(MongoMultiProperties.class)
public class MongoMultiConfig {

    private final MongoMultiProperties mongoMultiProperties;

    public MongoMultiConfig(MongoMultiProperties mongoMultiProperties) {
        this.mongoMultiProperties = mongoMultiProperties;
    }

    @Bean(name = "mongoFactory1")
    @Primary
    public MongoDatabaseFactory mongoFactory1() {
        return new SimpleMongoClientDatabaseFactory(mongoMultiProperties.getMongodb1().getUri());
    }

    @Bean(name = "mongoTemplate1")
    @Primary
    public MongoTemplate mongoTemplate1(@Qualifier("mongoFactory1") MongoDatabaseFactory mongoFactory1) {
        return new MongoTemplate(mongoFactory1);
    }

    @Bean(name = "mongoFactory2")
    public MongoDatabaseFactory mongoFactory2() {
        return new SimpleMongoClientDatabaseFactory(mongoMultiProperties.getMongodb2().getUri());
    }

    @Bean(name = "mongoTemplate2")
    public MongoTemplate mongoTemplate2(@Qualifier("mongoFactory2") MongoDatabaseFactory mongoFactory2) {
        return new MongoTemplate(mongoFactory2);
    }

    @Bean(name = "mongoFactory3")
    public MongoDatabaseFactory mongoFactory3() {
        return new SimpleMongoClientDatabaseFactory(mongoMultiProperties.getMongodb3().getUri());
    }

    @Bean(name = "mongoTemplate3")
    public MongoTemplate mongoTemplate3(@Qualifier("mongoFactory3") MongoDatabaseFactory mongoFactory3) {
        return new MongoTemplate(mongoFactory3);
    }
}

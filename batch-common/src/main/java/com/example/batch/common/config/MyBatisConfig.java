package com.example.batch.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class MyBatisConfig {
    
    @Primary
    @Bean(name = "sourceSqlSessionFactory")
    public SqlSessionFactory sourceSqlSessionFactory(@Qualifier("sourceDataSource") DataSource dataSource) throws Exception {
        return createSqlSessionFactory(dataSource, "source");
    }

    @Bean(name = "targetSqlSessionFactory")
    public SqlSessionFactory targetSqlSessionFactory(@Qualifier("targetDataSource") DataSource dataSource) throws Exception {
        return createSqlSessionFactory(dataSource, "target");
    }

    /**
     * 공통 팩토리 생성 로직 (Dry 원칙 준수 및 대칭성 확보)
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource, String name) throws Exception {
        log.info("### {} INITIALIZING (MyBatisConfig) ###", name);
        
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources("classpath*:mapper/" + name + "/**/*.xml"));
        
        factoryBean.afterPropertiesSet();
        
        SqlSessionFactory sessionFactory = factoryBean.getObject();
        log.info("### {} LOADED - Mapped Statements: {} ###", 
                 name, sessionFactory.getConfiguration().getMappedStatementNames().size());
                 
        return sessionFactory;
    }
}
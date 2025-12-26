package com.example.batch.common.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    /**
     * MariaDB 설정 (Target & Batch Meta Data)
     * @Primary를 붙여서 스프링 배치가 기본적으로 이 DB를 사용하게 합니다.
     */
    @Primary
    @Bean(name = "targetDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.target")
    public DataSource targetDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * MySQL 설정 (Source)
     */
    @Bean(name = "sourceDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.source")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().build();
    }
}
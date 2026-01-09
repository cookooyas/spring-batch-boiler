package com.example.batch.common.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
// @AllArgsConstructor
public class DataSourceConfig {

    private final Environment env;
    private final Binder binder;

    public DataSourceConfig(Environment env) {
        this.env = env;
        this.binder = Binder.get(env);
    }
    
    // @Primary
    @Bean(name = "commonDataSource")
    @ConfigurationProperties(prefix = "db-pool.common-db")
    public DataSource commonDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource() {
        return createDynamicDataSource("spring.datasource.source");
    }

    @Bean(name = "targetDataSource")
    public DataSource targetDataSource() {
        return createDynamicDataSource("spring.datasource.target");
    }

    private DataSource createDynamicDataSource(String key) {
        String prefixPath = env.getProperty(key);
        return binder.bind(prefixPath, Bindable.of(HikariDataSource.class)).get();
    }
}
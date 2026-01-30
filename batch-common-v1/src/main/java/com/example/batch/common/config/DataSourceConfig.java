package com.example.batch.common.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    private final Environment env;
    private final Binder binder;

    public DataSourceConfig(Environment env) {
        this.env = env;
        this.binder = Binder.get(env);
    }

    /**
     * 1. Common DB (Spring Batch 메타 데이터 관리용)
     * @Primary를 통해 배치의 JobRepository가 기본적으로 사용하는 DB로 지정됨
     */
    @Primary
    @Bean(name = "commonDataSource")
    @ConfigurationProperties(prefix = "db-pool.common-db")
    public DataSource commonDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 2. Source DB (데이터 원천 - MySQL 등)
     */
    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource() {
        return createDynamicDataSource("spring.datasource.source");
    }

    /**
     * 3. Target DB (데이터 목적지 - Oracle 등)
     */
    @Bean(name = "targetDataSource")
    public DataSource targetDataSource() {
        return createDynamicDataSource("spring.datasource.target");
    }

    /**
     * 4. Common DB용 트랜잭션 매니저
     * 배치의 실행 상태를 기록할 때 트랜잭션을 관리함
     */
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager commonTransactionManager(@Qualifier("commonDataSource") DataSource commonDataSource) {
        return new DataSourceTransactionManager(commonDataSource);
    }

    /**
     * 5. Target DB 전용 트랜잭션 매니저
     * 실제 이관 작업(Step)에서 Target DB의 커밋/롤백을 제어함
     */
    @Bean(name = "targetTransactionManager")
    public PlatformTransactionManager targetTransactionManager(@Qualifier("targetDataSource") DataSource targetDataSource) {
        // 직접 객체를 생성하여 반환함
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(targetDataSource);
        
        // 프로퍼티 설정이 정상인지 체크 (빈 초기화 검증)
        transactionManager.afterPropertiesSet();
        
        log.info("### targetTransactionManager INITIALIZED ###");
        return transactionManager; // 로컬 변수 리턴으로 재귀 호출 문제 해결
    }

    /**
     * 동적 데이터소스 생성 로직
     * 설정된 경로에서 프로퍼티를 읽어 즉시 HikariDataSource를 생성함
     */
    private DataSource createDynamicDataSource(String key) {
        String prefixPath = env.getProperty(key);
        if (prefixPath == null) {
            log.error("환경 변수 [{}] 에 해당하는 데이터소스 설정 경로가 없습니다.", key);
            return null;
        }

        // Binder를 통해 yml 설정값을 HikariDataSource 객체에 즉시 바인딩(생성)
        HikariDataSource dataSource = binder.bind(prefixPath, Bindable.of(HikariDataSource.class)).get();
        
        log.info("### Dynamic DataSource Created: [{}] ###", prefixPath);
        return dataSource;
    }
}
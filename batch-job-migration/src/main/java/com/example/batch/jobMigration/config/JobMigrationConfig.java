package com.example.batch.jobMigration.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Slf4j
@Configuration
public class JobMigrationConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sourceSqlSessionFactory;
    private final SqlSessionFactory targetSqlSessionFactory;

    /**
     * 생성자 주입을 통한 의존성 주입
     * @Qualifier를 사용하여 common 모듈에서 정의한 특정 SqlSessionFactory를 명확히 지정함
     */
    public JobMigrationConfig(
                JobRepository jobRepository,
                @Qualifier("targetTransactionManager") PlatformTransactionManager transactionManager,
                @Qualifier("sourceSqlSessionFactory") SqlSessionFactory sourceSqlSessionFactory,
                @Qualifier("targetSqlSessionFactory") SqlSessionFactory targetSqlSessionFactory
        ) {
            this.jobRepository = jobRepository;
            this.transactionManager = transactionManager; // Target DB용 트랜잭션 매니저
            this.sourceSqlSessionFactory = sourceSqlSessionFactory; // MySQL 연결용
            this.targetSqlSessionFactory = targetSqlSessionFactory; // Oracle 연결용
        }

    /**
     * 전체 배치 작업을 정의하는 Job 빈
     */
    @Bean
    public Job migrationJob() {
        return new JobBuilder("migrationJob", jobRepository)
                .start(migrationStep()) // 이관 Step 시작
                .build();
    }

    /**
     * 실제 데이터 처리 로직(Read -> Process -> Write)이 일어나는 Step 정의
     */
    @Bean
    public Step migrationStep() {
        return new StepBuilder("migrationStep", jobRepository)
                // <Input, Output> 타입 설정 및 Chunk 사이즈(10,000건) 설정
                // transactionManager는 데이터를 쓰는 Target DB 쪽 매니저를 사용함
                .<Map<String, Object>, Map<String, Object>>chunk(10000, transactionManager)
                .reader(migrationReader()) // 읽기 로직 연결
                .writer(migrationWriter()) // 쓰기 로직 연결
                .build();
    }

    /**
     * Source DB(MySQL)에서 데이터를 스트리밍 방식으로 읽어오는 Reader
     */
    @Bean
    public MyBatisCursorItemReader<Map<String, Object>> migrationReader() {
        return new MyBatisCursorItemReaderBuilder<Map<String, Object>>()
                .sqlSessionFactory(sourceSqlSessionFactory) // MySQL 세션 팩토리 사용
                .queryId("UserMapper.findAllUsers") // 매퍼 XML의 namespace + id
                .build();
    }

    /**
     * Target DB(Oracle)에 데이터를 배치 방식으로 써넣는 Writer
     */
    @Bean
    public MyBatisBatchItemWriter<Map<String, Object>> migrationWriter(){
        return new MyBatisBatchItemWriterBuilder<Map<String, Object>>()
                .sqlSessionFactory(targetSqlSessionFactory) // Oracle 세션 팩토리 사용
                .statementId("UserMapper.insertUser") // 매퍼 XML의 namespace + id
                .assertUpdates(false) // 실행 후 업데이트된 행 수가 0이어도 예외를 던지지 않음 (Oracle 배치 시 권장)
                .build();
    }
}
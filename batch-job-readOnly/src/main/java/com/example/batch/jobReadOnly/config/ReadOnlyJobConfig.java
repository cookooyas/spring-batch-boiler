package com.example.batch.jobReadOnly.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReadOnlyJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SqlSessionFactory sourceSqlSessionFactory;

    // public ReadOnlyJobConfig(
    //             JobRepository jobRepository,
    //             PlatformTransactionManager transactionManager,
    //             @Qualifier("sourceSqlSessionFactory") SqlSessionFactory sourceSqlSessionFactory
    //     ) {
    //         this.jobRepository = jobRepository;
    //         this.transactionManager = transactionManager;
    //         this.sourceSqlSessionFactory = sourceSqlSessionFactory;
    //     }

    @Bean
    public Job readOnlyJob() {
        return new JobBuilder("readOnlyJob", jobRepository)
                .start(readOnlyStep())
                .build();
    }

    @Bean
    public Step readOnlyStep() {
        return new StepBuilder("readOnlyStep", jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(10000, transactionManager)
                .reader(mysqlCursorReader())
                .writer(chunk -> {
                    // 100만 건을 다 찍으면 로그가 너무 많으므로 1000건당 요약 출력
                    log.info(">>>> Chunk Write 가동! 이번 청크 데이터 수: {}", chunk.size());
                    log.info(">>>> 첫 번째 데이터 샘플: {}", chunk.getItems().get(0));
                })
                .build();
    }

    @Bean
    public MyBatisCursorItemReader<Map<String, Object>> mysqlCursorReader() {
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] rs = resolver.getResources("classpath*:mapper/**/*.xml");
            log.info("=== classpath mapper xml count: {}", rs.length);
            for (var r : rs) {
                log.info("=== mapper xml: {}", r);
            }
        } catch (Exception e) {
            log.error("=== mapper xml scan failed", e);
        }
        var mapped = sourceSqlSessionFactory.getConfiguration().getMappedStatementNames();
        log.info("=== mapped statement sample size: {}", mapped.size());
        log.info("=== contains UserMapper.findAllUsers ? {}", mapped.contains("UserMapper.findAllUsers"));
        return new MyBatisCursorItemReaderBuilder<Map<String, Object>>()
                .sqlSessionFactory(sourceSqlSessionFactory)
                .queryId("UserMapper.findAllUsers")
                .build();
    }
}
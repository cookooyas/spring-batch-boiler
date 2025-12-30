package com.example.batch.joba.config;

import com.example.batch.joba.model.LogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration
public class HelloWorldJobConfig {

    /**
     * Job: 하나의 큰 작업 단위
     */
    @Bean
    public Job helloWorldJob(JobRepository jobRepository, Step helloWorldStep) {
        return new JobBuilder("helloWorldJob", jobRepository)
                .start(helloWorldStep)
                .build();
    }

    /**
     * Step: Job을 구성하는 세부 단계 (Chunk 방식)
     */
    @Bean
    public Step helloWorldStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("helloWorldStep", jobRepository)
                .<LogModel, LogModel>chunk(5, transactionManager) // 5개씩 묶어서 처리
                .reader(helloReader())
                .writer(helloWriter())
                .build();
    }

    /**
     * Reader: 데이터를 읽어오는 역할 (가상 데이터 10개)
     */
    @Bean
    public ItemReader<LogModel> helloReader() {
        List<LogModel> items = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new LogModel((long) i, "Hello World - " + i))
                .toList();
        return new ListItemReader<>(items);
    }

    /**
     * Writer: 읽어온 데이터를 처리(출력)하는 역할
     */
    @Bean
    public ItemWriter<LogModel> helloWriter() {
        return chunk -> {
            log.info(">>>> Chunk Writer 가동! (크기: {}) <<<<", chunk.size());
            chunk.forEach(item -> log.info("출력 데이터: {}", item));
        }; 
    }
}
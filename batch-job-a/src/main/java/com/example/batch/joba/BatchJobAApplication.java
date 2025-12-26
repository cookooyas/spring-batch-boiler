package com.example.batch.joba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// 중요: common 모듈의 설정(Config)을 읽어오기 위해 컴포넌트 스캔 범위를 넓힙니다.
@ComponentScan(basePackages = {"com.example.batch.common", "com.example.batch.joba"})
public class BatchJobAApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobAApplication.class, args);
    }
}
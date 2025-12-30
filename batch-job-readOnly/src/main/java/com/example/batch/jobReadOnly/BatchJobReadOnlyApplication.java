package com.example.batch.jobReadOnly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.example.batch")
public class BatchJobReadOnlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobReadOnlyApplication.class, args);
    }
}
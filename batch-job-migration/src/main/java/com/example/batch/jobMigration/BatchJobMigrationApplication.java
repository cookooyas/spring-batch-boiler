package com.example.batch.jobMigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.example.batch")
public class BatchJobMigrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobMigrationApplication.class, args);
    }
}
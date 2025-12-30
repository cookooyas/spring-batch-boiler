package com.example.batch.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BatchCommonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchCommonApplication.class, args);
	}

}

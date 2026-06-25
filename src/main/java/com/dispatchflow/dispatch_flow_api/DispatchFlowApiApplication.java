package com.dispatchflow.dispatch_flow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.dispatchflow")
@EnableJpaRepositories(basePackages = "com.dispatchflow.guides.infrastructure.persistence")
@EntityScan(basePackages = "com.dispatchflow.guides.infrastructure.persistence")
public class DispatchFlowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DispatchFlowApiApplication.class, args);
	}

	
}

package com.dispatchflow.consumer.dispatch_flow_consumer;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication(scanBasePackages = "com.dispatchflow")
public class DispatchFlowConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchFlowConsumerApplication.class, args);
    }
}

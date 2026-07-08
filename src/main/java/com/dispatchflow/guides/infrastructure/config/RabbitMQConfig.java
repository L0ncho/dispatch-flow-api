package com.dispatchflow.guides.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    
    public static final String EXCHANGE = "dispatch.exchange";
    public static final String MAIN_QUEUE = "guide.created.queue";
    public static final String DLQ_QUEUE = "guide.created.dlq";
    public static final String MAIN_ROUTING_KEY = "guide.created.routingKey";
    public static final String DLQ_ROUTING_KEY = "guide.created.dlqRoutingKey";

  
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    
    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(exchange()).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue()).to(exchange()).with(MAIN_ROUTING_KEY);
    }

    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
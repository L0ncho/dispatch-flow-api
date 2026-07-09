package com.dispatchflow.guides.infrastructure.config;

import com.dispatchflow.shared.messaging.RabbitMqTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(RabbitMqTopology.EXCHANGE);
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(RabbitMqTopology.DLQ_QUEUE).build();
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(RabbitMqTopology.MAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqTopology.EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqTopology.DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(exchange()).with(RabbitMqTopology.DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue()).to(exchange()).with(RabbitMqTopology.MAIN_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

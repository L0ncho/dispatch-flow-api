package com.dispatchflow.shared.messaging;

public final class RabbitMqTopology {

    public static final String EXCHANGE = "dispatch.exchange";
    public static final String MAIN_QUEUE = "guide.created.queue";
    public static final String DLQ_QUEUE = "guide.created.dlq";
    public static final String MAIN_ROUTING_KEY = "guide.created.routingKey";
    public static final String DLQ_ROUTING_KEY = "guide.created.dlqRoutingKey";

    private RabbitMqTopology() {
    }
}

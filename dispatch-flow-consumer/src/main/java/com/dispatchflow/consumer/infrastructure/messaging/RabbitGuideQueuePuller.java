package com.dispatchflow.consumer.infrastructure.messaging;

import com.dispatchflow.consumer.application.ports.GuideQueuePuller;
import com.dispatchflow.shared.messaging.GuideCreationMessage;
import com.dispatchflow.shared.messaging.RabbitMqTopology;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class RabbitGuideQueuePuller implements GuideQueuePuller {

    private final ConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public RabbitGuideQueuePuller(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Override
    public Optional<PulledGuideMessage> pullNext() {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        try {
            GetResponse response = channel.basicGet(RabbitMqTopology.MAIN_QUEUE, false);
            if (response == null) {
                closeQuietly(channel, connection);
                return Optional.empty();
            }

            MessageProperties properties = new MessageProperties();
            properties.setDeliveryTag(response.getEnvelope().getDeliveryTag());
            Message amqpMessage = new Message(response.getBody(), properties);
            Object converted = messageConverter.fromMessage(amqpMessage);
            if (!(converted instanceof GuideCreationMessage guideCreationMessage)) {
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, false);
                closeQuietly(channel, connection);
                throw new IllegalStateException("Unexpected message type from guide queue");
            }

            long deliveryTag = response.getEnvelope().getDeliveryTag();
            return Optional.of(new RabbitPulledGuideMessage(guideCreationMessage, channel, connection, deliveryTag));
        } catch (IOException error) {
            closeQuietly(channel, connection);
            throw new IllegalStateException("Failed to pull guide message from queue", error);
        }
    }

    private static void closeQuietly(Channel channel, Connection connection) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException | TimeoutException ignored) {
            // best effort
        }
        if (connection != null) {
            connection.close();
        }
    }

    private static final class RabbitPulledGuideMessage implements PulledGuideMessage {

        private final GuideCreationMessage payload;
        private final Channel channel;
        private final Connection connection;
        private final long deliveryTag;

        private RabbitPulledGuideMessage(
                GuideCreationMessage payload,
                Channel channel,
                Connection connection,
                long deliveryTag) {
            this.payload = payload;
            this.channel = channel;
            this.connection = connection;
            this.deliveryTag = deliveryTag;
        }

        @Override
        public GuideCreationMessage payload() {
            return payload;
        }

        @Override
        public void acknowledge() {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException error) {
                throw new IllegalStateException("Failed to acknowledge guide message", error);
            } finally {
                closeQuietly(channel, connection);
            }
        }

        @Override
        public void rejectWithoutRequeue() {
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException error) {
                throw new IllegalStateException("Failed to reject guide message", error);
            } finally {
                closeQuietly(channel, connection);
            }
        }
    }
}

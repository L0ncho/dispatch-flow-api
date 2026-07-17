package com.dispatchflow.consumer.unit.infrastructure.messaging;

import com.dispatchflow.shared.messaging.GuideCreationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideQueueMessageConversionTest {

    private final MessageConverter messageConverter = new JacksonJsonMessageConverter();

    @Test
    void convertsToGuideCreationMessageWhenTypeIdHeaderIsPresent() {
        GuideCreationMessage original = sampleMessage();
        Message published = messageConverter.toMessage(original, new MessageProperties());

        Object converted = messageConverter.fromMessage(published);

        assertInstanceOf(GuideCreationMessage.class, converted);
        assertEquals("track-1", ((GuideCreationMessage) converted).trackingId());
    }

    @Test
    void doesNotConvertToGuideCreationMessageWhenHeadersAreMissing() {
        GuideCreationMessage original = sampleMessage();
        Message published = messageConverter.toMessage(original, new MessageProperties());
        Message withoutTypeHeaders = new Message(published.getBody(), new MessageProperties());

        Object converted = messageConverter.fromMessage(withoutTypeHeaders);

        boolean isNotGuideCreationMessage = !(converted instanceof GuideCreationMessage);
        assertTrue(isNotGuideCreationMessage);
        assertFalse(converted instanceof GuideCreationMessage);
    }

    private static GuideCreationMessage sampleMessage() {
        return GuideCreationMessage.create(
                "track-1",
                "Transportes Rápidos",
                "María González",
                "Origen",
                "Destino",
                "Electrónicos",
                LocalDate.of(2026, 6, 2),
                "responsable@empresa.cl",
                Instant.parse("2026-06-02T09:00:00Z"));
    }
}

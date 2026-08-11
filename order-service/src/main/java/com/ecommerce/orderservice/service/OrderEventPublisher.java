package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.event.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderEvent orderEvent) {
        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderEvent.getOrderId().toString(), orderEvent);
    }

    public void publishOrderCancelledEvent(OrderEvent orderEvent) {
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, orderEvent.getOrderId().toString(), orderEvent);
    }
}


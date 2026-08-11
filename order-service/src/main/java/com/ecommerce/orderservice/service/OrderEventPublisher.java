package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.event.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private static final String ORDER_TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(OrderEvent orderEvent) {
        kafkaTemplate.send(ORDER_TOPIC, orderEvent.getOrderId().toString(), orderEvent);
    }
}

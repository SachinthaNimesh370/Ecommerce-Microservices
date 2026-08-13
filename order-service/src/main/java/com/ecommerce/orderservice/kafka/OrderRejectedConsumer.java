package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.event.OrderRejectedEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes 'order-rejected' events published by the Inventory Service.
 *
 * When the inventory service determines there is insufficient stock for
 * one or more items, it publishes an order-rejected event. This consumer
 * reacts to that event and automatically cancels the corresponding order,
 * completing the Defense-in-Depth / saga compensation pattern.
 *
 * Flow (Part H — Asynchronous):
 *   Inventory Service → [order-rejected] → Kafka → OrderRejectedConsumer → CANCELLED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRejectedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-rejected", groupId = "order-service-group")
    @Transactional
    public void handleOrderRejected(OrderRejectedEvent event) {
        log.warn("╔══════════════════════════════════════════════════════╗");
        log.warn("║  ORDER REJECTED by Inventory Service                 ║");
        log.warn("║  orderId : {}                                        ║", event.getOrderId());
        log.warn("║  userId  : {}                                        ║", event.getUserId());
        log.warn("║  reason  : {}                                        ║", event.getReason());
        log.warn("╚══════════════════════════════════════════════════════╝");

        orderRepository.findById(event.getOrderId()).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == OrderStatus.PENDING) {
                        order.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                        log.warn("Order #{} automatically cancelled due to insufficient stock. Reason: {}",
                                event.getOrderId(), event.getReason());
                    } else {
                        log.info("Order #{} is already in status '{}', skipping cancellation.",
                                event.getOrderId(), order.getStatus());
                    }
                },
                () -> log.error("Received order-rejected event for unknown orderId: {}", event.getOrderId())
        );
    }
}

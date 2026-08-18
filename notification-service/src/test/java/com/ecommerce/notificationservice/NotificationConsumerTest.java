package com.ecommerce.notificationservice;

import com.ecommerce.notificationservice.event.OrderEvent;
import com.ecommerce.notificationservice.event.OrderItemEvent;
import com.ecommerce.notificationservice.event.StockEvent;
import com.ecommerce.notificationservice.kafka.NotificationConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationConsumerTest {

    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        notificationConsumer = new NotificationConsumer();
    }

    @Test
    @DisplayName("Kafka Notification: Should handle order-created event without error")
    void handleOrderCreated_Success() {
        OrderItemEvent itemEvent = OrderItemEvent.builder()
                .productId(101L)
                .quantity(2)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(1001L)
                .userId(5L)
                .totalAmount(new BigDecimal("150.00"))
                .items(List.of(itemEvent))
                .build();

        assertDoesNotThrow(() -> notificationConsumer.handleOrderCreated(orderEvent));
    }

    @Test
    @DisplayName("Kafka Notification: Should handle order-cancelled event without error")
    void handleOrderCancelled_Success() {
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(1001L)
                .userId(5L)
                .build();

        assertDoesNotThrow(() -> notificationConsumer.handleOrderCancelled(orderEvent));
    }

    @Test
    @DisplayName("Kafka Notification: Should handle stock-updated event without error")
    void handleStockUpdated_Success() {
        StockEvent stockEvent = StockEvent.builder()
                .productId(101L)
                .previousQuantity(20)
                .newQuantity(18)
                .eventType("STOCK_REDUCED")
                .build();

        assertDoesNotThrow(() -> notificationConsumer.handleStockUpdated(stockEvent));
    }
}

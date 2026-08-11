package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.event.OrderEvent;
import com.ecommerce.productservice.event.OrderItemEvent;
import com.ecommerce.productservice.event.StockEvent;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for the product/inventory service.
 *
 * Listens to:
 *   - order-created  → reduce stock for each item in the order
 *   - order-cancelled → restore stock for each item in the order
 *
 * After each stock change it publishes a stock-updated event so
 * the Notification Service can react to inventory changes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final ProductService productService;
    private final StockEventPublisher stockEventPublisher;

    /**
     * Consume order-created events and reduce stock for every ordered item.
     */
    @KafkaListener(topics = "order-created", groupId = "product-service-group")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received order-created event | orderId: {} | items: {}", event.getOrderId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("order-created event has no items. orderId: {}", event.getOrderId());
            return;
        }

        for (OrderItemEvent item : event.getItems()) {
            try {
                int prevQty = productService.reduceStock(item.getProductId(), item.getQuantity());
                int newQty = prevQty - item.getQuantity();
                if (newQty < 0) newQty = 0;

                // Publish stock-updated event
                StockEvent stockEvent = StockEvent.builder()
                        .eventType("STOCK_REDUCED")
                        .productId(item.getProductId())
                        .previousQuantity(prevQty)
                        .newQuantity(newQty)
                        .build();
                stockEventPublisher.publishStockEvent(stockEvent);
            } catch (Exception e) {
                log.error("Failed to reduce stock for productId: {} | error: {}", item.getProductId(), e.getMessage());
            }
        }
    }

    /**
     * Consume order-cancelled events and restore stock for every cancelled item.
     */
    @KafkaListener(topics = "order-cancelled", groupId = "product-service-group")
    public void handleOrderCancelled(OrderEvent event) {
        log.info("Received order-cancelled event | orderId: {} | items: {}", event.getOrderId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("order-cancelled event has no items. orderId: {}", event.getOrderId());
            return;
        }

        for (OrderItemEvent item : event.getItems()) {
            try {
                int prevQty = productService.restoreStock(item.getProductId(), item.getQuantity());
                int newQty = prevQty + item.getQuantity();

                // Publish stock-updated event
                StockEvent stockEvent = StockEvent.builder()
                        .eventType("STOCK_RESTORED")
                        .productId(item.getProductId())
                        .previousQuantity(prevQty)
                        .newQuantity(newQty)
                        .build();
                stockEventPublisher.publishStockEvent(stockEvent);
            } catch (Exception e) {
                log.error("Failed to restore stock for productId: {} | error: {}", item.getProductId(), e.getMessage());
            }
        }
    }
}

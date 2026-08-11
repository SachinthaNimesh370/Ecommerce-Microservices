package com.ecommerce.inventoryservice.kafka;

import com.ecommerce.inventoryservice.event.OrderEvent;
import com.ecommerce.inventoryservice.event.OrderItemEvent;
import com.ecommerce.inventoryservice.event.OrderRejectedEvent;
import com.ecommerce.inventoryservice.event.StockEvent;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka consumer for the Inventory Service.
 *
 * Implements the flow from PDF Section 18:
 *   order-created  → Check Stock → Enough: reduce stock, Not Enough: publish order-rejected
 *   order-cancelled → Restore stock
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final InventoryEventPublisher inventoryEventPublisher;

    // ─── Order Created ────────────────────────────────────────────────────────

    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received order-created | orderId: {} | items: {}", event.getOrderId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("order-created has no items | orderId: {}", event.getOrderId());
            return;
        }

        List<OrderItemEvent> failedItems = new ArrayList<>();

        // ── Step 1: Check all items first ────────────────────────────────────
        for (OrderItemEvent item : event.getItems()) {
            boolean inStock = inventoryService.isInStock(item.getProductId(), item.getQuantity());
            if (!inStock) {
                log.warn("Insufficient stock for productId: {} | requested: {}",
                        item.getProductId(), item.getQuantity());
                failedItems.add(item);
            }
        }

        // ── Step 2: If any item is out of stock, reject the whole order ───────
        if (!failedItems.isEmpty()) {
            OrderRejectedEvent rejected = OrderRejectedEvent.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .reason("Insufficient stock for " + failedItems.size() + " item(s)")
                    .failedItems(failedItems)
                    .build();
            inventoryEventPublisher.publishOrderRejected(rejected);
            log.warn("Order rejected | orderId: {} | failedItems: {}", event.getOrderId(), failedItems);
            return;
        }

        // ── Step 3: All items in stock — reduce stock ─────────────────────────
        for (OrderItemEvent item : event.getItems()) {
            try {
                int[] prev = inventoryService.reduceStock(item.getProductId(), item.getQuantity());

                StockEvent stockEvent = StockEvent.builder()
                        .eventType("STOCK_REDUCED")
                        .productId(item.getProductId())
                        .previousAvailable(prev[0])
                        .newAvailable(prev[0] - item.getQuantity())
                        .previousReserved(prev[1])
                        .newReserved(prev[1] + item.getQuantity())
                        .build();
                inventoryEventPublisher.publishStockUpdated(stockEvent);

            } catch (InsufficientStockException e) {
                log.error("Stock reduce failed for productId: {} | {}", item.getProductId(), e.getMessage());
            }
        }
    }

    // ─── Order Cancelled ──────────────────────────────────────────────────────

    @KafkaListener(topics = "order-cancelled", groupId = "inventory-service-group")
    public void handleOrderCancelled(OrderEvent event) {
        log.info("Received order-cancelled | orderId: {} | items: {}", event.getOrderId(), event.getItems());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("order-cancelled has no items | orderId: {}", event.getOrderId());
            return;
        }

        for (OrderItemEvent item : event.getItems()) {
            try {
                int[] prev = inventoryService.restoreStock(item.getProductId(), item.getQuantity());

                StockEvent stockEvent = StockEvent.builder()
                        .eventType("STOCK_RESTORED")
                        .productId(item.getProductId())
                        .previousAvailable(prev[0])
                        .newAvailable(prev[0] + item.getQuantity())
                        .previousReserved(prev[1])
                        .newReserved(Math.max(0, prev[1] - item.getQuantity()))
                        .build();
                inventoryEventPublisher.publishStockUpdated(stockEvent);

            } catch (Exception e) {
                log.error("Stock restore failed for productId: {} | {}", item.getProductId(), e.getMessage());
            }
        }
    }
}

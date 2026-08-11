package com.ecommerce.notificationservice.kafka;

import com.ecommerce.notificationservice.event.OrderEvent;
import com.ecommerce.notificationservice.event.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Notification consumer — listens to all 3 Kafka topics and sends notifications.
 *
 * Topics consumed:
 *   - order-created   → "Order #X created successfully for user Y"
 *   - order-cancelled → "Order #X has been cancelled"
 *   - stock-updated   → "Stock for product #X: was N, now M"
 *
 * In a production system, these log statements would be replaced by
 * real notification delivery (email, SMS, push, etc.).
 */
@Component
@Slf4j
public class NotificationConsumer {

    // ─── Order Created ────────────────────────────────────────────────────────

    @KafkaListener(topics = "order-created", groupId = "notification-service-group")
    public void handleOrderCreated(OrderEvent event) {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  NOTIFICATION: Order Created                        ║");
        log.info("║  Order #{} created successfully for user #{}         ║",
                event.getOrderId(), event.getUserId());
        log.info("║  Total Amount : {}                                   ║", event.getTotalAmount());
        log.info("║  Items        : {}                                   ║", event.getItems());
        log.info("╚══════════════════════════════════════════════════════╝");

        // TODO: Send real email/SMS/push notification here
        sendNotification(
                event.getUserId(),
                String.format("Your order #%d has been placed successfully! Total: %s",
                        event.getOrderId(), event.getTotalAmount())
        );
    }

    // ─── Order Cancelled ──────────────────────────────────────────────────────

    @KafkaListener(topics = "order-cancelled", groupId = "notification-service-group")
    public void handleOrderCancelled(OrderEvent event) {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  NOTIFICATION: Order Cancelled                      ║");
        log.info("║  Order #{} has been cancelled for user #{}           ║",
                event.getOrderId(), event.getUserId());
        log.info("╚══════════════════════════════════════════════════════╝");

        sendNotification(
                event.getUserId(),
                String.format("Your order #%d has been cancelled.", event.getOrderId())
        );
    }

    // ─── Stock Updated ────────────────────────────────────────────────────────

    @KafkaListener(topics = "stock-updated", groupId = "notification-service-group")
    public void handleStockUpdated(StockEvent event) {
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  NOTIFICATION: Stock Updated                        ║");
        log.info("║  Product #{}: {} units → {} units ({})               ║",
                event.getProductId(),
                event.getPreviousQuantity(),
                event.getNewQuantity(),
                event.getEventType());
        if (event.getNewQuantity() != null && event.getNewQuantity() <= 5) {
            log.warn("║  ⚠ LOW STOCK ALERT: Product #{} has only {} left!    ║",
                    event.getProductId(), event.getNewQuantity());
        }
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    // ─── Internal helper ──────────────────────────────────────────────────────

    /**
     * Placeholder for actual notification delivery.
     * Replace with email/SMS/push service in production.
     */
    private void sendNotification(Long userId, String message) {
        log.info("[NOTIFICATION → User #{}] {}", userId, message);
    }
}

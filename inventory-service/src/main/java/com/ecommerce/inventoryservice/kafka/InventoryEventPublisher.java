package com.ecommerce.inventoryservice.kafka;

import com.ecommerce.inventoryservice.event.OrderRejectedEvent;
import com.ecommerce.inventoryservice.event.StockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventPublisher {

    private static final String STOCK_UPDATED_TOPIC  = "stock-updated";
    private static final String ORDER_REJECTED_TOPIC = "order-rejected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockUpdated(StockEvent event) {
        kafkaTemplate.send(STOCK_UPDATED_TOPIC, event.getProductId().toString(), event);
        log.info("Published stock-updated | productId: {} | available: {} → {}",
                event.getProductId(), event.getPreviousAvailable(), event.getNewAvailable());
    }

    public void publishOrderRejected(OrderRejectedEvent event) {
        kafkaTemplate.send(ORDER_REJECTED_TOPIC, event.getOrderId().toString(), event);
        log.warn("Published order-rejected | orderId: {} | reason: {}", event.getOrderId(), event.getReason());
    }
}

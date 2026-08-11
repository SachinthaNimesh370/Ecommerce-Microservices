package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.event.StockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockEventPublisher {

    private static final String STOCK_UPDATED_TOPIC = "stock-updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStockEvent(StockEvent stockEvent) {
        kafkaTemplate.send(STOCK_UPDATED_TOPIC, stockEvent.getProductId().toString(), stockEvent);
        log.info("Published stock-updated event for productId: {}", stockEvent.getProductId());
    }
}

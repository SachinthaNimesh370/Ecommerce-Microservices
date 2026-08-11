package com.ecommerce.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent {
    private String eventType;       // STOCK_REDUCED | STOCK_RESTORED
    private Long productId;
    private String productName;
    private Integer previousQuantity;
    private Integer newQuantity;
}

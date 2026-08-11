package com.ecommerce.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent {
    private String eventType;           // STOCK_REDUCED | STOCK_RESTORED
    private Long productId;
    private Integer previousAvailable;
    private Integer newAvailable;
    private Integer previousReserved;
    private Integer newReserved;
}

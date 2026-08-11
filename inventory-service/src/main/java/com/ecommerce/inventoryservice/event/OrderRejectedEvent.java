package com.ecommerce.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRejectedEvent {
    private Long orderId;
    private Long userId;
    private String reason;
    private List<OrderItemEvent> failedItems;
}

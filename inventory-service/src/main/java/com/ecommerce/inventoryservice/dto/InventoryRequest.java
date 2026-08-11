package com.ecommerce.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "availableQuantity is required")
    @Min(value = 0, message = "availableQuantity must be >= 0")
    private Integer availableQuantity;

    @Min(value = 0, message = "reservedQuantity must be >= 0")
    private Integer reservedQuantity = 0;
}

package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be greater than or equal to zero")
    private BigDecimal price;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be greater than or equal to zero")
    private Integer quantity;
}

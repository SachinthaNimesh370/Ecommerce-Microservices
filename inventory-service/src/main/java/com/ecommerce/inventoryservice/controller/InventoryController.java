package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * POST /api/inventory
     * Initialize inventory for a product. Requires ADMIN role.
     */
    @PostMapping
    public ResponseEntity<InventoryResponse> initializeInventory(@Valid @RequestBody InventoryRequest request) {
        return new ResponseEntity<>(inventoryService.initializeInventory(request), HttpStatus.CREATED);
    }

    /**
     * GET /api/inventory
     * Get all inventory records. Public.
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    /**
     * GET /api/inventory/product/{productId}
     * Get inventory for a specific product. Public.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    /**
     * GET /api/inventory/product/{productId}/check?quantity=N
     * Check if enough stock is available. Public.
     */
    @GetMapping("/product/{productId}/check")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        boolean inStock = inventoryService.isInStock(productId, quantity);
        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "requestedQuantity", quantity,
                "inStock", inStock
        ));
    }

    /**
     * PUT /api/inventory/product/{productId}/add?quantity=N
     * Manually add stock for a product. Requires ADMIN role.
     */
    @PutMapping("/product/{productId}/add")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.addStock(productId, quantity));
    }
}

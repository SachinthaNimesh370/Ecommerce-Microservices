package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.exception.ResourceNotFoundException;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // ─── Initialize / Create ─────────────────────────────────────────────────

    @Transactional
    public InventoryResponse initializeInventory(InventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new IllegalStateException(
                    "Inventory already exists for productId: " + request.getProductId()
            );
        }
        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .availableQuantity(request.getAvailableQuantity())
                .reservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : 0)
                .build();
        return mapToResponse(inventoryRepository.save(inventory));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InventoryResponse getByProductId(Long productId) {
        return mapToResponse(findByProductId(productId));
    }

    public boolean isInStock(Long productId, int requestedQuantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> inv.getAvailableQuantity() >= requestedQuantity)
                .orElse(false);
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    /**
     * Reduce availableQuantity and increase reservedQuantity when an order is placed.
     * Throws InsufficientStockException if stock is not enough.
     *
     * @return snapshot of quantities before the change
     */
    @Transactional
    public int[] reduceStock(Long productId, int quantity) {
        Inventory inv = findByProductId(productId);

        if (inv.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for productId: %d. Available: %d, Requested: %d",
                            productId, inv.getAvailableQuantity(), quantity)
            );
        }

        int prevAvailable = inv.getAvailableQuantity();
        int prevReserved  = inv.getReservedQuantity();

        inv.setAvailableQuantity(prevAvailable - quantity);
        inv.setReservedQuantity(prevReserved + quantity);

        inventoryRepository.save(inv);
        log.info("Stock reduced | productId: {} | available: {} → {} | reserved: {} → {}",
                productId, prevAvailable, inv.getAvailableQuantity(), prevReserved, inv.getReservedQuantity());

        return new int[]{prevAvailable, prevReserved};
    }

    /**
     * Restore availableQuantity and decrease reservedQuantity when an order is cancelled.
     *
     * @return snapshot of quantities before the change
     */
    @Transactional
    public int[] restoreStock(Long productId, int quantity) {
        Inventory inv = findByProductId(productId);

        int prevAvailable = inv.getAvailableQuantity();
        int prevReserved  = inv.getReservedQuantity();

        inv.setAvailableQuantity(prevAvailable + quantity);
        inv.setReservedQuantity(Math.max(0, prevReserved - quantity));

        inventoryRepository.save(inv);
        log.info("Stock restored | productId: {} | available: {} → {} | reserved: {} → {}",
                productId, prevAvailable, inv.getAvailableQuantity(), prevReserved, inv.getReservedQuantity());

        return new int[]{prevAvailable, prevReserved};
    }

    /**
     * Manually add stock (for admin replenishment).
     */
    @Transactional
    public InventoryResponse addStock(Long productId, int quantity) {
        Inventory inv = findByProductId(productId);
        inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        return mapToResponse(inventoryRepository.save(inv));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for productId: " + productId));
    }

    private InventoryResponse mapToResponse(Inventory inv) {
        return InventoryResponse.builder()
                .id(inv.getId())
                .productId(inv.getProductId())
                .availableQuantity(inv.getAvailableQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}

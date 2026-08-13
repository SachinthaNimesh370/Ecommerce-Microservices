package com.ecommerce.inventoryservice;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.event.OrderEvent;
import com.ecommerce.inventoryservice.event.OrderItemEvent;
import com.ecommerce.inventoryservice.event.StockEvent;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.kafka.InventoryEventConsumer;
import com.ecommerce.inventoryservice.kafka.InventoryEventPublisher;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryEventConsumer inventoryEventConsumer;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        inventoryEventConsumer = new InventoryEventConsumer(inventoryService, inventoryEventPublisher);

        sampleInventory = Inventory.builder()
                .id(1L)
                .productId(101L)
                .availableQuantity(50)
                .reservedQuantity(0)
                .build();
    }

    @Test
    @DisplayName("Initialize Inventory: Should save new inventory record")
    void initializeInventory_Success() {
        InventoryRequest request = InventoryRequest.builder()
                .productId(101L)
                .availableQuantity(50)
                .build();

        when(inventoryRepository.existsByProductId(101L)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);

        InventoryResponse response = inventoryService.initializeInventory(request);

        assertNotNull(response);
        assertEquals(101L, response.getProductId());
        assertEquals(50, response.getAvailableQuantity());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("In Stock Check: Should return true when available quantity is enough")
    void isInStock_True() {
        when(inventoryRepository.findByProductId(101L)).thenReturn(Optional.of(sampleInventory));

        boolean inStock = inventoryService.isInStock(101L, 10);

        assertTrue(inStock);
    }

    @Test
    @DisplayName("Stock Reduction: Should deduct available quantity and increase reserved quantity")
    void reduceStock_Success() {
        when(inventoryRepository.findByProductId(101L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);

        int[] snapshots = inventoryService.reduceStock(101L, 10);

        assertEquals(50, snapshots[0]); // Previous available
        assertEquals(0, snapshots[1]);  // Previous reserved
        assertEquals(40, sampleInventory.getAvailableQuantity());
        assertEquals(10, sampleInventory.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(sampleInventory);
    }

    @Test
    @DisplayName("Stock Reduction: Should throw InsufficientStockException when quantity requested exceeds available")
    void reduceStock_InsufficientStock() {
        when(inventoryRepository.findByProductId(101L)).thenReturn(Optional.of(sampleInventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reduceStock(101L, 100));
    }

    @Test
    @DisplayName("Stock Restoration: Should restore available quantity when order is cancelled")
    void restoreStock_Success() {
        sampleInventory.setAvailableQuantity(40);
        sampleInventory.setReservedQuantity(10);

        when(inventoryRepository.findByProductId(101L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);

        int[] snapshots = inventoryService.restoreStock(101L, 10);

        assertEquals(40, snapshots[0]);
        assertEquals(50, sampleInventory.getAvailableQuantity());
        assertEquals(0, sampleInventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Kafka Integration: Create Order Event -> Inventory reduces stock -> StockEvent published")
    void handleOrderCreated_KafkaFlow() {
        OrderItemEvent itemEvent = OrderItemEvent.builder()
                .productId(101L)
                .quantity(5)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(10L)
                .userId(1L)
                .eventType("ORDER_CREATED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of(itemEvent))
                .build();

        when(inventoryRepository.findByProductId(101L)).thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(sampleInventory);
        doNothing().when(inventoryEventPublisher).publishStockUpdated(any(StockEvent.class));

        inventoryEventConsumer.handleOrderCreated(orderEvent);

        verify(inventoryRepository, atLeastOnce()).save(any(Inventory.class));
        verify(inventoryEventPublisher, times(1)).publishStockUpdated(any(StockEvent.class));
    }
}

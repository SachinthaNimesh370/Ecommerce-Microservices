package com.ecommerce.orderservice;

import com.ecommerce.orderservice.dto.OrderItemRequest;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.event.OrderEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderEventPublisher;
import com.ecommerce.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;
    private OrderRequest orderRequest;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productDto = ProductDto.builder()
                .id(101L)
                .name("Smartphone")
                .price(new BigDecimal("699.99"))
                .quantity(50)
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .userId(1L)
                .items(List.of(itemRequest))
                .build();

        sampleOrder = new Order();
        sampleOrder.setId(10L);
        sampleOrder.setUserId(1L);
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setTotalAmount(new BigDecimal("1399.98"));
    }

    @Test
    @DisplayName("Create Order: Should create order, calculate total amount, and publish event")
    void createOrder_Success() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(new ResponseEntity<>("UserObj", HttpStatus.OK));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(productDto);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        doNothing().when(orderEventPublisher).publishOrderCreatedEvent(any(OrderEvent.class));

        Order createdOrder = orderService.createOrder(orderRequest);

        assertNotNull(createdOrder);
        assertEquals(10L, createdOrder.getId());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventPublisher, times(1)).publishOrderCreatedEvent(any(OrderEvent.class));
    }

    @Test
    @DisplayName("Get Order: Should retrieve order by ID")
    void getOrderById_Success() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        Order order = orderService.getOrderById(10L);

        assertNotNull(order);
        assertEquals(10L, order.getId());
        assertEquals(1L, order.getUserId());
    }

    @Test
    @DisplayName("Get Order: Should throw exception when order ID is not found")
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    @DisplayName("Cancel Order: Should update status to CANCELLED and publish event")
    void cancelOrder_Success() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        doNothing().when(orderEventPublisher).publishOrderCancelledEvent(any(OrderEvent.class));

        Order cancelledOrder = orderService.cancelOrder(10L);

        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(orderEventPublisher, times(1)).publishOrderCancelledEvent(any(OrderEvent.class));
    }

    @Test
    @DisplayName("Cancel Order: Should throw exception if order is already cancelled")
    void cancelOrder_AlreadyCancelled() {
        sampleOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(sampleOrder));

        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(10L));
    }
}

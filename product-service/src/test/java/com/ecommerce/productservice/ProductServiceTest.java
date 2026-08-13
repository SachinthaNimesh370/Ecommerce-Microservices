package com.ecommerce.productservice;

import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High-end gaming laptop")
                .price(new BigDecimal("1500.00"))
                .category("Electronics")
                .quantity(10)
                .build();

        productRequest = ProductRequest.builder()
                .name("Laptop")
                .description("High-end gaming laptop")
                .price(new BigDecimal("1500.00"))
                .category("Electronics")
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Create Product: Should create product and return response")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals(new BigDecimal("1500.00"), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Read Product: Should get product by ID")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
    }

    @Test
    @DisplayName("Read Product: Should throw ResourceNotFoundException when product ID is invalid")
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    @DisplayName("Read Product: Should return all products list")
    void getAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct));

        List<ProductResponse> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(1, products.size());
    }

    @Test
    @DisplayName("Update Product: Should update existing product details")
    void updateProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductRequest updateRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Updated specs")
                .price(new BigDecimal("1800.00"))
                .category("Electronics")
                .quantity(15)
                .build();

        ProductResponse updated = productService.updateProduct(1L, updateRequest);

        assertNotNull(updated);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product: Should delete product when present")
    void deleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete Product: Should throw ResourceNotFoundException when product to delete doesn't exist")
    void deleteProduct_NotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
    }
}

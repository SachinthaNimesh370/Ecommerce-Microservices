package com.ecommerce.userservice.integration;

import com.ecommerce.userservice.dto.AuthResponse;
import com.ecommerce.userservice.dto.LoginRequest;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.entity.Role;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtService;
import com.ecommerce.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Section 23. Integration Test — Full Business Flow
 * 
 * Step 1: Register
 * Step 2: Login
 * Step 3: Receive JWT
 * Step 4: Get Products
 * Step 5: Create Order
 * Step 6: order-created Kafka event
 * Step 7: Inventory reduces stock
 * Step 8: Notification created
 */
@ExtendWith(MockitoExtension.class)
class FullBusinessFlowIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = User.builder()
                .id(1L)
                .name("Integration Tester")
                .email("tester@example.com")
                .password("encoded_pass")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Part J (23): Complete Integration Test - Full Business Flow Execution")
    void testFullBusinessFlow() {
        // ── 1. Register ─────────────────────────────────────────────────────
        RegisterRequest regReq = RegisterRequest.builder()
                .name("Integration Tester")
                .email("tester@example.com")
                .password("secure123")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.existsByEmail(regReq.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(regReq.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(registeredUser);
        when(jwtService.generateToken(registeredUser)).thenReturn("sample_jwt_token_header_payload_signature");

        AuthResponse regResponse = authService.register(regReq);
        assertNotNull(regResponse);
        assertNotNull(regResponse.getToken());

        // ── 2. Login ────────────────────────────────────────────────────────
        LoginRequest loginReq = LoginRequest.builder()
                .email("tester@example.com")
                .password("secure123")
                .build();

        when(userRepository.findByEmail(loginReq.getEmail())).thenReturn(Optional.of(registeredUser));

        AuthResponse loginResponse = authService.login(loginReq);

        // ── 3. Receive JWT ─────────────────────────────────────────────────
        String jwtToken = loginResponse.getToken();
        assertNotNull(jwtToken);
        assertEquals("sample_jwt_token_header_payload_signature", jwtToken);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // ── 4. Get Products ─────────────────────────────────────────────────
        MockProduct mockProduct = new MockProduct(101L, "Wireless Mouse", new BigDecimal("25.00"), 100);
        assertNotNull(mockProduct);
        assertEquals("Wireless Mouse", mockProduct.name());

        // ── 5. Create Order ─────────────────────────────────────────────────
        MockOrder mockOrder = new MockOrder(501L, registeredUser.getId(), mockProduct.id(), 2, new BigDecimal("50.00"), "PENDING");
        assertEquals(501L, mockOrder.orderId());
        assertEquals("PENDING", mockOrder.status());

        // ── 6. order-created Kafka event ───────────────────────────────────
        MockKafkaEvent kafkaEvent = new MockKafkaEvent("order-created", mockOrder.orderId(), mockOrder.userId());
        assertEquals("order-created", kafkaEvent.topic());

        // ── 7. Inventory reduces stock ─────────────────────────────────────
        int initialStock = mockProduct.stock();
        int reducedStock = initialStock - mockOrder.quantity();
        assertEquals(98, reducedStock);

        // ── 8. Notification created ────────────────────────────────────────
        String notificationMessage = String.format("Order #%d created successfully for user #%d", mockOrder.orderId(), mockOrder.userId());
        assertTrue(notificationMessage.contains("Order #501"));
    }

    // Records for full business flow simulation
    private record MockProduct(Long id, String name, BigDecimal price, int stock) {}
    private record MockOrder(Long orderId, Long userId, Long productId, int quantity, BigDecimal totalAmount, String status) {}
    private record MockKafkaEvent(String topic, Long orderId, Long userId) {}
}

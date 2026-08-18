package com.ecommerce.apigateway;

import com.ecommerce.apigateway.filter.RouterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouterValidatorTest {

    private RouterValidator routerValidator;

    @BeforeEach
    void setUp() {
        routerValidator = new RouterValidator();
    }

    @Test
    @DisplayName("API Gateway Router: Open endpoints (register, login, actuator) should NOT be secured")
    void openApiEndpoints_NotSecured() {
        MockServerHttpRequest regReq = MockServerHttpRequest.post("/api/auth/register").build();
        MockServerHttpRequest loginReq = MockServerHttpRequest.post("/api/auth/login").build();
        MockServerHttpRequest actuatorReq = MockServerHttpRequest.get("/actuator/health").build();

        assertFalse(routerValidator.isSecured.test(regReq));
        assertFalse(routerValidator.isSecured.test(loginReq));
        assertFalse(routerValidator.isSecured.test(actuatorReq));
    }

    @Test
    @DisplayName("API Gateway Router: GET /api/products should NOT be secured (Public access)")
    void getProducts_NotSecured() {
        MockServerHttpRequest getProductsReq = MockServerHttpRequest.get("/api/products").build();
        assertFalse(routerValidator.isSecured.test(getProductsReq));
    }

    @Test
    @DisplayName("API Gateway Router: POST /api/orders and POST /api/products SHOULD be secured")
    void securedEndpoints() {
        MockServerHttpRequest createOrderReq = MockServerHttpRequest.post("/api/orders").build();
        MockServerHttpRequest createProductReq = MockServerHttpRequest.post("/api/products").build();

        assertTrue(routerValidator.isSecured.test(createOrderReq));
        assertTrue(routerValidator.isSecured.test(createProductReq));
    }
}

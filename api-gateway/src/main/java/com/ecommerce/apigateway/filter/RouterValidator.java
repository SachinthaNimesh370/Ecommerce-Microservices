package com.ecommerce.apigateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/actuator"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();

                if (openApiEndpoints.stream().anyMatch(path::startsWith)) {
                    return false;
                }

                if (path.startsWith("/api/products") && HttpMethod.GET.equals(request.getMethod())) {
                    return false;
                }

                return true;
            };
}

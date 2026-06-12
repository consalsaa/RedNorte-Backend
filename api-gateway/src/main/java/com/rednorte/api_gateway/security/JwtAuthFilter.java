package com.rednorte.api_gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro global del API Gateway que intercepta todas las peticiones
 * y valida el token JWT en el header Authorization.
 * Las rutas públicas (/auth/**) se excluyen de la validación.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/",
            "/auth/login",
            "/eureka",
            "/swagger-ui.html",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Permitir preflight OPTIONS de CORS sin validar JWT
        if (request.getMethod() != null && request.getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        // Verificar si la ruta es pública
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Verificar header Authorization
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Petición sin token JWT rechazada: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Token JWT inválido rechazado: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token válido - agregar claims al header para los microservicios
        Claims claims = jwtUtil.validateToken(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Name", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
                .build();

        log.info("Petición autenticada: usuario={}, rol={}, path={}",
                claims.getSubject(), claims.get("role"), path);

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.contains("/v3/api-docs")
                || path.contains("/swagger-ui")
                || path.contains("/swagger-resources")
                || path.contains("/webjars");
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad
    }
}

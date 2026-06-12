package com.rednorte.ms_usuarios.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuracion basica para microservicios y BFFs en RedNorte.
 * Habilita RestTemplate con soporte de balanceo de carga para consultar
 * otros microservicios en Eureka utilizando su ID logico.
 */
@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

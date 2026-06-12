package com.rednorte.ms_listas_espera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@org.springframework.cache.annotation.EnableCaching
public class MsListasEsperaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsListasEsperaApplication.class, args);
	}

}

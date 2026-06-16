package com.rednorte.ms_portal_paciente.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitasServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CitasServiceImpl citasService;

    @Test
    void testObtenerCitasPorRutExitoso() {
        String rut = "12.345.678-9";
        List<Map<String, Object>> mockCitas = new ArrayList<>();
        Map<String, Object> cita = new HashMap<>();
        cita.put("id", 100L);
        cita.put("estado", "AGENDADO");
        mockCitas.add(cita);

        ResponseEntity<List<Map<String, Object>>> mockResponse = new ResponseEntity<>(mockCitas, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/paciente/" + rut),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(mockResponse);

        List<Map<String, Object>> result = citasService.obtenerCitasPorRut(rut);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).get("id"));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void testFallbackObtenerCitas() {
        String rut = "12.345.678-9";
        Throwable throwable = new RuntimeException("Connection timed out");

        List<Map<String, Object>> result = citasService.fallbackObtenerCitas(rut, throwable);

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> fallbackMap = result.get(0);
        assertTrue(fallbackMap.containsKey("mensaje"));
        assertEquals(rut, fallbackMap.get("rut"));
        assertTrue(((String) fallbackMap.get("mensaje")).contains("El servicio de citas está temporalmente no disponible"));
    }
}

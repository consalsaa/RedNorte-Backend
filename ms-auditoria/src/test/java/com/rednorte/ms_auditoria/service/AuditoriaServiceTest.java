package com.rednorte.ms_auditoria.service;

import com.rednorte.ms_auditoria.repository.AtencionRepository;
import com.rednorte.ms_auditoria.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private AtencionRepository atencionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerEstadisticas_Success() {
        // Mock restTemplate call to return empty list or null during sync
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{1, 5L});

        when(auditoriaRepository.obtenerEstadisticasEsperaNative()).thenReturn(mockData);

        List<Map<String, Object>> result = auditoriaService.obtenerEstadisticas();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("prioridad"));
        assertEquals(5L, result.get(0).get("cantidad"));
    }

    @Test
    void obtenerEstadisticas_FallbackSuccess() {
        // Mock restTemplate call to return empty list or null during sync
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        // Native call throws exception to trigger fallback
        when(auditoriaRepository.obtenerEstadisticasEsperaNative()).thenThrow(new RuntimeException("Native query not supported"));

        List<Object[]> mockFallbackData = new ArrayList<>();
        mockFallbackData.add(new Object[]{2, 10L});
        when(atencionRepository.calcularEstadisticasEspera()).thenReturn(mockFallbackData);

        List<Map<String, Object>> result = auditoriaService.obtenerEstadisticas();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).get("prioridad"));
        assertEquals(10L, result.get(0).get("cantidad"));
    }

    @Test
    void obtenerEstadisticas_FallbackException() {
        // Mock restTemplate call to return empty list or null during sync
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(null);

        // Both native call and JPQL fallback throw exceptions
        when(auditoriaRepository.obtenerEstadisticasEsperaNative()).thenThrow(new RuntimeException("Native query not supported"));
        when(atencionRepository.calcularEstadisticasEspera()).thenThrow(new RuntimeException("Fallback query failed"));

        List<Map<String, Object>> result = auditoriaService.obtenerEstadisticas();

        assertTrue(result.isEmpty());
    }

    @Test
    void sincronizarAtenciones_Success() {
        List<Map<String, Object>> mockResponse = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id", 1);
        map1.put("estado", "EN_ESPERA");
        map1.put("prioridad", 3);
        mockResponse.add(map1);

        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockResponse);

        auditoriaService.sincronizarAtenciones();

        verify(atencionRepository, times(1)).deleteAllInBatch();
        verify(atencionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void sincronizarAtenciones_Exception() {
        // RestTemplate throws exception
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenThrow(new RuntimeException("API Gateway Offline"));

        // Call should handle the exception gracefully without throwing
        auditoriaService.sincronizarAtenciones();

        // Verify that no repository calls were made to modify DB on failure
        verify(atencionRepository, never()).deleteAllInBatch();
        verify(atencionRepository, never()).saveAll(anyList());
    }
}


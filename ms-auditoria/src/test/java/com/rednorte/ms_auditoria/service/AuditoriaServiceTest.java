package com.rednorte.ms_auditoria.service;

import com.rednorte.ms_auditoria.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerEstadisticas_Success() {
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{"ALTA", 5, 2.5, 1});

        when(auditoriaRepository.obtenerEstadisticasEsperaNative()).thenReturn(mockData);

        List<Map<String, Object>> result = auditoriaService.obtenerEstadisticas();

        assertEquals(1, result.size());
        assertEquals("ALTA", result.get(0).get("prioridad_medica"));
        assertEquals(5, result.get(0).get("total_pacientes_esperando"));
    }

    @Test
    void obtenerEstadisticas_Exception() {
        when(auditoriaRepository.obtenerEstadisticasEsperaNative()).thenThrow(new RuntimeException("DB Error"));

        List<Map<String, Object>> result = auditoriaService.obtenerEstadisticas();

        assertTrue(result.isEmpty());
    }
}

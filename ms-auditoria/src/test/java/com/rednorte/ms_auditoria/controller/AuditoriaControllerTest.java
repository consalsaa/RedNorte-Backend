package com.rednorte.ms_auditoria.controller;

import com.rednorte.ms_auditoria.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditoriaController.class)
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditoriaService auditoriaService;

    @Test
    void getEstadisticas_Success() throws Exception {
        List<Map<String, Object>> mockStats = new ArrayList<>();
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("prioridad_medica", "ALTA");
        stat1.put("total_pacientes_esperando", 5);
        mockStats.add(stat1);

        when(auditoriaService.obtenerEstadisticas()).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/auditoria/estadisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prioridad_medica").value("ALTA"))
                .andExpect(jsonPath("$[0].total_pacientes_esperando").value(5));
    }
}

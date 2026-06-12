package com.rednorte.ms_auditoria.controller;

import com.rednorte.ms_auditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @Autowired
    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Operation(summary = "Obtener estadísticas de espera", description = "Ejecuta el stored procedure para obtener indicadores de rendimiento del hospital.")
    @GetMapping("/estadisticas")
    public ResponseEntity<List<Map<String, Object>>> getEstadisticas() {
        return ResponseEntity.ok(auditoriaService.obtenerEstadisticas());
    }
}

package com.rednorte.ms_auditoria.service;

import com.rednorte.ms_auditoria.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Autowired
    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<Map<String, Object>> obtenerEstadisticas() {
        // En un entorno de producción con bases de datos vacías, fallará si no existen las tablas
        // Por seguridad, usaremos try-catch para evitar que el servicio se caiga
        try {
            List<Object[]> rawStats = auditoriaRepository.obtenerEstadisticasEsperaNative();
            List<Map<String, Object>> stats = new ArrayList<>();
            for (Object[] row : rawStats) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("prioridad_medica", row[0]);
                stat.put("total_pacientes_esperando", row[1]);
                stat.put("tiempo_promedio_dias", row[2]);
                stat.put("atenciones_reasignadas", row[3]);
                stats.add(stat);
            }
            return stats;
        } catch (Exception e) {
            // Retorna un arreglo vacío si el Stored Procedure falla o no existe en H2
            return new ArrayList<>();
        }
    }
}

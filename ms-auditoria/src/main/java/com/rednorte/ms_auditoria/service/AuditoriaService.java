package com.rednorte.ms_auditoria.service;

import com.rednorte.ms_auditoria.entity.Atencion;
import com.rednorte.ms_auditoria.repository.AtencionRepository;
import com.rednorte.ms_auditoria.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final AtencionRepository atencionRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public AuditoriaService(AuditoriaRepository auditoriaRepository, 
                            AtencionRepository atencionRepository, 
                            RestTemplate restTemplate) {
        this.auditoriaRepository = auditoriaRepository;
        this.atencionRepository = atencionRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Sincroniza las atenciones de ms-listas-espera hacia la base de datos local de auditoría.
     */
    public void sincronizarAtenciones() {
        try {
            String url = "http://MS-LISTAS-ESPERA/api/listas-espera/atenciones";
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            if (response != null) {
                // Limpiar atenciones locales anteriores para evitar duplicados o estados obsoletos
                atencionRepository.deleteAllInBatch();
                
                List<Atencion> atenciones = new ArrayList<>();
                for (Map<String, Object> map : response) {
                    Long id = ((Number) map.get("id")).longValue();
                    String estado = (String) map.get("estado");
                    Integer prioridad = ((Number) map.get("prioridad")).intValue();
                    atenciones.add(new Atencion(id, estado, prioridad));
                }
                atencionRepository.saveAll(atenciones);
            }
        } catch (Exception e) {
            System.err.println("Error sincronizando atenciones con ms-listas-espera: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> obtenerEstadisticas() {
        // 1. Sincronizar datos de atenciones
        sincronizarAtenciones();

        List<Map<String, Object>> stats = new ArrayList<>();
        
        // 2. Intentar ejecutar el Stored Procedure en base de datos de producción (PostgreSQL)
        try {
            List<Object[]> rawStats = auditoriaRepository.obtenerEstadisticasEsperaNative();
            for (Object[] row : rawStats) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("prioridad", ((Number) row[0]).intValue());
                stat.put("cantidad", ((Number) row[1]).longValue());
                stats.add(stat);
            }
            return stats;
        } catch (Exception e) {
            // 3. Fallback para desarrollo local (H2) utilizando consulta HQL/JPQL
            try {
                List<Object[]> rawStats = atencionRepository.calcularEstadisticasEspera();
                for (Object[] row : rawStats) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("prioridad", ((Number) row[0]).intValue());
                    stat.put("cantidad", ((Number) row[1]).longValue());
                    stats.add(stat);
                }
                return stats;
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }
    }
}

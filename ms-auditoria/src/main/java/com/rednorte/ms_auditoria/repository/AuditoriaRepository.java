package com.rednorte.ms_auditoria.repository;

import com.rednorte.ms_auditoria.entity.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaLog, Long> {

    /**
     * Llama al procedimiento almacenado utilizando la anotación @Procedure de Spring Data JPA.
     */
    @Procedure(procedureName = "sp_calcular_estadisticas_espera")
    void spCalcularEstadisticasEspera();

    /**
     * Consulta auxiliar nativa para recuperar las estadísticas tabulares devueltas por la función
     * sp_calcular_estadisticas_espera() de PostgreSQL.
     */
    @Query(value = "SELECT * FROM sp_calcular_estadisticas_espera()", nativeQuery = true)
    List<Object[]> obtenerEstadisticasEsperaNative();
}

package com.rednorte.ms_auditoria.repository;

import com.rednorte.ms_auditoria.entity.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {
    
    @Query("SELECT a.prioridad, COUNT(a) FROM Atencion a WHERE a.estado = 'EN_ESPERA' GROUP BY a.prioridad")
    List<Object[]> calcularEstadisticasEspera();
}

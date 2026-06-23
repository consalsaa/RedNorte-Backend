package com.rednorte.ms_listas_espera.config;

import com.rednorte.ms_listas_espera.entity.Atencion;
import com.rednorte.ms_listas_espera.entity.AtencionFactory;
import com.rednorte.ms_listas_espera.entity.EstadoAtencion;
import com.rednorte.ms_listas_espera.entity.Paciente;
import com.rednorte.ms_listas_espera.repository.AtencionRepository;
import com.rednorte.ms_listas_espera.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(PacienteRepository pacienteRepository, AtencionRepository atencionRepository) {
        return args -> {
            Paciente p1 = null;
            Paciente p2 = null;

            if (pacienteRepository.findByRut("12345678-9").isEmpty()) {
                Paciente paciente1 = new Paciente(
                    null,
                    "12345678-9",
                    "Juan Carlos",
                    "Pérez López",
                    LocalDate.of(1985, 3, 15),
                    "Av. Los Leones 1234, Santiago"
                );
                p1 = pacienteRepository.save(paciente1);
            } else {
                p1 = pacienteRepository.findByRut("12345678-9").orElse(null);
            }

            if (pacienteRepository.findByRut("98765432-1").isEmpty()) {
                Paciente paciente2 = new Paciente(
                    null,
                    "98765432-1",
                    "María Loreto",
                    "González Vera",
                    LocalDate.of(1990, 8, 22),
                    "Providencia 456, Santiago"
                );
                p2 = pacienteRepository.save(paciente2);
            } else {
                p2 = pacienteRepository.findByRut("98765432-1").orElse(null);
            }

            // Precargar atenciones si la base de datos está vacía
            if (atencionRepository.count() == 0 && p1 != null && p2 != null) {
                // Cita 1: AGENDADA (para Juan Pérez - se puede reasignar)
                Atencion atencion1 = AtencionFactory.crearAtencion("CONSULTA", p1, 3, "Consulta Cardiología");
                atencion1.setEstado(EstadoAtencion.AGENDADO);
                atencionRepository.save(atencion1);

                // Cita 2: EN_ESPERA (para María Loreto - prioridad 1 (máxima), para que sea la primera elegida para reasignar)
                Atencion atencion2 = AtencionFactory.crearAtencion("CIRUGIA", p2, 1, "Bypass Gástrico");
                atencion2.setEstado(EstadoAtencion.EN_ESPERA);
                atencionRepository.save(atencion2);
            }
        };
    }
}

package com.rednorte.ms_portal_paciente.config;

import com.rednorte.ms_portal_paciente.entity.Paciente;
import com.rednorte.ms_portal_paciente.entity.Prevision;
import com.rednorte.ms_portal_paciente.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(PacienteRepository pacienteRepository) {
        return args -> {
            if (pacienteRepository.findByRut("12345678-9").isEmpty()) {
                Paciente paciente = new Paciente();
                paciente.setRut("12345678-9");
                paciente.setNombres("Juan Carlos");
                paciente.setApellidos("Pérez López");
                paciente.setFechaNacimiento(LocalDate.of(1985, 3, 15));
                paciente.setDireccion("Av. Los Leones 1234, Santiago");
                paciente.setTelefono("+56912345678");
                paciente.setCorreo("juan.perez@email.com");
                paciente.setPrevision(Prevision.FONASA);
                paciente.setHistorialClinicoBasico("Hipertensión arterial. Alergia a la penicilina.");
                pacienteRepository.save(paciente);
            }
        };
    }
}

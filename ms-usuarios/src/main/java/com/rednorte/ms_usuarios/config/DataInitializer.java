package com.rednorte.ms_usuarios.config;

import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                usuarioRepository.save(new Usuario("admin", "admin123", "ROLE_ADMIN", ""));
                usuarioRepository.save(new Usuario("medico", "medico123", "ROLE_MEDICO", ""));
                usuarioRepository.save(new Usuario("paciente", "paciente123", "ROLE_PACIENTE", "12345678-9"));
            }
        };
    }
}

package com.rednorte.ms_portal_paciente.service;

import com.rednorte.ms_portal_paciente.entity.Paciente;
import com.rednorte.ms_portal_paciente.repository.PacienteRepository;
import com.rednorte.ms_portal_paciente.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Override
    public Paciente registrarPerfil(Paciente paciente) {
        if (pacienteRepository.findByRut(paciente.getRut()).isPresent()) {
            throw new RuntimeException("El paciente ya está registrado en el portal");
        }
        return pacienteRepository.save(paciente);
    }

    @Override
    public Paciente obtenerPerfilPorRut(String rut) {
        return pacienteRepository.findByRut(rut)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado en el portal"));
    }

    @Override
    public Paciente actualizarPerfil(String rut, Paciente datosNuevos) {
        Paciente paciente = obtenerPerfilPorRut(rut);
        
        // Actualizar datos permitidos
        paciente.setDireccion(datosNuevos.getDireccion());
        paciente.setTelefono(datosNuevos.getTelefono());
        paciente.setCorreo(datosNuevos.getCorreo());
        paciente.setPrevision(datosNuevos.getPrevision());
        
        if (datosNuevos.getHistorialClinicoBasico() != null) {
            paciente.setHistorialClinicoBasico(datosNuevos.getHistorialClinicoBasico());
        }
        
        return pacienteRepository.save(paciente);
    }
}

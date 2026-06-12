package com.rednorte.ms_listas_espera.service;

import com.rednorte.ms_listas_espera.entity.Paciente;
import com.rednorte.ms_listas_espera.exception.ResourceNotFoundException;
import com.rednorte.ms_listas_espera.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Override
    public Paciente registrarPaciente(Paciente paciente) {
        // Aquí podríamos invocar el Factory Pattern en el futuro si procesamos DTOS
        return pacienteRepository.save(paciente);
    }

    @Override
    public List<Paciente> obtenerTodos() {
        return pacienteRepository.findAll();
    }

    @Override
    public Paciente obtenerPorRut(String rut) {
        return pacienteRepository.findByRut(rut)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente con RUT " + rut + " no encontrado"));
    }
}

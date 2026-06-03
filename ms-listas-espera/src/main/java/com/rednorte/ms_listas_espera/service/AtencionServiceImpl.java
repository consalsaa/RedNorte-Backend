package com.rednorte.ms_listas_espera.service;

import com.rednorte.ms_listas_espera.controller.AtencionDTO;
import com.rednorte.ms_listas_espera.entity.Atencion;
import com.rednorte.ms_listas_espera.entity.AtencionFactory;
import com.rednorte.ms_listas_espera.entity.EstadoAtencion;
import com.rednorte.ms_listas_espera.entity.Paciente;
import com.rednorte.ms_listas_espera.entity.SagaStatus;
import com.rednorte.ms_listas_espera.repository.AtencionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtencionServiceImpl implements AtencionService {

    @Autowired
    private AtencionRepository atencionRepository;

    @Autowired
    private PacienteService pacienteService;

    @Override
    public Atencion registrarAtencion(AtencionDTO dto) {
        Paciente paciente = pacienteService.obtenerPorRut(dto.getRutPaciente());
        
        // Uso del Factory Method Pattern para instanciar la clase correcta
        Atencion atencion = AtencionFactory.crearAtencion(dto.getTipo(), paciente, dto.getPrioridad(), dto.getDetalle());
        
        return atencionRepository.save(atencion);
    }

    @Override
    public List<Atencion> obtenerListaEspera() {
        return atencionRepository.findByEstadoOrderByPrioridadAscFechaSolicitudAsc(EstadoAtencion.EN_ESPERA);
    }

    @Override
    public List<Atencion> obtenerTodas() {
        return atencionRepository.findAll();
    }

    @Override
    public Atencion actualizarEstado(Long id, String nuevoEstado) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atención con ID " + id + " no encontrada"));
        
        try {
            EstadoAtencion estado = EstadoAtencion.valueOf(nuevoEstado.toUpperCase());
            atencion.setEstado(estado);
            return atencionRepository.save(atencion);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido. Opciones válidas: EN_ESPERA, AGENDADO, ATENDIDO, CANCELADO");
        }
    }

    @Override
    public List<Atencion> obtenerPorRutPaciente(String rut) {
        return atencionRepository.findByPacienteRut(rut);
    }

    @Override
    public Atencion registrarAtencionSaga(AtencionDTO dto) {
        Paciente paciente = pacienteService.obtenerPorRut(dto.getRutPaciente());
        Atencion atencion = AtencionFactory.crearAtencion(dto.getTipo(), paciente, dto.getPrioridad(), dto.getDetalle());
        atencion.setSagaStatus(SagaStatus.PENDING);
        return atencionRepository.save(atencion);
    }

    @Override
    public Atencion cancelarAtencionSaga(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atención con ID " + id + " no encontrada"));
        atencion.setSagaStatus(SagaStatus.CANCELLED);
        atencion.setEstado(EstadoAtencion.CANCELADO);
        return atencionRepository.save(atencion);
    }

    @Override
    public Atencion confirmarAtencionSaga(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atención con ID " + id + " no encontrada"));
        atencion.setSagaStatus(SagaStatus.CONFIRMED);
        return atencionRepository.save(atencion);
    }
}

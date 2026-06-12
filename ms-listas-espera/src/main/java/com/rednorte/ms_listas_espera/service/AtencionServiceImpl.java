package com.rednorte.ms_listas_espera.service;

import com.rednorte.ms_listas_espera.controller.AtencionDTO;
import com.rednorte.ms_listas_espera.entity.Atencion;
import com.rednorte.ms_listas_espera.entity.AtencionFactory;
import com.rednorte.ms_listas_espera.entity.EstadoAtencion;
import com.rednorte.ms_listas_espera.entity.Paciente;
import com.rednorte.ms_listas_espera.entity.SagaStatus;
import com.rednorte.ms_listas_espera.repository.AtencionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.rednorte.ms_listas_espera.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Implementación del servicio {@link AtencionService} que gestiona la lógica de negocio
 * relacionada con las atenciones médicas y la lista de espera de RedNorte.
 * Aplica políticas de caché para optimizar las consultas de listas de espera.
 */
@Service
public class AtencionServiceImpl implements AtencionService {

    @Autowired
    private AtencionRepository atencionRepository;

    @Autowired
    private PacienteService pacienteService;

    /**
     * Registra una nueva atención en el sistema utilizando un DTO.
     * Busca al paciente por su RUT e instancia la atención adecuada utilizando un Factory Method.
     * Al registrar una nueva atención, se limpia la caché "listasEspera" para garantizar datos frescos.
     *
     * @param dto el objeto de transferencia de datos con la información de la atención.
     * @return la atención registrada y guardada en la base de datos.
     * @throws ResourceNotFoundException si el paciente con el RUT especificado no existe.
     */
    @Override
    @CacheEvict(value = "listasEspera", allEntries = true)
    public Atencion registrarAtencion(AtencionDTO dto) {
        Paciente paciente = pacienteService.obtenerPorRut(dto.getRutPaciente());
        
        // Uso del Factory Method Pattern para instanciar la clase correcta
        Atencion atencion = AtencionFactory.crearAtencion(dto.getTipo(), paciente, dto.getPrioridad(), dto.getDetalle());
        
        return atencionRepository.save(atencion);
    }

    /**
     * Obtiene la lista de atenciones pendientes ordenadas por prioridad de forma ascendente.
     * Este método utiliza caché {@code @Cacheable} con el nombre "listasEspera" para
     * optimizar las consultas sucesivas y evitar accesos redundantes a la base de datos.
     *
     * @return una lista de las atenciones médicas pendientes de atención.
     */
    @Override
    @Cacheable(value = "listasEspera")
    public List<Atencion> obtenerListaEspera() {
        return atencionRepository.findByEstadoOrderByPrioridadAscFechaSolicitudAsc(EstadoAtencion.EN_ESPERA);
    }

    /**
     * Recupera todas las atenciones médicas registradas en el sistema, independientemente de su estado.
     *
     * @return la lista completa de todas las atenciones registradas.
     */
    @Override
    public List<Atencion> obtenerTodas() {
        return atencionRepository.findAll();
    }

    /**
     * Actualiza el estado de una atención existente. Si el estado cambia con éxito,
     * se procede a invalidar toda la caché "listasEspera" para asegurar la consistencia.
     *
     * @param id el ID de la atención a actualizar.
     * @param nuevoEstado el nuevo estado deseado.
     * @return la atención médica con el estado actualizado y persistido.
     * @throws ResourceNotFoundException si no se encuentra la atención médica con el ID dado.
     * @throws IllegalArgumentException si el valor de nuevoEstado no coincide con ningún valor de {@link EstadoAtencion}.
     */
    @Override
    @CacheEvict(value = "listasEspera", allEntries = true)
    public Atencion actualizarEstado(Long id, String nuevoEstado) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención con ID " + id + " no encontrada"));
        
        try {
            EstadoAtencion estado = EstadoAtencion.valueOf(nuevoEstado.toUpperCase());
            atencion.setEstado(estado);
            return atencionRepository.save(atencion);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido. Opciones válidas: EN_ESPERA, AGENDADO, ATENDIDO, CANCELADO");
        }
    }

    /**
     * Busca y obtiene todas las atenciones médicas registradas para el RUT de un paciente determinado.
     *
     * @param rut el RUT del paciente a consultar.
     * @return la lista de atenciones pertenecientes a dicho paciente.
     */
    @Override
    public List<Atencion> obtenerPorRutPaciente(String rut) {
        return atencionRepository.findByPacienteRut(rut);
    }

    /**
     * Registra una atención médica en el contexto de una SAGA. Inicializa el estado
     * del ciclo de vida de la transacción distribuida como PENDING.
     *
     * @param dto el DTO con los datos para crear la atención.
     * @return la atención médica registrada con estado de transacción SAGA pendiente.
     * @throws ResourceNotFoundException si el paciente asociado al RUT especificado no existe.
     */
    @Override
    @CacheEvict(value = "listasEspera", allEntries = true)
    public Atencion registrarAtencionSaga(AtencionDTO dto) {
        Paciente paciente = pacienteService.obtenerPorRut(dto.getRutPaciente());
        Atencion atencion = AtencionFactory.crearAtencion(dto.getTipo(), paciente, dto.getPrioridad(), dto.getDetalle());
        atencion.setSagaStatus(SagaStatus.PENDING);
        return atencionRepository.save(atencion);
    }

    /**
     * Cancela la atención médica involucrada en una SAGA estableciendo su estado
     * como CANCELADO y su estado de transacción como CANCELLED.
     *
     * @param id el ID de la atención a cancelar.
     * @return la atención médica actualizada.
     * @throws ResourceNotFoundException si no existe la atención con el ID provisto.
     */
    @Override
    @CacheEvict(value = "listasEspera", allEntries = true)
    public Atencion cancelarAtencionSaga(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención con ID " + id + " no encontrada"));
        atencion.setSagaStatus(SagaStatus.CANCELLED);
        atencion.setEstado(EstadoAtencion.CANCELADO);
        return atencionRepository.save(atencion);
    }

    /**
     * Confirma la atención médica en una SAGA estableciendo su estado de
     * transacción distribuida como CONFIRMED.
     *
     * @param id el ID de la atención a confirmar.
     * @return la atención médica actualizada.
     * @throws ResourceNotFoundException si no existe la atención con el ID provisto.
     */
    @Override
    @CacheEvict(value = "listasEspera", allEntries = true)
    public Atencion confirmarAtencionSaga(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención con ID " + id + " no encontrada"));
        atencion.setSagaStatus(SagaStatus.CONFIRMED);
        return atencionRepository.save(atencion);
    }
}

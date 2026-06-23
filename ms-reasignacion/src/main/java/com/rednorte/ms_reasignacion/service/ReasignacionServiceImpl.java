package com.rednorte.ms_reasignacion.service;

import com.rednorte.ms_reasignacion.entity.Reasignacion;
import com.rednorte.ms_reasignacion.repository.ReasignacionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.rednorte.ms_reasignacion.config.RabbitMQConfig;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de reasignación automática.
 * Utiliza el patrón Circuit Breaker para garantizar resiliencia
 * cuando ms-listas-espera no está disponible.
 */
@Service
public class ReasignacionServiceImpl implements ReasignacionService {

    private static final Logger log = LoggerFactory.getLogger(ReasignacionServiceImpl.class);

    private static final String LISTAS_ESPERA_URL = "http://MS-LISTAS-ESPERA";

    @Autowired
    private ReasignacionRepository reasignacionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Procesa la cancelación de una atención.
     * 1. Marca la atención original como CANCELADO en ms-listas-espera.
     * 2. Busca la siguiente atención prioritaria EN_ESPERA.
     * 3. La marca como AGENDADO y registra la reasignación.
     * 
     * Circuit Breaker: Si ms-listas-espera no responde, se activa el fallback.
     */
    @Override
    @CircuitBreaker(name = "listasEsperaService", fallbackMethod = "fallbackProcesarCancelacion")
    public Reasignacion procesarCancelacion(Long atencionId) {
        log.info("Procesando cancelación de atención ID: {}", atencionId);

        // 1. Obtener los datos de la atención cancelada
        String urlAtencion = LISTAS_ESPERA_URL + "/api/listas-espera/atenciones/" + atencionId + "/estado?nuevoEstado=CANCELADO";
        ResponseEntity<Map> cancelResponse = restTemplate.exchange(
                urlAtencion, HttpMethod.PUT, null, Map.class);
        
        Map atencionCancelada = cancelResponse.getBody();
        String rutOriginal = "N/A";
        if (atencionCancelada != null && atencionCancelada.get("paciente") != null) {
            Map paciente = (Map) atencionCancelada.get("paciente");
            rutOriginal = (String) paciente.get("rut");
        }

        // 2. Obtener la lista de espera ordenada por prioridad
        String urlPendientes = LISTAS_ESPERA_URL + "/api/listas-espera/atenciones/pendientes";
        ResponseEntity<List<Map>> pendientesResponse = restTemplate.exchange(
                urlPendientes, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map>>() {});

        List<Map> pendientes = pendientesResponse.getBody();

        if (pendientes == null || pendientes.isEmpty()) {
            log.info("No hay pacientes en lista de espera para reasignar.");
            Reasignacion sinMatch = new Reasignacion();
            sinMatch.setAtencionCanceladaId(atencionId);
            sinMatch.setAtencionReasignadaId(0L);
            sinMatch.setRutPacienteOriginal(rutOriginal);
            sinMatch.setRutPacienteReasignado("N/A");
            sinMatch.setEstado("SIN_CANDIDATO");
            sinMatch.setObservaciones("No se encontraron pacientes en lista de espera para reasignar.");
            return reasignacionRepository.save(sinMatch);
        }

        // 3. Tomar el primer paciente prioritario que coincida clínicamente con la especialidad/tipo
        Map siguienteAtencion = null;
        String especialidadCancelada = obtenerDetalle(atencionCancelada);
        String tipoCancelado = obtenerTipoAtencion(atencionCancelada);

        // Intento 1: Buscar coincidencia exacta por tipo (Consulta/Cirugía) y especialidad/procedimiento
        for (Map pendiente : pendientes) {
            String especialidadPendiente = obtenerDetalle(pendiente);
            String tipoPendiente = obtenerTipoAtencion(pendiente);
            if (tipoCancelado.equals(tipoPendiente) && especialidadCancelada.equalsIgnoreCase(especialidadPendiente)) {
                siguienteAtencion = pendiente;
                log.info("Coincidencia clínica exacta encontrada para especialidad '{}': Atención ID {}", especialidadCancelada, pendiente.get("id"));
                break;
            }
        }

        // Intento 2 (Fallback): Buscar coincidencia general por tipo de atención
        if (siguienteAtencion == null) {
            for (Map pendiente : pendientes) {
                String tipoPendiente = obtenerTipoAtencion(pendiente);
                if (tipoCancelado.equals(tipoPendiente)) {
                    siguienteAtencion = pendiente;
                    log.info("Coincidencia de tipo general '{}' encontrada (Especialidad cancelada: '{}', pendiente: '{}'): Atención ID {}", 
                            tipoCancelado, especialidadCancelada, obtenerDetalle(pendiente), pendiente.get("id"));
                    break;
                }
            }
        }

        // Intento 3 (Último recurso): Tomar el primero de la lista
        if (siguienteAtencion == null) {
            siguienteAtencion = pendientes.get(0);
            log.info("No se encontró coincidencia por tipo o especialidad. Asignando primer paciente en fila por defecto: Atención ID {}", siguienteAtencion.get("id"));
        }

        Long siguienteId = ((Number) siguienteAtencion.get("id")).longValue();
        
        String rutReasignado = "N/A";
        if (siguienteAtencion.get("paciente") != null) {
            Map pacienteReasignado = (Map) siguienteAtencion.get("paciente");
            rutReasignado = (String) pacienteReasignado.get("rut");
        }

        // 4. Actualizar estado a AGENDADO
        String urlAgendar = LISTAS_ESPERA_URL + "/api/listas-espera/atenciones/" + siguienteId + "/estado?nuevoEstado=AGENDADO";
        restTemplate.exchange(urlAgendar, HttpMethod.PUT, null, Map.class);

        // 5. Registrar la reasignación
        Reasignacion reasignacion = new Reasignacion();
        reasignacion.setAtencionCanceladaId(atencionId);
        reasignacion.setAtencionReasignadaId(siguienteId);
        reasignacion.setRutPacienteOriginal(rutOriginal);
        reasignacion.setRutPacienteReasignado(rutReasignado);
        reasignacion.setEstado("EXITOSA");
        reasignacion.setObservaciones("Reasignación automática exitosa. "
                + "Atención " + siguienteId + " agendada para paciente " + rutReasignado);

        log.info("Reasignación exitosa: atención {} reasignada a paciente {}", siguienteId, rutReasignado);

        // Publicar evento en RabbitMQ
        try {
            Map<String, Object> messagePayload = Map.of(
                "atencionId", siguienteId,
                "rutPaciente", rutReasignado,
                "mensaje", "Reasignación automática exitosa. "
                        + "Atención ID " + siguienteId + " agendada para el paciente " + rutReasignado,
                "tipo", "REASIGNACION",
                "fecha", java.time.LocalDateTime.now().toString()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, messagePayload);
            log.info("Mensaje de reasignación enviado a RabbitMQ para atención ID: {}", siguienteId);
        } catch (Exception e) {
            log.error("Fallo al enviar mensaje a RabbitMQ: {}", e.getMessage());
        }

        return reasignacionRepository.save(reasignacion);
    }

    /**
     * Método fallback del Circuit Breaker.
     * Se ejecuta cuando ms-listas-espera no responde o el circuito está abierto.
     * Registra la reasignación como FALLIDA para reintentar manualmente luego.
     */
    public Reasignacion fallbackProcesarCancelacion(Long atencionId, Throwable t) {
        log.error("Circuit Breaker activado para atención {}: {}", atencionId, t.getMessage());

        Reasignacion fallback = new Reasignacion();
        fallback.setAtencionCanceladaId(atencionId);
        fallback.setAtencionReasignadaId(0L);
        fallback.setRutPacienteOriginal("N/A");
        fallback.setRutPacienteReasignado("N/A");
        fallback.setEstado("FALLIDA");
        fallback.setObservaciones("Circuit Breaker activado: el servicio de listas de espera "
                + "no está disponible. Error: " + t.getMessage());

        return reasignacionRepository.save(fallback);
    }

    @Override
    public List<Reasignacion> obtenerHistorial() {
        return reasignacionRepository.findAll();
    }

    @Override
    public List<Reasignacion> obtenerPorPaciente(String rut) {
        return reasignacionRepository.findByRutPacienteReasignado(rut);
    }

    private String obtenerDetalle(Map atencion) {
        if (atencion == null) return "";
        if (atencion.containsKey("especialidad") && atencion.get("especialidad") != null) {
            return (String) atencion.get("especialidad");
        }
        if (atencion.containsKey("tipoCirugia") && atencion.get("tipoCirugia") != null) {
            return (String) atencion.get("tipoCirugia");
        }
        if (atencion.containsKey("motivoEmergencia") && atencion.get("motivoEmergencia") != null) {
            return (String) atencion.get("motivoEmergencia");
        }
        if (atencion.containsKey("detalle") && atencion.get("detalle") != null) {
            return (String) atencion.get("detalle");
        }
        return "";
    }

    private String obtenerTipoAtencion(Map atencion) {
        if (atencion == null) return "CONSULTA";
        if (atencion.containsKey("especialidad")) return "CONSULTA";
        if (atencion.containsKey("tipoCirugia")) return "CIRUGIA";
        if (atencion.containsKey("motivoEmergencia")) return "EMERGENCIA";
        if (atencion.get("tipo") != null) return (String) atencion.get("tipo");
        return "CONSULTA";
    }
}

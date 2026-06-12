package com.rednorte.ms_listas_espera.controller;

import com.rednorte.ms_listas_espera.entity.Atencion;
import com.rednorte.ms_listas_espera.service.AtencionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que gestiona las peticiones relacionadas con el registro,
 * consulta y actualización de atenciones médicas en la lista de espera.
 * Proporciona endpoints para los médicos y el personal administrativo de RedNorte.
 */
@RestController
@RequestMapping("/api/listas-espera/atenciones")
public class AtencionController {

    @Autowired
    private AtencionService atencionService;

    /**
     * Registra una nueva atención médica en el sistema y la añade a la lista de espera.
     *
     * @param dto el DTO que contiene la información del paciente y la atención médica a registrar.
     * @return un {@link ResponseEntity} con la atención registrada y el estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Atencion> registrar(@RequestBody AtencionDTO dto) {
        Atencion nuevaAtencion = atencionService.registrarAtencion(dto);
        return new ResponseEntity<>(nuevaAtencion, HttpStatus.CREATED);
    }

    /**
     * Obtiene la lista de todas las atenciones médicas que se encuentran pendientes (estado EN_ESPERA).
     * El listado se retorna ordenado por prioridad de forma ascendente y por fecha de solicitud.
     *
     * @return un {@link ResponseEntity} con la lista de atenciones pendientes y el estado HTTP 200 (OK).
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Atencion>> listarPendientes() {
        return ResponseEntity.ok(atencionService.obtenerListaEspera());
    }

    /**
     * Obtiene la lista con todas las atenciones médicas registradas en el sistema.
     *
     * @return un {@link ResponseEntity} con el listado completo de atenciones y el estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Atencion>> listarTodas() {
        return ResponseEntity.ok(atencionService.obtenerTodas());
    }

    /**
     * Actualiza el estado de una atención médica específica.
     *
     * @param id el identificador único de la atención a actualizar.
     * @param nuevoEstado el nuevo estado para la atención (opciones válidas: EN_ESPERA, AGENDADO, ATENDIDO, CANCELADO).
     * @return un {@link ResponseEntity} con la atención actualizada y el estado HTTP 200 (OK).
     * @throws com.rednorte.ms_listas_espera.exception.ResourceNotFoundException si no existe la atención con el ID proporcionado.
     * @throws IllegalArgumentException si el estado proporcionado no es válido.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Atencion> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        Atencion atencionActualizada = atencionService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(atencionActualizada);
    }

    /**
     * Obtiene el historial de atenciones médicas asociadas al RUT de un paciente.
     *
     * @param rut el RUT del paciente para consultar sus atenciones.
     * @return un {@link ResponseEntity} con la lista de atenciones encontradas y el estado HTTP 200 (OK).
     */
    @GetMapping("/paciente/{rut}")
    public ResponseEntity<List<Atencion>> obtenerPorRutPaciente(@PathVariable String rut) {
        return ResponseEntity.ok(atencionService.obtenerPorRutPaciente(rut));
    }
}

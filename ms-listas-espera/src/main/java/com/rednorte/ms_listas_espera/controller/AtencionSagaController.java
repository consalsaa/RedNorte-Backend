package com.rednorte.ms_listas_espera.controller;

import com.rednorte.ms_listas_espera.entity.Atencion;
import com.rednorte.ms_listas_espera.service.AtencionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listas-espera/saga")
public class AtencionSagaController {

    @Autowired
    private AtencionService atencionService;

    @PostMapping("/crear")
    public ResponseEntity<Atencion> crearSaga(@RequestBody AtencionDTO dto) {
        Atencion atencion = atencionService.registrarAtencionSaga(dto);
        return new ResponseEntity<>(atencion, HttpStatus.CREATED);
    }

    @PutMapping("/cancelar/{id}")
    public ResponseEntity<Atencion> cancelarSaga(@PathVariable Long id) {
        Atencion atencion = atencionService.cancelarAtencionSaga(id);
        return ResponseEntity.ok(atencion);
    }

    @PutMapping("/confirmar/{id}")
    public ResponseEntity<Atencion> confirmarSaga(@PathVariable Long id) {
        Atencion atencion = atencionService.confirmarAtencionSaga(id);
        return ResponseEntity.ok(atencion);
    }
}

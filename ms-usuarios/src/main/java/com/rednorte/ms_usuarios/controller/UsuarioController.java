package com.rednorte.ms_usuarios.controller;

import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Listar todas las cuentas de usuario", description = "Retorna una lista con la información básica (username, role, rut) de todos los usuarios registrados.")
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listarTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Map<String, String>> response = usuarios.stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "role", u.getRole(),
                        "rut", u.getRut() != null ? u.getRut() : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}

package com.rednorte.ms_usuarios.controller;

import com.rednorte.ms_usuarios.service.AuthService;
import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Endpoint de inicio de sesión", description = "Valida credenciales y devuelve JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<String> tokenOpt = authService.login(username, password);

        if (tokenOpt.isPresent()) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if(usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                return ResponseEntity.ok(Map.of(
                        "token", tokenOpt.get(),
                        "username", usuario.getUsername(),
                        "role", usuario.getRole(),
                        "message", "Autenticación exitosa"
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Credenciales inválidas"));
    }
}

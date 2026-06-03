package com.rednorte.api_gateway.controller;

import com.rednorte.api_gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación.
 * Genera tokens JWT para usuarios autenticados.
 * En producción, se validaría contra una base de datos de usuarios.
 */
@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint de login que valida credenciales y devuelve un token JWT.
     * Para esta evaluación se usan credenciales de demostración.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Validación de credenciales (demo para evaluación)
        // En producción, se consultaría una base de datos de usuarios
        if (validarCredenciales(username, password)) {
            String role = obtenerRol(username);
            String token = jwtUtil.generateToken(username, role);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "role", role,
                    "message", "Autenticación exitosa"
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Credenciales inválidas"));
    }

    /**
     * Credenciales de demostración con control de acceso por roles.
     */
    private boolean validarCredenciales(String username, String password) {
        // Usuarios de demostración
        return ("admin".equals(username) && "admin123".equals(password)) ||
               ("medico".equals(username) && "medico123".equals(password)) ||
               ("paciente".equals(username) && "paciente123".equals(password));
    }

    private String obtenerRol(String username) {
        return switch (username) {
            case "admin" -> "ROLE_ADMIN";
            case "medico" -> "ROLE_MEDICO";
            case "paciente" -> "ROLE_PACIENTE";
            default -> "ROLE_USER";
        };
    }
}

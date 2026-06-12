package com.rednorte.ms_usuarios.service;

import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import com.rednorte.ms_usuarios.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> login(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Para la evaluación se compara en texto plano. En un entorno real se usaría BCrypt.
            if (usuario.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRole());
                return Optional.of(token);
            }
        }
        
        return Optional.empty();
    }
}

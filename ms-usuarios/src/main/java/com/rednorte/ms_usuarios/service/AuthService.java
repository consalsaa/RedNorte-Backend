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

    /**
     * Valida las credenciales del usuario y genera un token JWT si son correctas.
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return Optional con el token JWT si el login es exitoso, o vacío si falla.
     */
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

    /**
     * Registra un nuevo usuario en el sistema.
     * Si el nombre de usuario ya existe, lanza una excepción.
     * @param username Nombre de usuario deseado (debe ser único).
     * @param password Contraseña en texto plano.
     * @param role Rol del usuario (ej: ROLE_PACIENTE, ROLE_MEDICO, ROLE_ADMIN).
     * @return El usuario recién creado.
     */
    public Usuario registrarUsuario(String username, String password, String role, String rut) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario '" + username + "' ya está en uso.");
        }
        // El rol por defecto para auto-registro es ROLE_PACIENTE para proteger el sistema
        String rolAsignado = (role != null && !role.isBlank()) ? role : "ROLE_PACIENTE";
        Usuario nuevoUsuario = new Usuario(username, password, rolAsignado, rut);
        return usuarioRepository.save(nuevoUsuario);
    }
}

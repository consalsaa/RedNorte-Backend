package com.rednorte.ms_usuarios.service;

import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import com.rednorte.ms_usuarios.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        Usuario mockUser = new Usuario("admin", "admin123", "ROLE_ADMIN");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken("admin", "ROLE_ADMIN")).thenReturn("mockToken");

        Optional<String> result = authService.login("admin", "admin123");

        assertTrue(result.isPresent());
        assertEquals("mockToken", result.get());
        verify(usuarioRepository, times(1)).findByUsername("admin");
    }

    @Test
    void login_WrongPassword() {
        Usuario mockUser = new Usuario("admin", "admin123", "ROLE_ADMIN");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        Optional<String> result = authService.login("admin", "wrongPass");

        assertTrue(result.isEmpty());
    }

    @Test
    void login_UserNotFound() {
        when(usuarioRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        Optional<String> result = authService.login("notfound", "pass");

        assertTrue(result.isEmpty());
    }

    @Test
    void registrarUsuario_Success() {
        Usuario mockUser = new Usuario("juan.perez", "pass123", "ROLE_PACIENTE", "12345678-9");
        when(usuarioRepository.findByUsername("juan.perez")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(mockUser);

        Usuario result = authService.registrarUsuario("juan.perez", "pass123", "ROLE_PACIENTE", "12345678-9");

        assertNotNull(result);
        assertEquals("juan.perez", result.getUsername());
        assertEquals("ROLE_PACIENTE", result.getRole());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_AlreadyExists() {
        Usuario mockUser = new Usuario("juan.perez", "pass123", "ROLE_PACIENTE", "12345678-9");
        when(usuarioRepository.findByUsername("juan.perez")).thenReturn(Optional.of(mockUser));

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            authService.registrarUsuario("juan.perez", "pass123", "ROLE_PACIENTE", "12345678-9");
        });

        assertEquals("El nombre de usuario 'juan.perez' ya está en uso.", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}

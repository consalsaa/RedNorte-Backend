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
}

package com.rednorte.ms_usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.ms_usuarios.entity.Usuario;
import com.rednorte.ms_usuarios.repository.UsuarioRepository;
import com.rednorte.ms_usuarios.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success() throws Exception {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", "admin");
        creds.put("password", "admin123");

        Usuario mockUser = new Usuario("admin", "admin123", "ROLE_ADMIN");

        when(authService.login("admin", "admin123")).thenReturn(Optional.of("mockToken"));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void login_Failure() throws Exception {
        Map<String, String> creds = new HashMap<>();
        creds.put("username", "admin");
        creds.put("password", "wrong");

        when(authService.login("admin", "wrong")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }
}

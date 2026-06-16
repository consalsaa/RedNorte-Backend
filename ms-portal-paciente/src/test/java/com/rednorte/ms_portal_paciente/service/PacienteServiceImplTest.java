package com.rednorte.ms_portal_paciente.service;

import com.rednorte.ms_portal_paciente.entity.Paciente;
import com.rednorte.ms_portal_paciente.entity.Prevision;
import com.rednorte.ms_portal_paciente.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceImplTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteServiceImpl pacienteService;

    @Test
    void testRegistrarPerfilExitoso() {
        Paciente paciente = new Paciente();
        paciente.setRut("12.345.678-9");
        paciente.setNombres("Juan");
        paciente.setApellidos("Pérez");
        paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        paciente.setPrevision(Prevision.FONASA);

        when(pacienteRepository.findByRut(paciente.getRut())).thenReturn(Optional.empty());
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        Paciente result = pacienteService.registrarPerfil(paciente);

        assertNotNull(result);
        assertEquals(paciente.getRut(), result.getRut());
        verify(pacienteRepository, times(1)).findByRut(paciente.getRut());
        verify(pacienteRepository, times(1)).save(paciente);
    }

    @Test
    void testRegistrarPerfilExistenteThrowsException() {
        Paciente paciente = new Paciente();
        paciente.setRut("12.345.678-9");

        when(pacienteRepository.findByRut(paciente.getRut())).thenReturn(Optional.of(paciente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pacienteService.registrarPerfil(paciente);
        });

        assertEquals("El paciente ya está registrado en el portal", exception.getMessage());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void testObtenerPerfilPorRutExitoso() {
        String rut = "12.345.678-9";
        Paciente paciente = new Paciente();
        paciente.setRut(rut);

        when(pacienteRepository.findByRut(rut)).thenReturn(Optional.of(paciente));

        Paciente result = pacienteService.obtenerPerfilPorRut(rut);

        assertNotNull(result);
        assertEquals(rut, result.getRut());
        verify(pacienteRepository, times(1)).findByRut(rut);
    }

    @Test
    void testObtenerPerfilPorRutInexistenteThrowsException() {
        String rut = "12.345.678-9";
        when(pacienteRepository.findByRut(rut)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pacienteService.obtenerPerfilPorRut(rut);
        });

        assertEquals("Paciente no encontrado en el portal", exception.getMessage());
    }

    @Test
    void testActualizarPerfilExitoso() {
        String rut = "12.345.678-9";
        
        Paciente pacienteExistente = new Paciente();
        pacienteExistente.setRut(rut);
        pacienteExistente.setDireccion("Calle Antigua 123");
        pacienteExistente.setTelefono("911111111");
        pacienteExistente.setCorreo("antiguo@correo.com");
        pacienteExistente.setPrevision(Prevision.FONASA);
        pacienteExistente.setHistorialClinicoBasico("Historial Antiguo");

        Paciente datosNuevos = new Paciente();
        datosNuevos.setDireccion("Calle Nueva 456");
        datosNuevos.setTelefono("922222222");
        datosNuevos.setCorreo("nuevo@correo.com");
        datosNuevos.setPrevision(Prevision.ISAPRE);
        datosNuevos.setHistorialClinicoBasico("Historial Nuevo");

        when(pacienteRepository.findByRut(rut)).thenReturn(Optional.of(pacienteExistente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Paciente result = pacienteService.actualizarPerfil(rut, datosNuevos);

        assertNotNull(result);
        assertEquals("Calle Nueva 456", result.getDireccion());
        assertEquals("922222222", result.getTelefono());
        assertEquals("nuevo@correo.com", result.getCorreo());
        assertEquals(Prevision.ISAPRE, result.getPrevision());
        assertEquals("Historial Nuevo", result.getHistorialClinicoBasico());
        verify(pacienteRepository, times(1)).save(pacienteExistente);
    }

    @Test
    void testActualizarPerfilSinHistorialClinico() {
        String rut = "12.345.678-9";
        
        Paciente pacienteExistente = new Paciente();
        pacienteExistente.setRut(rut);
        pacienteExistente.setHistorialClinicoBasico("Historial Antiguo");

        Paciente datosNuevos = new Paciente();
        datosNuevos.setDireccion("Calle Nueva 456");
        datosNuevos.setHistorialClinicoBasico(null);

        when(pacienteRepository.findByRut(rut)).thenReturn(Optional.of(pacienteExistente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Paciente result = pacienteService.actualizarPerfil(rut, datosNuevos);

        assertNotNull(result);
        assertEquals("Historial Antiguo", result.getHistorialClinicoBasico());
        verify(pacienteRepository, times(1)).save(pacienteExistente);
    }
}

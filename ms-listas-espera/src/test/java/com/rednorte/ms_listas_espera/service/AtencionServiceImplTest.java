package com.rednorte.ms_listas_espera.service;

import com.rednorte.ms_listas_espera.controller.AtencionDTO;
import com.rednorte.ms_listas_espera.entity.*;
import com.rednorte.ms_listas_espera.repository.AtencionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtencionServiceImplTest {

    @Mock
    private AtencionRepository atencionRepository;

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private AtencionServiceImpl atencionService;

    private Paciente paciente;
    private Atencion consulta;
    private Atencion cirugia;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(
            1L, 
            "12345678-9", 
            "Juan", 
            "Pérez", 
            LocalDate.of(1985, 3, 15), 
            "Av. Los Leones 1234"
        );

        consulta = AtencionFactory.crearAtencion("CONSULTA", paciente, 3, "Cardiología");
        consulta.setId(1L);

        cirugia = AtencionFactory.crearAtencion("CIRUGIA", paciente, 1, "Cirugía Cardiovascular");
        cirugia.setId(2L);
    }

    @Test
    @DisplayName("Given valid DTO, when registrarAtencion, then save and return atencion")
    void registrarAtencionExitoso() {
        // Arrange (Given)
        AtencionDTO dto = new AtencionDTO();
        dto.setRutPaciente("12345678-9");
        dto.setTipo("CONSULTA");
        dto.setPrioridad(3);
        dto.setDetalle("Cardiología");

        when(pacienteService.obtenerPorRut("12345678-9")).thenReturn(paciente);
        when(atencionRepository.save(any(Atencion.class))).thenAnswer(invocation -> {
            Atencion saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        // Act (When)
        Atencion resultado = atencionService.registrarAtencion(dto);

        // Assert (Then)
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(paciente, resultado.getPaciente());
        assertEquals(3, resultado.getPrioridad());
        assertTrue(resultado instanceof AtencionConsulta);
        assertEquals("Cardiología", ((AtencionConsulta) resultado).getEspecialidad());
        
        verify(pacienteService, times(1)).obtenerPorRut("12345678-9");
        verify(atencionRepository, times(1)).save(any(Atencion.class));
    }

    @Test
    @DisplayName("Given non-existent patient, when registrarAtencion, then throw RuntimeException")
    void registrarAtencionPacienteNoEncontrado() {
        // Arrange (Given)
        AtencionDTO dto = new AtencionDTO();
        dto.setRutPaciente("99999999-9");
        dto.setTipo("CONSULTA");
        dto.setPrioridad(3);
        dto.setDetalle("Cardiología");

        when(pacienteService.obtenerPorRut("99999999-9"))
            .thenThrow(new RuntimeException("Paciente con RUT 99999999-9 no encontrado"));

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            atencionService.registrarAtencion(dto);
        });

        assertEquals("Paciente con RUT 99999999-9 no encontrado", exception.getMessage());
        verify(pacienteService, times(1)).obtenerPorRut("99999999-9");
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    @Test
    @DisplayName("When obtenerListaEspera, then return only pending attentions sorted")
    void obtenerListaEsperaExitoso() {
        // Arrange (Given)
        List<Atencion> esperaMock = Arrays.asList(cirugia, consulta);
        when(atencionRepository.findByEstadoOrderByPrioridadAscFechaSolicitudAsc(EstadoAtencion.EN_ESPERA))
            .thenReturn(esperaMock);

        // Act (When)
        List<Atencion> resultado = atencionService.obtenerListaEspera();

        // Assert (Then)
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(cirugia, resultado.get(0)); // G1
        assertEquals(consulta, resultado.get(1)); // G3
        
        verify(atencionRepository, times(1))
            .findByEstadoOrderByPrioridadAscFechaSolicitudAsc(EstadoAtencion.EN_ESPERA);
    }

    @Test
    @DisplayName("Given valid id and status, when actualizarEstado, then update and save")
    void actualizarEstadoExitoso() {
        // Arrange (Given)
        when(atencionRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(atencionRepository.save(any(Atencion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (When)
        Atencion resultado = atencionService.actualizarEstado(1L, "AGENDADO");

        // Assert (Then)
        assertNotNull(resultado);
        assertEquals(EstadoAtencion.AGENDADO, resultado.getEstado());
        verify(atencionRepository, times(1)).findById(1L);
        verify(atencionRepository, times(1)).save(consulta);
    }

    @Test
    @DisplayName("Given invalid id, when actualizarEstado, then throw RuntimeException")
    void actualizarEstadoAtencionNoEncontrada() {
        // Arrange (Given)
        when(atencionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            atencionService.actualizarEstado(99L, "AGENDADO");
        });

        assertEquals("Atención con ID 99 no encontrada", exception.getMessage());
        verify(atencionRepository, times(1)).findById(99L);
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    @Test
    @DisplayName("Given invalid state string, when actualizarEstado, then throw RuntimeException")
    void actualizarEstadoInvalido() {
        // Arrange (Given)
        when(atencionRepository.findById(1L)).thenReturn(Optional.of(consulta));

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            atencionService.actualizarEstado(1L, "ESTADO_DESCONOCIDO");
        });

        assertEquals("Estado inválido. Opciones válidas: EN_ESPERA, AGENDADO, ATENDIDO, CANCELADO", exception.getMessage());
        verify(atencionRepository, times(1)).findById(1L);
        verify(atencionRepository, never()).save(any(Atencion.class));
    }

    @Test
    @DisplayName("Given patient rut, when obtenerPorRutPaciente, then return patient attentions")
    void obtenerPorRutPacienteExitoso() {
        // Arrange (Given)
        List<Atencion> pacienteAtenciones = Arrays.asList(consulta, cirugia);
        when(atencionRepository.findByPacienteRut("12345678-9")).thenReturn(pacienteAtenciones);

        // Act (When)
        List<Atencion> resultado = atencionService.obtenerPorRutPaciente("12345678-9");

        // Assert (Then)
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(atencionRepository, times(1)).findByPacienteRut("12345678-9");
    }
}

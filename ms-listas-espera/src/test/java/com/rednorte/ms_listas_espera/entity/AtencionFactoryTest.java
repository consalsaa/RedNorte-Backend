package com.rednorte.ms_listas_espera.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AtencionFactoryTest {

    private Paciente paciente;

    @BeforeEach
    void setUp() {
        // Arrange base para todos los tests
        paciente = new Paciente(
            1L, 
            "12345678-9", 
            "Juan", 
            "Pérez", 
            LocalDate.of(1990, 5, 10), 
            "Av. Providencia 100"
        );
    }

    @Test
    @DisplayName("Debe crear una instancia de AtencionConsulta cuando el tipo es CONSULTA")
    void crearAtencionConsultaExitoso() {
        // Arrange & Act
        Atencion atencion = AtencionFactory.crearAtencion("CONSULTA", paciente, 3, "Cardiología");

        // Assert
        assertNotNull(atencion);
        assertTrue(atencion instanceof AtencionConsulta);
        assertEquals(paciente, atencion.getPaciente());
        assertEquals(3, atencion.getPrioridad());
        assertEquals("Cardiología", ((AtencionConsulta) atencion).getEspecialidad());
        assertEquals("Consulta General de especialidad: Cardiología", atencion.obtenerTipoMensaje());
        assertEquals(EstadoAtencion.EN_ESPERA, atencion.getEstado());
        assertNotNull(atencion.getFechaSolicitud());
        assertEquals(SagaStatus.CONFIRMED, atencion.getSagaStatus());
    }

    @Test
    @DisplayName("Debe crear una instancia de AtencionCirugia cuando el tipo es CIRUGIA")
    void crearAtencionCirugiaExitoso() {
        // Arrange & Act
        Atencion atencion = AtencionFactory.crearAtencion("CIRUGIA", paciente, 1, "Cirugía Cardiovascular");

        // Assert
        assertNotNull(atencion);
        assertTrue(atencion instanceof AtencionCirugia);
        assertEquals(paciente, atencion.getPaciente());
        assertEquals(1, atencion.getPrioridad());
        assertEquals("Cirugía Cardiovascular", ((AtencionCirugia) atencion).getTipoCirugia());
        assertTrue(((AtencionCirugia) atencion).getRequierePabellon());
        assertEquals("Cirugía: Cirugía Cardiovascular (Requiere Pabellón)", atencion.obtenerTipoMensaje());
        assertEquals(SagaStatus.CONFIRMED, atencion.getSagaStatus());
    }

    @Test
    @DisplayName("Debe crear una instancia de AtencionEmergencia cuando el tipo es EMERGENCIA")
    void crearAtencionEmergenciaExitoso() {
        // Arrange & Act
        Atencion atencion = AtencionFactory.crearAtencion("EMERGENCIA", paciente, 1, "Trauma Craneal");

        // Assert
        assertNotNull(atencion);
        assertTrue(atencion instanceof AtencionEmergencia);
        assertEquals(paciente, atencion.getPaciente());
        assertEquals(1, atencion.getPrioridad());
        assertEquals("Trauma Craneal", ((AtencionEmergencia) atencion).getMotivoEmergencia());
        assertFalse(((AtencionEmergencia) atencion).getRequiereUCI());
        assertEquals("Emergencia: Trauma Craneal", atencion.obtenerTipoMensaje());
        assertEquals(SagaStatus.CONFIRMED, atencion.getSagaStatus());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el tipo de atencion no es soportado")
    void crearAtencionTipoInvalido() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            AtencionFactory.crearAtencion("EXAMEN", paciente, 4, "Hemograma");
        });

        assertEquals("Tipo de atención no soportado: EXAMEN", exception.getMessage());
    }

    @Test
    @DisplayName("Debe ser insensible a mayúsculas y minúsculas al validar el tipo de atención")
    void crearAtencionInsensibleCase() {
        // Arrange & Act
        Atencion consulta = AtencionFactory.crearAtencion("coNsUlTa", paciente, 2, "Pediatría");
        Atencion cirugia = AtencionFactory.crearAtencion("cirUgiA", paciente, 2, "General");

        // Assert
        assertNotNull(consulta);
        assertTrue(consulta instanceof AtencionConsulta);
        assertNotNull(cirugia);
        assertTrue(cirugia instanceof AtencionCirugia);
    }
}

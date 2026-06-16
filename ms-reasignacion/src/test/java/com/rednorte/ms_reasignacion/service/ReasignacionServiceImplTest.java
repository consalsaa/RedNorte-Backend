package com.rednorte.ms_reasignacion.service;

import com.rednorte.ms_reasignacion.entity.Reasignacion;
import com.rednorte.ms_reasignacion.repository.ReasignacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReasignacionServiceImplTest {

    @Mock
    private ReasignacionRepository reasignacionRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReasignacionServiceImpl reasignacionService;

    @Test
    void testProcesarCancelacionExitoso() {
        Long atencionId = 1L;
        String rutOriginal = "12.345.678-9";
        String rutReasignado = "98.765.432-1";
        Long siguienteId = 456L;

        // Mock 1: Cancelar atención original
        Map<String, Object> atencionCanceladaMap = new HashMap<>();
        Map<String, String> pacienteOriginalMap = new HashMap<>();
        pacienteOriginalMap.put("rut", rutOriginal);
        atencionCanceladaMap.put("paciente", pacienteOriginalMap);
        ResponseEntity<Map> cancelResponse = new ResponseEntity<>(atencionCanceladaMap, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/" + atencionId + "/estado?nuevoEstado=CANCELADO"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Map.class)
        )).thenReturn(cancelResponse);

        // Mock 2: Obtener pendientes
        List<Map<String, Object>> pendientesList = new ArrayList<>();
        Map<String, Object> siguienteAtencionMap = new HashMap<>();
        siguienteAtencionMap.put("id", siguienteId);
        Map<String, String> pacienteReasignadoMap = new HashMap<>();
        pacienteReasignadoMap.put("rut", rutReasignado);
        siguienteAtencionMap.put("paciente", pacienteReasignadoMap);
        pendientesList.add(siguienteAtencionMap);

        ResponseEntity<List<Map<String, Object>>> pendientesResponse = new ResponseEntity<>(pendientesList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/pendientes"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(pendientesResponse);

        // Mock 3: Agendar siguiente
        ResponseEntity<Map> agendarResponse = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/" + siguienteId + "/estado?nuevoEstado=AGENDADO"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Map.class)
        )).thenReturn(agendarResponse);

        // Mock 4: Repository Save
        when(reasignacionRepository.save(any(Reasignacion.class))).thenAnswer(invocation -> {
            Reasignacion r = invocation.getArgument(0);
            r.setId(10L);
            return r;
        });

        // Run
        Reasignacion result = reasignacionService.procesarCancelacion(atencionId);

        // Asserts
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(atencionId, result.getAtencionCanceladaId());
        assertEquals(siguienteId, result.getAtencionReasignadaId());
        assertEquals(rutOriginal, result.getRutPacienteOriginal());
        assertEquals(rutReasignado, result.getRutPacienteReasignado());
        assertEquals("EXITOSA", result.getEstado());

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyMap());
        verify(reasignacionRepository, times(1)).save(any(Reasignacion.class));
    }

    @Test
    void testProcesarCancelacionSinCandidatos() {
        Long atencionId = 1L;
        String rutOriginal = "12.345.678-9";

        // Mock 1: Cancelar atención original
        Map<String, Object> atencionCanceladaMap = new HashMap<>();
        Map<String, String> pacienteOriginalMap = new HashMap<>();
        pacienteOriginalMap.put("rut", rutOriginal);
        atencionCanceladaMap.put("paciente", pacienteOriginalMap);
        ResponseEntity<Map> cancelResponse = new ResponseEntity<>(atencionCanceladaMap, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/" + atencionId + "/estado?nuevoEstado=CANCELADO"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Map.class)
        )).thenReturn(cancelResponse);

        // Mock 2: Obtener pendientes (vacío)
        ResponseEntity<List<Map<String, Object>>> pendientesResponse = new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/pendientes"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(pendientesResponse);

        // Mock 3: Repository Save
        when(reasignacionRepository.save(any(Reasignacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Run
        Reasignacion result = reasignacionService.procesarCancelacion(atencionId);

        // Asserts
        assertNotNull(result);
        assertEquals(atencionId, result.getAtencionCanceladaId());
        assertEquals(0L, result.getAtencionReasignadaId());
        assertEquals(rutOriginal, result.getRutPacienteOriginal());
        assertEquals("N/A", result.getRutPacienteReasignado());
        assertEquals("SIN_CANDIDATO", result.getEstado());

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyMap());
        verify(reasignacionRepository, times(1)).save(any(Reasignacion.class));
    }

    @Test
    void testProcesarCancelacionErrorRabbitMQ() {
        Long atencionId = 1L;
        String rutOriginal = "12.345.678-9";
        String rutReasignado = "98.765.432-1";
        Long siguienteId = 456L;

        // Mock 1: Cancelar atención original
        Map<String, Object> atencionCanceladaMap = new HashMap<>();
        Map<String, String> pacienteOriginalMap = new HashMap<>();
        pacienteOriginalMap.put("rut", rutOriginal);
        atencionCanceladaMap.put("paciente", pacienteOriginalMap);
        ResponseEntity<Map> cancelResponse = new ResponseEntity<>(atencionCanceladaMap, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/" + atencionId + "/estado?nuevoEstado=CANCELADO"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Map.class)
        )).thenReturn(cancelResponse);

        // Mock 2: Obtener pendientes
        List<Map<String, Object>> pendientesList = new ArrayList<>();
        Map<String, Object> siguienteAtencionMap = new HashMap<>();
        siguienteAtencionMap.put("id", siguienteId);
        Map<String, String> pacienteReasignadoMap = new HashMap<>();
        pacienteReasignadoMap.put("rut", rutReasignado);
        siguienteAtencionMap.put("paciente", pacienteReasignadoMap);
        pendientesList.add(siguienteAtencionMap);

        ResponseEntity<List<Map<String, Object>>> pendientesResponse = new ResponseEntity<>(pendientesList, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/pendientes"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(pendientesResponse);

        // Mock 3: Agendar siguiente
        ResponseEntity<Map> agendarResponse = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        when(restTemplate.exchange(
                eq("http://MS-LISTAS-ESPERA/api/listas-espera/atenciones/" + siguienteId + "/estado?nuevoEstado=AGENDADO"),
                eq(HttpMethod.PUT),
                isNull(),
                eq(Map.class)
        )).thenReturn(agendarResponse);

        // Mock 4: Rabbit MQ failure
        doThrow(new RuntimeException("Rabbit MQ Connection Error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        // Mock 5: Repository Save
        when(reasignacionRepository.save(any(Reasignacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Run
        Reasignacion result = reasignacionService.procesarCancelacion(atencionId);

        // Asserts
        assertNotNull(result);
        assertEquals("EXITOSA", result.getEstado()); // Reassignment is still successful even if notifications fail
        verify(reasignacionRepository, times(1)).save(any(Reasignacion.class));
    }

    @Test
    void testFallbackProcesarCancelacion() {
        Long atencionId = 1L;
        Throwable t = new RuntimeException("Service down");

        when(reasignacionRepository.save(any(Reasignacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reasignacion result = reasignacionService.fallbackProcesarCancelacion(atencionId, t);

        assertNotNull(result);
        assertEquals(atencionId, result.getAtencionCanceladaId());
        assertEquals(0L, result.getAtencionReasignadaId());
        assertEquals("N/A", result.getRutPacienteOriginal());
        assertEquals("N/A", result.getRutPacienteReasignado());
        assertEquals("FALLIDA", result.getEstado());
        assertTrue(result.getObservaciones().contains("Service down"));

        verify(reasignacionRepository, times(1)).save(any(Reasignacion.class));
    }

    @Test
    void testObtenerHistorial() {
        List<Reasignacion> list = List.of(new Reasignacion(), new Reasignacion());
        when(reasignacionRepository.findAll()).thenReturn(list);

        List<Reasignacion> result = reasignacionService.obtenerHistorial();
        assertEquals(2, result.size());
        verify(reasignacionRepository, times(1)).findAll();
    }

    @Test
    void testObtenerPorPaciente() {
        String rut = "12.345.678-9";
        List<Reasignacion> list = List.of(new Reasignacion());
        when(reasignacionRepository.findByRutPacienteReasignado(rut)).thenReturn(list);

        List<Reasignacion> result = reasignacionService.obtenerPorPaciente(rut);
        assertEquals(1, result.size());
        verify(reasignacionRepository, times(1)).findByRutPacienteReasignado(rut);
    }
}

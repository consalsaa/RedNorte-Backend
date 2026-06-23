package com.rednorte.ms_notificaciones.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long atencionId;

    @Column(nullable = false)
    private String rutPaciente;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private boolean leido;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public Notification() {
        this.fechaCreacion = LocalDateTime.now();
        this.leido = false;
    }

    public Notification(Long atencionId, String rutPaciente, String mensaje, String tipo) {
        this();
        this.atencionId = atencionId;
        this.rutPaciente = rutPaciente;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAtencionId() { return atencionId; }
    public void setAtencionId(Long atencionId) { this.atencionId = atencionId; }
    public String getRutPaciente() { return rutPaciente; }
    public void setRutPaciente(String rutPaciente) { this.rutPaciente = rutPaciente; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

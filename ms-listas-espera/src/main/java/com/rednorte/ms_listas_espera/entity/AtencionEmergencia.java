package com.rednorte.ms_listas_espera.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMERGENCIA")
public class AtencionEmergencia extends Atencion {

    private static final long serialVersionUID = 1L;

    private String motivoEmergencia;
    private Boolean requiereUCI;

    public AtencionEmergencia() {}

    public AtencionEmergencia(Paciente paciente, Integer prioridad, String motivoEmergencia, Boolean requiereUCI) {
        super(paciente, prioridad);
        this.motivoEmergencia = motivoEmergencia;
        this.requiereUCI = requiereUCI;
    }

    public String getMotivoEmergencia() { return motivoEmergencia; }
    public void setMotivoEmergencia(String motivoEmergencia) { this.motivoEmergencia = motivoEmergencia; }
    public Boolean getRequiereUCI() { return requiereUCI; }
    public void setRequiereUCI(Boolean requiereUCI) { this.requiereUCI = requiereUCI; }

    @Override
    public String obtenerTipoMensaje() {
        return "Emergencia: " + motivoEmergencia + (requiereUCI ? " (Requiere UCI)" : "");
    }
}

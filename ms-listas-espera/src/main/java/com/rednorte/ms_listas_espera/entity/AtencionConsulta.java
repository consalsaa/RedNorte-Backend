package com.rednorte.ms_listas_espera.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CONSULTA")
public class AtencionConsulta extends Atencion {

    private static final long serialVersionUID = 1L;

    private String especialidad;

    public AtencionConsulta() {}

    public AtencionConsulta(Paciente paciente, Integer prioridad, String especialidad) {
        super(paciente, prioridad);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String obtenerTipoMensaje() {
        return "Consulta General de especialidad: " + especialidad;
    }
}

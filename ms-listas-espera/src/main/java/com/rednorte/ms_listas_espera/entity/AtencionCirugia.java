package com.rednorte.ms_listas_espera.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CIRUGIA")
public class AtencionCirugia extends Atencion {

    private static final long serialVersionUID = 1L;

    private String tipoCirugia;
    private Boolean requierePabellon;

    public AtencionCirugia() {}

    public AtencionCirugia(Paciente paciente, Integer prioridad, String tipoCirugia, Boolean requierePabellon) {
        super(paciente, prioridad);
        this.tipoCirugia = tipoCirugia;
        this.requierePabellon = requierePabellon;
    }

    public String getTipoCirugia() { return tipoCirugia; }
    public void setTipoCirugia(String tipoCirugia) { this.tipoCirugia = tipoCirugia; }
    public Boolean getRequierePabellon() { return requierePabellon; }
    public void setRequierePabellon(Boolean requierePabellon) { this.requierePabellon = requierePabellon; }

    @Override
    public String obtenerTipoMensaje() {
        return "Cirugía: " + tipoCirugia + (requierePabellon ? " (Requiere Pabellón)" : "");
    }
}

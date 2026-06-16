package com.rednorte.ms_auditoria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "atenciones")
public class Atencion {

    @Id
    private Long id;
    private String estado;
    private Integer prioridad;

    public Atencion() {}

    public Atencion(Long id, String estado, Integer prioridad) {
        this.id = id;
        this.estado = estado;
        this.prioridad = prioridad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getPrioridad() { return prioridad; }
    public void setPrioridad(Integer prioridad) { this.prioridad = prioridad; }
}

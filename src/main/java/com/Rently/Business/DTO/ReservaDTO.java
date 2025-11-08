package com.Rently.Business.DTO;

import com.Rently.Persistence.Entity.EstadoReserva;
import java.time.LocalDate;

public class ReservaDTO {

    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer numeroHuespedes;
    private EstadoReserva estado;
    private Long usuarioId;
    private Long alojamientoId;
    private String alojamientoTitulo;


    public ReservaDTO() { }

    public ReservaDTO(Long id, LocalDate fechaInicio, LocalDate fechaFin, Integer numeroHuespedes,
                      Long usuarioId, Long alojamientoId) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.numeroHuespedes = numeroHuespedes;
        this.estado = EstadoReserva.PENDIENTE;
        this.usuarioId = usuarioId;
        this.alojamientoId = alojamientoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getNumeroHuespedes() { return numeroHuespedes; }
    public void setNumeroHuespedes(Integer numeroHuespedes) { this.numeroHuespedes = numeroHuespedes; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getAlojamientoId() { return alojamientoId; }
    public void setAlojamientoId(Long alojamientoId) { this.alojamientoId = alojamientoId; }

    public String getAlojamientoTitulo() {
        return alojamientoTitulo;
    }

    public void setAlojamientoTitulo(String alojamientoTitulo) {
        this.alojamientoTitulo = alojamientoTitulo;
    }

    @Override
    public String toString() {
        return "ReservaDTO{" +
                "id=" + id +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", numeroHuespedes=" + numeroHuespedes +
                ", estado=" + estado +
                ", usuarioId=" + usuarioId +
                ", alojamientoId=" + alojamientoId +
                '}';
    }
}

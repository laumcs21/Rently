package com.Rently.Persistence.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alojamiento_imagen")
public class AlojamientoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Column(nullable = false)
    private int orden = 1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AlojamientoImagen() {}

    public AlojamientoImagen(String url, int orden, Alojamiento alojamiento) {
        this.url = url;
        this.orden = orden;
        this.alojamiento = alojamiento;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public Alojamiento getAlojamiento() { return alojamiento; }
    public void setAlojamiento(Alojamiento alojamiento) { this.alojamiento = alojamiento; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "AlojamientoImagen{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", orden=" + orden +
                '}';
    }
}



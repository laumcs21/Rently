package com.Rently.Business.DTO;

public class ListingCardDTO {
    private Long id;
    private String titulo;
    private String ciudad;
    private Double precioPorNoche;
    private String portadaUrl;   // 👈 este es el que viene de MIN(i.url)

    // constructor que usa el @Query
    public ListingCardDTO(Long id,
                          String titulo,
                          String ciudad,
                          Double precioPorNoche,
                          String portadaUrl) {
        this.id = id;
        this.titulo = titulo;
        this.ciudad = ciudad;
        this.precioPorNoche = precioPorNoche;
        this.portadaUrl = (portadaUrl == null || portadaUrl.isBlank()) ? null : portadaUrl;
    }

    public ListingCardDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(Double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }
}

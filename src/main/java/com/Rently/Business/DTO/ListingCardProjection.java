package com.Rently.Business.DTO;


public interface ListingCardProjection {
    Long getId();
    String getTitulo();
    String getCiudad();
    Double getPrecioPorNoche();
    String getThumbnailUrl();

    default ListingCardDTO toDTO() {
        return new ListingCardDTO(getId(), getTitulo(), getCiudad(), getPrecioPorNoche(), getThumbnailUrl());
    }
}

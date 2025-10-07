package com.Rently.Business.Integration;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.ServicioDTO;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Persistence.Entity.TipoAlojamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AlojamientoServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AlojamientoService alojamientoService;

    @Autowired
    private TestDataFactory testDataFactory;

    private AnfitrionDTO anfitrion;
    private List<ServicioDTO> servicios;
    private AlojamientoDTO alojamiento;

    @BeforeEach
    void setup() {
        // 🔹 1. Limpiar la BD antes de cada test
        testDataFactory.clearAll();

        // 🔹 2. Crear datos base
        anfitrion = testDataFactory.createAnfitrion("anfitrion@test.com");
        servicios = testDataFactory.createServiciosDefault();
        alojamiento = testDataFactory.createAlojamiento(anfitrion, servicios);
    }

    @Test
    @DisplayName("CREATE - Alojamiento válido debe persistirse")
    void create_ValidAlojamiento_ShouldPersist() {
        assertThat(alojamiento).isNotNull();
        assertThat(alojamiento.getId()).isNotNull();
        assertThat(alojamiento.getTitulo()).isNotBlank();
    }

    @Test
    @DisplayName("FIND BY ID - Debe retornar alojamiento existente")
    void findById_Existing_ShouldReturn() {
        Optional<AlojamientoDTO> encontrado = alojamientoService.findById(alojamiento.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo(alojamiento.getTitulo());
    }

    @Test
    @DisplayName("FIND ALL - Debe retornar lista con alojamientos")
    void findAll_ShouldReturnList() {
        List<AlojamientoDTO> lista = alojamientoService.findAll();
        assertThat(lista).isNotEmpty();
    }

    @Test
    @DisplayName("FIND ACTIVE - Debe retornar alojamientos activos")
    void findActive_ShouldReturnList() {
        List<AlojamientoDTO> activos = alojamientoService.findActive();
        assertThat(activos).isNotEmpty();
    }

    @Test
    @DisplayName("FIND BY CITY - Debe filtrar por ciudad")
    void findByCity_ShouldReturnFiltered() {
        List<AlojamientoDTO> porCiudad = alojamientoService.findByCity(alojamiento.getCiudad());
        assertThat(porCiudad).isNotEmpty();
        assertThat(porCiudad.get(0).getCiudad()).isEqualTo(alojamiento.getCiudad());
    }

    @Test
    @DisplayName("FIND BY PRICE - Debe filtrar por rango de precio")
    void findByPrice_ShouldReturnFiltered() {
        List<AlojamientoDTO> porPrecio = alojamientoService.findByPrice(300000.0, 370000.0);
        assertThat(porPrecio).isNotEmpty();
    }

    @Test
    @DisplayName("READ - findByHost válido debe retornar lista")
    void findByHost_Valid_ShouldReturnList() {
        List<AlojamientoDTO> res = alojamientoService.findByHost(anfitrion.getId());
        assertThat(res).isNotEmpty();
        assertThat(res.get(0).getAnfitrionId()).isEqualTo(anfitrion.getId());
    }

    @Test
    @DisplayName("UPDATE - Alojamiento existente debe actualizarse")
    void update_Existing_ShouldUpdate() {
        AlojamientoDTO dto = new AlojamientoDTO();
        dto.setTitulo("Nuevo Titulo");
        dto.setPrecioPorNoche(200.0);

        Optional<AlojamientoDTO> actualizado = alojamientoService.update(alojamiento.getId(), dto);

        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getTitulo()).isEqualTo("Nuevo Titulo");
        assertThat(actualizado.get().getPrecioPorNoche()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("UPDATE - Alojamiento inexistente lanza excepción")
    void update_NonExistent_ShouldThrow() {
        AlojamientoDTO dto = new AlojamientoDTO();
        dto.setTitulo("No existe");

        assertThatThrownBy(() -> alojamientoService.update(9999L, dto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("DELETE - Alojamiento existente debe eliminarse")
    void delete_Existing_ShouldDelete() {
        boolean eliminado = alojamientoService.delete(alojamiento.getId());
        assertThat(eliminado).isTrue();
        Optional<AlojamientoDTO> eliminadoDto = alojamientoService.findById(alojamiento.getId());
        assertThat(eliminadoDto).isPresent();
        assertThat(eliminadoDto.get().isEliminado()).isTrue();
    }

    @Test
    @DisplayName("DELETE - Alojamiento inexistente lanza excepción")
    void delete_NonExistent_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.delete(9999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CREATE - Datos inválidos lanzan excepción")
    void create_Invalid_ShouldThrow() {
        AlojamientoDTO invalido = new AlojamientoDTO();
        assertThatThrownBy(() -> alojamientoService.create(invalido))
                .isInstanceOf(IllegalArgumentException.class);
    }
}



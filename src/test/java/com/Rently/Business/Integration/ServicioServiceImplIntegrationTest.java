package com.Rently.Business.Integration;

import com.Rently.Business.DTO.ServicioDTO;
import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.ServicioService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Persistence.Repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DisplayName("ServicioService - Integration Tests")
class ServicioServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private AlojamientoService alojamientoService;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private ServicioDTO servicioBase;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();
        servicioBase = servicioService.create(
                new ServicioDTO(null, "WiFi", "Internet de alta velocidad")
        );
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("CREATE - Servicio válido debe persistirse en BD")
    void create_ValidService_ShouldPersist() {
        ServicioDTO nuevo = new ServicioDTO(null, "Aire acondicionado", "Acceso a aire acondicionado");
        ServicioDTO guardado = servicioService.create(nuevo);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Aire acondicionado");

        assertThat(servicioRepository.findById(guardado.getId())).isPresent();
    }

    @Test
    @DisplayName("CREATE - Nombre inválido debe lanzar excepción")
    void create_InvalidName_ShouldThrow() {
        ServicioDTO invalido = new ServicioDTO(null, "   ", "Descripción inválida");

        assertThatThrownBy(() -> servicioService.create(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre es obligatorio");
    }

    // ==================== READ ====================

    @Test
    @DisplayName("READ - Buscar servicio por ID existente debe retornarlo")
    void read_FindById_Existing_ShouldReturn() {
        ServicioDTO encontrado = servicioService.findById(servicioBase.getId());

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre()).isEqualTo("WiFi");
    }

    @Test
    @DisplayName("READ - Listar todos los servicios debe devolver lista")
    void read_FindAll_ShouldReturnList() {
        servicioService.create(new ServicioDTO(null, "Parqueadero", "Estacionamiento gratis"));

        List<ServicioDTO> servicios = servicioService.findAll();

        assertThat(servicios).hasSize(2);
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("UPDATE - Servicio existente debe actualizarse en BD")
    void update_Existing_ShouldUpdate() {
        ServicioDTO cambios = new ServicioDTO(null, "WiFi Premium", "Internet 1Gbps");
        ServicioDTO actualizado = servicioService.update(servicioBase.getId(), cambios);

        assertThat(actualizado).isNotNull();
        assertThat(actualizado.getNombre()).isEqualTo("WiFi Premium");
        assertThat(actualizado.getDescripcion()).isEqualTo("Internet 1Gbps");
    }

    @Test
    @DisplayName("UPDATE - Servicio inexistente debe retornar null")
    void update_NonExistent_ShouldReturnNull() {
        ServicioDTO cambios = new ServicioDTO(null, "Fake", "No existe");

        ServicioDTO actualizado = servicioService.update(9999L, cambios);

        assertThat(actualizado).isNull();
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("DELETE - Servicio no asociado debe eliminarse")
    void delete_NotAssociated_ShouldDelete() {
        boolean eliminado = servicioService.delete(servicioBase.getId());

        assertThat(eliminado).isTrue();
        assertThat(servicioRepository.findById(servicioBase.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE - Servicio asociado a alojamiento debe lanzar excepción")
    void delete_Associated_ShouldThrowException() {
        AnfitrionDTO anfitrion = testDataFactory.createAnfitrion("anfitrion@test.com");
        AlojamientoDTO alojamiento = testDataFactory.createAlojamiento(anfitrion, List.of(servicioBase));

        assertThatThrownBy(() -> servicioService.delete(servicioBase.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar el servicio");
    }
}


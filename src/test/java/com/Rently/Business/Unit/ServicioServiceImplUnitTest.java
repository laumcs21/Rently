package com.Rently.Business.Unit;

import com.Rently.Business.DTO.ServicioDTO;
import com.Rently.Business.Service.impl.ServicioServiceImpl;
import com.Rently.Persistence.DAO.ServicioDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioService - Unit Tests con Mockito")
class ServicioServiceImplUnitTest {

    @Mock
    private ServicioDAO servicioDAO;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    private ServicioDTO validServicio;

    @BeforeEach
    void setUp() {
        validServicio = new ServicioDTO(1L, "WiFi", "Internet rápido");
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("CREATE - Servicio válido debe persistirse")
    void create_Valid_ShouldReturnDTO() {
        when(servicioDAO.crearServicio(validServicio)).thenReturn(validServicio);

        ServicioDTO result = servicioService.create(validServicio);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("WiFi");
        verify(servicioDAO).crearServicio(validServicio);
    }

    @Test
    @DisplayName("CREATE - Nombre inválido debe lanzar excepción")
    void create_InvalidName_ShouldThrow() {
        validServicio.setNombre("  ");
        assertThatThrownBy(() -> servicioService.create(validServicio))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre es obligatorio");
    }

    // ==================== READ ====================

    @Test
    @DisplayName("READ - Buscar servicio existente por ID debe retornar DTO")
    void findById_Existing_ShouldReturnDTO() {
        when(servicioDAO.obtenerServicioPorId(1L)).thenReturn(validServicio);

        ServicioDTO result = servicioService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("WiFi");
    }

    @Test
    @DisplayName("READ - Listar servicios debe retornar lista")
    void findAll_ShouldReturnList() {
        when(servicioDAO.obtenerServicios()).thenReturn(List.of(validServicio));

        List<ServicioDTO> result = servicioService.findAll();

        assertThat(result).hasSize(1);
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("UPDATE - Servicio válido debe actualizarse")
    void update_Valid_ShouldReturnUpdatedDTO() {
        ServicioDTO updated = new ServicioDTO(1L, "Piscina", "Acceso a piscina");
        when(servicioDAO.actualizarServicio(1L, updated)).thenReturn(updated);

        ServicioDTO result = servicioService.update(1L, updated);

        assertThat(result.getNombre()).isEqualTo("Piscina");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("DELETE - Servicio asociado debe lanzar excepción")
    void delete_Associated_ShouldThrow() {
        when(servicioDAO.estaAsociadoAAlgunAlojamiento(1L)).thenReturn(true);

        assertThatThrownBy(() -> servicioService.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar el servicio");
    }

    @Test
    @DisplayName("DELETE - Servicio no asociado debe eliminarse")
    void delete_NotAssociated_ShouldReturnTrue() {
        when(servicioDAO.estaAsociadoAAlgunAlojamiento(1L)).thenReturn(false);
        when(servicioDAO.eliminarServicio(1L)).thenReturn(true);

        boolean eliminado = servicioService.delete(1L);

        assertThat(eliminado).isTrue();
        verify(servicioDAO).eliminarServicio(1L);
    }
}


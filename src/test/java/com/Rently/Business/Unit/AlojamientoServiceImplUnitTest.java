package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.impl.AlojamientoServiceImpl;
import com.Rently.Persistence.DAO.AlojamientoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlojamientoService - Unit Tests")
class AlojamientoServiceImplUnitTest {

    @Mock
    private AlojamientoDAO alojamientoDAO;

    @InjectMocks
    private AlojamientoServiceImpl alojamientoService;

    private AlojamientoDTO validAlojamiento;
    private Long validId;
    private Long validHostId;

    @BeforeEach
    void setUp() {
        validId = 1L;
        validHostId = 10L;

        validAlojamiento = new AlojamientoDTO();
        validAlojamiento.setId(null); // null en create
        validAlojamiento.setTitulo("Casa Bonita");
        validAlojamiento.setCiudad("Medellín");
        validAlojamiento.setDireccion("Calle 123");
        validAlojamiento.setPrecioPorNoche(200.0);
        validAlojamiento.setCapacidadMaxima(4);
        validAlojamiento.setAnfitrionId(validHostId);
    }

    // CREATE - happy path
    @Test
    @DisplayName("CREATE - Alojamiento válido debe retornar alojamiento creado")
    void createAlojamiento_ValidData_ShouldReturnCreated() {
        AlojamientoDTO expected = new AlojamientoDTO();
        expected.setId(validId);
        expected.setTitulo(validAlojamiento.getTitulo());

        when(alojamientoDAO.crearAlojamiento(any(AlojamientoDTO.class))).thenReturn(expected);

        AlojamientoDTO result = alojamientoService.create(validAlojamiento);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(validId);
        assertThat(result.getTitulo()).isEqualTo("Casa Bonita");

        verify(alojamientoDAO, times(1)).crearAlojamiento(any(AlojamientoDTO.class));
    }

    @Test
    @DisplayName("CREATE - Titulo null debe lanzar IllegalArgumentException")
    void createAlojamiento_NullTitulo_ShouldThrow() {
        validAlojamiento.setTitulo(null);
        assertThatThrownBy(() -> alojamientoService.create(validAlojamiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo");
        verify(alojamientoDAO, never()).crearAlojamiento(any());
    }

    @Test
    @DisplayName("CREATE - Precio negativo debe lanzar IllegalArgumentException")
    void createAlojamiento_NegativePrice_ShouldThrow() {
        validAlojamiento.setPrecioPorNoche(-10.0);
        assertThatThrownBy(() -> alojamientoService.create(validAlojamiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("precioPorNoche");
    }

    @Test
    @DisplayName("CREATE - Capacidad invalida debe lanzar IllegalArgumentException")
    void createAlojamiento_InvalidCapacity_ShouldThrow() {
        validAlojamiento.setCapacidadMaxima(0);
        assertThatThrownBy(() -> alojamientoService.create(validAlojamiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacidadMaxima");
    }

    // READ
    @Test
    @DisplayName("READ - findById existente debe retornar alojamiento")
    void findById_Existing_ShouldReturn() {
        when(alojamientoDAO.buscarPorId(validId)).thenReturn(Optional.of(validAlojamiento));
        Optional<AlojamientoDTO> res = alojamientoService.findById(validId);
        assertThat(res).isPresent();
        assertThat(res.get().getTitulo()).isEqualTo("Casa Bonita");
    }

    @Test
    @DisplayName("READ - findByCity valida debe retornar lista")
    void findByCity_ShouldReturnList() {
        when(alojamientoDAO.buscarPorCiudad("Medellín")).thenReturn(List.of(validAlojamiento));
        List<AlojamientoDTO> res = alojamientoService.findByCity("Medellín");
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getCiudad()).isEqualTo("Medellín");
    }

    @Test
    @DisplayName("READ - findByPrice rango válido debe retornar lista")
    void findByPrice_ValidRange() {
        when(alojamientoDAO.buscarPorPrecio(100.0, 300.0)).thenReturn(List.of(validAlojamiento));
        List<AlojamientoDTO> res = alojamientoService.findByPrice(100.0, 300.0);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getPrecioPorNoche()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("READ - findByPrice rango inválido debe lanzar excepción")
    void findByPrice_InvalidRange_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.findByPrice(500.0, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo");
    }

    @Test
    @DisplayName("READ - findByHost válido debe retornar lista")
    void findByHost_Valid_ShouldReturnList() {
        when(alojamientoDAO.buscarPorAnfitrion(validHostId)).thenReturn(List.of(validAlojamiento));
        List<AlojamientoDTO> res = alojamientoService.findByHost(validHostId);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getAnfitrionId()).isEqualTo(validHostId);
    }

    // UPDATE
    @Test
    void updateAlojamiento_ValidData_ShouldReturnUpdated() {
        // Pre-check de existencia
        when(alojamientoDAO.buscarPorId(1L)).thenReturn(Optional.of(validAlojamiento));

        // Cambios solicitados
        AlojamientoDTO cambios = new AlojamientoDTO();
        cambios.setTitulo("Nuevo Título");
        cambios.setPrecioPorNoche(350000.0);

        // Resultado esperado tras actualizar
        AlojamientoDTO actualizado = validAlojamiento;
        actualizado.setTitulo("Nuevo Título");
        actualizado.setPrecioPorNoche(350000.0);

        when(alojamientoDAO.actualizar(eq(1L), any(AlojamientoDTO.class)))
                .thenReturn(Optional.of(actualizado));

        Optional<AlojamientoDTO> res = alojamientoService.update(1L, cambios);

        assertTrue(res.isPresent());
        assertEquals("Nuevo Título", res.get().getTitulo());
        assertEquals(350000.0, res.get().getPrecioPorNoche());
        verify(alojamientoDAO, times(1)).buscarPorId(1L);
        verify(alojamientoDAO, times(1)).actualizar(eq(1L), any(AlojamientoDTO.class));
    }


    @Test
    @DisplayName("UPDATE - id inválido lanza excepción")
    void updateAlojamiento_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.update(0L, validAlojamiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }

    @Test
    void deleteAlojamiento_ValidId_ShouldDelete() {
        // Pre-check de existencia
        when(alojamientoDAO.buscarPorId(1L)).thenReturn(Optional.of(validAlojamiento));
        // Ejecutar eliminación
        when(alojamientoDAO.eliminar(1L)).thenReturn(true);

        boolean eliminado = alojamientoService.delete(1L);

        assertTrue(eliminado);
        verify(alojamientoDAO, times(1)).buscarPorId(1L);
        verify(alojamientoDAO, times(1)).eliminar(1L);
    }


    @Test
    @DisplayName("DELETE - id inválido lanza excepción")
    void deleteAlojamiento_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.delete(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }

    @Test
    void updateAlojamiento_NotFound_ShouldThrow() {
        when(alojamientoDAO.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> alojamientoService.update(1L, new AlojamientoDTO()));

        verify(alojamientoDAO, times(1)).buscarPorId(1L);
        verify(alojamientoDAO, never()).actualizar(anyLong(), any());
    }

    @Test
    void deleteAlojamiento_NotFound_ShouldThrow() {
        when(alojamientoDAO.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> alojamientoService.delete(99L));

        verify(alojamientoDAO, times(1)).buscarPorId(99L);
        verify(alojamientoDAO, never()).eliminar(anyLong());
    }

}


package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.impl.AlojamientoServiceImpl;
import com.Rently.Persistence.DAO.AlojamientoDAO;
import com.Rently.Persistence.Entity.Reserva;
import com.Rently.Persistence.Repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlojamientoService - Unit Tests")
class AlojamientoServiceImplUnitTest {

    @Mock
    private AlojamientoDAO alojamientoDAO;

    @Mock
    private ReservaRepository reservaRepository;

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
    @DisplayName("UPDATE - datos válidos debe retornar actualizado")
    void updateAlojamiento_ValidData_ShouldReturnUpdated() {
        AlojamientoDTO updated = new AlojamientoDTO();
        updated.setId(validId);
        updated.setTitulo("Casa Actualizada");

        when(alojamientoDAO.buscarPorId(validId)).thenReturn(Optional.of(validAlojamiento));
        when(alojamientoDAO.actualizar(eq(validId), any(AlojamientoDTO.class)))
                .thenReturn(Optional.of(updated));

        Optional<AlojamientoDTO> res = alojamientoService.update(validId, validAlojamiento);
        assertThat(res).isPresent();
        assertThat(res.get().getTitulo()).isEqualTo("Casa Actualizada");
        verify(alojamientoDAO, times(1)).actualizar(eq(validId), any(AlojamientoDTO.class));
    }

    @Test
    @DisplayName("UPDATE - id inválido lanza excepción")
    void updateAlojamiento_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.update(0L, validAlojamiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }

    // DELETE
    @Test
    @DisplayName("DELETE - id válido debe eliminar")
    void deleteAlojamiento_ValidId_ShouldDelete() {
        when(alojamientoDAO.buscarPorId(validId)).thenReturn(Optional.of(validAlojamiento));
        when(alojamientoDAO.eliminar(validId)).thenReturn(true);
        boolean res = alojamientoService.delete(validId);
        assertThat(res).isTrue();
        verify(alojamientoDAO, times(1)).eliminar(validId);
    }

    @Test
    @DisplayName("DELETE - id inválido lanza excepción")
    void deleteAlojamiento_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> alojamientoService.delete(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }
}


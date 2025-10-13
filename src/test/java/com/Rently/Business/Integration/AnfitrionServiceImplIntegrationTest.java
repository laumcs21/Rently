package com.Rently.Business.Integration;

import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.AnfitrionService;
import com.Rently.Persistence.Repository.AnfitrionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DisplayName("AnfitrionService - Integration Tests")
class AnfitrionServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AnfitrionService anfitrionService;

    @Autowired
    private AnfitrionRepository anfitrionRepository;

    private AnfitrionDTO anfitrionBase;

    @BeforeEach
    void setUp() {
        anfitrionRepository.deleteAll();

        anfitrionBase = new AnfitrionDTO();
        anfitrionBase.setNombre("Anfitrion Test");
        anfitrionBase.setEmail("anfitrion@example.com");
        anfitrionBase.setTelefono("3001234567");
        anfitrionBase.setFechaNacimiento(LocalDate.of(1985, 5, 20));
        anfitrionBase.setContrasena("ClaveValida123");

        anfitrionBase = anfitrionService.create(anfitrionBase);
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("CREATE - Anfitrión válido debe persistirse")
    void create_Valid_ShouldPersist() {
        AnfitrionDTO nuevo = new AnfitrionDTO();
        nuevo.setNombre("Nuevo Anfitrion");
        nuevo.setEmail("nuevo@example.com");
        nuevo.setTelefono("3109876543");
        nuevo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        nuevo.setContrasena("ClaveValida123");

        AnfitrionDTO guardado = anfitrionService.create(nuevo);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getEmail()).isEqualTo("nuevo@example.com");

        assertThat(anfitrionRepository.findByEmail("nuevo@example.com")).isPresent();
    }

    @Test
    @DisplayName("CREATE - Email inválido debe lanzar excepción")
    void create_InvalidEmail_ShouldThrowException() {
        AnfitrionDTO invalido = new AnfitrionDTO();
        invalido.setNombre("Invalid Email");
        invalido.setEmail("correo-invalido");
        invalido.setContrasena("ClaveValida123");

        assertThatThrownBy(() -> anfitrionService.create(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato de email no es válido");
    }

    @Test
    @DisplayName("CREATE - Teléfono inválido debe lanzar excepción")
    void create_InvalidPhone_ShouldThrowException() {
        AnfitrionDTO invalido = new AnfitrionDTO();
        invalido.setNombre("Tel Invalid");
        invalido.setEmail("phone@example.com");
        invalido.setTelefono("abc123");
        invalido.setContrasena("ClaveValida123");

        assertThatThrownBy(() -> anfitrionService.create(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El teléfono debe tener entre 7 y 15 dígitos");
    }

    // ==================== READ ====================

    @Test
    @DisplayName("READ - Buscar por ID existente debe retornar anfitrión")
    void read_FindById_Existing_ShouldReturn() {
        Optional<AnfitrionDTO> encontrado = anfitrionService.findById(anfitrionBase.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("anfitrion@example.com");
    }

    @Test
    @DisplayName("READ - Buscar por ID inexistente devuelve vacío")
    void read_FindById_NonExistent_ShouldReturnEmpty() {
        Optional<AnfitrionDTO> result = anfitrionService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("READ - Listar todos devuelve lista real")
    void read_FindAll_ShouldReturnList() {
        List<AnfitrionDTO> anfitriones = anfitrionService.findAll();

        assertThat(anfitriones).hasSize(1); // Solo el anfitrión base
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("UPDATE - Anfitrión existente se actualiza correctamente")
    void update_Existing_ShouldUpdate() {
        anfitrionBase.setNombre("Nombre Actualizado");
        Optional<AnfitrionDTO> actualizado = anfitrionService.update(anfitrionBase.getId(), anfitrionBase);

        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getNombre()).isEqualTo("Nombre Actualizado");
    }

    @Test
    @DisplayName("UPDATE - Anfitrión inexistente lanza excepción")
    void update_NonExistent_ShouldThrowException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setNombre("Fake");

        assertThatThrownBy(() -> anfitrionService.update(999L, dto))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("DELETE - Anfitrión existente se elimina")
    void delete_Existing_ShouldRemove() {
        boolean eliminado = anfitrionService.delete(anfitrionBase.getId());

        assertThat(eliminado).isTrue();
        assertThat(anfitrionRepository.findById(anfitrionBase.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE - Anfitrión inexistente lanza excepción")
    void delete_NonExistent_ShouldThrowException() {
        assertThatThrownBy(() -> anfitrionService.delete(999L))
                .isInstanceOf(RuntimeException.class);
    }
}

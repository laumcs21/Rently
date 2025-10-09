package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.impl.AnfitrionServiceImpl;
import com.Rently.Persistence.DAO.AnfitrionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para AnfitrionServiceImpl con validaciones incluidas.
 */
class AnfitrionServiceImplUnitTest {

    @Mock
    private AnfitrionDAO anfitrionDAO;

    @InjectMocks
    private AnfitrionServiceImpl anfitrionService;

    private AnfitrionDTO anfitrionDTO;

    private AnfitrionDTO existente() {
        AnfitrionDTO a = new AnfitrionDTO();
        a.setId(1L);
        a.setNombre("Carlos Pérez");
        a.setEmail("carlos@example.com");
        a.setTelefono("3001234567");
        a.setFechaNacimiento(LocalDate.now().minusYears(30));
        return a;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        anfitrionDTO = new AnfitrionDTO();
        anfitrionDTO.setId(1L);
        anfitrionDTO.setNombre("Carlos Pérez");
        anfitrionDTO.setEmail("carlos@example.com");
        anfitrionDTO.setTelefono("3001234567");
        anfitrionDTO.setFechaNacimiento(LocalDate.of(1990, 5, 10));
    }

    // ================== CREATE ==================

    @Test
    void create_ValidData_ReturnsDTO() {
        when(anfitrionDAO.crearAnfitrion(any(AnfitrionDTO.class)))
                .thenReturn(anfitrionDTO);

        AnfitrionDTO result = anfitrionService.create(anfitrionDTO);

        assertNotNull(result);
        assertEquals("Carlos Pérez", result.getNombre());
        verify(anfitrionDAO, times(1)).crearAnfitrion(any());
    }

    @Test
    void create_InvalidEmail_ThrowsException() {
        anfitrionDTO.setEmail("correo-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(anfitrionDTO));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    @Test
    void create_NameEmpty_ThrowsException() {
        anfitrionDTO.setNombre("   ");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(anfitrionDTO));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    // Nuevo: DTO nulo
    @Test
    void create_NullDTO_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(null));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    // Nuevo: teléfono con formato inválido
    @Test
    void create_InvalidPhone_ThrowsException() {
        anfitrionDTO.setTelefono("12AB");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(anfitrionDTO));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    // Nuevo: fecha de nacimiento futura
    @Test
    void create_FutureBirthDate_ThrowsException() {
        anfitrionDTO.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(anfitrionDTO));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    // Nuevo: menor de edad (ej. < 18)
    @Test
    void create_Underage_ThrowsException() {
        anfitrionDTO.setFechaNacimiento(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.create(anfitrionDTO));

        verify(anfitrionDAO, never()).crearAnfitrion(any());
    }

    // ================== FIND BY ID ==================

    @Test
    void findById_ValidId_ReturnsDTO() {
        when(anfitrionDAO.buscarPorId(1L)).thenReturn(Optional.of(anfitrionDTO));

        Optional<AnfitrionDTO> result = anfitrionService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Carlos Pérez", result.get().getNombre());
        verify(anfitrionDAO, times(1)).buscarPorId(1L);
    }

    @Test
    void findById_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.findById(0L));

        verify(anfitrionDAO, never()).buscarPorId(any());
    }

    // Nuevo: ID válido pero no encontrado
    @Test
    void findById_NotFound_ReturnsEmpty() {
        when(anfitrionDAO.buscarPorId(2L)).thenReturn(Optional.empty());

        Optional<AnfitrionDTO> result = anfitrionService.findById(2L);

        assertTrue(result.isEmpty());
        verify(anfitrionDAO, times(1)).buscarPorId(2L);
    }

    // ================== FIND BY EMAIL ==================

    @Test
    void findByEmail_ValidEmail_ReturnsDTO() {
        when(anfitrionDAO.buscarPorEmail("carlos@example.com"))
                .thenReturn(Optional.of(anfitrionDTO));

        Optional<AnfitrionDTO> result = anfitrionService.findByEmail("carlos@example.com");

        assertTrue(result.isPresent());
        assertEquals("carlos@example.com", result.get().getEmail());
        verify(anfitrionDAO, times(1)).buscarPorEmail("carlos@example.com");
    }

    @Test
    void findByEmail_InvalidEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.findByEmail("correo-invalido"));

        verify(anfitrionDAO, never()).buscarPorEmail(any());
    }

    // Nuevo: email válido pero no encontrado
    @Test
    void findByEmail_NotFound_ReturnsEmpty() {
        when(anfitrionDAO.buscarPorEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        Optional<AnfitrionDTO> result = anfitrionService.findByEmail("noexiste@example.com");

        assertTrue(result.isEmpty());
        verify(anfitrionDAO, times(1)).buscarPorEmail("noexiste@example.com");
    }

    // ================== FIND BY NAME ==================

    @Test
    void findByName_ValidName_ReturnsList() {
        when(anfitrionDAO.buscarPorNombre("Carlos"))
                .thenReturn(List.of(anfitrionDTO));

        List<AnfitrionDTO> result = anfitrionService.findByName("Carlos");

        assertEquals(1, result.size());
        assertEquals("Carlos Pérez", result.get(0).getNombre());
        verify(anfitrionDAO, times(1)).buscarPorNombre("Carlos");
    }

    @Test
    void findByName_EmptyName_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.findByName("   "));

        verify(anfitrionDAO, never()).buscarPorNombre(any());
    }

    // Nuevo: nombre válido pero sin resultados
    @Test
    void findByName_NoResults_ReturnsEmptyList() {
        when(anfitrionDAO.buscarPorNombre("SinResultados"))
                .thenReturn(List.of());

        List<AnfitrionDTO> result = anfitrionService.findByName("SinResultados");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(anfitrionDAO, times(1)).buscarPorNombre("SinResultados");
    }

    // ================== UPDATE ==================

    @Test
    void update_ValidData_ReturnsDTO() {
        // Pre-check de existencia
        when(anfitrionDAO.buscarPorId(1L)).thenReturn(Optional.of(existente()));
        // Actualización
        when(anfitrionDAO.actualizarAnfitrion(eq(1L), any(AnfitrionDTO.class)))
                .thenReturn(Optional.of(anfitrionDTO));

        Optional<AnfitrionDTO> result = anfitrionService.update(1L, anfitrionDTO);

        assertTrue(result.isPresent());
        assertEquals("Carlos Pérez", result.get().getNombre());
        verify(anfitrionDAO, times(1)).buscarPorId(1L);
        verify(anfitrionDAO, times(1)).actualizarAnfitrion(eq(1L), any());
    }

    @Test
    void update_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(0L, anfitrionDTO));

        verify(anfitrionDAO, never()).buscarPorId(any());
        verify(anfitrionDAO, never()).actualizarAnfitrion(any(), any());
    }

    @Test
    void update_InvalidEmail_ThrowsException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setEmail("correo-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, dto));

        verify(anfitrionDAO, never()).buscarPorId(anyLong());
        verify(anfitrionDAO, never()).actualizarAnfitrion(anyLong(), any());
    }

    // Nuevo: DTO nulo en update
    @Test
    void update_NullDTO_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, null));

        verify(anfitrionDAO, never()).buscarPorId(any());
        verify(anfitrionDAO, never()).actualizarAnfitrion(any(), any());
    }

    // Nuevo: update con nombre en blanco
    @Test
    void update_NameEmpty_ThrowsException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setNombre("   ");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, dto));

        verify(anfitrionDAO, never()).buscarPorId(anyLong());
        verify(anfitrionDAO, never()).actualizarAnfitrion(anyLong(), any());
    }

    // Nuevo: update con teléfono inválido
    @Test
    void update_InvalidPhone_ThrowsException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setTelefono("xx");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, dto));

        verify(anfitrionDAO, never()).buscarPorId(anyLong());
        verify(anfitrionDAO, never()).actualizarAnfitrion(anyLong(), any());
    }

    // Nuevo: fecha de nacimiento futura en update
    @Test
    void update_FutureBirthDate_ThrowsException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, dto));

        verify(anfitrionDAO, never()).buscarPorId(anyLong());
        verify(anfitrionDAO, never()).actualizarAnfitrion(anyLong(), any());
    }

    // Nuevo: menor de edad en update
    @Test
    void update_Underage_ThrowsException() {
        AnfitrionDTO dto = new AnfitrionDTO();
        dto.setFechaNacimiento(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, dto));

        verify(anfitrionDAO, never()).buscarPorId(anyLong());
        verify(anfitrionDAO, never()).actualizarAnfitrion(anyLong(), any());
    }

    // Cambiado: no encontrado -> lanza RuntimeException (pre-check)
    @Test
    void update_NotFound_ShouldThrowException() {
        when(anfitrionDAO.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> anfitrionService.update(1L, new AnfitrionDTO()));

        verify(anfitrionDAO, times(1)).buscarPorId(1L);
        verify(anfitrionDAO, never()).actualizarAnfitrion(any(), any());
    }

    // ================== DELETE ==================

    @Test
    void delete_ValidId_ReturnsTrue() {
        // El service NO hace pre-check, solo llama eliminar
        when(anfitrionDAO.eliminarAnfitrion(1L)).thenReturn(true);

        boolean result = anfitrionService.delete(1L);

        assertTrue(result);
        verify(anfitrionDAO, times(1)).eliminarAnfitrion(1L);
        verify(anfitrionDAO, never()).buscarPorId(anyLong());
    }

    @Test
    void delete_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.delete(0L));

        verify(anfitrionDAO, never()).buscarPorId(any());
        verify(anfitrionDAO, never()).eliminarAnfitrion(any());
    }

    @Test
    void delete_NonExistentId_ShouldThrowException() {
        // Si tu service lanza RuntimeException cuando eliminar=false:
        when(anfitrionDAO.eliminarAnfitrion(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> anfitrionService.delete(99L));
        assertTrue(ex.getMessage().toLowerCase().contains("no existe"));

        verify(anfitrionDAO, times(1)).eliminarAnfitrion(99L);
        verify(anfitrionDAO, never()).buscarPorId(anyLong());
    }
}





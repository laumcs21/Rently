package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.impl.AnfitrionServiceImpl;
import com.Rently.Persistence.DAO.AnfitrionDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AnfitrionServiceImpl anfitrionService;

    private AnfitrionDTO anfitrionDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        anfitrionDTO = new AnfitrionDTO();
        anfitrionDTO.setId(1L);
        anfitrionDTO.setNombre("Carlos Pérez");
        anfitrionDTO.setEmail("carlos@example.com");
        anfitrionDTO.setTelefono("3001234567");
        anfitrionDTO.setContrasena("ClaveAnfitrion123");
        anfitrionDTO.setFechaNacimiento(LocalDate.of(1990, 5, 10));
    }

    // ================== CREATE ==================

    @Test
    void create_ValidData_ReturnsDTO() {
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(anfitrionDAO.crearAnfitrion(any(AnfitrionDTO.class))).thenReturn(anfitrionDTO);

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

    // ================== UPDATE ==================

    @Test
    void update_ValidData_ReturnsDTO() {
        when(anfitrionDAO.actualizarAnfitrion(eq(1L), any(AnfitrionDTO.class)))
                .thenReturn(Optional.of(anfitrionDTO));

        Optional<AnfitrionDTO> result = anfitrionService.update(1L, anfitrionDTO);

        assertTrue(result.isPresent());
        assertEquals("Carlos Pérez", result.get().getNombre());
        verify(anfitrionDAO, times(1)).actualizarAnfitrion(eq(1L), any());
    }

    @Test
    void update_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(0L, anfitrionDTO));

        verify(anfitrionDAO, never()).actualizarAnfitrion(any(), any());
    }

    @Test
    void update_InvalidEmail_ThrowsException() {
        anfitrionDTO.setEmail("correo-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.update(1L, anfitrionDTO));

        verify(anfitrionDAO, never()).actualizarAnfitrion(any(), any());
    }

    // ================== DELETE ==================

    @Test
    void delete_ValidId_ReturnsTrue() {
        when(anfitrionDAO.eliminarAnfitrion(1L)).thenReturn(true);

        boolean result = anfitrionService.delete(1L);

        assertTrue(result);
        verify(anfitrionDAO, times(1)).eliminarAnfitrion(1L);
    }

    @Test
    void delete_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> anfitrionService.delete(0L));

        verify(anfitrionDAO, never()).eliminarAnfitrion(any());
    }
}



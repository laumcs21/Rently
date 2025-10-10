package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.Service.impl.AdministradorServiceImpl;
import com.Rently.Persistence.DAO.AdministradorDAO;
import com.Rently.Persistence.Entity.Rol;
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
import static org.mockito.ArgumentMatchers.anyString;

class AdministradorServiceImplUnitTest {

    @Mock
    private AdministradorDAO administradorDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdministradorServiceImpl administradorService;

    private AdministradorDTO adminDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminDTO = new AdministradorDTO();
        adminDTO.setId(1L);
        adminDTO.setNombre("Admin User");
        adminDTO.setEmail("admin@example.com");
        adminDTO.setTelefono("3201234567");
        adminDTO.setContrasena("ClaveAdmin123");
        adminDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        adminDTO.setRol(Rol.ADMINISTRADOR);
    }

    // ================== CREATE ==================

    @Test
    void create_ValidData_ReturnsDTO() {
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(administradorDAO.crearAdministrador(any(AdministradorDTO.class), anyString())).thenReturn(adminDTO);

        AdministradorDTO result = administradorService.create(adminDTO);

        assertNotNull(result);
        assertEquals("Admin User", result.getNombre());
        verify(administradorDAO, times(1)).crearAdministrador(any(), anyString());
    }

    @Test
    void create_InvalidEmail_ThrowsException() {
        adminDTO.setEmail("correo-invalido");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_InvalidRole_ThrowsException() {
        adminDTO.setRol(Rol.USUARIO);

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    // NUEVOS: create con DTO nulo / nombre vacío / rol nulo / teléfono inválido / fecha futura / menor de edad
    @Test
    void create_NullDTO_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(null));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_NameEmpty_ThrowsException() {
        adminDTO.setNombre("   ");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_NullRole_ThrowsException() {
        adminDTO.setRol(null);

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_InvalidPhone_ThrowsException() {
        adminDTO.setTelefono("12AB");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_FutureBirthDate_ThrowsException() {
        adminDTO.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    @Test
    void create_Underage_ThrowsException() {
        adminDTO.setFechaNacimiento(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.create(adminDTO));

        verify(administradorDAO, never()).crearAdministrador(any());
    }

    // ================== FIND ==================

    @Test
    void findById_ValidId_ReturnsDTO() {
        when(administradorDAO.buscarPorId(1L)).thenReturn(Optional.of(adminDTO));

        Optional<AdministradorDTO> result = administradorService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Admin User", result.get().getNombre());
        verify(administradorDAO, times(1)).buscarPorId(1L);
    }

    @Test
    void findById_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.findById(0L));
    }

    // NUEVO: ID válido pero no encontrado
    @Test
    void findById_NotFound_ReturnsEmpty() {
        when(administradorDAO.buscarPorId(99L)).thenReturn(Optional.empty());

        Optional<AdministradorDTO> result = administradorService.findById(99L);

        assertTrue(result.isEmpty());
        verify(administradorDAO, times(1)).buscarPorId(99L);
    }

    @Test
    void findByEmail_ValidEmail_ReturnsDTO() {
        when(administradorDAO.buscarPorEmail("admin@example.com"))
                .thenReturn(Optional.of(adminDTO));

        Optional<AdministradorDTO> result = administradorService.findByEmail("admin@example.com");

        assertTrue(result.isPresent());
        assertEquals("admin@example.com", result.get().getEmail());
        verify(administradorDAO, times(1)).buscarPorEmail("admin@example.com");
    }

    @Test
    void findByEmail_InvalidEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.findByEmail("invalido"));
    }

    // NUEVO: email válido pero no encontrado
    @Test
    void findByEmail_NotFound_ReturnsEmpty() {
        when(administradorDAO.buscarPorEmail("noadmin@example.com"))
                .thenReturn(Optional.empty());

        Optional<AdministradorDTO> result = administradorService.findByEmail("noadmin@example.com");

        assertTrue(result.isEmpty());
        verify(administradorDAO, times(1)).buscarPorEmail("noadmin@example.com");
    }

    @Test
    void findAll_ReturnsList() {
        when(administradorDAO.listarTodos()).thenReturn(List.of(adminDTO));

        List<AdministradorDTO> result = administradorService.findAll();

        assertEquals(1, result.size());
        verify(administradorDAO, times(1)).listarTodos();
    }

    // NUEVO: lista vacía
    @Test
    void findAll_Empty_ReturnsEmptyList() {
        when(administradorDAO.listarTodos()).thenReturn(List.of());

        List<AdministradorDTO> result = administradorService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(administradorDAO, times(1)).listarTodos();
    }

    // ================== UPDATE ==================

    @Test
    void update_ValidData_ReturnsDTO() {
        // Pre-check de existencia que hace el service
        when(administradorDAO.buscarPorId(1L)).thenReturn(Optional.of(adminDTO));

        // Actualización
        when(administradorDAO.actualizarAdministrador(eq(1L), any(AdministradorDTO.class)))
                .thenReturn(Optional.of(adminDTO));

        Optional<AdministradorDTO> result = administradorService.update(1L, adminDTO);

        assertTrue(result.isPresent());
        assertEquals("Admin User", result.get().getNombre());

        verify(administradorDAO, times(1)).buscarPorId(1L);
        verify(administradorDAO, times(1)).actualizarAdministrador(eq(1L), any());
    }


    @Test
    void update_InvalidEmail_ThrowsException() {
        adminDTO.setEmail("invalido");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_InvalidRole_ThrowsException() {
        adminDTO.setRol(Rol.USUARIO);

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    // NUEVOS: id inválido / DTO nulo / nombre vacío / teléfono inválido / fecha futura / menor de edad / no encontrado
    @Test
    void update_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(0L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_NullDTO_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, null));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_NameEmpty_ThrowsException() {
        adminDTO.setNombre("   ");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_InvalidPhone_ThrowsException() {
        adminDTO.setTelefono("xx");

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_FutureBirthDate_ThrowsException() {
        adminDTO.setFechaNacimiento(LocalDate.now().plusDays(1));

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_Underage_ThrowsException() {
        adminDTO.setFechaNacimiento(LocalDate.now().minusYears(17));

        assertThrows(IllegalArgumentException.class,
                () -> administradorService.update(1L, adminDTO));

        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
    }

    @Test
    void update_NotFound_ShouldThrowException() {
        // El service hace un pre-check: busca por ID y si no existe, lanza excepción.
        when(administradorDAO.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> administradorService.update(1L, adminDTO));

        // Al no existir, no debe intentar actualizar
        verify(administradorDAO, never()).actualizarAdministrador(any(), any());
        verify(administradorDAO, times(1)).buscarPorId(1L);
    }


    // ================== DELETE ==================

    @Test
    void delete_ValidId_ReturnsTrue() {
        when(administradorDAO.eliminarAdministrador(1L)).thenReturn(true);

        boolean result = administradorService.delete(1L);

        assertTrue(result);
        verify(administradorDAO, times(1)).eliminarAdministrador(1L);
    }

    @Test
    void delete_InvalidId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> administradorService.delete(0L));

        verify(administradorDAO, never()).eliminarAdministrador(any());
    }

    // NUEVO: id válido pero inexistente -> asume que el servicio lanza excepción si DAO devuelve false
    @Test
    void delete_NonExistentId_ThrowsException() {
        when(administradorDAO.eliminarAdministrador(99L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> administradorService.delete(99L));

        verify(administradorDAO, times(1)).eliminarAdministrador(99L);
    }
}


package com.Rently.Business.Unit;

import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.impl.ReservaServiceImpl;
import com.Rently.Persistence.DAO.*;
import com.Rently.Persistence.Entity.EstadoReserva;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Entity.Usuario;
import com.Rently.Persistence.Mapper.PersonaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReservaService - Unit Tests")
class ReservaServiceImplUnitTest {

    @Mock
    private ReservaDAO reservaDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private AnfitrionDAO anfitrionDAO;

    @Mock
    private AdministradorDAO administradorDAO;

    @Mock
    private AlojamientoDAO alojamientoDAO;

    @Mock
    private PersonaMapper personaMapper;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private ReservaDTO validReserva;
    private UsuarioDTO usuarioDTO;
    private AlojamientoDTO alojamientoDTO;
    private Usuario usuarioEntity;
    private Long validId;

    @BeforeEach
    void setUp() {
        validId = 1L;

        // Setup UsuarioDTO
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(validId);
        usuarioDTO.setEmail("test@test.com");
        usuarioDTO.setNombre("Test User");
        usuarioDTO.setRol(Rol.USUARIO);
        usuarioDTO.setActivo(true);

        // Setup Usuario Entity
        usuarioEntity = new Usuario();
        usuarioEntity.setId(validId);
        usuarioEntity.setEmail("test@test.com");
        usuarioEntity.setRol(Rol.USUARIO);

        // Setup AlojamientoDTO
        alojamientoDTO = new AlojamientoDTO();
        alojamientoDTO.setId(2L);
        alojamientoDTO.setTitulo("Casa Test");
        alojamientoDTO.setEliminado(false);

        // Setup ReservaDTO válida
        validReserva = new ReservaDTO();
        validReserva.setId(null);
        validReserva.setFechaInicio(LocalDate.now().plusDays(1));
        validReserva.setFechaFin(LocalDate.now().plusDays(3));
        validReserva.setNumeroHuespedes(2);
        validReserva.setUsuarioId(validId);
        validReserva.setAlojamientoId(2L);
        validReserva.setEstado(EstadoReserva.PENDIENTE);

        // Mock Security Context
        mockSecurityContext("test@test.com", Rol.USUARIO);
    }

    private void mockSecurityContext(String email, Rol rol) {
        UserDetails userDetails = User.builder()
                .username(email)
                .password("password")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + rol.name())
                ))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // =================== CREATE TESTS ===================

    @Test
    @DisplayName("CREATE - Reserva válida debe retornar reserva creada")
    void createReserva_ValidData_ShouldReturnCreated() {
        // Arrange
        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.of(usuarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamientoDTO));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);
        when(reservaDAO.listarPorAlojamiento(2L)).thenReturn(Collections.emptyList());

        ReservaDTO expected = new ReservaDTO();
        expected.setId(10L);
        expected.setEstado(EstadoReserva.PENDIENTE);
        when(reservaDAO.crear(any(ReservaDTO.class))).thenReturn(expected);

        // Act
        ReservaDTO result = reservaService.create(validReserva);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
        verify(reservaDAO, times(1)).crear(any(ReservaDTO.class));
    }

    @Test
    @DisplayName("CREATE - Reserva null debe lanzar IllegalArgumentException")
    void createReserva_NullReserva_ShouldThrow() {
        assertThatThrownBy(() -> reservaService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserva no puede ser nula");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Fecha fin antes de inicio debe lanzar excepción")
    void createReserva_InvalidDates_ShouldThrow() {
        validReserva.setFechaFin(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de fin");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Usuario inexistente debe lanzar excepción")
    void createReserva_NonExistentUser_ShouldThrow() {
        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.empty());
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no existe");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Usuario inactivo debe lanzar excepción")
    void createReserva_InactiveUser_ShouldThrow() {
        usuarioDTO.setActivo(false);
        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.of(usuarioDTO));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("activo");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Alojamiento inexistente debe lanzar excepción")
    void createReserva_NonExistentAlojamiento_ShouldThrow() {
        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.of(usuarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.empty());
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("alojamiento");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Alojamiento eliminado debe lanzar excepción")
    void createReserva_DeletedAlojamiento_ShouldThrow() {
        alojamientoDTO.setEliminado(true);
        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.of(usuarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamientoDTO));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no está disponible");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Fechas solapadas debe lanzar excepción")
    void createReserva_OverlappingDates_ShouldThrow() {
        ReservaDTO existente = new ReservaDTO();
        existente.setFechaInicio(LocalDate.now().plusDays(2));
        existente.setFechaFin(LocalDate.now().plusDays(4));
        existente.setEstado(EstadoReserva.CONFIRMADA);

        when(usuarioDAO.buscarPorId(validId)).thenReturn(Optional.of(usuarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamientoDTO));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);
        when(reservaDAO.listarPorAlojamiento(2L)).thenReturn(List.of(existente));

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una reserva activa");
        verify(reservaDAO, never()).crear(any());
    }

    @Test
    @DisplayName("CREATE - Usuario no puede crear para otro usuario")
    void createReserva_DifferentUser_ShouldThrow() {
        validReserva.setUsuarioId(999L); // Diferente al usuario autenticado

        // Mock para que el usuario 999 exista
        UsuarioDTO otroUsuario = new UsuarioDTO();
        otroUsuario.setId(999L);
        otroUsuario.setActivo(true);

        when(usuarioDAO.buscarPorId(999L)).thenReturn(Optional.of(otroUsuario));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamientoDTO));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.create(validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puede crear reservas en nombre de otro usuario");
        verify(reservaDAO, never()).crear(any());
    }


    // =================== READ TESTS ===================

    @Test
    @DisplayName("READ - findById existente debe retornar reserva")
    void findById_Existing_ShouldReturn() {
        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));

        Optional<ReservaDTO> result = reservaService.findById(validId);

        assertThat(result).isPresent();
        assertThat(result.get().getUsuarioId()).isEqualTo(validId);
        verify(reservaDAO, times(1)).buscarPorId(validId);
    }

    @Test
    @DisplayName("READ - findById con ID inválido debe lanzar excepción")
    void findById_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> reservaService.findById(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
        verify(reservaDAO, never()).buscarPorId(any());
    }

    @Test
    @DisplayName("READ - findByUserId debe retornar lista de reservas")
    void findByUserId_ShouldReturnList() {
        when(reservaDAO.listarPorUsuario(validId)).thenReturn(List.of(validReserva));

        List<ReservaDTO> result = reservaService.findByUserId(validId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsuarioId()).isEqualTo(validId);
        verify(reservaDAO, times(1)).listarPorUsuario(validId);
    }

    @Test
    @DisplayName("READ - findByAlojamientoId debe retornar lista de reservas")
    void findByAlojamientoId_ShouldReturnList() {
        when(reservaDAO.listarPorAlojamiento(2L)).thenReturn(List.of(validReserva));

        List<ReservaDTO> result = reservaService.findByAlojamientoId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlojamientoId()).isEqualTo(2L);
        verify(reservaDAO, times(1)).listarPorAlojamiento(2L);
    }

    // =================== CANCEL BY USER TESTS ===================

    @Test
    @DisplayName("CANCEL - Usuario puede cancelar reserva con más de 48 horas de anticipación")
    void cancelByUser_WithMoreThan48Hours_ShouldCancel() {
        validReserva.setId(validId);
        validReserva.setFechaInicio(LocalDate.now().plusDays(5));

        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);
        when(reservaDAO.cambiarEstado(validId, EstadoReserva.CANCELADA)).thenReturn(true);

        ReservaDTO result = reservaService.cancelByUser(validId);

        assertThat(result.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(reservaDAO, times(1)).cambiarEstado(validId, EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("CANCEL - No permite cancelar con menos de 48 horas de anticipación")
    void cancelByUser_WithLessThan48Hours_ShouldThrow() {
        validReserva.setId(validId);
        validReserva.setFechaInicio(LocalDate.now().plusDays(1));

        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.cancelByUser(validId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pueden cancelar reservas con menos de 48 horas de anticipación");

        verify(reservaDAO, never()).cambiarEstado(any(), any());
    }

    // =================== UPDATE TESTS ===================

    @Test
    @DisplayName("UPDATE - datos válidos debe retornar actualizado")
    void updateReserva_ValidData_ShouldReturnUpdated() {
        ReservaDTO updated = new ReservaDTO();
        updated.setId(validId);
        updated.setNumeroHuespedes(4);

        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);
        when(reservaDAO.actualizar(eq(validId), any(ReservaDTO.class))).thenReturn(Optional.of(updated));

        Optional<ReservaDTO> result = reservaService.update(validId, validReserva);

        assertThat(result).isPresent();
        assertThat(result.get().getNumeroHuespedes()).isEqualTo(4);
        verify(reservaDAO, times(1)).actualizar(eq(validId), any(ReservaDTO.class));
    }


    @Test
    @DisplayName("UPDATE - ID inválido debe lanzar excepción")
    void updateReserva_InvalidId_ShouldThrow() {
        // Mock del usuario autenticado (aunque no debería llegar a usarlo)
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.update(0L, validReserva))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
        verify(reservaDAO, never()).actualizar(any(), any());
    }

    @Test
    @DisplayName("UPDATE - Reserva confirmada no se puede modificar")
    void updateReserva_ConfirmedReserva_ShouldThrow() {
        validReserva.setEstado(EstadoReserva.CONFIRMADA);
        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.update(validId, validReserva))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede modificar");
        verify(reservaDAO, never()).actualizar(any(), any());
    }

    // =================== UPDATE STATE TESTS ===================

    @Test
    @DisplayName("UPDATE STATE - Cancelar propia reserva debe funcionar")
    void updateState_CancelOwnReserva_ShouldWork() {
        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);
        when(reservaDAO.cambiarEstado(validId, EstadoReserva.CANCELADA)).thenReturn(true);

        boolean result = reservaService.updateState(validId, EstadoReserva.CANCELADA);

        assertThat(result).isTrue();
        verify(reservaDAO, times(1)).cambiarEstado(validId, EstadoReserva.CANCELADA);
    }

    @Test
    @DisplayName("UPDATE STATE - Usuario no puede confirmar reserva")
    void updateState_UserCannotConfirm_ShouldThrow() {
        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.updateState(validId, EstadoReserva.CONFIRMADA))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("administrador");
        verify(reservaDAO, never()).cambiarEstado(any(), any());
    }

    // =================== DELETE TESTS ===================

    @Test
    @DisplayName("DELETE - Usuario no puede eliminar reservas")
    void deleteReserva_UserCannotDelete_ShouldThrow() {
        when(reservaDAO.buscarPorId(validId)).thenReturn(Optional.of(validReserva));
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.delete(validId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tiene permisos");
        verify(reservaDAO, never()).eliminar(any());
    }

    @Test
    @DisplayName("DELETE - ID inválido debe lanzar excepción")
    void deleteReserva_InvalidId_ShouldThrow() {
        // Mock del usuario autenticado
        when(usuarioDAO.buscarPorEmail("test@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(personaMapper.dtoToUsuario(usuarioDTO)).thenReturn(usuarioEntity);

        assertThatThrownBy(() -> reservaService.delete(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
        verify(reservaDAO, never()).eliminar(any());
    }
}

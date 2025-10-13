package com.Rently.Business.Unit;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Business.Service.impl.ComentarioServiceImpl;
import com.Rently.Configuration.Security.JwtService;
import com.Rently.Persistence.DAO.AlojamientoDAO;
import com.Rently.Persistence.DAO.AnfitrionDAO;
import com.Rently.Persistence.DAO.ComentarioDAO;
import com.Rently.Persistence.Entity.Alojamiento;
import com.Rently.Persistence.Entity.Anfitrion;
import com.Rently.Persistence.Entity.Comentario;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Mapper.AlojamientoMapper;
import com.Rently.Persistence.Repository.AnfitrionRepository;
import com.Rently.Persistence.Repository.ComentarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ComentarioServiceImpl - Unit Tests")
class ComentarioServiceImplUnitTest {

    @Mock private ComentarioDAO comentarioDAO;
    @Mock private AnfitrionDAO anfitrionDAO;
    @Mock private AlojamientoDAO alojamientoDAO;
    @Mock private AlojamientoDTO alojamientoDTO;
    @Mock private UsuarioService usuarioService;
    @Mock private AlojamientoMapper alojamientoMapper;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private AnfitrionRepository anfitrionRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private ComentarioServiceImpl comentarioService;

    private ComentarioDTO comentarioDTO;
    private UsuarioDTO usuarioDTO;
    private String token;
    private Long validId;

    @BeforeEach
    void setUp() {
        validId = 1L;
        token = "Bearer validtoken";

        comentarioDTO = new ComentarioDTO();
        comentarioDTO.setId(validId);
        comentarioDTO.setAlojamientoId(2L);
        comentarioDTO.setUsuarioId(3L);
        comentarioDTO.setComentario("Excelente alojamiento");
        comentarioDTO.setCalificacion(5);

        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(3L);
        usuarioDTO.setEmail("user@test.com");
        usuarioDTO.setRol(Rol.USUARIO);
        usuarioDTO.setActivo(true);

        comentarioService = new ComentarioServiceImpl(jwtService);

        ReflectionTestUtils.setField(comentarioService, "comentarioDAO", comentarioDAO);
        ReflectionTestUtils.setField(comentarioService, "comentarioRepository", comentarioRepository);
        ReflectionTestUtils.setField(comentarioService, "anfitrionRepository", anfitrionRepository);
        ReflectionTestUtils.setField(comentarioService, "usuarioService", usuarioService);
        ReflectionTestUtils.setField(comentarioService, "alojamientoDAO", alojamientoDAO);
        ReflectionTestUtils.setField(comentarioService, "anfitrionDAO", anfitrionDAO);
        ReflectionTestUtils.setField(comentarioService, "alojamientoMapper", alojamientoMapper);

        when(jwtService.extractUsername(anyString())).thenReturn("user@test.com");
        when(usuarioService.findUserByEmail("user@test.com")).thenReturn(Optional.of(usuarioDTO));
    }



    // =================== CREATE TESTS ===================

    @Test
    @DisplayName("CREATE - Comentario válido debe ser creado correctamente")
    void createComentario_ValidData_ShouldReturnCreated() {
        when(comentarioDAO.crearComentario(any(ComentarioDTO.class))).thenReturn(comentarioDTO);

        ComentarioDTO result = comentarioService.create(comentarioDTO);

        assertThat(result).isNotNull();
        assertThat(result.getComentario()).isEqualTo("Excelente alojamiento");
        verify(comentarioDAO, times(1)).crearComentario(any(ComentarioDTO.class));
    }

    @Test
    @DisplayName("CREATE - Comentario nulo debe lanzar IllegalArgumentException")
    void createComentario_Null_ShouldThrow() {
        assertThatThrownBy(() -> comentarioService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser nulo");
    }

    @Test
    @DisplayName("CREATE - Falta alojamientoId debe lanzar excepción")
    void createComentario_NoAlojamiento_ShouldThrow() {
        comentarioDTO.setAlojamientoId(null);
        assertThatThrownBy(() -> comentarioService.create(comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alojamiento");
    }

    @Test
    @DisplayName("CREATE - Falta usuarioId debe lanzar excepción")
    void createComentario_NoUsuario_ShouldThrow() {
        comentarioDTO.setUsuarioId(null);
        assertThatThrownBy(() -> comentarioService.create(comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("usuario");
    }

    @Test
    @DisplayName("CREATE - Texto vacío debe lanzar excepción")
    void createComentario_TextoVacio_ShouldThrow() {
        comentarioDTO.setComentario("   ");
        assertThatThrownBy(() -> comentarioService.create(comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("texto del comentario");
    }

    @Test
    @DisplayName("CREATE - Calificación fuera de rango debe lanzar excepción")
    void createComentario_InvalidRating_ShouldThrow() {
        comentarioDTO.setCalificacion(10);
        assertThatThrownBy(() -> comentarioService.create(comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("calificación");
    }

    // =================== READ TESTS ===================

    @Test
    @DisplayName("READ - Buscar comentarios por alojamiento retorna lista válida")
    void findByAlojamientoId_ShouldReturnList() {
        when(comentarioDAO.obtenerComentariosPorAlojamiento(2L)).thenReturn(List.of(comentarioDTO));

        List<ComentarioDTO> result = comentarioService.findByAlojamientoId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComentario()).contains("Excelente");
        verify(comentarioDAO, times(1)).obtenerComentariosPorAlojamiento(2L);
    }

    @Test
    @DisplayName("READ - Buscar por alojamiento sin resultados lanza excepción")
    void findByAlojamientoId_EmptyList_ShouldThrow() {
        when(comentarioDAO.obtenerComentariosPorAlojamiento(2L)).thenReturn(List.of());

        assertThatThrownBy(() -> comentarioService.findByAlojamientoId(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se encontraron comentarios");
    }

    @Test
    @DisplayName("READ - ID de alojamiento inválido debe lanzar excepción")
    void findByAlojamientoId_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> comentarioService.findByAlojamientoId(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =================== UPDATE TESTS ===================

    @Test
    @DisplayName("UPDATE - Comentario válido se actualiza correctamente")
    void updateComentario_ValidData_ShouldReturnUpdated() {
        ComentarioDTO updated = new ComentarioDTO();
        updated.setId(validId);
        updated.setComentario("Actualizado");

        when(comentarioDAO.actualizarComentario(eq(validId), any(ComentarioDTO.class)))
                .thenReturn(updated);

        ComentarioDTO result = comentarioService.update(validId, comentarioDTO);

        assertThat(result).isNotNull();
        assertThat(result.getComentario()).isEqualTo("Actualizado");
        verify(comentarioDAO, times(1)).actualizarComentario(eq(validId), any(ComentarioDTO.class));
    }

    @Test
    @DisplayName("UPDATE - ID inválido debe lanzar excepción")
    void updateComentario_InvalidId_ShouldThrow() {
        assertThatThrownBy(() -> comentarioService.update(0L, comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("UPDATE - Comentario vacío debe lanzar excepción")
    void updateComentario_TextoVacio_ShouldThrow() {
        comentarioDTO.setComentario("   ");
        assertThatThrownBy(() -> comentarioService.update(validId, comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("texto del comentario");
    }

    @Test
    @DisplayName("UPDATE - Calificación fuera de rango lanza excepción")
    void updateComentario_InvalidRating_ShouldThrow() {
        comentarioDTO.setCalificacion(8);
        assertThatThrownBy(() -> comentarioService.update(validId, comentarioDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("calificación");
    }

    // =================== RESPONDER TESTS ===================

    @Test
    @DisplayName("RESPONDER - Anfitrión propietario puede responder")
    void responderComentario_AnfitrionPropietario_ShouldSucceed() {
        // 🔹 Simular token y extracción de usuario
        when(jwtService.extractUsername(anyString())).thenReturn("user@test.com");

        // 🔹 Crear anfitrión autenticado
        Anfitrion anfitrion = new Anfitrion();
        anfitrion.setId(10L);
        anfitrion.setEmail("user@test.com");

        // 🔹 Mockear repositorios y DAOs
        when(anfitrionRepository.findByEmail("user@test.com")).thenReturn(Optional.of(anfitrion));
        when(comentarioDAO.buscarPorId(1L)).thenReturn(Optional.of(comentarioDTO));

        // 🔹 Mock de alojamiento con mismo ID de anfitrión
        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(2L);
        alojamiento.setAnfitrionId(10L);
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamiento));

        // 🔹 Mock del DAO de respuesta
        when(comentarioDAO.responderComentario(1L, "Gracias!")).thenReturn(comentarioDTO);

        // 🔹 Ejecutar método
        ComentarioDTO result = comentarioService.addResponse(1L, "Gracias!", token);

        // 🔹 Validar resultado
        assertThat(result).isNotNull();
        verify(comentarioDAO).responderComentario(1L, "Gracias!");
    }



    @Test
    @DisplayName("RESPONDER - Comentario sin alojamiento válido lanza excepción")
    void responderComentario_InvalidAlojamiento_ShouldThrow() {
        Anfitrion anfitrion = new Anfitrion();
        anfitrion.setId(10L);
        anfitrion.setEmail("user@test.com");

        when(anfitrionRepository.findByEmail("user@test.com")).thenReturn(Optional.of(anfitrion));
        when(comentarioDAO.buscarPorId(1L)).thenReturn(Optional.of(comentarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comentarioService.addResponse(1L, "Gracias!", token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El comentario no está asociado");
    }

    @Test
    @DisplayName("RESPONDER - Anfitrión no propietario lanza excepción")
    void responderComentario_AnfitrionNoPropietario_ShouldThrow() {
        Anfitrion anfitrion = new Anfitrion();
        anfitrion.setId(99L);
        anfitrion.setEmail("user@test.com");

        when(anfitrionRepository.findByEmail("user@test.com")).thenReturn(Optional.of(anfitrion));
        when(comentarioDAO.buscarPorId(1L)).thenReturn(Optional.of(comentarioDTO));
        when(alojamientoDAO.buscarPorId(2L)).thenReturn(Optional.of(alojamientoDTO));

        assertThatThrownBy(() -> comentarioService.addResponse(1L, "Respuesta", token))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Solo el anfitrión del alojamiento puede responder");
    }
    // =================== DELETE TESTS ===================

    @Test
    @DisplayName("DELETE - Usuario dueño del comentario puede eliminarlo")
    void deleteComentario_PropietarioPuedeEliminar() {
        when(comentarioDAO.buscarPorId(validId)).thenReturn(Optional.of(comentarioDTO));

        comentarioService.delete(validId, token);

        verify(comentarioDAO, times(1)).eliminarComentario(validId);
    }

    @Test
    @DisplayName("DELETE - Usuario no dueño ni admin lanza SecurityException")
    void deleteComentario_NoAutorizado_ShouldThrow() {
        ComentarioDTO otro = new ComentarioDTO();
        otro.setId(validId);
        otro.setUsuarioId(999L);
        when(comentarioDAO.buscarPorId(validId)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> comentarioService.delete(validId, token))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No tiene permisos");
    }

    @Test
    @DisplayName("DELETE - Admin puede eliminar cualquier comentario")
    void deleteComentario_AdminPuedeEliminar() {
        usuarioDTO.setRol(Rol.ADMINISTRADOR);
        when(usuarioService.findUserByEmail("user@test.com")).thenReturn(Optional.of(usuarioDTO));
        when(comentarioDAO.buscarPorId(validId)).thenReturn(Optional.of(comentarioDTO));

        comentarioService.delete(validId, token);

        verify(comentarioDAO, times(1)).eliminarComentario(validId);
    }
}


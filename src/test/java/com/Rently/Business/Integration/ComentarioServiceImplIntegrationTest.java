package com.Rently.Business.Integration;

import com.Rently.Business.DTO.*;
import com.Rently.Business.Service.*;
import com.Rently.Persistence.Repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests de integración para ComentarioService con autenticación simulada.
 */
@Import(TestJwtConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComentarioServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory factory;

    @Autowired
    private ComentarioService comentarioService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AlojamientoService alojamientoService;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SecurityTestHelper securityHelper;

    private UsuarioDTO usuario;
    private UsuarioDTO otroUsuario;
    private AnfitrionDTO anfitrion;
    private AdministradorDTO admin;
    private AlojamientoDTO alojamiento;

    @BeforeEach
    void setup() {
        factory.clearAll();
        securityHelper.clearAuthentication();

        usuario = factory.createUsuario("usuario@test.com");
        otroUsuario = factory.createUsuario("otro@test.com");
        anfitrion = factory.createAnfitrion("anfitrion@test.com");
        admin = factory.createAdmin("admin@test.com");
        alojamiento = factory.createAlojamiento(anfitrion, factory.createServiciosDefault());

        securityHelper.authenticateUser(usuario);
    }

    @AfterEach
    void cleanup() {
        securityHelper.clearAuthentication();
    }

    // =================== CREACIÓN ===================

    @Test
    @Order(1)
    @DisplayName("Crear comentario exitosamente")
    void crearComentario_exito() {
        ComentarioDTO comentario = factory.createComentario(usuario, alojamiento);
        ComentarioDTO creado = comentarioService.create(comentario);

        assertNotNull(creado.getId());
        assertEquals(usuario.getId(), creado.getUsuarioId());
        assertEquals(alojamiento.getId(), creado.getAlojamientoId());
        assertEquals(5, creado.getCalificacion());
    }

    @Test
    @Order(2)
    @DisplayName("Error: comentario nulo")
    void crearComentario_nulo_error() {
        assertThrows(IllegalArgumentException.class, () -> comentarioService.create(null));
    }

    @Test
    @Order(3)
    @DisplayName("Error: calificación fuera de rango")
    void crearComentario_calificacionInvalida_error() {
        ComentarioDTO comentario = factory.createComentario(usuario, alojamiento);
        comentario.setCalificacion(8); // Inválido

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.create(comentario));
        assertTrue(ex.getMessage().contains("calificación"));
    }

    @Test
    @Order(4)
    @DisplayName("Error: texto vacío")
    void crearComentario_textoVacio_error() {
        ComentarioDTO comentario = factory.createComentario(usuario, alojamiento);
        comentario.setComentario(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.create(comentario));
        assertTrue(ex.getMessage().contains("vacío"));
    }

    @Test
    @Order(5)
    @DisplayName("Error: usuario inexistente")
    void crearComentario_usuarioInexistente_error() {
        ComentarioDTO comentario = factory.createComentario(usuario, alojamiento);
        comentario.setUsuarioId(99999L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> comentarioService.create(comentario));
        assertTrue(ex.getMessage().toLowerCase().contains("usuario"));
    }

    // =================== LECTURA ===================

    @Test
    @Order(6)
    @DisplayName("Listar comentarios por alojamiento")
    void listarComentarios_porAlojamiento() {
        ComentarioDTO c1 = factory.createComentario(usuario, alojamiento);
        comentarioService.create(c1);

        List<ComentarioDTO> lista = comentarioService.findByAlojamientoId(alojamiento.getId());
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(c -> c.getAlojamientoId().equals(alojamiento.getId())));
    }

    @Test
    @Order(7)
    @DisplayName("Error: listar con ID inválido")
    void listarComentarios_idInvalido_error() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.findByAlojamientoId(0L));
        assertTrue(ex.getMessage().contains("válido"));
    }

    // =================== ACTUALIZACIÓN ===================

    @Test
    @Order(8)
    @DisplayName("Actualizar comentario exitosamente")
    void actualizarComentario_exito() {
        ComentarioDTO creado = comentarioService.create(factory.createComentario(usuario, alojamiento));

        ComentarioDTO cambios = new ComentarioDTO();
        cambios.setComentario("Muy buen sitio");
        cambios.setCalificacion(4);

        ComentarioDTO actualizado = comentarioService.update(creado.getId(), cambios);

        assertEquals("Muy buen sitio", actualizado.getComentario());
        assertEquals(4, actualizado.getCalificacion());
    }

    @Test
    @Order(9)
    @DisplayName("Error: actualizar con ID inválido")
    void actualizarComentario_idInvalido_error() {
        ComentarioDTO cambios = new ComentarioDTO();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.update(0L, cambios));
        assertTrue(ex.getMessage().contains("ID"));
    }

    // =================== RESPUESTA ===================

    @Test
    @Order(10)
    @DisplayName("Responder comentario exitosamente (anfitrión del alojamiento)")
    void responderComentario_exito() {
        ComentarioDTO comentario = comentarioService.create(factory.createComentario(usuario, alojamiento));

        securityHelper.authenticateUser(anfitrion);

        ComentarioDTO respondido = comentarioService.addResponse(comentario.getId(), "Gracias por tu opinión", "Bearer faketoken");

        assertNotNull(respondido);
        assertEquals(comentario.getId(), respondido.getId());
    }

    @Test
    @Order(11)
    @DisplayName("Error: respuesta vacía")
    void responderComentario_respuestaVacia_error() {
        ComentarioDTO comentario = comentarioService.create(factory.createComentario(usuario, alojamiento));
        securityHelper.authenticateUser(anfitrion);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.addResponse(comentario.getId(), " ", "Bearer faketoken"));
        assertTrue(ex.getMessage().contains("vacía"));
    }

    @Test
    @Order(12)
    @DisplayName("Error: no anfitrión intenta responder")
    void responderComentario_noAnfitrion_error() {
        ComentarioDTO comentario = comentarioService.create(factory.createComentario(usuario, alojamiento));
        securityHelper.authenticateUser(usuario); // No es anfitrión

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> comentarioService.addResponse(comentario.getId(), "Gracias!", "Bearer faketoken"));
        assertTrue(ex.getMessage().toLowerCase().contains("anfitrión"));
    }

    // =================== ELIMINACIÓN ===================

    @Test
    @Order(13)
    @DisplayName("Eliminar comentario por su autor exitosamente")
    void eliminarComentario_autor_exito() {
        ComentarioDTO comentario = comentarioService.create(factory.createComentario(usuario, alojamiento));

        boolean eliminado = assertDoesNotThrow(() -> {
            comentarioService.delete(comentario.getId(), "Bearer faketoken");
            return true;
        });

        assertTrue(eliminado);
    }

    @Test
    @Order(14)
    @DisplayName("Error: usuario no propietario intenta eliminar")
    void eliminarComentario_noPropietario_error() {
        ComentarioDTO comentario = comentarioService.create(factory.createComentario(usuario, alojamiento));

        securityHelper.authenticateUser(otroUsuario);

        SecurityException ex = assertThrows(SecurityException.class,
                () -> comentarioService.delete(comentario.getId(), "Bearer faketoken"));
        assertTrue(ex.getMessage().contains("permisos"));
    }


    @Test
    @Order(15)
    @DisplayName("Error: eliminar comentario inexistente")
    void eliminarComentario_inexistente_error() {
        securityHelper.authenticateUser(admin);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> comentarioService.delete(99999L, "Bearer faketoken"));
        assertTrue(ex.getMessage().toLowerCase().contains("no encontrado"));
    }
}


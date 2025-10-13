package com.Rently.Business.Integration;

import com.Rently.Business.DTO.*;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Persistence.Entity.EstadoReserva;
import com.Rently.Persistence.Entity.Usuario;
import com.Rently.Persistence.Repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para ReservaService con autenticación simulada.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservaServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestDataFactory factory;

    @Autowired
    private ReservaService reservaService;

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

    // =================== CREACIÓN DE RESERVAS ===================

    @Test
    @Order(1)
    @DisplayName("Crear reserva exitosamente")
    void crearReserva_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        assertNotNull(creada.getId());
        assertEquals(EstadoReserva.PENDIENTE, creada.getEstado());
        assertEquals(usuario.getId(), creada.getUsuarioId());
        assertEquals(alojamiento.getId(), creada.getAlojamientoId());
    }

    @Test
    @Order(2)
    @DisplayName("Error al crear reserva: fecha fin anterior a fecha inicio")
    void crearReserva_fechaFinAntesInicio_error() {
        ReservaDTO reserva = new ReservaDTO(
                null,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(2), // Fecha fin antes de inicio
                2,
                usuario.getId(),
                alojamiento.getId()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservaService.create(reserva));
        assertTrue(ex.getMessage().contains("fecha de fin no puede ser anterior"));
    }

    @Test
    @Order(3)
    @DisplayName("Error al crear reserva: datos nulos")
    void crearReserva_datosNulos_error() {
        assertThrows(IllegalArgumentException.class, () -> reservaService.create(null));
    }

    @Test
    @Order(4)
    @DisplayName("Error al crear reserva: fechas nulas")
    void crearReserva_fechasNulas_error() {
        ReservaDTO reserva = new ReservaDTO(
                null, null, null, 2, usuario.getId(), alojamiento.getId()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservaService.create(reserva));
        assertTrue(ex.getMessage().contains("fechas son obligatorias"));
    }

    @Test
    @Order(5)
    @DisplayName("Error al crear reserva: usuario inexistente")
    void crearReserva_usuarioInexistente_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        reserva.setUsuarioId(99999L); // ID inexistente

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.create(reserva));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    @Order(6)
    @DisplayName("Error al crear reserva: usuario inactivo")
    void crearReserva_usuarioInactivo_error() {
        Usuario usuarioEntity = usuarioRepository.findById(usuario.getId()).orElseThrow();
        usuarioEntity.setActivo(false);
        usuarioRepository.save(usuarioEntity);

        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.create(reserva));

        System.out.println("🔍 Mensaje de error real: " + ex.getMessage());

        String mensaje = ex.getMessage().toLowerCase();
        assertTrue(mensaje.contains("activo") || mensaje.contains("no existe"),
                "Esperaba un error de usuario inactivo o inexistente, pero recibió: " + ex.getMessage());
    }

    @Test
    @Order(7)
    @DisplayName("Error al crear reserva: alojamiento eliminado")
    void crearReserva_alojamientoEliminado_error() {
        alojamientoService.delete(alojamiento.getId());

        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.create(reserva));

        String mensaje = ex.getMessage().toLowerCase();
        assertTrue(mensaje.contains("no existe") || mensaje.contains("no está disponible"),
                "Esperaba error de alojamiento no disponible, pero recibió: " + ex.getMessage());
    }

    @Test
    @Order(8)
    @DisplayName("Error al crear reserva: solapamiento de fechas")
    void crearReserva_solapamientoFechas_error() {
        ReservaDTO r1 = factory.createReserva(usuario, alojamiento);
        reservaService.create(r1);

        ReservaDTO r2 = new ReservaDTO(
                null,
                r1.getFechaInicio().plusDays(1),
                r1.getFechaFin().plusDays(1),
                2,
                usuario.getId(),
                alojamiento.getId()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.create(r2));
        assertTrue(ex.getMessage().contains("Ya existe una reserva activa"));
    }

    @Test
    @Order(9)
    @DisplayName("Crear reserva: sin solapamiento si otra está cancelada")
    void crearReserva_sinSolapamiento_siOtraCancelada() {
        ReservaDTO r1 = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada1 = reservaService.create(r1);
        reservaService.updateState(creada1.getId(), EstadoReserva.CANCELADA);

        ReservaDTO r2 = new ReservaDTO(
                null,
                r1.getFechaInicio(),
                r1.getFechaFin(),
                2,
                usuario.getId(),
                alojamiento.getId()
        );

        ReservaDTO creada2 = assertDoesNotThrow(() -> reservaService.create(r2));
        assertNotNull(creada2.getId());
    }

    @Test
    @Order(10)
    @DisplayName("Error al crear reserva: usuario intenta crear para otro usuario")
    void crearReserva_usuarioNoPuedeCrearParaOtro_error() {
        ReservaDTO reserva = factory.createReserva(otroUsuario, alojamiento);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.create(reserva));
        assertTrue(ex.getMessage().contains("No puede crear reservas en nombre de otro usuario"));
    }

    @Test
    @Order(11)
    @DisplayName("Admin puede crear reserva para cualquier usuario")
    void crearReserva_adminPuedeCrearParaOtro() {
        securityHelper.authenticateUser(admin);
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = assertDoesNotThrow(() -> reservaService.create(reserva));
        assertNotNull(creada.getId());
        assertEquals(usuario.getId(), creada.getUsuarioId());
    }




    // =================== ACTUALIZACIÓN DE RESERVAS ===================

    @Test
    @Order(12)
    @DisplayName("Actualizar reserva exitosamente")
    void actualizarReserva_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        ReservaDTO cambios = new ReservaDTO(
                creada.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                usuario.getId(),
                alojamiento.getId()
        );

        Optional<ReservaDTO> actualizada = reservaService.update(creada.getId(), cambios);

        assertTrue(actualizada.isPresent());
        assertEquals(4, actualizada.get().getNumeroHuespedes());
        assertEquals(LocalDate.now().plusDays(10), actualizada.get().getFechaInicio());
    }

    @Test
    @Order(13)
    @DisplayName("Error al actualizar: usuario no es propietario")
    void actualizarReserva_usuarioNoPropietario_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(otroUsuario);

        ReservaDTO cambios = new ReservaDTO(
                creada.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                4,
                usuario.getId(),
                alojamiento.getId()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.update(creada.getId(), cambios));
        assertTrue(ex.getMessage().contains("No tiene permisos"));
    }

    @Test
    @Order(14)
    @DisplayName("Error al actualizar: reserva confirmada")
    void actualizarReserva_estadoConfirmada_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(anfitrion);
        reservaService.updateState(creada.getId(), EstadoReserva.CONFIRMADA);

        securityHelper.authenticateUser(usuario);
        ReservaDTO cambios = new ReservaDTO(
                creada.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                3,
                usuario.getId(),
                alojamiento.getId()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.update(creada.getId(), cambios));
        assertTrue(ex.getMessage().contains("No se puede modificar una reserva confirmada"));
    }


    @Test
    @Order(15)
    @DisplayName("Error al actualizar: reserva rechazada")
    void actualizarReserva_estadoRechazada_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(admin);
        reservaService.updateState(creada.getId(), EstadoReserva.RECHAZADA);

        securityHelper.authenticateUser(usuario);
        ReservaDTO cambios = new ReservaDTO(
                creada.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                3,
                usuario.getId(),
                alojamiento.getId()
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.update(creada.getId(), cambios));
        assertTrue(ex.getMessage().contains("rechazada"));
    }

    @Test
    @Order(16)
    @DisplayName("Admin puede actualizar cualquier reserva")
    void actualizarReserva_adminPuedeActualizarCualquiera() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);
        securityHelper.authenticateUser(admin);
        ReservaDTO cambios = new ReservaDTO(
                creada.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                5,
                usuario.getId(),
                alojamiento.getId()
        );

        Optional<ReservaDTO> actualizada = assertDoesNotThrow(
                () -> reservaService.update(creada.getId(), cambios));
        assertTrue(actualizada.isPresent());
        assertEquals(5, actualizada.get().getNumeroHuespedes());
    }

    // =================== CAMBIO DE ESTADO ===================

    @Test
    @Order(17)
    @DisplayName("Admin confirma reserva exitosamente")
    void cambiarEstado_confirmarPorAdmin_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(admin);
        boolean updated = reservaService.updateState(creada.getId(), EstadoReserva.CONFIRMADA);

        assertTrue(updated);
        Optional<ReservaDTO> opt = reservaService.findById(creada.getId());
        assertEquals(EstadoReserva.CONFIRMADA, opt.get().getEstado());
    }

    @Test
    @Order(18)
    @DisplayName("Usuario cancela su reserva exitosamente")
    void cambiarEstado_cancelarPorUsuario_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        boolean canceled = reservaService.updateState(creada.getId(), EstadoReserva.CANCELADA);

        assertTrue(canceled);
        Optional<ReservaDTO> opt = reservaService.findById(creada.getId());
        assertEquals(EstadoReserva.CANCELADA, opt.get().getEstado());
    }

    @Test
    @Order(19)
    @DisplayName("Error: usuario intenta cancelar reserva de otro")
    void cambiarEstado_cancelarNoPropietario_error() {
        // Crear reserva como usuario
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(otroUsuario);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.updateState(creada.getId(), EstadoReserva.CANCELADA));
        assertTrue(ex.getMessage().contains("No puede cancelar"));
    }

    @Test
    @Order(20)
    @DisplayName("Error: usuario intenta confirmar reserva (solo admin)")
    void cambiarEstado_usuarioNoPuedeConfirmar_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.updateState(creada.getId(), EstadoReserva.CONFIRMADA));
        assertTrue(ex.getMessage().contains("administrador"));
    }

    @Test
    @Order(21)
    @DisplayName("Error: usuario intenta rechazar reserva (solo admin)")
    void cambiarEstado_usuarioNoPuedeRechazar_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.updateState(creada.getId(), EstadoReserva.RECHAZADA));
        assertTrue(ex.getMessage().contains("administrador"));
    }

    @Test
    @Order(22)
    @DisplayName("Admin puede rechazar reserva")
    void cambiarEstado_adminRechazaReserva_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(admin);
        boolean updated = reservaService.updateState(creada.getId(), EstadoReserva.RECHAZADA);

        assertTrue(updated);
        Optional<ReservaDTO> opt = reservaService.findById(creada.getId());
        assertEquals(EstadoReserva.RECHAZADA, opt.get().getEstado());
    }

    // =================== ELIMINACIÓN ===================

    @Test
    @Order(23)
    @DisplayName("Admin elimina reserva exitosamente")
    void eliminarReserva_admin_exito() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        securityHelper.authenticateUser(admin);
        boolean deleted = reservaService.delete(creada.getId());

        assertTrue(deleted);
        Optional<ReservaDTO> opt = reservaService.findById(creada.getId());
        assertTrue(opt.isEmpty());
    }

    @Test
    @Order(24)
    @DisplayName("Error: usuario no puede eliminar reserva")
    void eliminarReserva_usuario_error() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.delete(creada.getId()));
        assertTrue(ex.getMessage().contains("No tiene permisos"));
    }

    @Test
    @Order(25)
    @DisplayName("Error: eliminar reserva inexistente")
    void eliminarReserva_inexistente_error() {
        securityHelper.authenticateUser(admin);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservaService.delete(99999L));
        assertTrue(ex.getMessage().contains("no encontrada"));
    }

    // =================== LISTADOS ===================

    @Test
    @Order(26)
    @DisplayName("Listar todas las reservas")
    void listarReservas_todas() {
        ReservaDTO r1 = factory.createReserva(usuario, alojamiento);
        ReservaDTO r2 = factory.createReserva(usuario, alojamiento);

        r2.setFechaInicio(LocalDate.now().plusDays(10));
        r2.setFechaFin(LocalDate.now().plusDays(12));

        reservaService.create(r1);
        reservaService.create(r2);

        List<ReservaDTO> lista = reservaService.findAll();
        assertTrue(lista.size() >= 2);
    }

    @Test
    @Order(27)
    @DisplayName("Listar reservas por usuario")
    void listarReservas_porUsuario() {
        ReservaDTO r1 = factory.createReserva(usuario, alojamiento);
        reservaService.create(r1);

        List<ReservaDTO> lista = reservaService.findByUserId(usuario.getId());
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(r -> r.getUsuarioId().equals(usuario.getId())));
    }

    @Test
    @Order(28)
    @DisplayName("Listar reservas por alojamiento")
    void listarReservas_porAlojamiento() {
        ReservaDTO r1 = factory.createReserva(usuario, alojamiento);
        reservaService.create(r1);

        List<ReservaDTO> lista = reservaService.findByAlojamientoId(alojamiento.getId());
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(r -> r.getAlojamientoId().equals(alojamiento.getId())));
    }

    @Test
    @Order(29)
    @DisplayName("Buscar reserva por ID")
    void buscarReserva_porId() {
        ReservaDTO reserva = factory.createReserva(usuario, alojamiento);
        ReservaDTO creada = reservaService.create(reserva);

        Optional<ReservaDTO> encontrada = reservaService.findById(creada.getId());
        assertTrue(encontrada.isPresent());
        assertEquals(creada.getId(), encontrada.get().getId());
    }

    @Test
    @Order(30)
    @DisplayName("Buscar reserva inexistente retorna vacío")
    void buscarReserva_inexistente() {
        Optional<ReservaDTO> encontrada = reservaService.findById(99999L);
        assertTrue(encontrada.isEmpty());
    }
}





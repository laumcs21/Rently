package com.Rently.Business.Integration;

import com.Rently.Business.DTO.*;
import com.Rently.Business.Service.*;
import com.Rently.Persistence.Entity.EstadoPago;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Entity.TipoAlojamiento;
import com.Rently.Persistence.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Factoría de datos para pruebas de integración.
 * ⚠️ Se elimina la limpieza automática dentro de cada método
 *     para evitar borrar datos entre tests.
 */
@Component
@Transactional
public class TestDataFactory {

    // ================== Servicios ==================
    @Autowired private UsuarioService usuarioService;
    @Autowired private AnfitrionService anfitrionService;
    @Autowired private AdministradorService administradorService;
    @Autowired private ServicioService servicioService;
    @Autowired private AlojamientoService alojamientoService;

    // ================== Repositorios ==================
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AnfitrionRepository anfitrionRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private AlojamientoRepository alojamientoRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private ComentarioRepository comentarioRepository;
    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private AlojamientoImagenRepository alojamientoImagenRepository;

    // ================== Utilidad ==================
    private String uniqueEmail(String baseEmail) {
        return baseEmail.replace("@", "+" + System.currentTimeMillis() + "@");
    }

    public void clearAll() {
            transaccionRepository.deleteAllInBatch();
            notificacionRepository.deleteAllInBatch();
            comentarioRepository.deleteAllInBatch();
            reservaRepository.deleteAllInBatch();
            alojamientoImagenRepository.deleteAllInBatch();
            alojamientoRepository.deleteAllInBatch();
            servicioRepository.deleteAllInBatch();
            administradorRepository.deleteAllInBatch();
            anfitrionRepository.deleteAllInBatch();
            usuarioRepository.deleteAllInBatch();

    }

    // ========================= USUARIOS =========================
    public UsuarioDTO createUsuario(String email) {
        return usuarioService.registerUser(
                new UsuarioDTO(
                        null,
                        "Usuario Test",
                        uniqueEmail(email),
                        "3001234567",
                        "Password123A",
                        LocalDate.of(1995, 1, 15),
                        Rol.USUARIO,
                        "usuario.jpg"
                )
        );
    }

    // ========================= ANFITRIONES =========================
    public AnfitrionDTO createAnfitrion(String email) {
        return anfitrionService.create(
                new AnfitrionDTO(
                        null,
                        "Anfitrion Test",
                        uniqueEmail(email),
                        "3107654321",
                        "Password123A",
                        LocalDate.of(1987, 5, 22),
                        Rol.ANFITRION,
                        "anfitrion.jpg"
                )
        );
    }

    // ========================= ADMINISTRADORES =========================
    public AdministradorDTO createAdmin(String email) {
        return administradorService.create(
                new AdministradorDTO(
                        null,
                        "Admin Test",
                        uniqueEmail(email),
                        "3119876543",
                        "AdminPass123A",
                        LocalDate.of(1980, 9, 30),
                        Rol.ADMINISTRADOR,
                        "admin.jpg"
                )
        );
    }

    // ========================= SERVICIOS =========================
    public ServicioDTO createServicio(String nombre) {
        return servicioService.create(
                new ServicioDTO(
                        null,
                        nombre,
                        "Descripción del servicio: " + nombre
                )
        );
    }

    public List<ServicioDTO> createServiciosDefault() {
        return Arrays.asList(
                createServicio("Piscina"),
                createServicio("Parqueadero"),
                createServicio("Desayuno incluido")
        );
    }

    // ========================= ALOJAMIENTOS =========================
    public AlojamientoDTO createAlojamiento(AnfitrionDTO anfitrion, List<ServicioDTO> servicios) {
        return alojamientoService.create(
                new AlojamientoDTO(
                        null,
                        "Casa Bonita",
                        "Hermosa casa frente al mar",
                        "Cartagena",
                        "Calle 123 #45-67",
                        10.123,
                        -75.456,
                        350000.0,
                        6,
                        false,
                        anfitrion.getId(),
                        TipoAlojamiento.CASA,
                        Arrays.asList("img1.jpg", "img2.jpg", "img3.jpg"),
                        servicios.stream().map(ServicioDTO::getId).toList(),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );
    }

    // ========================= RESERVAS =========================
    public ReservaDTO createReserva(UsuarioDTO usuario, AlojamientoDTO alojamiento) {
        return new ReservaDTO(
                null,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(7),
                3,
                usuario.getId(),
                alojamiento.getId()
        );
    }

    // ========================= COMENTARIOS =========================
    public ComentarioDTO createComentario(UsuarioDTO usuario, AlojamientoDTO alojamiento) {
        return new ComentarioDTO(
                null,
                5,
                "Excelente alojamiento, volveré pronto",
                null,
                LocalDate.now(),
                usuario.getId(),
                alojamiento.getId()
        );
    }

    // ========================= NOTIFICACIONES =========================
    public NotificacionDTO createNotificacion(UsuarioDTO usuario, ReservaDTO reserva) {
        return new NotificacionDTO(
                null,
                usuario.getId(),
                reserva.getId(),
                "Tu reserva fue creada con éxito",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ========================= TRANSACCIONES =========================
    public TransaccionDTO createTransaccion(ReservaDTO reserva) {
        return new TransaccionDTO(
                null,
                reserva.getId(),
                1400000.0,
                "COP",
                "Tarjeta de crédito",
                EstadoPago.APROBADO,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ========================= IMAGENES ALOJAMIENTO =========================
    public AlojamientoImagenDTO createImagen(Long alojamientoId, String url, int orden) {
        return new AlojamientoImagenDTO(
                null,
                url,
                orden,
                alojamientoId,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}


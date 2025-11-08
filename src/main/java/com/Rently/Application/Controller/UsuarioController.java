package com.Rently.Application.Controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.Rently.Business.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.AuthService;
import com.Rently.Business.Service.ComentarioService;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Persistence.Entity.EstadoReserva;
import com.Rently.Business.Service.FotoPerfilService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la gestión de usuarios, búsquedas, reservas y comentarios")
public class UsuarioController {


    private final UsuarioService usuarioService;
    private final AuthService authService;
    private final ReservaService reservaService;
    private final ComentarioService comentarioService;
    private final AlojamientoService alojamientoService;
    private FotoPerfilService fotoPerfilService;

    public UsuarioController(UsuarioService usuarioService,
                             AuthService authService,
                             ReservaService reservaService,
                             ComentarioService comentarioService,
                             AlojamientoService alojamientoService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.reservaService = reservaService;
        this.comentarioService = comentarioService;
        this.alojamientoService = alojamientoService;
    }

    // ---------------- CRUD de Usuarios ----------------

    @PostMapping
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en la plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta: datos incompletos"),
            @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioDTO usuario) {
        try {
            UsuarioDTO nuevoUsuario = usuarioService.registerUser(usuario);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(nuevoUsuario.getId())
                    .toUri();
            return ResponseEntity.created(location).body(nuevoUsuario);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Solicitud incorrecta: datos incompletos"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no proporcionado"));
            }

            String token = authHeader.substring(7);
            PersonaDTO user = authService.verifyToken(token); // Usa tu AuthService existente
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene la información de un usuario específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioDTO> obtenerUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        return usuarioService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos personales", description = "Permite al usuario actualizar su información personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> actualizarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @RequestBody UsuarioDTO usuario) {
        try {
            usuario.setId(id);
            UsuarioDTO usuarioActualizado = usuarioService.updateUserProfile(id, usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo actualizar el usuario" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cuenta", description = "Elimina la cuenta del usuario de la plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar usuario con reservas activas")
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        usuarioService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping(path = "/{id}/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> actualizarFotoPerfilUsuario(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws Exception {

        String url = fotoPerfilService.actualizarFotoPerfil(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/{id}/foto-perfil")
    public ResponseEntity<Void> eliminarFotoPerfilUsuario(@PathVariable Long id) throws Exception {
        fotoPerfilService.eliminarFotoPerfil(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Autenticación ----------------

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> iniciarSesionForm(
            @RequestParam("email") String email,
            @RequestParam("password") String password) {
        return procesarLogin(email, password);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> iniciarSesionJson(@RequestBody(required = false) AuthRequest request) {
        String email = request != null ? request.getEmail() : null;
        String password = request != null ? request.getPassword() : null;
        return procesarLogin(email, password);
    }

    private ResponseEntity<Map<String, Object>> procesarLogin(String email, String password) {
        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Solicitud incorrecta"));
        }

        try {
            AuthResponse authResponse = authService.login(
                    AuthRequest.builder()
                            .email(email)
                            .password(password)
                            .build()
            );
            return ResponseEntity.ok(Map.of("token", authResponse.getToken()));
        } catch (AuthenticationException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas"));
        }
    }



    @PostMapping("/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña actual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Map<String, Object>> cambiarContrasena(
            @RequestBody Map<String, Object> cambioRequest) {
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña cambiada exitosamente"));
    }

    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un código de recuperación al correo del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código de recuperación enviado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Map<String, Object>> solicitarRecuperacion(
            @Parameter(description = "Email del usuario") @RequestParam String email) {
        return ResponseEntity.ok(Map.of("mensaje", "Código de recuperación enviado"));
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña usando el código de recuperación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Código de recuperación inválido o expirado")
    })
    public ResponseEntity<Map<String, Object>> restablecerContrasena(
            @RequestBody Map<String, Object> restablecimientoRequest) {
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida exitosamente"));
    }

    // ---------------- Búsqueda de Alojamientos ----------------

    @GetMapping("/alojamientos")
    @Operation(summary = "Buscar alojamientos", description = "Busca alojamientos disponibles con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos")
    public ResponseEntity<Page<AlojamientoDTO>> buscarAlojamientos(
            @Parameter(description = "Ciudad de búsqueda") @RequestParam(required = false) String ciudad,
            @Parameter(description = "Fecha de inicio de la estadía") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin de la estadía") @RequestParam(required = false) LocalDate fechaFin,
            @Parameter(description = "Precio mínimo por noche") @RequestParam(required = false) BigDecimal precioMin,
            @Parameter(description = "Precio máximo por noche") @RequestParam(required = false) BigDecimal precioMax,
            @Parameter(description = "Número de huéspedes") @RequestParam(required = false) Integer numeroHuespedes,
            @Parameter(description = "Servicios requeridos") @RequestParam(required = false) List<String> servicios,
            @Parameter(description = "Tipo de alojamiento") @RequestParam(required = false) String tipoAlojamiento,
            @Parameter(description = "Calificación mínima") @RequestParam(required = false) Double calificacionMin,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "precio") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "asc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/alojamiento/{id}")
    @Operation(summary = "Ver detalles de alojamiento", description = "Obtiene los detalles completos de un alojamiento específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalles del alojamiento obtenidos"),
            @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado")
    })
    public ResponseEntity<?> obtenerDetalleAlojamiento(
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long id) {
        var alojamientoOpt = alojamientoService.findById(id);
        if (alojamientoOpt.isPresent()) {
            return ResponseEntity.ok(alojamientoOpt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Alojamiento no encontrado"));
    }

    @GetMapping("/alojamiento/{id}/disponibilidad")
    @Operation(summary = "Verificar disponibilidad", description = "Verifica la disponibilidad de un alojamiento en fechas específicas")
    @ApiResponse(responseCode = "200", description = "Disponibilidad verificada")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long id,
            @Parameter(description = "Fecha de inicio") @RequestParam LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/alojamiento/{id}/comentarios")
    @Operation(summary = "Ver comentarios de alojamiento", description = "Obtiene todos los comentarios de un alojamiento ordenados por fecha")
    @ApiResponse(responseCode = "200", description = "Comentarios obtenidos exitosamente")
    public ResponseEntity<Page<ComentarioDTO>> obtenerComentariosAlojamiento(
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Gestión de Reservas ----------------

    @PostMapping("/reserva")
    @Operation(summary = "Crear reserva", description = "Crea una nueva reserva para un alojamiento en fechas disponibles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "409", description = "Las fechas seleccionadas no están disponibles"),
            @ApiResponse(responseCode = "400", description = "Datos de reserva inválidos")
    })
    public ResponseEntity<?> crearReserva(@RequestBody ReservaDTO reserva) {
        try {
            ReservaDTO creada = reservaService.create(reserva);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(creada.getId())
                    .toUri();
            return ResponseEntity.created(location).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "Error al crear la reserva" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("solapan")
                    || message.toLowerCase().contains("disponible")
                    ? HttpStatus.CONFLICT
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", message));
        }
    }

    @GetMapping("/{id}/reserva")
    @Operation(summary = "Listar reservas del usuario", description = "Obtiene todas las reservas realizadas por el usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas obtenidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "El usuario no tiene reservas registradas")
    })
    public ResponseEntity<?> listarReservas(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Estado de la reserva") @RequestParam(required = false) String estado,
            @Parameter(description = "Fecha de inicio del filtro") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin del filtro") @RequestParam(required = false) LocalDate fechaFin,
            @Parameter(description = "N?mero de p?gina", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tama?o de p?gina", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Direcci?n de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Par?metros de paginaci?n inv?lidos."));
        }

        try {
            List<ReservaDTO> reservas = reservaService.findByUserId(id);
            if (reservas == null || reservas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El usuario no tiene reservas registradas"));
            }

            List<ReservaDTO> filtradas = new ArrayList<>(reservas);

            if (estado != null && !estado.isBlank()) {
                try {
                    EstadoReserva estadoFiltro = EstadoReserva.valueOf(estado.trim().toUpperCase());
                    filtradas.removeIf(r -> r.getEstado() != estadoFiltro);
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Estado de reserva inv?lido."));
                }
            }

            if (fechaInicio != null) {
                filtradas.removeIf(r -> r.getFechaInicio() == null || r.getFechaInicio().isBefore(fechaInicio));
            }

            if (fechaFin != null) {
                filtradas.removeIf(r -> r.getFechaFin() == null || r.getFechaFin().isAfter(fechaFin));
            }

            Comparator<ReservaDTO> comparator;
            switch (sortBy.toLowerCase()) {
                case "fechainicio" -> comparator = Comparator.comparing(ReservaDTO::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder()));
                case "fechafin" -> comparator = Comparator.comparing(ReservaDTO::getFechaFin, Comparator.nullsLast(Comparator.naturalOrder()));
                case "estado" -> comparator = Comparator.comparing(r -> r.getEstado() != null ? r.getEstado().name() : "");
                case "id" -> comparator = Comparator.comparing(ReservaDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
                default -> comparator = Comparator.comparing(ReservaDTO::getFechaInicio, Comparator.nullsLast(Comparator.naturalOrder()));
            }

            if ("desc".equalsIgnoreCase(sortDir)) {
                comparator = comparator.reversed();
            }
            filtradas.sort(comparator);

            int fromIndex = Math.min(page * size, filtradas.size());
            int toIndex = Math.min(fromIndex + size, filtradas.size());
            List<ReservaDTO> pageContent = filtradas.subList(fromIndex, toIndex);

            Page<ReservaDTO> resultado = new PageImpl<>(pageContent, PageRequest.of(page, size), filtradas.size());

            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            String mensaje = e.getMessage() == null ? "No se pudieron obtener las reservas" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", mensaje));
        }
    }

    @GetMapping("/{id}/reserva/{reservaId}")
    @Operation(summary = "Obtener detalle de reserva", description = "Obtiene los detalles de una reserva específica del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada o no pertenece al usuario")
    })
    public ResponseEntity<ReservaDTO> obtenerReserva(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long reservaId) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reserva/{id}")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva existente con más de 48 horas de anticipación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada exitosamente"),
            @ApiResponse(responseCode = "409", description = "No se pueden cancelar reservas con menos de 48 horas de anticipación"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<?> cancelarReserva(
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long id) {
        try {
            ReservaDTO cancelada = reservaService.cancelByUser(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Reserva cancelada",
                    "estado", cancelada.getEstado().name()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo cancelar la reserva" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", message));
        }
    }

    // ---------------- Gestión de Comentarios ----------------

    @PostMapping("/reserva/{id}/comentario")
    @Operation(summary = "Crear comentario", description = "Permite al usuario dejar un comentario después de completar una estadía")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud incorrecta: el usuario no ha finalizado la estadía"),
            @ApiResponse(responseCode = "409", description = "Ya existe un comentario para esta reserva")
    })
    public ResponseEntity<?> crearComentario(
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long id,
            @RequestBody ComentarioDTO comentario) {
        try {
            ReservaDTO reserva = reservaService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (reserva.getEstado() != null && reserva.getEstado() != EstadoReserva.FINALIZADA) {
                throw new IllegalStateException("Solo se pueden comentar reservas finalizadas.");
            }

            comentario.setUsuarioId(reserva.getUsuarioId());
            comentario.setAlojamientoId(reserva.getAlojamientoId());

            ComentarioDTO creado = comentarioService.create(comentario);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().toLowerCase().contains("no encontrada")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.CONFLICT;
            return ResponseEntity.status(status)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo crear el comentario" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }
    }

    @GetMapping("/{id}/comentarios")
    @Operation(summary = "Listar comentarios del usuario", description = "Obtiene todos los comentarios realizados por el usuario")
    @ApiResponse(responseCode = "200", description = "Comentarios obtenidos exitosamente")
    public ResponseEntity<Page<ComentarioDTO>> listarComentarios(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}/comentarios/{comentarioId}")
    @Operation(summary = "Editar comentario", description = "Permite al usuario editar un comentario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comentario no encontrado o no pertenece al usuario"),
            @ApiResponse(responseCode = "400", description = "El comentario no puede ser editado")
    })
    public ResponseEntity<ComentarioDTO> editarComentario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del comentario", example = "5") @PathVariable Long comentarioId,
            @RequestBody ComentarioDTO comentario) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}/comentarios/{comentarioId}")
    @Operation(summary = "Eliminar comentario", description = "Permite al usuario eliminar un comentario propio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comentario no encontrado o no pertenece al usuario")
    })
    public ResponseEntity<Void> eliminarComentario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del comentario", example = "5") @PathVariable Long comentarioId) {
        return ResponseEntity.noContent().build();
    }

    // ---------------- Funcionalidad Opcional: Favoritos ----------------

    @PostMapping("/{id}/favoritos/{alojamientoId}")
    @Operation(summary = "Agregar a favoritos", description = "Agrega un alojamiento a la lista de favoritos del usuario")
    @ApiResponse(responseCode = "200", description = "Alojamiento agregado a favoritos")
    public ResponseEntity<Map<String, Object>> agregarFavorito(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId) {
        return ResponseEntity.ok(Map.of("mensaje", "Alojamiento agregado a favoritos"));
    }

    @DeleteMapping("/{id}/favoritos/{alojamientoId}")
    @Operation(summary = "Quitar de favoritos", description = "Quita un alojamiento de la lista de favoritos del usuario")
    @ApiResponse(responseCode = "200", description = "Alojamiento quitado de favoritos")
    public ResponseEntity<Map<String, Object>> quitarFavorito(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId) {
        return ResponseEntity.ok(Map.of("mensaje", "Alojamiento quitado de favoritos"));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Listar favoritos", description = "Obtiene todos los alojamientos marcados como favoritos por el usuario")
    @ApiResponse(responseCode = "200", description = "Lista de favoritos obtenida")
    public ResponseEntity<Page<AlojamientoDTO>> listarFavoritos(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Historial y Preferencias ----------------

    @GetMapping("/{id}/historial-busquedas")
    @Operation(summary = "Historial de búsquedas", description = "Obtiene el historial de búsquedas del usuario")
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    public ResponseEntity<Page<Map<String, Object>>> obtenerHistorialBusquedas(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}/recomendaciones")
    @Operation(summary = "Obtener recomendaciones", description = "Obtiene alojamientos recomendados basados en el historial del usuario")
    @ApiResponse(responseCode = "200", description = "Recomendaciones obtenidas")
    public ResponseEntity<Page<AlojamientoDTO>> obtenerRecomendaciones(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Dashboard Usuario ----------------

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Dashboard del usuario", description = "Obtiene un resumen de actividad del usuario")
    @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerDashboard(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Notificaciones ----------------

    @GetMapping("/{id}/notificaciones")
    @Operation(summary = "Obtener notificaciones", description = "Obtiene las notificaciones del usuario")
    @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas")
    public ResponseEntity<Page<Map<String, Object>>> obtenerNotificaciones(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "Solo no leídas") @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            @Parameter(description = "Tipo de notificación") @RequestParam(required = false) String tipo,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}/notificaciones/{notificacionId}/marcar-leida")
    @Operation(summary = "Marcar notificación como leída", description = "Marca una notificación específica como leída")
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída")
    public ResponseEntity<Map<String, Object>> marcarNotificacionLeida(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la notificación", example = "3") @PathVariable Long notificacionId) {
        return ResponseEntity.ok(Map.of("mensaje", "Notificación marcada como leída"));
    }
}




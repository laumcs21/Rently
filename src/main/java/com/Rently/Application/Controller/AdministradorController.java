package com.Rently.Application.Controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Business.Service.FotoPerfilService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/administradores")
@Tag(name = "Administradores", description = "Operaciones relacionadas con la gestión administrativa del sistema")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final UsuarioService usuarioService;
    private final AlojamientoService alojamientoService;
    private final FotoPerfilService fotoPerfilService;

    public AdministradorController(AdministradorService administradorService,
                                   UsuarioService usuarioService,
                                   AlojamientoService alojamientoService,
                                   FotoPerfilService fotoPerfilService) {
        this.administradorService = administradorService;
        this.usuarioService = usuarioService;
        this.alojamientoService = alojamientoService;
        this.fotoPerfilService = fotoPerfilService;
    }

    // ---------------- CRUD de Administradores ----------------

    @GetMapping("/")
    public String home() {
        return "🚀 Rently está corriendo correctamente!";
    }

    @PostMapping
    @Operation(summary = "Crear administrador", description = "Registra un nuevo administrador en la plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Email duplicado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AdministradorDTO> crearAdministrador(@RequestBody AdministradorDTO administrador) {
        AdministradorDTO adminCreado = administradorService.create(administrador);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(adminCreado.getId())
                .toUri();
        return ResponseEntity.created(location).body(adminCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener administrador por ID", description = "Obtiene la información de un administrador específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador encontrado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    public ResponseEntity<AdministradorDTO> obtenerAdministrador(
            @Parameter(description = "ID del administrador", example = "1") @PathVariable Long id) {
        return administradorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar administrador", description = "Modifica la información de un administrador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador actualizado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> actualizarAdministrador(
            @Parameter(description = "ID del administrador", example = "1") @PathVariable Long id,
            @RequestBody AdministradorDTO administrador) {
        try {
            administrador.setId(id);
            AdministradorDTO actualizado = administradorService.update(id, administrador)
                    .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo actualizar el administrador" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar administrador", description = "Elimina un administrador de la plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Administrador eliminado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    public ResponseEntity<Void> eliminarAdministrador(
            @Parameter(description = "ID del administrador", example = "1") @PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> actualizarFotoPerfilAdministrador(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws Exception {

        String url = fotoPerfilService.actualizarFotoPerfil(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/{id}/foto-perfil")
    public ResponseEntity<Void> eliminarFotoPerfilAdministrador(@PathVariable Long id) throws Exception {
        fotoPerfilService.eliminarFotoPerfil(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Gestión de Usuarios (TC-24) ----------------

    @PostMapping("/usuarios")
    @Operation(summary = "Crear usuario", description = "El administrador crea un nuevo usuario en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<?> crearUsuario(@RequestBody UsuarioDTO usuario) {
        try {
            UsuarioDTO creado = usuarioService.registerUser(usuario);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(creado.getId())
                    .toUri();
            return ResponseEntity.created(location).body(creado);
        } catch (IllegalStateException e) {
            String message = e.getMessage() == null ? "El email ya está registrado" : e.getMessage();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", message));
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null ? "Datos inválidos" : e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", message));
        }
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios", description = "Obtiene la lista de todos los usuarios con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    public ResponseEntity<?> listarUsuarios(
            @Parameter(description = "Filtro por rol") @RequestParam(required = false) String filter,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Parámetros de paginación inválidos."));
        }

        List<UsuarioDTO> usuarios = new ArrayList<>(usuarioService.findAllUsers());
        if (filter != null && !filter.trim().isEmpty()) {
            usuarios = filtrarUsuarios(usuarios, filter);
        }

        Comparator<UsuarioDTO> comparator = obtenerComparadorUsuarios(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        usuarios.sort(comparator);

        int total = usuarios.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<UsuarioDTO> contenido = usuarios.subList(fromIndex, toIndex);

        Page<UsuarioDTO> resultado = new PageImpl<>(contenido, PageRequest.of(page, size), total);
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Editar usuario", description = "El administrador edita los datos de un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> editarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @RequestBody UsuarioDTO usuario) {
        try {
            usuario.setId(id);
            UsuarioDTO actualizado = usuarioService.updateUserProfile(id, usuario);
            return ResponseEntity.ok(actualizado);
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

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Eliminar usuario", description = "El administrador elimina un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El usuario no existe"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<?> eliminarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        try {
            usuarioService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message == null || message.toLowerCase().contains("no encontrado")) {
                message = "El usuario no existe";
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", message));
        }
    }

    // ---------------- Gestión de Anfitriones ----------------

    @PostMapping("/anfitriones")
    @Operation(summary = "Crear anfitrión", description = "El administrador crea un nuevo anfitrión en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anfitrión creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<AnfitrionDTO> crearAnfitrion(@RequestBody AnfitrionDTO anfitrion) {
        return ResponseEntity.status(201).body(null);
    }

    @GetMapping("/anfitriones")
    @Operation(summary = "Listar anfitriones", description = "Obtiene la lista de todos los anfitriones")
    @ApiResponse(responseCode = "200", description = "Lista de anfitriones obtenida exitosamente")
    public ResponseEntity<Page<AnfitrionDTO>> listarAnfitriones(
            @Parameter(description = "Filtro por estado") @RequestParam(required = false) String estado,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/anfitriones/{id}")
    @Operation(summary = "Editar anfitrión", description = "El administrador edita los datos de un anfitrión existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anfitrión actualizado"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AnfitrionDTO> editarAnfitrion(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @RequestBody AnfitrionDTO anfitrion) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/anfitriones/{id}")
    @Operation(summary = "Eliminar anfitrión", description = "El administrador elimina un anfitrión del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anfitrión eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado")
    })
    public ResponseEntity<Void> eliminarAnfitrion(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    // ---------------- Gestión de Alojamientos (TC-26) ----------------

    @GetMapping("/alojamientos")
    @Operation(summary = "Listar todos los alojamientos", description = "Obtiene la lista de todos los alojamientos del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de alojamientos obtenida exitosamente")
    public ResponseEntity<?> listarTodosAlojamientos(
            @Parameter(description = "Filtro por ciudad") @RequestParam(required = false) String ciudad,
            @Parameter(description = "Filtro por estado") @RequestParam(required = false) String estado,
            @Parameter(description = "ID del anfitrión") @RequestParam(required = false) Long anfitrionId,
            @Parameter(description = "Filtro por título") @RequestParam(required = false) String titulo,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Parámetros de paginación inválidos."));
        }

        List<AlojamientoDTO> alojamientos = new ArrayList<>(alojamientoService.findAll());

        if (ciudad != null && !ciudad.trim().isEmpty()) {
            String ciudadFiltro = ciudad.trim().toLowerCase();
            alojamientos = alojamientos.stream()
                    .filter(a -> a.getCiudad() != null && a.getCiudad().toLowerCase().contains(ciudadFiltro))
                    .collect(Collectors.toList());
        }

        if (titulo != null && !titulo.trim().isEmpty()) {
            String tituloFiltro = titulo.trim().toLowerCase();
            alojamientos = alojamientos.stream()
                    .filter(a -> a.getTitulo() != null && a.getTitulo().toLowerCase().contains(tituloFiltro))
                    .collect(Collectors.toList());
        }

        if (estado != null && !estado.trim().isEmpty()) {
            String estadoLower = estado.trim().toLowerCase();
            alojamientos = alojamientos.stream()
                    .filter(a -> {
                        if ("activo".equals(estadoLower)) {
                            return !a.isEliminado();
                        }
                        if ("inactivo".equals(estadoLower) || "eliminado".equals(estadoLower)) {
                            return a.isEliminado();
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        if (anfitrionId != null) {
            alojamientos = alojamientos.stream()
                    .filter(a -> a.getAnfitrionId() != null && a.getAnfitrionId().equals(anfitrionId))
                    .collect(Collectors.toList());
        }

        Comparator<AlojamientoDTO> comparator = obtenerComparadorAlojamientos(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        alojamientos.sort(comparator);

        int total = alojamientos.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<AlojamientoDTO> contenido = alojamientos.subList(fromIndex, toIndex);

        Page<AlojamientoDTO> resultado = new PageImpl<>(contenido, PageRequest.of(page, size), total);
        return ResponseEntity.ok(resultado);
    }

@PostMapping("/anfitriones/{id}/alojamientos")
    @Operation(summary = "Crear alojamiento", description = "El administrador crea un alojamiento para un anfitrión específico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alojamiento creado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AlojamientoDTO> crearAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @RequestBody AlojamientoDTO alojamiento) {
        return ResponseEntity.status(201).body(null);
    }

    @PutMapping("/anfitriones/{id}/alojamientos/{alojamientoId}")
    @Operation(summary = "Actualizar alojamiento", description = "El administrador actualiza los datos de un alojamiento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alojamiento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AlojamientoDTO> actualizarAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @RequestBody AlojamientoDTO alojamiento) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/anfitriones/{id}/alojamientos/{alojamientoId}")
    @Operation(summary = "Eliminar alojamiento", description = "El administrador elimina un alojamiento específico")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alojamiento eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El alojamiento no existe"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar alojamiento con reservas futuras")
    })
    public ResponseEntity<Void> eliminarAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId) {
        return ResponseEntity.noContent().build();
    }

    // ---------------- Gestión de Reservas (TC-28) ----------------

    @GetMapping("/reservas")
    @Operation(summary = "Listar y filtrar reservas", description = "El administrador visualiza y filtra reservas por diversos parámetros")
    @ApiResponse(responseCode = "200", description = "Lista de reservas filtradas obtenida exitosamente")
    public ResponseEntity<Page<ReservaDTO>> listarReservas(
            @Parameter(description = "Estado de la reserva") @RequestParam(required = false) String estado,
            @Parameter(description = "Fecha de inicio del filtro") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin del filtro") @RequestParam(required = false) LocalDate fechaFin,
            @Parameter(description = "ID del alojamiento") @RequestParam(required = false) Long alojamientoId,
            @Parameter(description = "ID del usuario") @RequestParam(required = false) Long usuarioId,
            @Parameter(description = "ID del anfitrión") @RequestParam(required = false) Long anfitrionId,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/reservas/{reservaId}")
    @Operation(summary = "Obtener detalle de reserva", description = "Obtiene los detalles completos de una reserva específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    public ResponseEntity<ReservaDTO> obtenerDetalleReserva(
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long reservaId) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Estadísticas y Reportes ----------------

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas del sistema", description = "Obtiene métricas generales de la plataforma")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "Fecha de inicio para métricas") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin para métricas") @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard administrativo", description = "Obtiene un resumen ejecutivo de la plataforma")
    @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerDashboard() {
        return ResponseEntity.ok(null);
    }

    private List<UsuarioDTO> filtrarUsuarios(List<UsuarioDTO> usuarios, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return usuarios;
        }
        String[] partes = filter.split(":", 2);
        if (partes.length == 2) {
            String clave = partes[0].trim().toLowerCase();
            String valor = partes[1].trim().toLowerCase();
            return usuarios.stream()
                    .filter(usuario -> switch (clave) {
                        case "rol" -> usuario.getRol() != null && usuario.getRol().name().equalsIgnoreCase(valor);
                        case "nombre" -> usuario.getNombre() != null && usuario.getNombre().toLowerCase().contains(valor);
                        case "email" -> usuario.getEmail() != null && usuario.getEmail().toLowerCase().contains(valor);
                        default -> true;
                    })
                    .collect(Collectors.toList());
        }

        String termino = filter.trim().toLowerCase();
        return usuarios.stream()
                .filter(usuario -> (usuario.getNombre() != null && usuario.getNombre().toLowerCase().contains(termino))
                        || (usuario.getEmail() != null && usuario.getEmail().toLowerCase().contains(termino))
                        || (usuario.getRol() != null && usuario.getRol().name().toLowerCase().contains(termino)))
                .collect(Collectors.toList());
    }

    private Comparator<UsuarioDTO> obtenerComparadorUsuarios(String sortBy) {
        String campo = sortBy == null ? "" : sortBy.trim().toLowerCase();
        return switch (campo) {
            case "nombre" -> Comparator.comparing(
                    usuario -> usuario.getNombre() != null ? usuario.getNombre().toLowerCase() : "",
                    Comparator.naturalOrder());
            case "email" -> Comparator.comparing(
                    usuario -> usuario.getEmail() != null ? usuario.getEmail().toLowerCase() : "",
                    Comparator.naturalOrder());
            case "rol" -> Comparator.comparing(
                    usuario -> usuario.getRol() != null ? usuario.getRol().name() : "",
                    String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(
                    UsuarioDTO::getId,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }

    private Comparator<AlojamientoDTO> obtenerComparadorAlojamientos(String sortBy) {
        String campo = sortBy == null ? "" : sortBy.trim().toLowerCase();
        return switch (campo) {
            case "titulo" -> Comparator.comparing(
                    alojamiento -> alojamiento.getTitulo() != null ? alojamiento.getTitulo().toLowerCase() : "",
                    Comparator.naturalOrder());
            case "ciudad" -> Comparator.comparing(
                    alojamiento -> alojamiento.getCiudad() != null ? alojamiento.getCiudad().toLowerCase() : "",
                    Comparator.naturalOrder());
            case "precio" -> Comparator.comparing(
                    alojamiento -> alojamiento.getPrecioPorNoche(),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(
                    AlojamientoDTO::getId,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }
}







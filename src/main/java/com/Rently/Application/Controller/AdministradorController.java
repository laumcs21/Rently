package com.Rently.Application.Controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/administradores")
@Tag(name = "Administradores", description = "Operaciones relacionadas con la gestión administrativa del sistema")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
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

    // ---------------- Gestión de Usuarios (TC-24) ----------------

    @PostMapping("/usuarios")
    @Operation(summary = "Crear usuario", description = "El administrador crea un nuevo usuario en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email duplicado")
    })
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioDTO usuario) {
        return ResponseEntity.status(201).body(null);
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios", description = "Obtiene la lista de todos los usuarios con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    public ResponseEntity<Page<UsuarioDTO>> listarUsuarios(
            @Parameter(description = "Filtro por rol") @RequestParam(required = false) String filter,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/usuarios/{id}")
    @Operation(summary = "Editar usuario", description = "El administrador edita los datos de un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<UsuarioDTO> editarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @RequestBody UsuarioDTO usuario) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Eliminar usuario", description = "El administrador elimina un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El usuario no existe"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Page<AlojamientoDTO>> listarTodosAlojamientos(
            @Parameter(description = "Filtro por ciudad") @RequestParam(required = false) String ciudad,
            @Parameter(description = "Filtro por estado") @RequestParam(required = false) String estado,
            @Parameter(description = "ID del anfitrión") @RequestParam(required = false) Long anfitrionId,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
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
}







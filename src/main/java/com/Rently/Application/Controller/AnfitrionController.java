package com.Rently.Application.Controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.Service.AnfitrionService;
import com.Rently.Business.Service.ComentarioService;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Persistence.Entity.EstadoReserva;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/anfitriones")
@Tag(name = "Anfitriones", description = "Operaciones relacionadas con la gestión de anfitriones y sus alojamientos")
public class AnfitrionController {

    private final AnfitrionService anfitrionService;
    private final AlojamientoService alojamientoService;
    private final ComentarioService comentarioService;
    private final ReservaService reservaService;

    public AnfitrionController(AnfitrionService anfitrionService,
                               AlojamientoService alojamientoService,
                               ComentarioService comentarioService,
                               ReservaService reservaService) {
        this.anfitrionService = anfitrionService;
        this.alojamientoService = alojamientoService;
        this.comentarioService = comentarioService;
        this.reservaService = reservaService;
    }

    // ---------------- CRUD de Anfitriones ----------------

    @PostMapping
    @Operation(summary = "Crear anfitrión", description = "Registra un nuevo anfitrión en la plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anfitrión creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    public ResponseEntity<AnfitrionDTO> crearAnfitrion(@RequestBody AnfitrionDTO anfitrion) {
        AnfitrionDTO anfitrionCreado = anfitrionService.create(anfitrion);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(anfitrionCreado.getId())
                .toUri();
        return ResponseEntity.created(location).body(anfitrionCreado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener anfitrión por ID", description = "Obtiene la información de un anfitrión específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anfitrión encontrado"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado")
    })
    public ResponseEntity<AnfitrionDTO> obtenerAnfitrion(
            @Parameter(description = "ID del anfitr�n", example = "1") @PathVariable Long id) {
        return anfitrionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar anfitrión", description = "Modifica la información de un anfitrión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anfitrión actualizado"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> actualizarAnfitrion(
            @Parameter(description = "ID del anfitri�n", example = "1") @PathVariable Long id,
            @RequestBody AnfitrionDTO anfitrion) {
        try {
            anfitrion.setId(id);
            AnfitrionDTO actualizado = anfitrionService.update(id, anfitrion)
                    .orElseThrow(() -> new RuntimeException("Anfitrión no encontrado"));
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo actualizar el anfitri�n" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar anfitrión", description = "Elimina un anfitrión de la plataforma (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anfitrión eliminado"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar anfitrión con reservas activas")
    })
    public ResponseEntity<Void> eliminarAnfitrion(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Dashboard del anfitrión", description = "Obtiene un resumen de actividad y métricas generales")
    @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerDashboard(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "Fecha de inicio para métricas") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin para métricas") @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Gestión de Alojamientos ----------------

    @GetMapping("/{id}/alojamientos")
    @Operation(summary = "Listar alojamientos del anfitrión", description = "Obtiene todos los alojamientos registrados por un anfitrión con paginación")
    @ApiResponse(responseCode = "200", description = "Lista de alojamientos obtenida exitosamente")
    public ResponseEntity<Page<AlojamientoDTO>> listarAlojamientos(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "Estado del alojamiento") @RequestParam(required = false) String estado,
            @Parameter(description = "Ciudad") @RequestParam(required = false) String ciudad,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{id}/alojamientos")
    @Operation(summary = "Crear alojamiento", description = "Registra un nuevo alojamiento para un anfitrión")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alojamiento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Anfitrión no encontrado")
    })
    public ResponseEntity<?> crearAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @RequestBody AlojamientoDTO alojamiento) {
        return anfitrionService.findById(id)
                .map(host -> {
                    try {
                        alojamiento.setAnfitrionId(id);
                        AlojamientoDTO creado = alojamientoService.create(alojamiento);
                        URI location = ServletUriComponentsBuilder
                                .fromCurrentRequest()
                                .path("/{alojamientoId}")
                                .buildAndExpand(creado.getId())
                                .toUri();
                        return ResponseEntity.created(location).body(creado);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Anfitrión no encontrado")));
    }

    @GetMapping("/{id}/alojamientos/{alojamientoId}")
    @Operation(summary = "Obtener alojamiento específico", description = "Obtiene los detalles de un alojamiento específico del anfitrión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alojamiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado o no pertenece al anfitrión")
    })
    public ResponseEntity<AlojamientoDTO> obtenerAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}/alojamientos/{alojamientoId}")
    @Operation(summary = "Actualizar alojamiento", description = "Modifica los datos de un alojamiento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alojamiento actualizado"),
            @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<AlojamientoDTO> actualizarAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @RequestBody AlojamientoDTO alojamiento) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}/alojamientos/{alojamientoId}")
    @Operation(summary = "Eliminar alojamiento", description = "Elimina un alojamiento específico (soft delete). Solo si no tiene reservas futuras")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alojamiento eliminado"),
            @ApiResponse(responseCode = "404", description = "Alojamiento no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar alojamiento con reservas futuras")
    })
    public ResponseEntity<?> eliminarAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId) {
        if (anfitrionService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anfitrión no encontrado"));
        }

        Optional<AlojamientoDTO> alojamiento = alojamientoService.findById(alojamientoId);
        if (alojamiento.isEmpty() || !id.equals(alojamiento.get().getAnfitrionId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Alojamiento no encontrado"));
        }

        try {
            alojamientoService.delete(alojamientoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/alojamientos/{alojamientoId}/disponibilidad")
    @Operation(summary = "Ver disponibilidad", description = "Obtiene el calendario de disponibilidad de un alojamiento")
    @ApiResponse(responseCode = "200", description = "Calendario obtenido exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerDisponibilidad(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @Parameter(description = "Fecha de inicio") @RequestParam LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{id}/alojamientos/{alojamientoId}/bloquear-fechas")
    @Operation(summary = "Bloquear fechas", description = "Bloquea fechas específicas para que no estén disponibles para reserva")
    @ApiResponse(responseCode = "200", description = "Fechas bloqueadas exitosamente")
    public ResponseEntity<Map<String, Object>> bloquearFechas(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @RequestBody Map<String, Object> bloquearRequest) {
        return ResponseEntity.ok(Map.of("mensaje", "Fechas bloqueadas exitosamente"));
    }

    // ---------------- Métricas y Estadísticas ----------------

    @GetMapping("/{id}/alojamientos/{alojamientoId}/metricas")
    @Operation(summary = "Obtener métricas de alojamiento", description = "Obtiene métricas básicas: número de reservas, promedio de calificaciones, ingresos")
    @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente")
    public ResponseEntity<Map<String, Object>> obtenerMetricas(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @Parameter(description = "Fecha de inicio para filtrar métricas") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin para filtrar métricas") @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}/metricas-generales")
    @Operation(summary = "Métricas generales del anfitrión", description = "Obtiene métricas consolidadas de todos los alojamientos del anfitrión")
    @ApiResponse(responseCode = "200", description = "Métricas generales obtenidas")
    public ResponseEntity<Map<String, Object>> obtenerMetricasGenerales(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "Fecha de inicio") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin") @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Gestión de Reservas ----------------

    

    @GetMapping("/{id}/reservas")
    @Operation(summary = "Listar reservas", description = "Obtiene todas las reservas realizadas en los alojamientos de un anfitrión con filtros")
    @ApiResponse(responseCode = "200", description = "Lista de reservas obtenida exitosamente")
    public ResponseEntity<?> listarReservas(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "Estado de la reserva") @RequestParam(required = false) String estado,
            @Parameter(description = "Fecha de inicio del filtro") @RequestParam(required = false) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin del filtro") @RequestParam(required = false) LocalDate fechaFin,
            @Parameter(description = "ID del alojamiento") @RequestParam(required = false) Long alojamientoId,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenar por", example = "fechaCreacion") @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @Parameter(description = "Dirección de orden", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Parámetros de paginación inválidos."));
        }

        Optional<AnfitrionDTO> anfitrionOpt = anfitrionService.findById(id);
        if (anfitrionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anfitrión no encontrado"));
        }

        List<AlojamientoDTO> alojamientos = alojamientoService.findByHost(id);
        if (alojamientoId != null) {
            boolean pertenece = alojamientos.stream()
                    .anyMatch(a -> a.getId().equals(alojamientoId));
            if (!pertenece) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El alojamiento no pertenece al anfitrión."));
            }
            alojamientos = alojamientos.stream()
                    .filter(a -> a.getId().equals(alojamientoId))
                    .collect(Collectors.toList());
        }

        List<ReservaDTO> reservas = alojamientos.stream()
                .flatMap(aloj -> reservaService.findByAlojamientoId(aloj.getId()).stream())
                .collect(Collectors.toList());

        if (estado != null && !estado.trim().isEmpty()) {
            EstadoReserva estadoFiltro;
            try {
                estadoFiltro = EstadoReserva.valueOf(estado.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Estado de reserva inválido."));
            }
            reservas = reservas.stream()
                    .filter(r -> estadoFiltro.equals(r.getEstado()))
                    .collect(Collectors.toList());
        }

        if (fechaInicio != null) {
            reservas = reservas.stream()
                    .filter(r -> r.getFechaInicio() != null && !r.getFechaInicio().isBefore(fechaInicio))
                    .collect(Collectors.toList());
        }

        if (fechaFin != null) {
            reservas = reservas.stream()
                    .filter(r -> r.getFechaFin() != null && !r.getFechaFin().isAfter(fechaFin))
                    .collect(Collectors.toList());
        }

        Comparator<ReservaDTO> comparator;
        String sortField = sortBy == null ? "" : sortBy.trim().toLowerCase();
        switch (sortField) {
            case "fechafin" -> comparator = Comparator.comparing(
                    ReservaDTO::getFechaFin,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "estado" -> comparator = Comparator.comparing(
                    r -> r.getEstado() != null ? r.getEstado().name() : "",
                    String.CASE_INSENSITIVE_ORDER
            );
            case "alojamientoid" -> comparator = Comparator.comparing(
                    ReservaDTO::getAlojamientoId,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "usuarioid" -> comparator = Comparator.comparing(
                    ReservaDTO::getUsuarioId,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> comparator = Comparator.comparing(
                    ReservaDTO::getFechaInicio,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        reservas.sort(comparator);

        int total = reservas.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ReservaDTO> contenido = reservas.subList(fromIndex, toIndex);

        Page<ReservaDTO> resultado = new PageImpl<>(contenido, PageRequest.of(page, size), total);
        return ResponseEntity.ok(resultado);
    }


    @GetMapping("/{id}/reservas/{reservaId}")
    @Operation(summary = "Obtener detalle de reserva", description = "Obtiene los detalles de una reserva específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada o no pertenece al anfitrión")
    })
    public ResponseEntity<ReservaDTO> obtenerReserva(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long reservaId) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{id}/reservas/{reservaId}/aprobar")
    @Operation(summary = "Aprobar reserva", description = "El anfitrión aprueba una reserva pendiente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva aprobada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "400", description = "La reserva no está en estado pendiente")
    })
    public ResponseEntity<Map<String, Object>> aprobarReserva(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long reservaId) {
        if (anfitrionService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anfitrión no encontrado."));
        }

        Optional<ReservaDTO> reservaOpt = reservaService.findById(reservaId);
        if (reservaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reserva no encontrada."));
        }

        ReservaDTO reserva = reservaOpt.get();

        Optional<AlojamientoDTO> alojamientoOpt = alojamientoService.findById(reserva.getAlojamientoId());
        if (alojamientoOpt.isEmpty() || !id.equals(alojamientoOpt.get().getAnfitrionId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reserva no encontrada."));
        }

        if (reserva.getEstado() == null || reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Solo se pueden aprobar reservas en estado pendiente."));
        }

        try {
            boolean updated = reservaService.updateState(reservaId, EstadoReserva.CONFIRMADA);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se pudo actualizar el estado de la reserva."));
            }
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo actualizar el estado de la reserva." : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }

        ReservaDTO actualizada = reservaService.findById(reservaId)
                .orElse(reserva);
        Long respuestaId = actualizada.getId() != null ? actualizada.getId() : reservaId;
        EstadoReserva estadoFinal = actualizada.getEstado() != null ? actualizada.getEstado() : EstadoReserva.CONFIRMADA;

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", respuestaId);
        respuesta.put("estado", estadoFinal.name());

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}/reservas/{reservaId}/rechazar")
    @Operation(summary = "Rechazar reserva", description = "El anfitrión rechaza una reserva pendiente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva rechazada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada"),
            @ApiResponse(responseCode = "400", description = "La reserva no está en estado pendiente")
    })
    public ResponseEntity<Map<String, Object>> rechazarReserva(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la reserva", example = "5") @PathVariable Long reservaId,
            @RequestBody Map<String, Object> rechazoRequest) {
        if (anfitrionService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anfitrión no encontrado."));
        }

        Optional<ReservaDTO> reservaOpt = reservaService.findById(reservaId);
        if (reservaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reserva no encontrada."));
        }

        ReservaDTO reserva = reservaOpt.get();

        Optional<AlojamientoDTO> alojamientoOpt = alojamientoService.findById(reserva.getAlojamientoId());
        if (alojamientoOpt.isEmpty() || !id.equals(alojamientoOpt.get().getAnfitrionId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reserva no encontrada."));
        }

        if (reserva.getEstado() == null || reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Solo se pueden rechazar reservas en estado pendiente."));
        }

        Object motivoObj = rechazoRequest != null ? rechazoRequest.get("motivo") : null;
        if (motivoObj == null || motivoObj.toString().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El motivo de rechazo es obligatorio."));
        }
        String motivo = motivoObj.toString().trim();

        try {
            boolean updated = reservaService.updateState(reservaId, EstadoReserva.RECHAZADA);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se pudo actualizar el estado de la reserva."));
            }
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo actualizar el estado de la reserva." : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }

        ReservaDTO actualizada = reservaService.findById(reservaId)
                .orElse(reserva);
        Long respuestaId = actualizada.getId() != null ? actualizada.getId() : reservaId;
        EstadoReserva estadoFinal = actualizada.getEstado() != null ? actualizada.getEstado() : EstadoReserva.RECHAZADA;

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id", respuestaId);
        respuesta.put("estado", estadoFinal.name());
        respuesta.put("motivo", motivo);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}/reservas/pendientes")
    @Operation(summary = "Reservas pendientes", description = "Obtiene todas las reservas que requieren confirmación del anfitrión")
    @ApiResponse(responseCode = "200", description = "Reservas pendientes obtenidas")
    public ResponseEntity<Page<ReservaDTO>> obtenerReservasPendientes(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Gestión de Comentarios ----------------

    @GetMapping("/{id}/comentarios")
    @Operation(summary = "Listar comentarios", description = "Obtiene todos los comentarios de los alojamientos del anfitrión")
    @ApiResponse(responseCode = "200", description = "Comentarios obtenidos exitosamente")
    public ResponseEntity<Page<ComentarioDTO>> listarComentarios(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento") @RequestParam(required = false) Long alojamientoId,
            @Parameter(description = "Solo comentarios sin respuesta") @RequestParam(defaultValue = "false") boolean sinRespuesta,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}/alojamientos/{alojamientoId}/comentarios")
    @Operation(summary = "Comentarios de alojamiento", description = "Obtiene todos los comentarios de un alojamiento específico")
    @ApiResponse(responseCode = "200", description = "Comentarios del alojamiento obtenidos")
    public ResponseEntity<Page<ComentarioDTO>> listarComentariosAlojamiento(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del alojamiento", example = "10") @PathVariable Long alojamientoId,
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{id}/comentarios/{comentarioId}/responder")
    @Operation(summary = "Responder comentario", description = "Permite al anfitrión responder un comentario en uno de sus alojamientos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Respuesta enviada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comentario no encontrado"),
            @ApiResponse(responseCode = "400", description = "El comentario ya tiene respuesta")
    })
    public ResponseEntity<?> responderComentario(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del comentario", example = "5") @PathVariable Long comentarioId,
            @RequestBody Map<String, Object> respuestaRequest,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token de autenticación requerido."));
        }

        Optional<AnfitrionDTO> anfitrionOpt = anfitrionService.findById(id);
        if (anfitrionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Anfitrión no encontrado."));
        }

        Object respuestaObj = respuestaRequest != null ? respuestaRequest.get("respuesta") : null;
        if (respuestaObj == null || respuestaObj.toString().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La respuesta no puede estar vac�a."));
        }

        String respuesta = respuestaObj.toString().trim();

        try {
            ComentarioDTO actualizado = comentarioService.addResponse(comentarioId, respuesta, authorizationHeader);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().toLowerCase().contains("no encontrado")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.CONFLICT;
            return ResponseEntity.status(status)
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? "No se pudo responder el comentario" : e.getMessage();
            HttpStatus status = message.toLowerCase().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("error", message));
        }
    }

    @PutMapping("/{id}/comentarios/{comentarioId}/respuesta")
    @Operation(summary = "Editar respuesta", description = "Permite editar una respuesta existente a un comentario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Respuesta actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Comentario o respuesta no encontrados")
    })
    public ResponseEntity<ComentarioDTO> editarRespuestaComentario(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del comentario", example = "5") @PathVariable Long comentarioId,
            @RequestBody Map<String, Object> respuestaRequest) {
        return ResponseEntity.ok(null);
    }

    // ---------------- Notificaciones ----------------

    @GetMapping("/{id}/notificaciones")
    @Operation(summary = "Obtener notificaciones", description = "Obtiene las notificaciones del anfitrión")
    @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas")
    public ResponseEntity<Page<Map<String, Object>>> obtenerNotificaciones(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
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
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id,
            @Parameter(description = "ID de la notificación", example = "3") @PathVariable Long notificacionId) {
        return ResponseEntity.ok(Map.of("mensaje", "Notificación marcada como leída"));
    }

    @PutMapping("/{id}/notificaciones/marcar-todas-leidas")
    @Operation(summary = "Marcar todas como leídas", description = "Marca todas las notificaciones del anfitrión como leídas")
    @ApiResponse(responseCode = "200", description = "Todas las notificaciones marcadas como leídas")
    public ResponseEntity<Map<String, Object>> marcarTodasNotificacionesLeidas(
            @Parameter(description = "ID del anfitrión", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones marcadas como leídas"));
    }
}






package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Persistence.DAO.*;
import com.Rently.Persistence.Entity.*;
import com.Rently.Persistence.Mapper.PersonaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de reservas usando autenticación JWT.
 */
@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private AnfitrionDAO anfitrionDAO;

    @Autowired
    private AdministradorDAO administradorDAO;

    @Autowired
    private PersonaMapper personaMapper;

    @Autowired
    private AlojamientoDAO alojamientoDAO;

    // ================= METODOS AUXILIARES =================

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Debe iniciar sesión para realizar esta acción.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else {
            return principal.toString();
        }
    }

    private Persona getPersonaActual() {
        String email = getCurrentUsername();
        System.out.println("🔍 Email del SecurityContext: " + email);

        Optional<UsuarioDTO> usuarioOpt = usuarioDAO.buscarPorEmail(email);
        if (usuarioOpt.isPresent()) {
            return personaMapper.dtoToUsuario(usuarioOpt.get());
        }

        Optional<AnfitrionDTO> anfitrionOpt = anfitrionDAO.buscarPorEmail(email);
        if (anfitrionOpt.isPresent()) {
            return personaMapper.dtoToAnfitrion(anfitrionOpt.get());
        }

        Optional<AdministradorDTO> adminOpt = administradorDAO.buscarPorEmail(email);
        if (adminOpt.isPresent()) {
            return personaMapper.dtoToAdmin(adminOpt.get());
        }

        System.out.println("❌ NO SE ENCONTRÓ LA PERSONA EN LA BD");
        throw new RuntimeException("Persona autenticada no encontrada en la BD");
    }

    private boolean isAdminAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    // ================= CRUD RESERVAS =================

    @Override
    public ReservaDTO create(ReservaDTO reservaDTO) {
        validateReservaData(reservaDTO);

        var usuarioOpt = usuarioDAO.buscarPorId(reservaDTO.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("El usuario con ID " + reservaDTO.getUsuarioId() + " no existe.");
        }
        if (!usuarioOpt.get().isActivo()) {
            throw new RuntimeException("El usuario no está activo y no puede reservar.");
        }

        var alojamientoOpt = alojamientoDAO.buscarPorId(reservaDTO.getAlojamientoId());
        if (alojamientoOpt.isEmpty()) {
            throw new RuntimeException("El alojamiento con ID " + reservaDTO.getAlojamientoId() + " no existe.");
        }
        if (alojamientoOpt.get().isEliminado()) {
            throw new RuntimeException("El alojamiento no está disponible para reservas.");
        }

        Persona actorActual = getPersonaActual();

        // Validar permisos: Usuario solo puede crear para sí mismo
        if (actorActual instanceof Usuario usuarioActual) {
            if (!usuarioActual.getId().equals(reservaDTO.getUsuarioId())) {
                throw new RuntimeException("No puede crear reservas en nombre de otro usuario.");
            }
        }
        // Admin y anfitrion pueden crear reservas para cualquier usuario

        List<ReservaDTO> reservas = reservaDAO.listarPorAlojamiento(reservaDTO.getAlojamientoId());
        boolean solapada = reservas.stream().anyMatch(r ->
                fechasSeSolapan(reservaDTO.getFechaInicio(), reservaDTO.getFechaFin(),
                        r.getFechaInicio(), r.getFechaFin())
                        && r.getEstado() != EstadoReserva.CANCELADA
                        && r.getEstado() != EstadoReserva.RECHAZADA
        );
        if (solapada) {
            throw new RuntimeException("Ya existe una reserva activa en ese rango de fechas.");
        }

        reservaDTO.setEstado(EstadoReserva.PENDIENTE);
        return reservaDAO.crear(reservaDTO);
    }

    @Override
    public Optional<ReservaDTO> findById(Long id) {
        validateId(id);
        return reservaDAO.buscarPorId(id);
    }

    @Override
    public List<ReservaDTO> findAll() {
        return reservaDAO.listarTodas();
    }

    @Override
    public List<ReservaDTO> findByUserId(Long userId) {
        validateId(userId);
        return reservaDAO.listarPorUsuario(userId);
    }

    @Override
    public List<ReservaDTO> findByAlojamientoId(Long alojamientoId) {
        validateId(alojamientoId);
        return reservaDAO.listarPorAlojamiento(alojamientoId);
    }

    @Override
    public ReservaDTO cancelByUser(Long id) {
        validateId(id);

        ReservaDTO reserva = reservaDAO.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("La reserva con ID " + id + " no existe."));

        Persona personaActual = getPersonaActual();

        boolean esAdmin = personaActual instanceof Usuario u && u.getRol() == Rol.ADMINISTRADOR
                || personaActual instanceof Administrador;
        boolean esPropietario = personaActual instanceof Usuario u && u.getId().equals(reserva.getUsuarioId());

        if (!esAdmin && !esPropietario) {
            throw new RuntimeException("No puede cancelar una reserva que no le pertenece.");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            return reserva;
        }

        if (reserva.getEstado() == EstadoReserva.FINALIZADA || reserva.getEstado() == EstadoReserva.RECHAZADA) {
            throw new IllegalStateException("No es posible cancelar una reserva en el estado actual.");
        }

        LocalDate fechaInicio = reserva.getFechaInicio();
        if (fechaInicio == null) {
            throw new IllegalStateException("La reserva no tiene una fecha de inicio definida.");
        }

        long horasHastaInicio = ChronoUnit.HOURS.between(LocalDateTime.now(), fechaInicio.atStartOfDay());
        if (horasHastaInicio < 48) {
            throw new IllegalStateException("No se pueden cancelar reservas con menos de 48 horas de anticipación");
        }

        boolean actualizada = reservaDAO.cambiarEstado(id, EstadoReserva.CANCELADA);
        if (!actualizada) {
            throw new RuntimeException("No se pudo cancelar la reserva.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reserva;
    }

    @Override
    public Optional<ReservaDTO> update(Long id, ReservaDTO reservaDTO) {
        Persona personaActual = getPersonaActual();

        validateId(id);
        validateReservaData(reservaDTO);

        Optional<ReservaDTO> existente = reservaDAO.buscarPorId(id);
        if (existente.isEmpty()) {
            throw new RuntimeException("La reserva con ID " + id + " no existe.");
        }

        if (existente.get().getEstado() == EstadoReserva.CONFIRMADA
                || existente.get().getEstado() == EstadoReserva.RECHAZADA) {
            throw new RuntimeException("No se puede modificar una reserva confirmada o rechazada.");
        }

        boolean esAdmin = personaActual instanceof Usuario u && u.getRol() == Rol.ADMINISTRADOR
                || personaActual instanceof Administrador;

        boolean esPropietario = personaActual instanceof Usuario u && u.getId().equals(reservaDTO.getUsuarioId());

        if (!esAdmin && !esPropietario) {
            throw new RuntimeException("No tiene permisos para modificar esta reserva.");
        }

        return reservaDAO.actualizar(id, reservaDTO);
    }

    @Override
    public boolean updateState(Long id, EstadoReserva newState) {
        Persona personaActual = getPersonaActual();

        validateId(id);
        if (newState == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }

        ReservaDTO reserva = reservaDAO.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("La reserva con ID " + id + " no existe."));

        boolean esAdmin = personaActual instanceof Usuario u && u.getRol() == Rol.ADMINISTRADOR
                || personaActual instanceof Administrador;

        boolean esAnfitrion = personaActual instanceof Anfitrion;

        if ((newState == EstadoReserva.CONFIRMADA || newState == EstadoReserva.RECHAZADA)
                && !(esAdmin || esAnfitrion)) {
            throw new RuntimeException("Solo un administrador o anfitrión puede confirmar o rechazar reservas.");
        }

        boolean esPropietario = personaActual instanceof Usuario u && u.getId().equals(reserva.getUsuarioId());
        if (newState == EstadoReserva.CANCELADA && !(esAdmin || esPropietario)) {
            throw new RuntimeException("No puede cancelar una reserva que no le pertenece.");
        }

        return reservaDAO.cambiarEstado(id, newState);
    }

    @Override
    public boolean delete(Long id) {
        Persona personaActual = getPersonaActual();
        validateId(id);

        Optional<ReservaDTO> reservaOpt = reservaDAO.buscarPorId(id);
        if (reservaOpt.isEmpty()) {
            throw new RuntimeException("Reserva con ID " + id + " no encontrada.");
        }

        boolean esAdmin = personaActual instanceof Usuario u && u.getRol() == Rol.ADMINISTRADOR
                || personaActual instanceof Administrador;

        if (!esAdmin) {
            throw new RuntimeException("No tiene permisos para eliminar reservas. Solo puede cancelarlas.");
        }

        return reservaDAO.eliminar(id);
    }

    // ================= VALIDACIONES AUXILIARES =================

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido.");
        }
    }

    private void validateReservaData(ReservaDTO reservaDTO) {
        if (reservaDTO == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
        if (reservaDTO.getUsuarioId() == null || reservaDTO.getUsuarioId() <= 0) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (reservaDTO.getAlojamientoId() == null || reservaDTO.getAlojamientoId() <= 0) {
            throw new IllegalArgumentException("El alojamiento es obligatorio.");
        }
        if (reservaDTO.getFechaInicio() == null || reservaDTO.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (reservaDTO.getFechaFin().isBefore(reservaDTO.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    private boolean fechasSeSolapan(LocalDate inicio1, LocalDate fin1,
                                    LocalDate inicio2, LocalDate fin2) {
        return !fin1.isBefore(inicio2) && !fin2.isBefore(inicio1);
    }
}



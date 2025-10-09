package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.AnfitrionService;
import com.Rently.Persistence.DAO.AnfitrionDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementación del servicio para la gestión de anfitriones.
 * Contiene validaciones de negocio antes de invocar al DAO.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AnfitrionServiceImpl implements AnfitrionService {

    private final AnfitrionDAO anfitrionDAO;

    // Regex simple para validar emails
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * CREATE - Crear nuevo anfitrión
     */
    @Override
    public AnfitrionDTO create(AnfitrionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de creación no puede ser nulo");
        }

        log.info("Creando anfitrión: {}", dto.getEmail());

        validateAnfitrionData(dto);
        validarFechaNacimientoObligatoriaYMayorDeEdad(dto.getFechaNacimiento());

        return anfitrionDAO.crearAnfitrion(dto);
    }

    /**
     * READ - Buscar anfitrión por ID
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AnfitrionDTO> findById(Long id) {
        log.debug("Buscando anfitrión ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido y mayor que 0");
        }

        return anfitrionDAO.buscarPorId(id);
    }

    /**
     * READ - Buscar anfitrión por email
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AnfitrionDTO> findByEmail(String email) {
        log.debug("Buscando anfitrión por email: {}", email);

        if (email == null || email.trim().isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email proporcionado no es válido");
        }

        return anfitrionDAO.buscarPorEmail(email);
    }

    /**
     * READ - Buscar anfitriones por nombre
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnfitrionDTO> findByName(String name) {
        log.debug("Buscando anfitriones por nombre: {}", name);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return anfitrionDAO.buscarPorNombre(name);
    }

    /**
     * READ ALL - Listar todos
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnfitrionDTO> findAll() {
        log.debug("Listando todos los anfitriones");
        return anfitrionDAO.listarTodos();
    }

    // En AnfitrionServiceImpl
    @Override
    public Optional<AnfitrionDTO> update(Long id, AnfitrionDTO dto) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido");
        if (dto == null) throw new IllegalArgumentException("El DTO de actualización no puede ser nulo");

        // ✅ Validaciones de campos OPCIONALES: si vienen, se validan; si no, se omiten.
        if (dto.getNombre() != null && dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (dto.getEmail() != null && !dto.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        if (dto.getTelefono() != null && !dto.getTelefono().matches("^\\d{7,15}$")) {
            throw new IllegalArgumentException("El teléfono es obligatorio y debe contener entre 7 y 15 dígitos numéricos");
        }
        if (dto.getFechaNacimiento() != null) {
            LocalDate hoy = LocalDate.now();
            if (dto.getFechaNacimiento().isAfter(hoy)) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
            }
            if (dto.getFechaNacimiento().plusYears(18).isAfter(hoy)) {
                throw new IllegalArgumentException("El anfitrión debe ser mayor de edad (>= 18)");
            }
        }

        // ✅ Pre-check de existencia (RuntimeException si no existe)
        anfitrionDAO.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Anfitrión con ID " + id + " no existe"));

        // Actualizar
        return anfitrionDAO.actualizarAnfitrion(id, dto)
                .map(Optional::of)
                .orElseThrow(() -> new RuntimeException("Anfitrión con ID " + id + " no existe"));
    }


    @Override
    public boolean delete(Long id) {
        log.info("Eliminando anfitrión ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido y mayor que 0");
        }

        boolean eliminado = anfitrionDAO.eliminarAnfitrion(id);

        if (!eliminado) {
            throw new RuntimeException("Anfitrión con ID " + id + " no existe");
        }

        return true;
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================

    private void validateAnfitrionData(AnfitrionDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dto.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }

        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new IllegalArgumentException("El formato de email no es válido");
        }

        if (dto.getTelefono() != null && !dto.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos numéricos");
        }

        if (dto.getFechaNacimiento() != null && dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede estar en el futuro");
        }
    }

    private void validateAnfitrionUpdateData(AnfitrionDTO dto) {
        if (dto.getNombre() != null) {
            if (dto.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if (dto.getNombre().length() > 100) {
                throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
            }
        }

        if (dto.getEmail() != null && !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new IllegalArgumentException("El formato de email no es válido");
        }

        if (dto.getTelefono() != null && !dto.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos numéricos");
        }

        if (dto.getFechaNacimiento() != null && dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede estar en el futuro");
        }
    }

    private void validarFechaNacimientoObligatoriaYMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 18) {
            throw new IllegalArgumentException("El anfitrión debe ser mayor de edad (>= 18)");
        }
    }
}

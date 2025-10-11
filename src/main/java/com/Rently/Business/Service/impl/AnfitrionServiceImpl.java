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
    public AnfitrionDTO create(AnfitrionDTO anfitrionDTO) {
        log.info("Creando anfitrión: {}", anfitrionDTO != null ? anfitrionDTO.getEmail() : "null");

        if (anfitrionDTO == null) {
            throw new IllegalArgumentException("El DTO no puede ser nulo");
        }

        validateAnfitrionData(anfitrionDTO);
        validateAge(anfitrionDTO);

        return anfitrionDAO.crearAnfitrion(anfitrionDTO);
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

    /**
     * UPDATE - Actualizar anfitrión existente
     */
    @Override
    public Optional<AnfitrionDTO> update(Long id, AnfitrionDTO anfitrionDTO) {
        log.info("Actualizando anfitrión ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido y mayor que 0");
        }
        if (anfitrionDTO == null) {
            throw new IllegalArgumentException("El DTO no puede ser nulo");
        }

        // 🔹 Primero validar todo (edad incluida)
        validateAnfitrionUpdateData(anfitrionDTO);

        // 🔹 Si pasó validación (no es menor de edad), recién toca el DAO
        anfitrionDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Anfitrión con ID " + id + " no encontrado"));

        Optional<AnfitrionDTO> actualizado = anfitrionDAO.actualizarAnfitrion(id, anfitrionDTO);

        if (actualizado.isEmpty()) {
            throw new IllegalArgumentException("Anfitrión con ID " + id + " no existe");
        }

        return actualizado;
    }


    /**
     * DELETE - Eliminar anfitrión
     */
    @Override
    public boolean delete(Long id) {
        log.info("Eliminando anfitrión ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido y mayor que 0");
        }

        boolean eliminado = anfitrionDAO.eliminarAnfitrion(id);

        if (!eliminado) {
            throw new IllegalArgumentException("Anfitrión con ID " + id + " no existe");
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
        validateAge(dto);
    }

    private void validateAge(AnfitrionDTO dto) {
        if (dto.getFechaNacimiento() == null) {
            return;
        }

        int edad = Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new IllegalArgumentException("El anfitrión debe ser mayor de edad");
        }
    }
}


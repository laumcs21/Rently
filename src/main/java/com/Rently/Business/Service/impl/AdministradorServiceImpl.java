package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.Exception.InvalidRequestException;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Persistence.DAO.AdministradorDAO;
import com.Rently.Persistence.Entity.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorDAO administradorDAO;
    private final PasswordEncoder passwordEncoder;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";


    @Override
    public AdministradorDTO create(AdministradorDTO administradorDTO) {
        log.info("Creando administrador: {}", administradorDTO);

        validateAdminData(administradorDTO);

        administradorDAO.buscarPorEmail(administradorDTO.getEmail()).ifPresent(existing -> {
            throw new InvalidRequestException("Solicitud incorrecta");
        });

        String contrasenaCodificada = passwordEncoder.encode(administradorDTO.getContrasena());

        return administradorDAO.crearAdministrador(administradorDTO, contrasenaCodificada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministradorDTO> findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return administradorDAO.buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministradorDTO> findByEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        return administradorDAO.buscarPorEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministradorDTO> findAll() {
        return administradorDAO.listarTodos();
    }

    @Override
    public Optional<AdministradorDTO> update(Long id, AdministradorDTO administradorDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        validateAdminUpdateData(administradorDTO);

        Optional<AdministradorDTO> updated = administradorDAO.actualizarAdministrador(id, administradorDTO);

        if (updated.isEmpty()) {
            throw new RuntimeException("Administrador con ID " + id + " no encontrado");
        }

        return updated;
    }


    @Override
    public boolean delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        boolean eliminado = administradorDAO.eliminarAdministrador(id);
        if (!eliminado) {
            throw new RuntimeException("Administrador con ID " + id + " no encontrado");
        }
        return eliminado;
    }


    // ================== VALIDACIONES ==================

    private void validateAdminData(AdministradorDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }

        // Nombre obligatorio
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        // Email válido
        if (dto.getEmail() == null || !dto.getEmail().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("El email no es válido");
        }

        if (dto.getContrasena() == null || dto.getContrasena().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (dto.getEmail() == null || !dto.getEmail().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("El email no es válido");
        }

        // Teléfono numérico y de longitud mínima
        if (dto.getTelefono() == null || !dto.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe ser numérico y contener entre 7 y 15 dígitos");
        }

        // Fecha de nacimiento válida y mayor de edad
        if (dto.getFechaNacimiento() == null ||
                Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El administrador debe ser mayor de edad");
        }

        // Rol obligatorio y debe ser ADMIN
        if (dto.getRol() == null || !dto.getRol().equals(Rol.ADMINISTRADOR)) {
            throw new IllegalArgumentException("El rol debe ser ADMINISTRADOR");
        }
    }

    private void validateAdminUpdateData(AdministradorDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El administrador no puede ser nulo");
        }

        // Validaciones opcionales (solo si los campos no son nulos)
        if (dto.getNombre() != null && dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        if (dto.getEmail() != null &&
                !dto.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("El email no es válido");
        }

        if (dto.getTelefono() != null && !dto.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe ser numérico y contener entre 7 y 15 dígitos");
        }

        if (dto.getFechaNacimiento() != null &&
                Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El administrador debe ser mayor de edad");
        }

        if (dto.getRol() != null && !dto.getRol().equals(Rol.ADMINISTRADOR)) {
            throw new IllegalArgumentException("El rol debe ser ADMINISTRADOR");
        }
    }
}

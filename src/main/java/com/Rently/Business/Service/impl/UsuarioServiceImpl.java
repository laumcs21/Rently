package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Persistence.Entity.Usuario;
import com.Rently.Persistence.Mapper.PersonaMapper;
import com.Rently.Persistence.Repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaMapper personaMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PersonaMapper personaMapper,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaMapper = personaMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO registerUser(UsuarioDTO usuarioDTO) {
        log.info("Registrando usuario con email {}", usuarioDTO.getEmail());

        // Validaciones de datos de negocio
        validateUserData(usuarioDTO);

        // Verificar si el email ya existe
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            log.warn("Intento de registro con email duplicado: {}", usuarioDTO.getEmail());
            throw new IllegalStateException("El email " + usuarioDTO.getEmail() + " ya está en uso.");
        }

        // Mapeo DTO → Entidad
        Usuario usuario = personaMapper.dtoToUsuario(usuarioDTO);

        // Encriptar contraseña antes de guardar
        usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));

        // Guardar en BD
        Usuario savedUsuario = usuarioRepository.save(usuario);

        log.info("Usuario registrado con éxito. ID: {}", savedUsuario.getId());
        return personaMapper.usuarioToDTO(savedUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findUserById(Long id) {
        log.debug("Buscando usuario por ID {}", id);
        return usuarioRepository.findById(id)
                .map(personaMapper::usuarioToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findUserByEmail(String email) {
        log.debug("Buscando usuario por email {}", email);
        return usuarioRepository.findByEmail(email)
                .map(personaMapper::usuarioToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAllUsers() {
        log.debug("Obteniendo todos los usuarios");
        return personaMapper.usuariosToDTO(usuarioRepository.findAll());
    }

    @Transactional
    @Override
    public UsuarioDTO updateUserProfile(Long id, UsuarioDTO cambios) {
        log.info("Actualizando usuario con ID {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String emailActual = usuario.getEmail();

        // Aplica cambios (el mapper puede tocar email, pero validamos con el DTO 'cambios')
        personaMapper.updateUsuarioFromDTO(usuario, cambios);

        // VALIDACIÓN DE UNICIDAD DE EMAIL (sólo si viene y es distinto al actual)
        if (cambios.getEmail() != null && !cambios.getEmail().isBlank()) {
            String nuevoEmail = cambios.getEmail().trim();

            if (!nuevoEmail.equalsIgnoreCase(emailActual)) {
                usuarioRepository.findByEmail(nuevoEmail)
                        .filter(otro -> !otro.getId().equals(usuario.getId()))
                        .ifPresent(otro -> {
                            throw new IllegalStateException("El email ya está en uso");
                        });

                // Si quieres normalizar (opcional):
                // nuevoEmail = nuevoEmail.toLowerCase(Locale.ROOT);

                usuario.setEmail(nuevoEmail);
            }
        }

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado con éxito. ID {}", guardado.getId());
        return personaMapper.usuarioToDTO(guardado);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Eliminando usuario con ID {}", id);

        if (!usuarioRepository.existsById(id)) {
            log.warn("Intento de eliminar usuario inexistente ID {}", id);
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);

        log.info("Usuario eliminado con éxito ID {}", id);
    }

    // =======================================================
    // VALIDACIONES PRIVADAS
    // =======================================================

    private void validateUserData(UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getNombre() == null || usuarioDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (usuarioDTO.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }

        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!isValidEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        if (usuarioDTO.getContrasena() == null || usuarioDTO.getContrasena().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (!isValidPassword(usuarioDTO.getContrasena())) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula y un número");
        }

        if (usuarioDTO.getTelefono() == null || !usuarioDTO.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono es obligatorio y debe contener entre 7 y 15 dígitos numéricos");
        }

        if (usuarioDTO.getFechaNacimiento() != null && usuarioDTO.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        if (usuarioDTO.getRol() == null) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio");
        }
    }

    private void validateUserUpdateData(UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getNombre() != null) {
            if (usuarioDTO.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            if (usuarioDTO.getNombre().length() > 100) {
                throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
            }
        }

        if (usuarioDTO.getEmail() != null && !isValidEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        if (usuarioDTO.getContrasena() != null && !isValidPassword(usuarioDTO.getContrasena())) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula y un número");
        }

        if (usuarioDTO.getTelefono() != null && !usuarioDTO.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos numéricos");
        }

        if (usuarioDTO.getFechaNacimiento() != null && usuarioDTO.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
    }
}


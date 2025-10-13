package com.Rently.Business.Integration;

import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DisplayName("UsuarioService - Integration Tests")
class UsuarioServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    private UsuarioDTO usuarioBase;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();

        usuarioBase = testDataFactory.createUsuario("juan@example.com");
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("CREATE - Usuario válido debe persistirse en la BD")
    void create_ValidUser_ShouldPersist() {
        // Creamos el DTO manualmente, sin registrar aún
        UsuarioDTO nuevo = new UsuarioDTO();
        nuevo.setNombre("Usuario Test");
        nuevo.setEmail("hector@example.com");
        nuevo.setTelefono("3001234567");
        nuevo.setContrasena("Password123A");
        nuevo.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        nuevo.setRol(Rol.USUARIO);
        nuevo.setFotoPerfil("foto.jpg");

        UsuarioDTO guardado = usuarioService.registerUser(nuevo);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).contains("Usuario Test");

        assertThat(usuarioRepository.findByEmail(guardado.getEmail())).isPresent();
    }

    @Test
    @DisplayName("CREATE - Email duplicado debe lanzar excepción")
    void create_DuplicateEmail_ShouldThrowException() {
        UsuarioDTO duplicado = usuarioBase;

        assertThatThrownBy(() -> usuarioService.registerUser(duplicado))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está en uso");
    }

    @Test
    @DisplayName("CREATE - Email inválido debe lanzar excepción")
    void create_InvalidEmail_ShouldThrowException() {
        UsuarioDTO invalido = new UsuarioDTO();
        invalido.setNombre("Usuario Inválido");
        invalido.setEmail("correo-invalido");
        invalido.setTelefono("3001234567");
        invalido.setContrasena("Password123A");
        invalido.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        invalido.setRol(Rol.USUARIO);
        invalido.setFotoPerfil("foto.jpg");

        assertThatThrownBy(() -> usuarioService.registerUser(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato del email no es válido");
    }

    @Test
    @DisplayName("CREATE - Contraseña débil debe lanzar excepción")
    void create_WeakPassword_ShouldThrowException() {
        UsuarioDTO weakPass = testDataFactory.createUsuario("weak@example.com");
        weakPass.setContrasena("123");

        assertThatThrownBy(() -> usuarioService.registerUser(weakPass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contraseña debe tener al menos 8 caracteres");
    }

    @Test
    @DisplayName("CREATE - Teléfono inválido debe lanzar excepción")
    void create_InvalidPhone_ShouldThrowException() {
        UsuarioDTO invalidPhone = testDataFactory.createUsuario("phone@example.com");
        invalidPhone.setTelefono("12AB");

        assertThatThrownBy(() -> usuarioService.registerUser(invalidPhone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teléfono es obligatorio y debe contener");
    }

    // ==================== READ ====================

    @Test
    @DisplayName("READ - Buscar usuario existente por ID debe retornarlo")
    void read_FindById_ExistingUser_ShouldReturn() {
        Optional<UsuarioDTO> encontrado = usuarioService.findUserById(usuarioBase.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).contains("Usuario Test");
    }

    @Test
    @DisplayName("READ - Buscar usuario inexistente debe devolver vacío")
    void read_FindById_NonExistent_ShouldReturnEmpty() {
        Optional<UsuarioDTO> result = usuarioService.findUserById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("READ - Listar todos los usuarios debe devolver lista real")
    void read_FindAll_ShouldReturnList() {
        testDataFactory.createUsuario("otro@example.com");

        List<UsuarioDTO> usuarios = usuarioService.findAllUsers();

        assertThat(usuarios).hasSize(2);
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("UPDATE - Usuario existente debe actualizarse en BD")
    void update_ExistingUser_ShouldUpdate() {
        usuarioBase.setNombre("Nuevo Nombre");

        UsuarioDTO actualizado = usuarioService.updateUserProfile(usuarioBase.getId(), usuarioBase);

        assertThat(actualizado.getNombre()).isEqualTo("Nuevo Nombre");

        assertThat(usuarioRepository.findById(usuarioBase.getId()))
                .get()
                .extracting(u -> u.getNombre())
                .isEqualTo("Nuevo Nombre");
    }

    @Test
    @DisplayName("UPDATE - Usuario inexistente debe lanzar excepción")
    void update_NonExistent_ShouldThrowException() {
        UsuarioDTO fake = testDataFactory.createUsuario("fake@example.com");

        assertThatThrownBy(() -> usuarioService.updateUserProfile(999L, fake))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("DELETE - Usuario existente debe eliminarse")
    void delete_ExistingUser_ShouldRemove() {
        usuarioService.deleteUser(usuarioBase.getId());

        assertThat(usuarioRepository.findById(usuarioBase.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE - Usuario inexistente debe lanzar excepción")
    void delete_NonExistentUser_ShouldThrowException() {
        assertThatThrownBy(() -> usuarioService.deleteUser(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}




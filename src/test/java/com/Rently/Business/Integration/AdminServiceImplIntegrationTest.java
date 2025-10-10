package com.Rently.Business.Integration;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Persistence.Entity.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class AdminServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AdministradorService administradorService;

    // ================== CREATE ==================
    @Test
    @DisplayName("CREATE - Administrador válido debe persistirse")
    void create_ValidAdmin_ShouldPersist() {
        AdministradorDTO admin = new AdministradorDTO();
        admin.setNombre("Admin Test");
        admin.setEmail("admin+" + System.currentTimeMillis() + "@example.com");
        admin.setTelefono("1234567890");
        admin.setFechaNacimiento(LocalDate.now().minusYears(30));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasena("ClaveValida123");

        AdministradorDTO guardado = administradorService.create(admin);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Admin Test");
    }

    // ================== READ ==================
    @Test
    @DisplayName("READ - Buscar administrador por ID")
    void findById_ExistingAdmin_ShouldReturnAdmin() {
        AdministradorDTO admin = new AdministradorDTO();
        admin.setNombre("Admin ID Test");
        admin.setEmail("adminid+" + System.currentTimeMillis() + "@example.com");
        admin.setTelefono("1234567890");
        admin.setFechaNacimiento(LocalDate.now().minusYears(30));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasena("ClaveValida123");

        AdministradorDTO guardado = administradorService.create(admin);
        Optional<AdministradorDTO> encontrado = administradorService.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Admin ID Test");
    }

    @Test
    @DisplayName("READ - Buscar administrador por email")
    void findByEmail_ExistingAdmin_ShouldReturnAdmin() {
        String email = "adminemail+" + System.currentTimeMillis() + "@example.com";

        AdministradorDTO admin = new AdministradorDTO();
        admin.setNombre("Admin Email Test");
        admin.setEmail(email);
        admin.setTelefono("1234567890");
        admin.setFechaNacimiento(LocalDate.now().minusYears(30));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasena("ClaveValida123");

        administradorService.create(admin);

        Optional<AdministradorDTO> encontrado = administradorService.findByEmail(email);
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("READ ALL - Listar todos los administradores")
    void findAll_ShouldReturnList() {
        List<AdministradorDTO> admins = administradorService.findAll();
        assertThat(admins).isNotNull();
    }

    // ================== UPDATE ==================
    @Test
    @DisplayName("UPDATE - Administrador existente debe actualizarse")
    void update_ExistingAdmin_ShouldUpdate() {
        AdministradorDTO admin = new AdministradorDTO();
        admin.setNombre("Admin Update Test");
        admin.setEmail("adminupdate+" + System.currentTimeMillis() + "@example.com");
        admin.setTelefono("1234567890");
        admin.setFechaNacimiento(LocalDate.now().minusYears(30));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasena("ClaveValida123");

        AdministradorDTO guardado = administradorService.create(admin);

        AdministradorDTO cambios = new AdministradorDTO();
        cambios.setNombre("Admin Updated");

        AdministradorDTO actualizado = administradorService.update(guardado.getId(), cambios).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Admin Updated");
    }

    @Test
    @DisplayName("UPDATE - Administrador inexistente lanza excepción")
    void update_NonExistent_ShouldThrowException() {
        AdministradorDTO cambios = new AdministradorDTO();
        cambios.setNombre("Fake Admin");

        assertThatThrownBy(() -> administradorService.update(99999L, cambios))
                .isInstanceOf(RuntimeException.class);
    }

    // ================== DELETE ==================
    @Test
    @DisplayName("DELETE - Administrador existente debe eliminarse")
    void delete_ExistingAdmin_ShouldDelete() {
        AdministradorDTO admin = new AdministradorDTO();
        admin.setNombre("Admin Delete Test");
        admin.setEmail("admindelete+" + System.currentTimeMillis() + "@example.com");
        admin.setTelefono("1234567890");
        admin.setFechaNacimiento(LocalDate.now().minusYears(30));
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasena("ClaveValida123");

        AdministradorDTO guardado = administradorService.create(admin);
        boolean eliminado = administradorService.delete(guardado.getId());
        assertThat(eliminado).isTrue();
    }

    @Test
    @DisplayName("DELETE - Administrador inexistente lanza excepción")
    void delete_NonExistent_ShouldThrowException() {
        assertThatThrownBy(() -> administradorService.delete(99999L))
                .isInstanceOf(RuntimeException.class);
    }

    // ================== VALIDACIONES ==================
    @Test
    @DisplayName("CREATE - Administrador inválido lanza excepción")
    void create_InvalidAdmin_ShouldThrowException() {
        AdministradorDTO admin = new AdministradorDTO(); // todo nulo

        assertThatThrownBy(() -> administradorService.create(admin))
                .isInstanceOf(IllegalArgumentException.class);
    }
}


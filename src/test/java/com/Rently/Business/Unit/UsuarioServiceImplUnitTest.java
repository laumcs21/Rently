package com.Rently.Business.Unit;

import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.impl.UsuarioServiceImpl;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Entity.Usuario;
import com.Rently.Persistence.Mapper.PersonaMapper;
import com.Rently.Persistence.Repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de UsuarioServiceImpl
 * Formato consistente: GIVEN – WHEN – THEN (en DisplayName) + verificación clara de oráculos.
 * NOTA: No incluye envío de email de bienvenida.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl | Unit Tests")
class UsuarioServiceImplUnitTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PersonaMapper personaMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioDTO dtoValido;
    private Usuario entidadValida;

    @BeforeEach
    void init() {
        dtoValido = new UsuarioDTO(
                1L,
                "Juan Pérez",
                "juan@example.com",
                "3001234567",
                "Password123",              // ≥8 + mayúscula + número
                LocalDate.of(1990, 1, 1),
                Rol.USUARIO,
                "perfil.jpg"
        );

        entidadValida = new Usuario();
        entidadValida.setId(1L);
        entidadValida.setNombre("Juan Pérez");
        entidadValida.setEmail("juan@example.com");
        entidadValida.setTelefono("3001234567");
        entidadValida.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        entidadValida.setRol(Rol.USUARIO);
        entidadValida.setContrasena("encodedPass");
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    @DisplayName("CREATE | GIVEN DTO válido WHEN registerUser THEN persiste y encripta contraseña")
    void registerUser_valid_shouldPersist_andEncodePassword() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
        when(personaMapper.dtoToUsuario(dtoValido)).thenAnswer(inv -> {
            Usuario u = new Usuario();
            u.setNombre(dtoValido.getNombre());
            u.setEmail(dtoValido.getEmail());
            u.setTelefono(dtoValido.getTelefono());
            u.setFechaNacimiento(dtoValido.getFechaNacimiento());
            u.setRol(dtoValido.getRol());
            u.setContrasena(dtoValido.getContrasena()); // será reemplazada por la codificada
            return u;
        });
        when(passwordEncoder.encode("Password123")).thenReturn("ENCODED");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(personaMapper.usuarioToDTO(any(Usuario.class))).thenReturn(dtoValido);

        ArgumentCaptor<Usuario> cap = ArgumentCaptor.forClass(Usuario.class);

        UsuarioDTO out = usuarioService.registerUser(dtoValido);

        assertThat(out).isNotNull();
        assertThat(out.getEmail()).isEqualTo("juan@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(usuarioRepository).save(cap.capture());
        assertThat(cap.getValue().getContrasena()).isEqualTo("ENCODED");
    }

    @Test
    @DisplayName("CREATE | GIVEN email duplicado WHEN registerUser THEN IllegalStateException")
    void registerUser_duplicateEmail_shouldThrow() {
        when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(entidadValida));

        assertThatThrownBy(() -> usuarioService.registerUser(dtoValido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está en uso");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE | GIVEN email inválido WHEN registerUser THEN IllegalArgumentException")
    void registerUser_invalidEmail_shouldThrow() {
        dtoValido.setEmail("correo-invalido");

        assertThatThrownBy(() -> usuarioService.registerUser(dtoValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato del email no es válido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE | GIVEN contraseña débil WHEN registerUser THEN IllegalArgumentException")
    void registerUser_weakPassword_shouldThrow() {
        dtoValido.setContrasena("abc"); // < 8, sin mayúscula/numero

        assertThatThrownBy(() -> usuarioService.registerUser(dtoValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos 8")
                .hasMessageContaining("mayúscula")
                .hasMessageContaining("número");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE | GIVEN teléfono con formato inválido WHEN registerUser THEN IllegalArgumentException")
    void registerUser_invalidPhone_shouldThrow() {
        dtoValido.setTelefono("12AB");

        assertThatThrownBy(() -> usuarioService.registerUser(dtoValido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teléfono");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE | GIVEN campos obligatorios faltantes WHEN registerUser THEN IllegalArgumentException")
    void registerUser_missingRequiredFields_shouldThrow() {
        UsuarioDTO incompleto = new UsuarioDTO();
        assertThatThrownBy(() -> usuarioService.registerUser(incompleto))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // READ
    // =========================================================

    @Test
    @DisplayName("READ | GIVEN ID existente WHEN findUserById THEN retorna DTO")
    void findUserById_existing_shouldReturn() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidadValida));
        when(personaMapper.usuarioToDTO(entidadValida)).thenReturn(dtoValido);

        Optional<UsuarioDTO> out = usuarioService.findUserById(1L);

        assertThat(out).isPresent();
        assertThat(out.get().getEmail()).isEqualTo("juan@example.com");
    }

    @Test
    @DisplayName("READ | GIVEN base con registros WHEN findAllUsers THEN retorna lista")
    void findAllUsers_shouldReturnList() {
        when(usuarioRepository.findAll()).thenReturn(List.of(entidadValida));
        when(personaMapper.usuariosToDTO(anyList())).thenReturn(List.of(dtoValido));

        List<UsuarioDTO> lista = usuarioService.findAllUsers();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getEmail()).isEqualTo("juan@example.com");
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    @DisplayName("UPDATE | GIVEN ID existente y datos válidos WHEN updateUserProfile THEN retorna DTO actualizado")
    void updateUserProfile_valid_shouldUpdate() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidadValida));
        doAnswer(inv -> {
            Usuario target = inv.getArgument(0);
            UsuarioDTO src = inv.getArgument(1);
            if (src.getNombre() != null) target.setNombre(src.getNombre());
            if (src.getTelefono() != null) target.setTelefono(src.getTelefono());
            if (src.getEmail() != null) target.setEmail(src.getEmail());
            return null;
        }).when(personaMapper).updateUsuarioFromDTO(any(Usuario.class), any(UsuarioDTO.class));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(personaMapper.usuarioToDTO(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            UsuarioDTO dto = new UsuarioDTO();
            dto.setId(u.getId());
            dto.setNombre(u.getNombre());
            dto.setEmail(u.getEmail());
            dto.setTelefono(u.getTelefono());
            dto.setFechaNacimiento(u.getFechaNacimiento());
            dto.setRol(u.getRol());
            return dto;
        });

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setNombre("Nuevo Nombre");
        cambios.setTelefono("3009876543");

        UsuarioDTO out = usuarioService.updateUserProfile(1L, cambios);

        assertThat(out).isNotNull();
        assertThat(out.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(out.getTelefono()).isEqualTo("3009876543");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("UPDATE | GIVEN ID inexistente WHEN updateUserProfile THEN RuntimeException")
    void updateUserProfile_nonExistent_shouldThrow() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.updateUserProfile(99L, dtoValido))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("UPDATE | GIVEN email ya usado por otro WHEN updateUserProfile THEN IllegalStateException")
    void updateUserProfile_emailToExisting_shouldThrow() {
        // El usuario actual en BD
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidadValida));

        // Simula que el nuevo email ya pertenece a OTRO usuario
        Usuario otro = new Usuario();
        otro.setId(2L);
        otro.setEmail("exists@example.com");

        // 👇 MUY IMPORTANTE: stub al método que realmente llama tu service
        when(usuarioRepository.findByEmail("exists@example.com"))
                .thenReturn(Optional.of(otro));

        UsuarioDTO cambios = new UsuarioDTO();
        cambios.setEmail("exists@example.com");

        assertThatThrownBy(() -> usuarioService.updateUserProfile(1L, cambios))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está en uso");

        // No debe intentar persistir cambios
        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    @DisplayName("DELETE | GIVEN ID existente WHEN deleteUser THEN no lanza excepción y borra")
    void deleteUser_existing_shouldDelete() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> usuarioService.deleteUser(1L)).doesNotThrowAnyException();
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE | GIVEN ID inexistente WHEN deleteUser THEN RuntimeException")
    void deleteUser_nonExistent_shouldThrow() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.deleteUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}



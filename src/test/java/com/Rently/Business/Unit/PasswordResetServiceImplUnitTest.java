package com.Rently.Business.Unit;

import com.Rently.Business.Service.EmailService;
import com.Rently.Business.Service.Util.SimpleCodeGenerator;
import com.Rently.Business.Service.impl.PasswordResetServiceImpl;
import com.Rently.Persistence.Entity.*;
import com.Rently.Persistence.Repository.PasswordResetTokenRepository;
import com.Rently.Persistence.Repository.PersonaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetServiceImpl | Unit Tests con subclases (Usuario/Anfitrión/Administrador)")
class PasswordResetServiceImplUnitTest {

    @Mock private PersonaRepository personaRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SimpleCodeGenerator codeGenerator;
    @Mock private EmailService mailService;

    @InjectMocks
    private PasswordResetServiceImpl service;

    // -------------------- Proveedor de instancias concretas --------------------
    static Stream<Arguments> personasConcretas() {
        return Stream.of(
                Arguments.of("USUARIO", usuario(1001L, "user@example.com", "User Test")),
                Arguments.of("ANFITRION", anfitrion(1002L, "host@example.com", "Host Test")),
                Arguments.of("ADMINISTRADOR", administrador(1003L, "admin@example.com", "Admin Test"))
        );
    }

    private static Usuario usuario(Long id, String email, String nombre) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setNombre(nombre);
        u.setRol(Rol.USUARIO);
        u.setActivo(true);
        u.setContrasena("ENC_OLD");
        return u;
    }

    private static Anfitrion anfitrion(Long id, String email, String nombre) {
        Anfitrion a = new Anfitrion();
        a.setId(id);
        a.setEmail(email);
        a.setNombre(nombre);
        a.setRol(Rol.ANFITRION);
        a.setActivo(true);
        a.setContrasena("ENC_OLD");
        a.setDescripcion("anfitrión de pruebas"); // si tu entidad lo requiere
        return a;
    }

    private static Administrador administrador(Long id, String email, String nombre) {
        Administrador ad = new Administrador();
        ad.setId(id);
        ad.setEmail(email);
        ad.setNombre(nombre);
        ad.setRol(Rol.ADMINISTRADOR);
        ad.setActivo(true);
        ad.setContrasena("ENC_OLD");
        return ad;
    }

    // =====================================================================
    // forgotPassword (feliz) para los tres tipos concretos
    // =====================================================================
    @ParameterizedTest(name = "FORGOT | {0} → borra tokens previos, crea uno y envía correo")
    @MethodSource("personasConcretas")
    void forgotPassword_ok_porRol(String label, Persona persona) {
        when(personaRepository.findByEmail(persona.getEmail()))
                .thenReturn(Optional.of(persona));
        when(codeGenerator.generate6Digits())
                .thenReturn("123456");
        when(tokenRepository.deleteByPersonaAndUsedFalse(persona))
                .thenReturn(0);

        when(tokenRepository.saveAndFlush(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.requestCode(persona.getEmail());

        verify(tokenRepository, times(2))
                .deleteByPersonaAndUsedFalse(eq(persona));

        ArgumentCaptor<PasswordResetToken> cap = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, times(1)).saveAndFlush(cap.capture());

        PasswordResetToken saved = cap.getValue();
        assertThat(saved.getPersona()).isEqualTo(persona);
        assertThat(saved.getCode()).isEqualTo("123456");
        assertThat(saved.getUsed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());

        verify(mailService, times(1)).send(persona.getEmail(), "123456");
        verify(codeGenerator, times(1)).generate6Digits();
    }


    @Test
    @DisplayName("FORGOT | GIVEN email inexistente WHEN requestCode THEN no falla ni filtra información")
    void forgotPassword_emailNoExiste() {
        String email = "no@existe.com";
        when(personaRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatCode(() -> service.requestCode(email)).doesNotThrowAnyException();

        verify(tokenRepository, never()).deleteByPersonaAndUsedFalse(any());
        verify(tokenRepository, never()).saveAndFlush(any());
        verify(mailService, never()).send(anyString(), anyString());

        verify(personaRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(personaRepository, tokenRepository, mailService);
    }


    // =====================================================================
    // resetPassword (feliz) para los tres tipos concretos
    // =====================================================================
    @ParameterizedTest(name = "RESET | {0} → encripta nueva pass y marca token como usado")
    @MethodSource("personasConcretas")
    void resetPassword_ok_porRol(String label, Persona persona) {
        String code = "654321";
        String newPass = "NuevaP4ss";

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setPersona(persona);
        token.setCode(code);
        token.setUsed(false);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(personaRepository.findByEmail(persona.getEmail())).thenReturn(Optional.of(persona));
        when(tokenRepository.findTopByPersonaAndUsedFalseOrderByCreatedAtDesc(persona)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(newPass)).thenReturn("ENC_NEW");
        when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.resetPassword(persona.getEmail(), code, newPass);

        assertThat(persona.getContrasena()).isEqualTo("ENC_NEW");
        assertThat(token.getUsed()).isTrue();

        verify(passwordEncoder).encode(newPass);
        verify(personaRepository).save(persona);
        verify(tokenRepository).save(token);
    }

    // =====================================================================
    // resetPassword (errores)
    // =====================================================================
    @Test
    @DisplayName("RESET | código incorrecto → IllegalArgumentException")
    void resetPassword_codigoIncorrecto() {
        Persona persona = usuario(2001L, "u@ex.com", "U");
        PasswordResetToken token = new PasswordResetToken();
        token.setPersona(persona);
        token.setCode("CORRECTO");
        token.setUsed(false);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(personaRepository.findByEmail(persona.getEmail())).thenReturn(Optional.of(persona));
        when(tokenRepository.findTopByPersonaAndUsedFalseOrderByCreatedAtDesc(persona)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(persona.getEmail(), "MALO", "Xx123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        verify(passwordEncoder, never()).encode(anyString());
        verify(personaRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("RESET | token vencido → IllegalArgumentException")
    void resetPassword_tokenVencido() {
        Persona persona = anfitrion(2002L, "h@ex.com", "H");
        String code = "111111";

        PasswordResetToken token = new PasswordResetToken();
        token.setPersona(persona);
        token.setCode(code);
        token.setUsed(false);
        token.setCreatedAt(Instant.now().minus(20, ChronoUnit.MINUTES));
        token.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(personaRepository.findByEmail(persona.getEmail())).thenReturn(Optional.of(persona));
        when(tokenRepository.findTopByPersonaAndUsedFalseOrderByCreatedAtDesc(persona)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(persona.getEmail(), code, "Xx123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        verify(passwordEncoder, never()).encode(anyString());
        verify(personaRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }
}


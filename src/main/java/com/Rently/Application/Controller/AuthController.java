package com.Rently.Application.Controller;

import com.Rently.Business.DTO.Auth.ForgotPasswordRequestDTO;
import com.Rently.Business.DTO.Auth.ResetPasswordDTO;
import com.Rently.Business.Service.EmailService;
import com.Rently.Business.Service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.Service.AuthService;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService mailService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, EmailService mailService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.mailService = mailService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgot(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        try {
            passwordResetService.requestCode(dto.getEmail());
        } catch (Exception e) {
            log.warn("Forgot error for {}: {}", dto.getEmail(), e.toString());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        passwordResetService.resetPassword(
                dto.getEmail().trim(),
                dto.getCode().trim(),
                dto.getNewPassword()
        );
        return ResponseEntity.ok().body(Map.of("message", "Contraseña actualizada"));
    }

}

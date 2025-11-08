package com.Rently.Application.Controller;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.DTO.PersonaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ObjectMapper mapper;

    // Constructor explícito (Spring lo autowirea sin @Autowired al haber un solo constructor)
    public AuthController(AuthService authService, ObjectMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
        // por si el bean no vino con módulos ya registrados:
        this.mapper.findAndRegisterModules();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, Object> data) {
        String rol = String.valueOf(data.get("rol"));

        switch (rol.toUpperCase()) {
            case "USUARIO": {
                UsuarioDTO dto = mapper.convertValue(data, UsuarioDTO.class);
                return ResponseEntity.ok(authService.registerUsuario(dto));
            }
            case "ANFITRION": {
                AnfitrionDTO dto = mapper.convertValue(data, AnfitrionDTO.class);
                return ResponseEntity.ok(authService.registerAnfitrion(dto));
            }
            case "ADMINISTRADOR": {
                AdministradorDTO dto = mapper.convertValue(data, AdministradorDTO.class);
                return ResponseEntity.ok(authService.registerAdministrador(dto));
            }
            default:
                throw new IllegalArgumentException("Rol inválido: " + rol);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest dto) {
        try {
            AuthResponse response = authService.login(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no proporcionado"));
            }
            PersonaDTO user = authService.verifyToken(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}

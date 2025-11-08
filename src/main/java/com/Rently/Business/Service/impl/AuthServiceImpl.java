package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.DTO.PersonaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Business.Service.AnfitrionService;
import com.Rently.Business.Service.AuthService;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Configuration.Security.JwtService;
import com.Rently.Persistence.Entity.Persona;
import com.Rently.Persistence.Mapper.PersonaMapper;
import com.Rently.Persistence.Repository.AdministradorRepository;
import com.Rently.Persistence.Repository.AnfitrionRepository;
import com.Rently.Persistence.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioService usuarioService;
    private final AnfitrionService anfitrionService;
    private final AdministradorService administradorService;
    private final PersonaMapper personaMapper;
    private final UsuarioRepository usuarioRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final AdministradorRepository administradorRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse registerUsuario(UsuarioDTO dto) {
        var saved = usuarioService.registerUser(dto);
        var usuario = usuarioRepository.findByEmail(saved.getEmail()).orElseThrow();
        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    public AuthResponse registerAnfitrion(AnfitrionDTO dto) {
        var saved = anfitrionService.create(dto);
        var anfitrion = anfitrionRepository.findByEmail(saved.getEmail()).orElseThrow();
        String token = jwtService.generateToken(anfitrion);
        return AuthResponse.builder()
                .token(token)
                .nombre(anfitrion.getNombre())
                .rol(anfitrion.getRol())
                .build();
    }

    @Override
    public AuthResponse registerAdministrador(AdministradorDTO dto) {
        var saved = administradorService.create(dto);
        var admin = administradorRepository.findByEmail(saved.getEmail()).orElseThrow();
        String token = jwtService.generateToken(admin);
        return AuthResponse.builder()
                .token(token)
                .nombre(admin.getNombre())
                .rol(admin.getRol())
                .build();
    }

    /**
     * Auténtica a un usuario y genera un token de autenticación.
     *
     * @param request la solicitud de autenticación con el email y la contraseña
     * @return una respuesta de autenticación con el token generado
     */
    @Override
    public AuthResponse login(AuthRequest request) {
        // 1) autentica email + password (usa tu PasswordEncoder y UserDetailsService configurados)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2) busca la "persona" por email en cualquiera de las 3 tablas
        Persona persona =
                usuarioRepository.findByEmail(request.getEmail()).map(u -> (Persona) u)
                        .orElseGet(() -> anfitrionRepository.findByEmail(request.getEmail()).map(a -> (Persona) a)
                                .orElseGet(() -> administradorRepository.findByEmail(request.getEmail()).map(ad -> (Persona) ad)
                                        .orElseThrow(() -> new RuntimeException("No existe usuario/anfitrión/admin con ese email"))
                                )
                        );

        // 3) firma el token con claims de rol
        String token = jwtService.generateToken(persona);

        // (opcional pero MUY útil para el front)
        return AuthResponse.builder()
                .token(token)
                .nombre(persona.getNombre())
                .rol(persona.getRol()) // enum
                .build();
    }

    @Override
    public PersonaDTO verifyToken(String token) {
        try {
            String email = jwtService.extractUsername(token);

            var usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                return personaMapper.usuarioToDTO(usuarioOpt.get());
            }

            var anfitrionOpt = anfitrionRepository.findByEmail(email);
            if (anfitrionOpt.isPresent()) {
                return personaMapper.anfitrionToDTO(anfitrionOpt.get());
            }

            var adminOpt = administradorRepository.findByEmail(email);
            if (adminOpt.isPresent()) {
                return personaMapper.adminToDTO(adminOpt.get());
            }

            throw new RuntimeException("Cuenta no encontrada para el email del token");
        } catch (Exception e) {
            throw new RuntimeException("Token inválido o expirado");
        }
    }


}

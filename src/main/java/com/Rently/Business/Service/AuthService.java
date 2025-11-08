package com.Rently.Business.Service;

import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.DTO.PersonaDTO;
import com.Rently.Business.DTO.UsuarioDTO;

/**
 * Interfaz de servicio para la autenticación.
 */
public interface AuthService {


    AuthResponse registerUsuario(UsuarioDTO dto);
    AuthResponse registerAnfitrion(AnfitrionDTO dto);
    AuthResponse registerAdministrador(AdministradorDTO dto);

    /**
     * Autentica a un usuario y genera un token de autenticación.
     *
     * @param request la solicitud de autenticación con el email y la contraseña
     * @return una respuesta de autenticación con el token generado
     */
    AuthResponse login(AuthRequest request);

    PersonaDTO verifyToken(String token);
}

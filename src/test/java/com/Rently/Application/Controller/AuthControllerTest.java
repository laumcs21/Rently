package com.Rently.Application.Controller;


import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.Auth.AuthResponse;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AuthService;
import com.Rently.Business.Service.EmailService;
import com.Rently.Business.Service.PasswordResetService;
import com.Rently.Persistence.Entity.Rol;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordResetService passwordResetService;

    @Test
    void registerShouldReturnToken() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setNombre("Test User");
        request.setEmail("test@example.com");
        request.setContrasena("Password123");
        request.setTelefono("3001234567");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        request.setRol(Rol.USUARIO);

        when(authService.register(any(UsuarioDTO.class)))
                .thenReturn(AuthResponse.builder().token("registered-token").build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("registered-token"));
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        AuthRequest request = new AuthRequest("test@example.com", "Password123");

        when(authService.login(any(AuthRequest.class)))
                .thenReturn(AuthResponse.builder().token("login-token").build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));
    }
}

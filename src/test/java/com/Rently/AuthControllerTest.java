package com.Rently;

import com.Rently.Business.DTO.Auth.AuthRequest;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Persistence.Entity.Rol;
import com.Rently.Persistence.Entity.Usuario;
import com.Rently.Persistence.Repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        // Limpiar el repositorio antes de cada prueba para asegurar un estado limpio
        usuarioRepository.deleteAll();
    }

    @Test
    void testRegisterUser() throws Exception {
        String email = "test+" + System.currentTimeMillis() + "@example.com";

        UsuarioDTO newUser = new UsuarioDTO();
        newUser.setNombre("Test User");
        newUser.setEmail(email);
        newUser.setTelefono("3001234567");        // <-- requerido por el servicio
        newUser.setContrasena("Password123");     // <-- cumple política
        newUser.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        newUser.setRol(Rol.USUARIO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())               // si tu endpoint devuelve 201, cámbialo a isCreated()
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void testLoginUser() throws Exception {
        // 1) Registrar con datos válidos y email único
        String email = "login+" + System.currentTimeMillis() + "@example.com";

        UsuarioDTO newUser = new UsuarioDTO();
        newUser.setNombre("Test User");
        newUser.setEmail(email);
        newUser.setTelefono("3001234567");
        newUser.setContrasena("Password123");     // <-- misma contraseña para login
        newUser.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        newUser.setRol(Rol.USUARIO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // 2) Login con las MISMAS credenciales
        AuthRequest authRequest = new AuthRequest(email, "Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void testAccessProtectedEndpointWithToken() throws Exception {
        // 1) Registrar usuario con email único
        String email = "secure+" + System.currentTimeMillis() + "@example.com";

        UsuarioDTO newUser = new UsuarioDTO();
        newUser.setNombre("Test User");
        newUser.setEmail(email);
        newUser.setTelefono("3001234567");
        newUser.setContrasena("Password123");
        newUser.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        newUser.setRol(Rol.USUARIO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // 2) Obtener el usuario real desde la BD por el email dinámico
        Usuario savedUser = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en BD"));

        // 3) Login para obtener token (misma contraseña)
        AuthRequest authRequest = new AuthRequest(email, "Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseString).get("token").asText();

        // 4) Acceder al endpoint protegido con el token
        mockMvc.perform(get("/api/usuarios/{id}", savedUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        // Intentar acceder al endpoint protegido sin token
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden()); // Spring Security 6 devuelve 403 por defecto si no hay token
    }
}

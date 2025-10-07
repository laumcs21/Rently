package com.Rently.Application.Controller;


import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void crearUsuarioShouldReturnCreated() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("user@test.com");
        request.setFechaNacimiento(LocalDate.of(1995, 5, 20));

        UsuarioDTO response = new UsuarioDTO();
        response.setId(50L);
        response.setEmail("user@test.com");

        when(usuarioService.registerUser(any(UsuarioDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/usuarios/50")))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void crearUsuarioShouldReturnConflictOnDuplicateEmail() throws Exception {
        when(usuarioService.registerUser(any(UsuarioDTO.class))).thenThrow(new IllegalStateException("Duplicado"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioDTO())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicado"));
    }

    @Test
    void obtenerUsuarioShouldReturnNotFoundWhenMissing() throws Exception {
        when(usuarioService.findUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarUsuarioShouldReturnUpdated() throws Exception {
        UsuarioDTO updated = new UsuarioDTO();
        updated.setId(3L);
        updated.setNombre("Nuevo");

        when(usuarioService.updateUserProfile(Mockito.eq(3L), any(UsuarioDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/usuarios/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    void eliminarUsuarioShouldReturnNoContent() throws Exception {
        doNothing().when(usuarioService).deleteUser(4L);

        mockMvc.perform(delete("/api/usuarios/{id}", 4L))
                .andExpect(status().isNoContent());
    }
}

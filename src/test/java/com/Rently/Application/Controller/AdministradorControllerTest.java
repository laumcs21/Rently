package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.Service.AdministradorService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AdministradorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class AdministradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdministradorService administradorService;

    @Test
    void homeShouldReturnHealthMessage() throws Exception {
        mockMvc.perform(get("/api/administradores/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Rently")));
    }

    @Test
    void crearAdministradorShouldReturnCreated() throws Exception {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(1L);
        dto.setNombre("Admin");
        dto.setEmail("admin@rently.com");
        dto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        when(administradorService.create(any(AdministradorDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/administradores/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

        @Test
    void actualizarAdministradorShouldReturnOk() throws Exception {
        AdministradorDTO actualizado = new AdministradorDTO();
        actualizado.setId(5L);
        actualizado.setNombre("Admin Actualizado");

        when(administradorService.update(org.mockito.ArgumentMatchers.eq(5L), any(AdministradorDTO.class)))
                .thenReturn(java.util.Optional.of(actualizado));

        mockMvc.perform(put("/api/administradores/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdministradorDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Admin Actualizado"));
    }

    @Test
    void actualizarAdministradorShouldReturnBadRequest() throws Exception {
        when(administradorService.update(org.mockito.ArgumentMatchers.eq(5L), any(AdministradorDTO.class)))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        mockMvc.perform(put("/api/administradores/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdministradorDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"));
    }

    @Test
    void actualizarAdministradorShouldReturnNotFound() throws Exception {
        when(administradorService.update(org.mockito.ArgumentMatchers.eq(15L), any(AdministradorDTO.class)))
                .thenThrow(new RuntimeException("Administrador no encontrado"));

        mockMvc.perform(put("/api/administradores/{id}", 15L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdministradorDTO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Administrador no encontrado"));
    }
@Test
    void eliminarAdministradorShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/administradores/{id}", 4L))
                .andExpect(status().isNoContent());
    }

    @Test
    void listarReservasShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/administradores/reservas"))
                .andExpect(status().isOk());
    }
}



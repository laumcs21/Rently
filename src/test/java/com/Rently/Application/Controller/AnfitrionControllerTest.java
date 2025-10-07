package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.Service.AnfitrionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnfitrionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class AnfitrionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnfitrionService anfitrionService;

    @Test
    void crearAnfitrionShouldReturnCreated() throws Exception {
        AnfitrionDTO response = new AnfitrionDTO();
        response.setId(10L);
        response.setNombre("Carlos");
        when(anfitrionService.create(any(AnfitrionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/anfitriones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnfitrionDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void obtenerDashboardShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/anfitriones/{id}/dashboard", 3L))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerNotificacionesShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/anfitriones/{id}/notificaciones", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void marcarNotificacionLeidaShouldReturnOkMessage() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/anfitriones/{id}/notificaciones/{nId}/marcar-leida", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").isString());
    }
}

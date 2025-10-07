package com.Rently.Application.Controller;


import com.Rently.Business.DTO.ServicioDTO;
import com.Rently.Business.Service.ServicioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServicioService servicioService;

    @Test
    void createShouldReturnServicio() throws Exception {
        ServicioDTO response = new ServicioDTO();
        response.setId(1L);
        response.setNombre("Piscina");
        when(servicioService.create(any(ServicioDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ServicioDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Piscina"));
    }

    @Test
    void getAllShouldReturnServicios() throws Exception {
        when(servicioService.findAll()).thenReturn(List.of(new ServicioDTO()));

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdShouldReturnNotFoundWhenNull() throws Exception {
        when(servicioService.findById(9L)).thenReturn(null);

        mockMvc.perform(get("/api/servicios/{id}", 9L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateShouldReturnUpdatedServicio() throws Exception {
        ServicioDTO updated = new ServicioDTO();
        updated.setId(2L);
        when(servicioService.update(eq(2L), any(ServicioDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/servicios/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void deleteShouldReturnNoContentWhenDeleted() throws Exception {
        when(servicioService.delete(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/servicios/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(servicioService).delete(5L);
    }

    @Test
    void deleteShouldReturnNotFoundWhenNotDeleted() throws Exception {
        when(servicioService.delete(5L)).thenReturn(false);

        mockMvc.perform(delete("/api/servicios/{id}", 5L))
                .andExpect(status().isNotFound());
    }
}

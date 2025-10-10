package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.AlojamientoService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlojamientoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class AlojamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlojamientoService alojamientoService;

    @Test
    void createShouldReturnCreatedAlojamiento() throws Exception {
        AlojamientoDTO request = new AlojamientoDTO();
        request.setTitulo("Casa test");
        AlojamientoDTO response = new AlojamientoDTO();
        response.setId(1L);
        response.setTitulo("Casa test");

        when(alojamientoService.create(any(AlojamientoDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/alojamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Casa test"));

        verify(alojamientoService).create(any(AlojamientoDTO.class));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(alojamientoService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/alojamientos/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllShouldReturnList() throws Exception {
        AlojamientoDTO dto = new AlojamientoDTO();
        dto.setId(1L);
        when(alojamientoService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/alojamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deleteShouldReturnNoContentWhenDeleted() throws Exception {
        when(alojamientoService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/alojamientos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(alojamientoService).delete(1L);
    }

    @Test
    void deleteShouldReturnNotFoundWhenMissing() throws Exception {
        when(alojamientoService.delete(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/alojamientos/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}

package com.Rently.Application.Controller;


import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Persistence.Entity.EstadoReserva;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaService reservaService;

    @Test
    void createShouldReturnCreatedReserva() throws Exception {
        ReservaDTO response = new ReservaDTO();
        response.setId(10L);
        when(reservaService.create(any(ReservaDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getByIdShouldReturnReservaWhenPresent() throws Exception {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(4L);
        when(reservaService.findById(4L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/reservas/{id}", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L));
    }

    @Test
    void getAllShouldReturnList() throws Exception {
        when(reservaService.findAll()).thenReturn(List.of(new ReservaDTO()));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk());
    }

    @Test
    void updateShouldReturnNotFoundWhenMissing() throws Exception {
        when(reservaService.update(eq(5L), any(ReservaDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/reservas/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaDTO())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStateShouldReturnNoContentWhenSuccess() throws Exception {
        when(reservaService.updateState(3L, EstadoReserva.CONFIRMADA)).thenReturn(true);

        mockMvc.perform(patch("/api/reservas/{id}/estado", 3L)
                        .param("estado", EstadoReserva.CONFIRMADA.name()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturnNotFoundWhenServiceReturnsFalse() throws Exception {
        when(reservaService.delete(2L)).thenReturn(false);

        mockMvc.perform(delete("/api/reservas/{id}", 2L))
                .andExpect(status().isNotFound());
    }
}

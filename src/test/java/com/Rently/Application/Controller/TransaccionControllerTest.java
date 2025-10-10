package com.Rently.Application.Controller;


import com.Rently.Business.DTO.TransaccionDTO;
import com.Rently.Business.Service.TransaccionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransaccionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransaccionService transaccionService;

    @Test
    void createShouldReturnTransaccion() throws Exception {
        TransaccionDTO response = new TransaccionDTO();
        response.setId(11L);
        when(transaccionService.create(any(TransaccionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransaccionDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(transaccionService.findById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transacciones/{id}", 3L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllShouldReturnList() throws Exception {
        when(transaccionService.findAll()).thenReturn(List.of(new TransaccionDTO()));

        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk());
    }

    @Test
    void approveShouldReturnUpdated() throws Exception {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(4L);
        when(transaccionService.approve(4L)).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/api/transacciones/{id}/approve", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L));
    }

    @Test
    void rejectShouldReturnNotFound() throws Exception {
        when(transaccionService.reject(5L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/transacciones/{id}/reject", 5L))
                .andExpect(status().isNotFound());

        verify(transaccionService).reject(5L);
    }
}

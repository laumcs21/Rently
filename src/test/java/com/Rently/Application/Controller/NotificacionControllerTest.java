package com.Rently.Application.Controller;


import com.Rently.Business.DTO.NotificacionDTO;
import com.Rently.Business.Service.NotificacionService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService notificacionService;

    @Test
    void createShouldReturnCreatedNotification() throws Exception {
        NotificacionDTO request = new NotificacionDTO();
        request.setMensaje("Nueva reserva");
        NotificacionDTO response = new NotificacionDTO();
        response.setId(3L);
        response.setMensaje("Nueva reserva");

        when(notificacionService.create(any(NotificacionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.mensaje").value("Nueva reserva"));
    }

    @Test
    void getByIdShouldReturnNotFound() throws Exception {
        when(notificacionService.findById(8L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notificaciones/{id}", 8L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllShouldReturnNotifications() throws Exception {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(9L);
        when(notificacionService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9L));
    }

    @Test
    void updateShouldReturnNotFoundWhenMissing() throws Exception {
        NotificacionDTO request = new NotificacionDTO();
        when(notificacionService.update(eq(2L), any(NotificacionDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/notificaciones/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/notificaciones/{id}", 5L))
                .andExpect(status().isNoContent());

        verify(notificacionService).delete(5L);
    }

    @Test
    void markAllReadShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/notificaciones/usuario/{userId}/mark-all-read", 6L))
                .andExpect(status().isNoContent());

        verify(notificacionService).markAllAsRead(6L);
    }

    @Test
    void unreadByUserShouldReturnList() throws Exception {
        when(notificacionService.findUnreadNotificationsByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/notificaciones/usuario/{userId}/unread", 1L))
                .andExpect(status().isOk());
    }
}

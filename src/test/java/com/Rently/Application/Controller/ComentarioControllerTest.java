package com.Rently.Application.Controller;


import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.Service.ComentarioService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComentarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComentarioService comentarioService;

    @Test
    void createShouldReturnCreatedComentario() throws Exception {
        ComentarioDTO request = new ComentarioDTO();
        request.setComentario("Excelente");
        ComentarioDTO response = new ComentarioDTO();
        response.setId(5L);
        response.setComentario("Excelente");

        when(comentarioService.create(any(ComentarioDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.comentario").value("Excelente"));

        verify(comentarioService).create(any(ComentarioDTO.class));
    }

    @Test
    void getByAlojamientoShouldReturnList() throws Exception {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(2L);
        when(comentarioService.findByAlojamientoId(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/comentarios/alojamiento/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void addResponseShouldDelegateToService() throws Exception {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(7L);
        when(comentarioService.addResponse(eq(7L), eq("Gracias"), eq("Bearer token")))
                .thenReturn(dto);

        mockMvc.perform(post("/api/comentarios/{id}/respuesta", 7L)
                        .param("response", "Gracias")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L));

        verify(comentarioService).addResponse(7L, "Gracias", "Bearer token");
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/comentarios/{id}", 4L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(comentarioService).delete(4L, "Bearer token");
    }
}

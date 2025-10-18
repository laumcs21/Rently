package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.AnfitrionDTO;
import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.Service.AnfitrionService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.ComentarioService;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Business.Service.FotoPerfilService;
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
import org.mockito.Mockito;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @MockBean
    private AlojamientoService alojamientoService;

    @MockBean
    private ComentarioService comentarioService;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private FotoPerfilService fotoPerfilService;

    @Test
    void listarReservasShouldReturnOk() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        when(anfitrionService.findById(1L)).thenReturn(Optional.of(anfitrion));

        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        when(alojamientoService.findByHost(1L)).thenReturn(List.of(alojamiento));

        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(9001L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaInicio(LocalDate.of(2025, 11, 1));
        reserva.setFechaFin(LocalDate.of(2025, 11, 5));

        when(reservaService.findByAlojamientoId(501L)).thenReturn(List.of(reserva));

        mockMvc.perform(get("/api/anfitriones/{id}/reservas", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9001L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }


    @Test
    void aprobarReservaShouldReturnOk() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        when(anfitrionService.findById(1L)).thenReturn(Optional.of(anfitrion));

        ReservaDTO pendiente = new ReservaDTO();
        pendiente.setId(9001L);
        pendiente.setAlojamientoId(501L);
        pendiente.setEstado(EstadoReserva.PENDIENTE);

        ReservaDTO confirmada = new ReservaDTO();
        confirmada.setId(9001L);
        confirmada.setAlojamientoId(501L);
        confirmada.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaService.findById(9001L)).thenReturn(Optional.of(pendiente), Optional.of(confirmada));

        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        alojamiento.setAnfitrionId(1L);
        when(alojamientoService.findById(501L)).thenReturn(Optional.of(alojamiento));

        when(reservaService.updateState(9001L, EstadoReserva.CONFIRMADA)).thenReturn(true);

        mockMvc.perform(put("/api/anfitriones/{id}/reservas/{reservaId}/aprobar", 1L, 9001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void rechazarReservaShouldReturnOk() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        when(anfitrionService.findById(1L)).thenReturn(Optional.of(anfitrion));

        ReservaDTO pendiente = new ReservaDTO();
        pendiente.setId(9001L);
        pendiente.setAlojamientoId(501L);
        pendiente.setEstado(EstadoReserva.PENDIENTE);

        ReservaDTO rechazada = new ReservaDTO();
        rechazada.setId(9001L);
        rechazada.setAlojamientoId(501L);
        rechazada.setEstado(EstadoReserva.RECHAZADA);

        when(reservaService.findById(9001L)).thenReturn(Optional.of(pendiente), Optional.of(rechazada));

        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        alojamiento.setAnfitrionId(1L);
        when(alojamientoService.findById(501L)).thenReturn(Optional.of(alojamiento));

        when(reservaService.updateState(9001L, EstadoReserva.RECHAZADA)).thenReturn(true);

        String body = """
                { "motivo": "Fechas no disponibles por mantenimiento" }
                """;

        mockMvc.perform(put("/api/anfitriones/{id}/reservas/{reservaId}/rechazar", 1L, 9001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9001))
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivo").value("Fechas no disponibles por mantenimiento"));
    }

    @Test
    void listarReservasShouldReturnNotFoundWhenHostMissing() throws Exception {
        when(anfitrionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/anfitriones/{id}/reservas", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Anfitrión no encontrado"));
    }
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
    void obtenerAnfitrionShouldReturnOk() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        anfitrion.setNombre("Carlos");
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        mockMvc.perform(get("/api/anfitriones/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void actualizarAnfitrionShouldReturnOk() throws Exception {
        AnfitrionDTO actualizado = new AnfitrionDTO();
        actualizado.setId(2L);
        actualizado.setNombre("Anfitri�n Actualizado");

        when(anfitrionService.update(Mockito.eq(2L), any(AnfitrionDTO.class)))
                .thenReturn(java.util.Optional.of(actualizado));

        mockMvc.perform(put("/api/anfitriones/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnfitrionDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Anfitri�n Actualizado"));
    }

    @Test
    void actualizarAnfitrionShouldReturnBadRequestOnInvalidData() throws Exception {
        when(anfitrionService.update(Mockito.eq(2L), any(AnfitrionDTO.class)))
                .thenThrow(new IllegalArgumentException("Datos inv�lidos"));

        mockMvc.perform(put("/api/anfitriones/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnfitrionDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inv�lidos"));
    }

    @Test
    void actualizarAnfitrionShouldReturnNotFoundWhenMissing() throws Exception {
        when(anfitrionService.update(Mockito.eq(99L), any(AnfitrionDTO.class)))
                .thenThrow(new RuntimeException("Anfitri�n no encontrado"));

        mockMvc.perform(put("/api/anfitriones/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnfitrionDTO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Anfitri�n no encontrado"));
    }
@Test
    void crearAlojamientoShouldReturnCreated() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        AlojamientoDTO created = new AlojamientoDTO();
        created.setId(501L);
        created.setTitulo("Apartamento céntrico");
        when(alojamientoService.create(any(AlojamientoDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/anfitriones/{id}/alojamientos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlojamientoDTO())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/anfitriones/1/alojamientos/501")))
                .andExpect(jsonPath("$.id").value(501L));
    }

    @Test
    void crearAlojamientoShouldReturnNotFoundWhenHostMissing() throws Exception {
        when(anfitrionService.findById(99L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/api/anfitriones/{id}/alojamientos", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlojamientoDTO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
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
    @Test
    void eliminarAlojamientoShouldReturnNoContent() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        alojamiento.setAnfitrionId(1L);
        when(alojamientoService.findById(501L)).thenReturn(java.util.Optional.of(alojamiento));
        when(alojamientoService.delete(501L)).thenReturn(true);

        mockMvc.perform(delete("/api/anfitriones/{id}/alojamientos/{alojamientoId}", 1L, 501L))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarAlojamientoShouldReturnBadRequestWhenFutureReservations() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        alojamiento.setAnfitrionId(1L);
        when(alojamientoService.findById(501L)).thenReturn(java.util.Optional.of(alojamiento));
        doThrow(new IllegalStateException("No se puede eliminar alojamiento con reservas futuras"))
                .when(alojamientoService).delete(501L);

        mockMvc.perform(delete("/api/anfitriones/{id}/alojamientos/{alojamientoId}", 1L, 501L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede eliminar alojamiento con reservas futuras"));
    }

    @Test
    void eliminarAlojamientoShouldReturnNotFoundWhenAlojamientoMissing() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));
        when(alojamientoService.findById(501L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/anfitriones/{id}/alojamientos/{alojamientoId}", 1L, 501L))
                .andExpect(status().isNotFound());
    }

    @Test
    void responderComentarioShouldReturnOkWithBody() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        ComentarioDTO respuesta = new ComentarioDTO();
        respuesta.setId(770L);
        respuesta.setComentario("Excelente alojamiento, muy limpio y céntrico.");
        respuesta.setRespuesta("Muchas gracias por tu comentario, esperamos verte de nuevo.");

        when(comentarioService.addResponse(
                Mockito.eq(770L),
                Mockito.eq("Muchas gracias por tu comentario, esperamos verte de nuevo."),
                Mockito.anyString()
        )).thenReturn(respuesta);

        String body = """
                {
                  "respuesta": "Muchas gracias por tu comentario, esperamos verte de nuevo."
                }
                """;

        mockMvc.perform(post("/api/anfitriones/{id}/comentarios/{comentarioId}/responder", 1L, 770L)
                        .header("Authorization", "Bearer token-anfitrion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(770))
                .andExpect(jsonPath("$.comentario").value("Excelente alojamiento, muy limpio y céntrico."))
                .andExpect(jsonPath("$.respuesta").value("Muchas gracias por tu comentario, esperamos verte de nuevo."));
    }

    @Test
    void responderComentarioShouldReturnUnauthorizedWhenMissingToken() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        String body = """
                { "respuesta": "Gracias" }
                """;

        mockMvc.perform(post("/api/anfitriones/{id}/comentarios/{comentarioId}/responder", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token de autenticación requerido."));

        Mockito.verify(comentarioService, Mockito.never()).addResponse(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void responderComentarioShouldReturnNotFoundWhenHostMissing() throws Exception {
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.empty());

        String body = """
                { "respuesta": "Gracias" }
                """;

        mockMvc.perform(post("/api/anfitriones/{id}/comentarios/{comentarioId}/responder", 1L, 10L)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Anfitrión no encontrado."));

        Mockito.verify(comentarioService, Mockito.never()).addResponse(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void responderComentarioShouldReturnConflictWhenAlreadyResponded() throws Exception {
        AnfitrionDTO anfitrion = new AnfitrionDTO();
        anfitrion.setId(1L);
        when(anfitrionService.findById(1L)).thenReturn(java.util.Optional.of(anfitrion));

        Mockito.when(comentarioService.addResponse(Mockito.eq(770L), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new IllegalStateException("No es posible responder un comentario que ya tiene respuesta."));

        String body = """
                { "respuesta": "Gracias nuevamente" }
                """;

        mockMvc.perform(post("/api/anfitriones/{id}/comentarios/{comentarioId}/responder", 1L, 770L)
                        .header("Authorization", "Bearer token-anfitrion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No es posible responder un comentario que ya tiene respuesta."));
    }
}





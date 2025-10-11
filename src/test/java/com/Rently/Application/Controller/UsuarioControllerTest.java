package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.AuthService;
import com.Rently.Business.Service.ComentarioService;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Business.Service.UsuarioService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @MockBean
    private AuthService authService;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private ComentarioService comentarioService;

    @MockBean
    private AlojamientoService alojamientoService;
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
    void crearReservaShouldReturnCreated() throws Exception {
        ReservaDTO request = new ReservaDTO();
        request.setUsuarioId(1L);
        request.setAlojamientoId(501L);
        request.setFechaInicio(LocalDate.of(2025, 11, 1));
        request.setFechaFin(LocalDate.of(2025, 11, 5));
        request.setNumeroHuespedes(2);

        ReservaDTO response = new ReservaDTO();
        response.setId(9001L);
        response.setFechaInicio(request.getFechaInicio());
        response.setFechaFin(request.getFechaFin());

        when(reservaService.create(any(ReservaDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/usuarios/reserva/9001")))
                .andExpect(jsonPath("$.id").value(9001L))
                .andExpect(jsonPath("$.fechaInicio").value("2025-11-01"))
                .andExpect(jsonPath("$.fechaFin").value("2025-11-05"));
    }

    @Test
    void crearReservaShouldReturnConflictWhenDatesOverlap() throws Exception {
        when(reservaService.create(any(ReservaDTO.class)))
                .thenThrow(new RuntimeException("Las fechas seleccionadas se solapan con otra reserva"));

        mockMvc.perform(post("/api/usuarios/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaDTO())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Las fechas seleccionadas se solapan con otra reserva"));
    }

    @Test
    void crearReservaShouldReturnBadRequestOnInvalidData() throws Exception {
        when(reservaService.create(any(ReservaDTO.class)))
                .thenThrow(new IllegalArgumentException("El usuario es obligatorio."));

        mockMvc.perform(post("/api/usuarios/reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El usuario es obligatorio."));
    }

    @Test
    void cancelarReservaShouldReturnOkWithMessage() throws Exception {
        ReservaDTO cancelada = new ReservaDTO();
        cancelada.setId(9001L);
        cancelada.setEstado(EstadoReserva.CANCELADA);

        when(reservaService.cancelByUser(9001L)).thenReturn(cancelada);

        mockMvc.perform(post("/api/usuarios/reserva/{id}", 9001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Reserva cancelada"))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void cancelarReservaShouldReturnConflictWhenWithin48Hours() throws Exception {
        when(reservaService.cancelByUser(9001L))
                .thenThrow(new IllegalStateException("No se pueden cancelar reservas con menos de 48 horas de anticipaciï¿½n"));

        mockMvc.perform(post("/api/usuarios/reserva/{id}", 9001L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No se pueden cancelar reservas con menos de 48 horas de anticipaciï¿½n"));
    }

    @Test
    void listarReservasShouldReturnPagedContent() throws Exception {
        ReservaDTO r1 = new ReservaDTO();
        r1.setId(9001L);
        r1.setEstado(EstadoReserva.CONFIRMADA);
        r1.setFechaInicio(LocalDate.of(2025, 11, 1));
        r1.setFechaFin(LocalDate.of(2025, 11, 5));

        ReservaDTO r2 = new ReservaDTO();
        r2.setId(9002L);
        r2.setEstado(EstadoReserva.CANCELADA);
        r2.setFechaInicio(LocalDate.of(2025, 10, 1));
        r2.setFechaFin(LocalDate.of(2025, 10, 3));

        when(reservaService.findByUserId(77L)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/usuarios/{id}/reserva", 77L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9001L))
                .andExpect(jsonPath("$.content[1].estado").value("CANCELADA"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listarReservasShouldFilterByEstado() throws Exception {
        ReservaDTO r1 = new ReservaDTO();
        r1.setId(9001L);
        r1.setEstado(EstadoReserva.CONFIRMADA);
        r1.setFechaInicio(LocalDate.of(2025, 11, 1));
        r1.setFechaFin(LocalDate.of(2025, 11, 5));

        ReservaDTO r2 = new ReservaDTO();
        r2.setId(9002L);
        r2.setEstado(EstadoReserva.CANCELADA);
        r2.setFechaInicio(LocalDate.of(2025, 10, 1));
        r2.setFechaFin(LocalDate.of(2025, 10, 3));

        when(reservaService.findByUserId(77L)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/usuarios/{id}/reserva", 77L).param("estado", "CONFIRMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].estado").value("CONFIRMADA"));
    }

    @Test
    void listarReservasShouldReturnBadRequestWhenEmpty() throws Exception {
        when(reservaService.findByUserId(77L)).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios/{id}/reserva", 77L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El usuario no tiene reservas registradas"));
    }

    @Test
    void crearComentarioShouldReturnOkWithBody() throws Exception {
        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(9001L);
        reserva.setUsuarioId(77L);
        reserva.setAlojamientoId(55L);
        reserva.setEstado(EstadoReserva.FINALIZADA);

        ComentarioDTO response = new ComentarioDTO();
        response.setId(770L);
        response.setCalificacion(5);
        response.setComentario("Excelente alojamiento, muy limpio y centrico.");

        when(reservaService.findById(9001L)).thenReturn(Optional.of(reserva));
        when(comentarioService.create(any(ComentarioDTO.class))).thenReturn(response);

        String body = """
                {
                  "calificacion": 5,
                  "comentario": "Excelente alojamiento, muy limpio y centrico."
                }
                """;

        mockMvc.perform(post("/api/usuarios/reserva/{id}/comentario", 9001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(770))
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.comentario").value("Excelente alojamiento, muy limpio y centrico."));
    }

    @Test
    void crearComentarioShouldReturnConflictWhenReservaNotFinalizada() throws Exception {
        ReservaDTO reserva = new ReservaDTO();
        reserva.setId(9001L);
        reserva.setUsuarioId(77L);
        reserva.setAlojamientoId(55L);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaService.findById(9001L)).thenReturn(Optional.of(reserva));

        String body = """
                {
                  "calificacion": 5,
                  "comentario": "Todo bien"
                }
                """;

        mockMvc.perform(post("/api/usuarios/reserva/{id}/comentario", 9001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Solo se pueden comentar reservas finalizadas."));

        verify(comentarioService, never()).create(any());
    }

    @Test
    void crearComentarioShouldReturnNotFoundWhenReservaMissing() throws Exception {
        when(reservaService.findById(9001L)).thenReturn(Optional.empty());

        String body = """
                {
                  "calificacion": 4,
                  "comentario": "Sin reserva"
                }
                """;

        mockMvc.perform(post("/api/usuarios/reserva/{id}/comentario", 9001L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reserva no encontrada"));

        verify(comentarioService, never()).create(any());
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
    void obtenerDetalleAlojamientoShouldReturnOk() throws Exception {
        AlojamientoDTO alojamiento = new AlojamientoDTO();
        alojamiento.setId(501L);
        alojamiento.setTitulo("Apartamento cÃ©ntrico");
        alojamiento.setCiudad("MedellÃ­n");
        alojamiento.setDireccion("Calle 10 #20-30");
        alojamiento.setPrecioPorNoche(120.0);
        alojamiento.setCapacidadMaxima(4);
        alojamiento.setImagenes(List.of("imagen-1.jpg"));
        alojamiento.setServiciosId(List.of(1L, 2L, 3L));

        when(alojamientoService.findById(501L)).thenReturn(Optional.of(alojamiento));

        mockMvc.perform(get("/api/usuarios/alojamiento/{id}", 501L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501L))
                .andExpect(jsonPath("$.titulo").value("Apartamento cÃ©ntrico"))
                .andExpect(jsonPath("$.serviciosId[0]").value(1L));
    }

    @Test
    void obtenerDetalleAlojamientoShouldReturnNotFound() throws Exception {
        when(alojamientoService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/alojamiento/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alojamiento no encontrado"));
    }

    @Test
    void actualizarUsuarioShouldReturnUpdated() throws Exception {
        UsuarioDTO updated = new UsuarioDTO();
        updated.setId(3L);
        updated.setNombre("Nuevo");

        when(usuarioService.updateUserProfile(eq(3L), any(UsuarioDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/usuarios/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }


    @Test
    void actualizarUsuarioShouldReturnBadRequestOnInvalidData() throws Exception {
        when(usuarioService.updateUserProfile(eq(3L), any(UsuarioDTO.class)))
                .thenThrow(new IllegalArgumentException("Datos invalidos"));

        mockMvc.perform(put("/api/usuarios/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"));
    }

    @Test
    void actualizarUsuarioShouldReturnNotFoundWhenMissing() throws Exception {
        when(usuarioService.updateUserProfile(eq(9L), any(UsuarioDTO.class)))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/api/usuarios/{id}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioDTO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    void eliminarUsuarioShouldReturnNoContent() throws Exception {
        doNothing().when(usuarioService).deleteUser(4L);

        mockMvc.perform(delete("/api/usuarios/{id}", 4L))
                .andExpect(status().isNoContent());
    }
}




package com.Rently.Application.Controller;


import com.Rently.Business.DTO.AdministradorDTO;
import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.UsuarioDTO;
import com.Rently.Business.Service.AdministradorService;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.UsuarioService;
import com.Rently.Business.Service.FotoPerfilService;
import com.Rently.Persistence.Entity.Rol;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private AlojamientoService alojamientoService;

    @MockBean
    private FotoPerfilService fotoPerfilService;

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
    void crearUsuarioGestionShouldReturnCreated() throws Exception {
        var request = new UsuarioDTO();
        request.setNombre("Usuario Creado por Admin");
        request.setEmail("nuevo.usuario@mail.com");
        request.setTelefono("3005554444");
        request.setContrasena("ClaveAdmin123");

        var creado = new UsuarioDTO();
        creado.setId(777L);
        creado.setNombre("Usuario Creado por Admin");
        creado.setEmail("nuevo.usuario@mail.com");

        when(usuarioService.registerUser(any(UsuarioDTO.class))).thenReturn(creado);

        mockMvc.perform(post("/api/administradores/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/administradores/usuarios/777")))
                .andExpect(jsonPath("$.id").value(777))
                .andExpect(jsonPath("$.email").value("nuevo.usuario@mail.com"));
    }

    @Test
    void eliminarUsuarioShouldReturnBadRequestWhenUserMissing() throws Exception {
        doThrow(new RuntimeException("Usuario no encontrado con id: 999")).when(usuarioService).deleteUser(999L);

        mockMvc.perform(delete("/api/administradores/usuarios/{id}", 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El usuario no existe"));
    }

    @Test
    void editarUsuarioShouldReturnOk() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setNombre("Usuario Modificado por Admin");

        UsuarioDTO actualizado = new UsuarioDTO();
        actualizado.setId(777L);
        actualizado.setNombre("Usuario Modificado por Admin");

        when(usuarioService.updateUserProfile(eq(777L), any(UsuarioDTO.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/administradores/usuarios/{id}", 777L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(777))
                .andExpect(jsonPath("$.nombre").value("Usuario Modificado por Admin"));
    }

    @Test
    void listarUsuariosShouldFilterByRol() throws Exception {
        UsuarioDTO admin = new UsuarioDTO();
        admin.setId(5L);
        admin.setNombre("Admin Uno");
        admin.setRol(Rol.ADMINISTRADOR);

        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setId(101L);
        usuario1.setNombre("Juan Pérez");
        usuario1.setRol(Rol.USUARIO);

        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setId(777L);
        usuario2.setNombre("Usuario Creado por Admin");
        usuario2.setRol(Rol.USUARIO);

        when(usuarioService.findAllUsers()).thenReturn(List.of(admin, usuario1, usuario2));

        mockMvc.perform(get("/api/administradores/usuarios")
                        .param("filter", "rol:USUARIO")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(101))
                .andExpect(jsonPath("$.content[1].id").value(777));
    }

    @Test
    void listarAlojamientosShouldApplyFilters() throws Exception {
        AlojamientoDTO alojamiento1 = new AlojamientoDTO();
        alojamiento1.setId(9001L);
        alojamiento1.setTitulo("Casa en la playa");
        alojamiento1.setCiudad("Amneia");
        alojamiento1.setAnfitrionId(101L);

        AlojamientoDTO alojamiento2 = new AlojamientoDTO();
        alojamiento2.setId(9002L);
        alojamiento2.setTitulo("Departamento centro");
        alojamiento2.setCiudad("Bogotá");
        alojamiento2.setAnfitrionId(202L);

        when(alojamientoService.findAll()).thenReturn(List.of(alojamiento1, alojamiento2));

        mockMvc.perform(get("/api/administradores/alojamientos")
                        .param("anfitrionId", "101")
                        .param("titulo", "casa")
                        .param("ciudad", "amneia")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(9001));
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



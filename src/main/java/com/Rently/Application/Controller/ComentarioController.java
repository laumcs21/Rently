package com.Rently.Application.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Rently.Business.DTO.ComentarioDTO;
import com.Rently.Business.Service.ComentarioService;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping
    public ResponseEntity<ComentarioDTO> create(@RequestBody ComentarioDTO dto) {
        // La interfaz exige solo ComentarioDTO (sin token)
        return ResponseEntity.ok(comentarioService.create(dto));
    }

    @GetMapping("/alojamiento/{alojamientoId}")
    public ResponseEntity<List<ComentarioDTO>> getByAlojamiento(@PathVariable Long alojamientoId) {
        return ResponseEntity.ok(comentarioService.findByAlojamientoId(alojamientoId));
    }

    @PostMapping("/{id}/respuesta")
    public ResponseEntity<ComentarioDTO> addResponse(
            @PathVariable Long id,
            @RequestParam String response,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseEntity.ok(comentarioService.addResponse(id, response, token));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        comentarioService.delete(id, token);
        return ResponseEntity.noContent().build();
    }
}

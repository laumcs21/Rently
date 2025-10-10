package com.Rently.Application.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.AlojamientoService;
@RestController
@RequestMapping("/api/alojamientos")
public class AlojamientoController {

    
    public AlojamientoController(AlojamientoService alojamientoService) {
        this.alojamientoService = alojamientoService;
    }
private final AlojamientoService alojamientoService;

    @PostMapping
    public ResponseEntity<AlojamientoDTO> create(@RequestBody AlojamientoDTO dto) {
        return ResponseEntity.ok(alojamientoService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlojamientoDTO> getById(@PathVariable Long id) {
        Optional<AlojamientoDTO> found = alojamientoService.findById(id);
        return found.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AlojamientoDTO>> getAll() {
        return ResponseEntity.ok(alojamientoService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlojamientoDTO> update(@PathVariable Long id, @RequestBody AlojamientoDTO dto) {
        Optional<AlojamientoDTO> updated = alojamientoService.update(id, dto);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = alojamientoService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

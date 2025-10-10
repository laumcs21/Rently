package com.Rently.Application.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Rently.Business.DTO.ServicioDTO;
import com.Rently.Business.Service.ServicioService;
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    
    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }
private final ServicioService servicioService;

    @PostMapping
    public ResponseEntity<ServicioDTO> create(@RequestBody ServicioDTO dto) {
        return ResponseEntity.ok(servicioService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ServicioDTO>> getAll() {
        return ResponseEntity.ok(servicioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO> getById(@PathVariable Long id) {
        ServicioDTO dto = servicioService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioDTO> update(@PathVariable Long id, @RequestBody ServicioDTO dto) {
        ServicioDTO updated = servicioService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = servicioService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

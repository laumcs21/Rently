package com.Rently.Application.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Business.Service.ReservaService;
import com.Rently.Persistence.Entity.EstadoReserva;
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }
private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaDTO> create(@RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(reservaService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> getById(@PathVariable Long id) {
        Optional<ReservaDTO> found = reservaService.findById(id);
        return found.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> getAll() {
        return ResponseEntity.ok(reservaService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaDTO> update(@PathVariable Long id, @RequestBody ReservaDTO dto) {
        Optional<ReservaDTO> updated = reservaService.update(id, dto);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> updateState(@PathVariable Long id, @RequestParam EstadoReserva estado) {
        boolean ok = reservaService.updateState(id, estado);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = reservaService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

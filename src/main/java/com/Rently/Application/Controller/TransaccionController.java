package com.Rently.Application.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Rently.Business.DTO.TransaccionDTO;
import com.Rently.Business.Service.TransaccionService;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping
    public ResponseEntity<TransaccionDTO> create(@RequestBody TransaccionDTO dto) {
        return ResponseEntity.ok(transaccionService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDTO> getById(@PathVariable Long id) {
        Optional<TransaccionDTO> found = transaccionService.findById(id);
        return found.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TransaccionDTO>> getAll() {
        return ResponseEntity.ok(transaccionService.findAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TransaccionDTO> approve(@PathVariable Long id) {
        Optional<TransaccionDTO> updated = transaccionService.approve(id);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TransaccionDTO> reject(@PathVariable Long id) {
        Optional<TransaccionDTO> updated = transaccionService.reject(id);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
   }
   
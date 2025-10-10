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

import com.Rently.Business.DTO.NotificacionDTO;
import com.Rently.Business.Service.NotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping
    public ResponseEntity<NotificacionDTO> create(@RequestBody NotificacionDTO dto) {
        return ResponseEntity.ok(notificacionService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacionDTO> getById(@PathVariable Long id) {
        Optional<NotificacionDTO> found = notificacionService.findById(id);
        return found.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> getAll() {
        return ResponseEntity.ok(notificacionService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificacionDTO> update(@PathVariable Long id, @RequestBody NotificacionDTO dto) {
        Optional<NotificacionDTO> updated = notificacionService.update(id, dto);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // El servicio no devuelve boolean → no intentes asignar
        notificacionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuario/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        notificacionService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{userId}/unread")
    public ResponseEntity<List<NotificacionDTO>> unreadByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificacionService.findUnreadNotificationsByUserId(userId));
    }
}

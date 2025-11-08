package com.Rently.Application.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.Rently.Business.DTO.ListingCardDTO;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Rently.Business.DTO.AlojamientoDTO;

@RestController
@RequestMapping("/api/alojamientos")
public class AlojamientoController {


    public AlojamientoController(AlojamientoService alojamientoService, ImageService imageService) {
        this.alojamientoService = alojamientoService;
        this.imageService = imageService;
    }

    private final AlojamientoService alojamientoService;
    private final ImageService imageService;


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

    /**
     * GET /alojamientos/featured?precioMax=300000&servicios=wifi,parqueadero&limit=12
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ListingCardDTO>> getFeatured(
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String servicios,
            @RequestParam(required = false) Integer limit
    ) {
        List<String> svc = (servicios == null || servicios.isBlank())
                ? List.of()
                : Arrays.stream(servicios.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        return ResponseEntity.ok(
                alojamientoService.getFeaturedRandom(precioMax, svc, limit)
        );
    }

        @PostMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('ANFITRION')")
        public ResponseEntity<?> subirImagen(
                @PathVariable Long id,
                @RequestParam("file") MultipartFile file
        ) {
            try {
                System.out.println("Archivo recibido: " + (file != null ? file.getOriginalFilename() : "null"));

                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("El archivo de imagen es obligatorio.");
                }

                // 1. subir a Cloudinary
                ImageService.Upload upload = imageService.uploadImage(file, "alojamientos/" + id, null);

                // 2. guardar url en BD
                alojamientoService.agregarImagen(id, upload.getUrl());

                return ResponseEntity.ok(upload);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError()
                        .body("Error al subir la imagen: " + e.getMessage());
            }
        }

    @GetMapping("/mis")
    public ResponseEntity<?> getMisAlojamientos(@RequestParam("anfitrionId") Long anfitrionId) {
        System.out.println(">>> /api/alojamientos/mis?anfitrionId=" + anfitrionId);
        try {
            List<AlojamientoDTO> lista = alojamientoService.findByHost(anfitrionId);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            // 👇 aquí vas a ver el error REAL en la respuesta
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en /api/alojamientos/mis: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/imagenes")
    public ResponseEntity<Void> eliminarImagen(
            @PathVariable Long id,
            @RequestParam("url") String url
    ) {
        alojamientoService.eliminarImagen(id, url);
        return ResponseEntity.noContent().build();
    }

}

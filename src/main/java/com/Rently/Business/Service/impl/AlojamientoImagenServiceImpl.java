
package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AlojamientoImagenDTO;
import com.Rently.Business.Service.AlojamientoImagenService;
import com.Rently.Business.Service.ImageService; // Tu implementación real es CloudinaryImageServiceImpl
import com.Rently.Persistence.Entity.Alojamiento;
import com.Rently.Persistence.Entity.AlojamientoImagen;
import com.Rently.Persistence.Mapper.AlojamientoImagenMapper;
import com.Rently.Persistence.Repository.AlojamientoImagenRepository;
import com.Rently.Persistence.Repository.AlojamientoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlojamientoImagenServiceImpl implements AlojamientoImagenService {

    private final AlojamientoRepository alojamientoRepo;
    private final AlojamientoImagenRepository imagenRepo;
    private final AlojamientoImagenMapper mapper;
    private final ImageService imageService;

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoImagenDTO> listar(Long anfitrionId, Long alojamientoId) {
        validarPropiedad(anfitrionId, alojamientoId);
        return imagenRepo.findByAlojamientoIdOrderByOrdenAsc(alojamientoId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public AlojamientoImagenDTO subir(Long anfitrionId, Long alojamientoId, MultipartFile file, Integer orden) throws Exception {
        Alojamiento aloj = validarPropiedad(anfitrionId, alojamientoId);

        // Subida a Cloudinary usando tu ImageService existente
        var up = imageService.uploadImage(file, "rently/alojamientos/" + alojamientoId, "aloj_" + alojamientoId);

        // Crear entidad y calcular 'orden' si no llega
        AlojamientoImagen img = new AlojamientoImagen();
        img.setAlojamiento(aloj);
        img.setUrl(up.url());

        int nextOrden = (orden != null && orden > 0)
                ? orden
                : imagenRepo.findByAlojamientoIdOrderByOrdenAsc(alojamientoId)
                .stream()
                .map(AlojamientoImagen::getOrden)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;

        img.setOrden(nextOrden);

        return mapper.toDTO(imagenRepo.save(img));
    }

    @Override
    public void borrar(Long anfitrionId, Long alojamientoId, Long imagenId) throws Exception {
        validarPropiedad(anfitrionId, alojamientoId);

        AlojamientoImagen img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada"));

        if (!img.getAlojamiento().getId().equals(alojamientoId)) {
            throw new IllegalArgumentException("La imagen no pertenece al alojamiento");
        }

        String publicId = imageService.tryExtractPublicId(img.getUrl()); // ya lo tienes en tu ImageService
        imageService.deleteByPublicId(publicId);

        imagenRepo.delete(img);
    }

    private Alojamiento validarPropiedad(Long anfitrionId, Long alojamientoId) {
        Alojamiento aloj = alojamientoRepo.findByIdAndEliminadoFalse(alojamientoId)
                .orElseThrow(() -> new EntityNotFoundException("Alojamiento no encontrado"));

        if (aloj.getAnfitrion() == null || !aloj.getAnfitrion().getId().equals(anfitrionId)) {
            throw new IllegalArgumentException("El alojamiento no pertenece al anfitrión");
        }
        return aloj;
    }
}

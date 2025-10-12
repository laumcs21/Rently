package com.Rently.Business.Service.impl;

import com.Rently.Business.Service.FotoPerfilService;
import com.Rently.Business.Service.ImageService;
import com.Rently.Persistence.Entity.Persona;
import com.Rently.Persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class FotoPerfilServiceImpl implements FotoPerfilService {

    private final PersonaRepository personaRepository;
    private final ImageService imageService;

    @Override
    public String actualizarFotoPerfil(Long personaId, MultipartFile file) throws Exception {
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada: " + personaId));

        // 1) si ya hay foto, elimina en Cloudinary
        if (StringUtils.isNotBlank(p.getFotoPerfil())) {
            String publicId = imageService.tryExtractPublicId(p.getFotoPerfil());
            if (StringUtils.isNotBlank(publicId)) {
                imageService.deleteByPublicId(publicId);
            }
        }

        // 2) sube nueva imagen: rently/perfiles/persona_{id}
        String folder = "perfiles";
        String publicIdHint = "persona_" + personaId;
        ImageService.Upload up = imageService.uploadImage(file, folder, publicIdHint);

        // 3) guarda URL segura en la entidad
        p.setFotoPerfil(up.url());
        personaRepository.save(p);

        return up.url();
    }

    @Override
    public void eliminarFotoPerfil(Long personaId) throws Exception {
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada: " + personaId));

        if (StringUtils.isNotBlank(p.getFotoPerfil())) {
            String publicId = imageService.tryExtractPublicId(p.getFotoPerfil());
            if (StringUtils.isNotBlank(publicId)) {
                imageService.deleteByPublicId(publicId);
            }
            p.setFotoPerfil(null);
            personaRepository.save(p);
        }
    }
}

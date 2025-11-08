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

        // Eliminar anterior si existe
        if (StringUtils.isNotBlank(p.getFotoPerfil())) {
            String publicId = imageService.tryExtractPublicId(p.getFotoPerfil());
            imageService.deleteByPublicId(publicId);
        }

        // Subir nueva a carpeta rently/perfiles
        ImageService.Upload up = imageService.uploadImage(file, "perfiles", "persona_" + personaId);

        // Persistir URL
        p.setFotoPerfil(up.getUrl());
        personaRepository.save(p);

        return up.getUrl();
    }

    @Override
    public void eliminarFotoPerfil(Long personaId) throws Exception {
        Persona p = personaRepository.findById(personaId)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada: " + personaId));

        if (StringUtils.isNotBlank(p.getFotoPerfil())) {
            String publicId = imageService.tryExtractPublicId(p.getFotoPerfil());
            imageService.deleteByPublicId(publicId);
            p.setFotoPerfil(null);
            personaRepository.save(p);
        }
    }
}

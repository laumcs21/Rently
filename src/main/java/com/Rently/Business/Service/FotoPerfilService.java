package com.Rently.Business.Service;

import org.springframework.web.multipart.MultipartFile;

public interface FotoPerfilService {
    /** Sube nueva foto y actualiza Persona.fotoPerfil, borrando la anterior si existe. Devuelve la URL segura. */
    String actualizarFotoPerfil(Long personaId, MultipartFile file) throws Exception;

    /** Elimina la foto del perfil en Cloudinary (si existe) y limpia el campo. */
    void eliminarFotoPerfil(Long personaId) throws Exception;
}

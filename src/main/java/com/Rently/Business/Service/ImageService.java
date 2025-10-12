package com.Rently.Business.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Interfaz para el servicio de gestión de imágenes.
 * Mantiene compatibilidad con los métodos existentes (upload/delete)
 * y añade operaciones más ricas para carpeta/publicId y utilidades.
 */
public interface ImageService {

    /* -------------------- EXISTENTES (compatibilidad) -------------------- */

    /**
     * Sube una imagen con parámetros por defecto.
     * @return Mapa con resultado de Cloudinary (secure_url, public_id, etc.)
     */
    Map upload(MultipartFile file) throws IOException;

    /**
     * Elimina por public_id (forma antigua que devuelve Map).
     */
    Map delete(String publicId) throws IOException;


    /* -------------------- NUEVOS (usados por FotoPerfilServiceImpl) -------------------- */

    /**
     * Sube una imagen permitiendo definir carpeta lógica y una pista de publicId.
     * Devuelve un objeto tipado con url/publicId/format.
     */
    Upload uploadImage(MultipartFile file, String folder, String publicIdHint) throws IOException;

    /**
     * Elimina por public_id (forma tipada/void).
     */
    void deleteByPublicId(String publicId) throws IOException;

    /**
     * Intenta extraer el public_id (sin extensión) desde un secure_url de Cloudinary.
     * Ej: https://res.cloudinary.com/<cloud>/image/upload/v.../rently/perfiles/persona_15.jpg
     * => rently/perfiles/persona_15
     */
    default String tryExtractPublicId(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            int i = url.indexOf("/upload/");
            if (i < 0) return null;
            String path = url.substring(i + 8); // después de "/upload/"
            if (path.startsWith("v")) {
                int slash = path.indexOf('/');
                if (slash > -1) path = path.substring(slash + 1);
            }
            int dot = path.lastIndexOf('.');
            if (dot > 0) path = path.substring(0, dot);
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resultado tipado de subida.
     */
    record Upload(String url, String publicId, String format) {}
}

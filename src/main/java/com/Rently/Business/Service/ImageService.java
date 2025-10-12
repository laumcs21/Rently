package com.Rently.Business.Service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    record Upload(String url, String publicId, String format) {}

    Upload uploadImage(MultipartFile file, String folder, String publicIdHint) throws Exception;

    void deleteByPublicId(String publicId) throws Exception;

    /** Intenta extraer el public_id desde una secure_url de Cloudinary */
    String tryExtractPublicId(String secureUrl);
}

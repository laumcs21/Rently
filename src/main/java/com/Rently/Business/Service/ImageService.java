package com.Rently.Business.Service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    Upload uploadImage(MultipartFile file, String folder, String publicIdHint) throws Exception;

    void deleteByPublicId(String publicId) throws Exception;

    /** Intenta extraer el public_id desde una secure_url de Cloudinary */
    String tryExtractPublicId(String secureUrl);

    class Upload {
        private final String url;
        private final String publicId;
        private final String format;

        public Upload(String url, String publicId, String format) {
            this.url = url;
            this.publicId = publicId;
            this.format = format;
        }

        public String getUrl() {
            return url;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getFormat() {
            return format;
        }
    }
}

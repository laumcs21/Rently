package com.Rently.Business.Service.impl;

import com.Rently.Business.Service.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación del ImageService que utiliza Cloudinary como proveedor.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.base-folder:rently}")
    private String baseFolder;



    @Override
    public Map upload(MultipartFile multipartFile) throws IOException {
        File file = convert(multipartFile);
        try {
            return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        } finally {
            // elimina el temporal pase lo que pase
            if (file != null && file.exists()) file.delete();
        }
    }

    @Override
    public Map delete(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }



    @Override
    public Upload uploadImage(MultipartFile file, String folder, String publicIdHint) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio.");
        }

        String folderPath = StringUtils.isBlank(folder) ? baseFolder : (baseFolder + "/" + folder);

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folderPath,
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", true,
                "overwrite", true
        );

        if (StringUtils.isNotBlank(publicIdHint)) {
            params.put("public_id", publicIdHint);
        }

        // Subimos desde bytes (evita temporales)
        Map<?, ?> res = cloudinary.uploader().upload(file.getBytes(), params);

        return new Upload(
                (String) res.get("secure_url"),
                (String) res.get("public_id"),
                (String) res.get("format")
        );
    }

    @Override
    public void deleteByPublicId(String publicId) throws IOException {
        if (StringUtils.isNotBlank(publicId)) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }


    /* -------------------- Utilidad interna -------------------- */

    private File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNullElse(
                multipartFile.getOriginalFilename(),
                "upload_" + System.nanoTime()
        ));
        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(multipartFile.getBytes());
        }
        return file;
    }
}

package com.Rently.Business.Service.impl;

import com.Rently.Business.Service.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.base-folder:rently}")
    private String baseFolder;

    @Override
    public Upload uploadImage(MultipartFile file, String folder, String publicIdHint) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio.");
        }

        String folderPath = StringUtils.isBlank(folder) ? baseFolder : (baseFolder + "/" + folder);

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folderPath,
                "overwrite", true,
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", true
        );
        if (StringUtils.isNotBlank(publicIdHint)) {
            params.put("public_id", publicIdHint);
        }

        Map<?, ?> upload = cloudinary.uploader().upload(file.getBytes(), params);

        String url = (String) upload.get("secure_url");
        String publicId = (String) upload.get("public_id");
        String format = (String) upload.get("format");

        return new Upload(url, publicId, format);
    }

    @Override
    public void deleteByPublicId(String publicId) throws Exception {
        if (StringUtils.isNotBlank(publicId)) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    @Override
    public String tryExtractPublicId(String url) {
        if (StringUtils.isBlank(url)) return null;
        try {
            String path = url.substring(url.indexOf("/upload/") + 8);
            if (path.startsWith("v")) path = path.substring(path.indexOf("/") + 1);
            int dot = path.lastIndexOf(".");
            if (dot > 0) path = path.substring(0, dot);
            return path; // p.ej. rently/perfiles/persona_15
        } catch (Exception e) {
            return null;
        }
    }
}

package com.Rently.Application.Controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Rently.Business.Service.ImageService;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }
private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<Map> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(imageService.upload(file));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Map> delete(@PathVariable String publicId) throws IOException {
        return ResponseEntity.ok(imageService.delete(publicId));
    }
}

package com.Rently.Application.Controller;


import com.Rently.Business.Service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllerTestConfig.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Test
    void uploadShouldReturnServiceResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "data".getBytes());
        when(imageService.upload(any())).thenReturn(Map.of("url", "http://cloud/image"));

        mockMvc.perform(multipart("/api/images/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://cloud/image"));

        verify(imageService).upload(any());
    }

    @Test
    void deleteShouldReturnServiceResponse() throws Exception {
        when(imageService.delete("abc123")).thenReturn(Map.of("result", "ok"));

        mockMvc.perform(delete("/api/images/{id}", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ok"));

        verify(imageService).delete(eq("abc123"));
    }
}

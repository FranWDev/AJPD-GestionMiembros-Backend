package org.dubini.gestion.controller;

import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.config.SupabaseStorageProperties;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.service.ImageService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessKeyProperties accessKeyProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageService;

    private String authHeader;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ImageService imageService(SupabaseStorageProperties properties) {
            return new ImageService(properties) {
                @Override
                public ImageResponseDTO saveImage(MultipartFile file) throws IOException {
                    return new ImageResponseDTO("test.png", "https://mock.supabase.co/storage/v1/object/public/bucket/test.png", 15);
                }
            };
        }
    }

    @BeforeEach
    void setup() throws Exception {
        accessKeyProperties.setAccessKey(passwordEncoder.encode("testkey"));

        String loginBody = new JSONObject()
                .put("accessKey", "testkey")
                .toString();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new JSONObject(loginResponse).getString("token");
        authHeader = "Bearer " + token;
    }

    @Test
    void testUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", MediaType.IMAGE_PNG_VALUE, "PNG_IMAGE_BYTES".getBytes());

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.file.url").value("https://mock.supabase.co/storage/v1/object/public/bucket/test.png"))
                .andExpect(jsonPath("$.file.name").value("test.png"));
    }
}


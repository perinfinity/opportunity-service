package com.perinfinity.volunteering.opportunity.controller;

import com.perinfinity.volunteering.opportunity.dto.StoredImage;
import com.perinfinity.volunteering.opportunity.exception.ResourceNotFoundException;
import com.perinfinity.volunteering.opportunity.service.IImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private IImageStorageService imageStorageService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    @Test
    void upload_shouldReturn201WithIdAndUrl() throws Exception {
        when(imageStorageService.store(any())).thenReturn("abc123");
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc123"))
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.endsWith("/api/v1/images/abc123")));
    }

    @Test
    void getImage_shouldReturnBytesWithContentType() throws Exception {
        byte[] bytes = {10, 20, 30};
        when(imageStorageService.load("abc123"))
                .thenReturn(new StoredImage(new ByteArrayResource(bytes), "image/png"));

        mockMvc.perform(get("/api/v1/images/abc123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void getImage_shouldReturn404_whenImageDoesNotExist() throws Exception {
        when(imageStorageService.load("missing")).thenThrow(new ResourceNotFoundException("Image non trouvée"));

        mockMvc.perform(get("/api/v1/images/missing"))
                .andExpect(status().isNotFound());
    }
}

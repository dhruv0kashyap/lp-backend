package com.linkedin;

import com.linkedin.controller.FileUploadController;
import com.linkedin.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadControllerTest {

private FileUploadController fileUploadController;
private Path tempDir;

@BeforeEach
void setUp() throws IOException {
    fileUploadController = new FileUploadController();
    tempDir = Files.createTempDirectory("upload-test");
    ReflectionTestUtils.setField(fileUploadController, "uploadDir", tempDir.toString());
}

@Test
void uploadFile_EmptyFile_ReturnsBadRequest() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            new byte[0]
    );

    ResponseEntity<ApiResponse<String>> response = fileUploadController.uploadFile(file);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Please select a file to upload", response.getBody().getMessage());
}

@Test
void uploadFile_InvalidContentType_ReturnsBadRequest() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "hello".getBytes()
    );

    ResponseEntity<ApiResponse<String>> response = fileUploadController.uploadFile(file);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Only images and PDF files are allowed", response.getBody().getMessage());
}

@Test
void uploadFile_FileTooLarge_ReturnsBadRequest() throws IOException {
    byte[] largeContent = new byte[11 * 1024 * 1024];
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.pdf",
            "application/pdf",
            largeContent
    );

    ResponseEntity<ApiResponse<String>> response = fileUploadController.uploadFile(file);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("File size must not exceed 10MB", response.getBody().getMessage());
}

@Test
void uploadFile_ValidImage_ReturnsSuccess() throws IOException {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "photo.png",
            "image/png",
            "image-content".getBytes()
    );

    ResponseEntity<ApiResponse<String>> response = fileUploadController.uploadFile(file);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("File uploaded successfully", response.getBody().getMessage());
    assertNotNull(response.getBody().getData());
    assertTrue(response.getBody().getData().startsWith("/api/files/"));
}

@Test
void serveFile_FileExists_ReturnsResource() throws IOException {
    Path filePath = tempDir.resolve("sample.pdf");
    Files.write(filePath, "pdf-content".getBytes());

    ResponseEntity<org.springframework.core.io.Resource> response =
            fileUploadController.serveFile("sample.pdf");

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("application/pdf", response.getHeaders().getContentType().toString());
}

@Test
void serveFile_FileNotFound_Returns404() throws IOException {
    ResponseEntity<org.springframework.core.io.Resource> response =
            fileUploadController.serveFile("missing.pdf");

    assertEquals(404, response.getStatusCode().value());
}

}




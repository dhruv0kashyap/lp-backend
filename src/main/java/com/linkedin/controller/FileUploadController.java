package com.linkedin.controller;
import com.linkedin.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@Tag(name = "File Upload", description = "Upload images and documents")
@Slf4j
public class FileUploadController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file (image or PDF resume)")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only images and PDF files are allowed"));
        }

        // Max 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must not exceed 10MB"));
        }

        String fileName = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/api/files/" + fileName;
        log.info("File uploaded: {}", fileUrl);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
    }

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Serve an uploaded file")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(
            @PathVariable String fileName) throws IOException {

        Path filePath = Paths.get(uploadDir).resolve(fileName);
        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Detect the correct Content-Type from the file extension
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            // Fallback: derive from extension
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (lower.endsWith(".png")) {
                contentType = "image/png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lower.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lower.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lower.endsWith(".doc")) {
                contentType = "application/msword";
            } else if (lower.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                contentType = "application/octet-stream";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
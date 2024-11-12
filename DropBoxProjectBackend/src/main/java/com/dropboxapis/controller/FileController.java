package com.dropboxapis.controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.dropboxapis.model.FileMetadata;
import com.dropboxapis.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")

@RestController
@RequestMapping("/api/files")
public class FileController {

  @Autowired
  private FileService fileService;

  private final String UPLOAD_DIR = "/Users/hemantsingh/Desktop/test/";

  // Upload File API
  @PostMapping("/upload")
  public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
    // Ensure the directory exists
    File directory = new File(UPLOAD_DIR);
    if (!directory.exists()) {
      boolean created = directory.mkdirs();
      if (!created) {
        throw new IOException("Failed to create upload directory");
      }
    }

    // Save the uploaded file
    File dest = new File(UPLOAD_DIR + file.getOriginalFilename());
    file.transferTo(dest);

    // Save file metadata
    FileMetadata metadata = new FileMetadata();
    metadata.setName(file.getOriginalFilename());
    metadata.setType(file.getContentType());
    metadata.setSize(file.getSize());
    metadata.setUploadTime(LocalDateTime.now());

    fileService.saveFileMetadata(metadata);

    return "File uploaded successfully!";
  }

  // List All Files API
  @GetMapping
  public List<FileMetadata> listFiles() {
    return fileService.getAllFiles();
  }

  // Download or View File API
  @GetMapping("/download/{fileName}")
  public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, @RequestParam(defaultValue = "attachment") String mode) throws IOException {
    // Locate the file
    File file = new File(UPLOAD_DIR + fileName);
    if (!file.exists()) {
      throw new FileNotFoundException("File not found: " + fileName);
    }

    // Create a resource from the file
    Path path = file.toPath();
    Resource resource = new UrlResource(path.toUri());
    if (!resource.exists() || !resource.isReadable()) {
      throw new FileNotFoundException("File not found or unreadable: " + fileName);
    }

    // Determine the content type
    String contentType = "application/octet-stream"; // Default binary content
    if (fileName.endsWith(".txt")) {
      contentType = "text/plain";
    } else if (fileName.endsWith(".csv")) {
      contentType = "text/csv";
    } else if (fileName.endsWith(".json")) {
      contentType = "application/json";
    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
      contentType = "image/jpeg";
    } else if (fileName.endsWith(".png")) {
      contentType = "image/png";
    }

    // Set Content-Disposition based on mode
    String disposition = mode.equalsIgnoreCase("inline") ? "inline" : "attachment";

    // Return the file as a response
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + file.getName() + "\"")
        .body(resource);
  }
}

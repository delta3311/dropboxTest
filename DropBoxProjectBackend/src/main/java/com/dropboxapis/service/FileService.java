package com.dropboxapis.service;

import com.dropboxapis.model.FileMetadata;
import com.dropboxapis.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

  @Autowired
  private FileMetadataRepository fileMetadataRepository;

  // Method to get all files
  public List<FileMetadata> getAllFiles() {
    return fileMetadataRepository.findAll();
  }

  // Method to save file metadata
  public FileMetadata saveFileMetadata(FileMetadata metadata) {
    return fileMetadataRepository.save(metadata);
  }
}

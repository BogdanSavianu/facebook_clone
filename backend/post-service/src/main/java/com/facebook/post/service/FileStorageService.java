package com.facebook.post.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    Path loadFile(String filename);
    void deleteFile(String filename);
    void init(); 
} 
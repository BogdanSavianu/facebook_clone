package com.facebook.post.service.impl;

import com.facebook.post.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path fileStorageLocation;
    private final String cleanedFileStoragePathPrefix;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir, 
                                @Value("${file.upload-path-prefix}") String rawPathPrefix) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        String tempPathPrefix = rawPathPrefix.trim();
        if (!tempPathPrefix.startsWith("/")) {
            tempPathPrefix = "/" + tempPathPrefix;
        }
        if (!tempPathPrefix.endsWith("/")) {
            tempPathPrefix = tempPathPrefix + "/";
        }
        this.cleanedFileStoragePathPrefix = tempPathPrefix;
        logger.info("FileStorageService: Raw path prefix from properties: '{}', Cleaned path prefix: '{}'", rawPathPrefix, this.cleanedFileStoragePathPrefix);

        init();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Created upload directory: {}", this.fileStorageLocation.toString());
        } catch (Exception ex) {
            logger.error("Could not create upload directory! Path: {}", this.fileStorageLocation.toString(), ex);
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        try {
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            if(uniqueFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Stored file: {} at path: {}", uniqueFileName, targetLocation.toString());
            return cleanedFileStoragePathPrefix + uniqueFileName;
        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Path loadFile(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path filePath = loadFile(filename);
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", filename);
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", filename, ex);
        }
    }
} 
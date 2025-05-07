package com.facebook.post.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

// Import SLF4J Logger and LoggerFactory
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class); // Added logger

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-path-prefix}")
    private String uploadPathPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure the path prefix ends with "/" if it doesn't, and the pattern with "/**"
        String cleanedUploadPathPrefix = uploadPathPrefix.trim(); // Trim the path prefix
        if (!cleanedUploadPathPrefix.startsWith("/")) { // Ensure it starts with a slash
            cleanedUploadPathPrefix = "/" + cleanedUploadPathPrefix;
        }
        if (!cleanedUploadPathPrefix.endsWith("/")) { // Ensure it ends with a slash
            cleanedUploadPathPrefix = cleanedUploadPathPrefix + "/";
        }

        String publicPath = cleanedUploadPathPrefix; // Use the cleaned prefix
        String resourceLocation = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize().toString() + "/";

        logger.info("Configuring resource handler for public path: {} to serve from resource location: {}", publicPath + "**", resourceLocation); // Added log statement

        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(resourceLocation);
        
        // Example: if uploadPathPrefix is "/post-images/" and uploadDir is "./uploads/post-images"
        // This will serve files from ./uploads/post-images/ at http://localhost:8083/post-images/<filename>
    }
} 
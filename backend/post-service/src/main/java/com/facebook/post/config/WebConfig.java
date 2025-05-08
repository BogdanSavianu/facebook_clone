package com.facebook.post.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload-path-prefix}")
    private String uploadPathPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String cleanedUploadPathPrefix = uploadPathPrefix.trim();
        if (!cleanedUploadPathPrefix.startsWith("/")) {
            cleanedUploadPathPrefix = "/" + cleanedUploadPathPrefix;
        }
        if (!cleanedUploadPathPrefix.endsWith("/")) {
            cleanedUploadPathPrefix = cleanedUploadPathPrefix + "/";
        }

        String publicPath = cleanedUploadPathPrefix;
        String resourceLocation = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize().toString() + "/";

        logger.info("Configuring resource handler for public path: {} to serve from resource location: {}", publicPath + "**", resourceLocation);

        registry.addResourceHandler(publicPath + "**")
                .addResourceLocations(resourceLocation);
        
        logger.info("Configuring resource handler for public path: {} to serve from resource location: {}", publicPath + "**", resourceLocation);
    }
} 
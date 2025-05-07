package com.facebook.comment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${comment.file.upload-path-prefix:/default-comment-images/}")
    private String commentImageUploadPathPrefix;

    @Value("${comment.file.upload-dir:./default-uploads/comment-images}")
    private String commentImageUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPattern = commentImageUploadPathPrefix.trim();
        if (!urlPattern.startsWith("/")) {
            urlPattern = "/" + urlPattern;
        }
        if (!urlPattern.endsWith("/")) {
            urlPattern = urlPattern + "/";
        }
        urlPattern = urlPattern + "**";

        String resolvedUploadDir = Paths.get(commentImageUploadDir).toFile().getAbsolutePath();
        String resourceLocation = "file:" + resolvedUploadDir + "/";
        if (!resolvedUploadDir.endsWith("/") && !resolvedUploadDir.endsWith("\\")) {
             resourceLocation = "file:" + resolvedUploadDir + System.getProperty("file.separator");
        }


        registry.addResourceHandler(urlPattern)
                .addResourceLocations(resourceLocation);
        
        System.out.println("INFO: Serving comment images from URL pattern: " + urlPattern + " at location: " + resourceLocation);
    }
} 
package com.example.lcms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Stores uploaded images in a local directory and returns the
 * relative path that can be used to serve the file via /uploads/**.
 */
@Service
public class ImageStorageService {

    private final Path rootLocation;

    public ImageStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + rootLocation, e);
        }
    }

    /**
     * Saves the uploaded file and returns its relative path (e.g. "courses/uuid-filename.jpg").
     *
     * @param file      the uploaded multipart file
     * @param subfolder a subfolder inside the uploads directory (e.g. "courses" or "learners")
     * @return relative path from the uploads root
     */
    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path subPath = rootLocation.resolve(subfolder);
            Files.createDirectories(subPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String storedName = UUID.randomUUID() + extension;
            Path destination = subPath.resolve(storedName).normalize();

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Return path relative to uploads root: "subfolder/storedName"
            return subfolder + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    /**
     * Deletes a previously stored image.
     *
     * @param relativePath path relative to the uploads root
     */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Log but don't fail the request
            System.err.println("Could not delete file: " + relativePath + " – " + e.getMessage());
        }
    }
}

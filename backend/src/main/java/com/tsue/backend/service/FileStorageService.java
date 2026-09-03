package com.tsue.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDir;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для загрузок: " + storageDir, e);
        }
    }

    /**
     * Сохраняет файл на диск под уникальным именем.
     * Возвращает это уникальное имя (storedFileName) — его нужно сохранить в БД.
     */
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;

        try {
            Path target = storageDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить файл: " + originalName, e);
        }

        return storedFileName;
    }

    public Resource loadAsResource(String storedFileName) {
        try {
            Path filePath = storageDir.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Файл не найден: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Некорректный путь к файлу: " + storedFileName, e);
        }
    }
}

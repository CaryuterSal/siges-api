package dev.spiffocode.sigesapi.common.infrastructure.service;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.StorageException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String path) throws StorageException;

    void deleteFile(String fileUrl) throws StorageException;
}

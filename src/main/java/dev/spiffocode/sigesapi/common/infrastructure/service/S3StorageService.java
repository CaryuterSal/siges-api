package dev.spiffocode.sigesapi.common.infrastructure.service;

import dev.spiffocode.sigesapi.common.infrastructure.config.S3Properties;
import dev.spiffocode.sigesapi.common.infrastructure.exceptions.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final Tika tika;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/svg+xml");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public String uploadFile(MultipartFile file, String path) throws StorageException {
        try {
            byte[] bytes = file.getBytes();
            String contentType = detectAndValidate(bytes, file.getSize());

            String key = path + "/" + UUID.randomUUID() + extensionFromType(contentType);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();

            log.info("\n=== OUTGOING S3 UPLOAD ===\nBucket: {}\nKey: {}\nContentType: {}\n==========================",
                    s3Properties.getBucketName(), key, contentType);
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("S3 Upload successful: {}", key);

            return buildUrl(key);
        } catch (S3Exception | IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new StorageException("Failed to upload file", e);
        }
    }

    @Override
    public String uploadFile(byte[] file, String filename, String path) throws StorageException {
        String contentType = detectAndValidate(file, file.length);

        String key = path + "/" + UUID.randomUUID() + extensionFromType(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        log.info("\n=== OUTGOING S3 UPLOAD ===\nBucket: {}\nKey: {}\nContentType: {}\n==========================",
                s3Properties.getBucketName(), key, contentType);
        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file));
            log.info("S3 Upload successful: {}", key);
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new StorageException("Failed to upload file", e);
        }

        return buildUrl(key);
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())
            return;

        String key = fileUrl.replace(buildBaseUrl() + "/", "");

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();

        log.info("\n=== OUTGOING S3 DELETE ===\nBucket: {}\nKey: {}\n==========================",
                s3Properties.getBucketName(), key);
        try {
            s3Client.deleteObject(request);
            log.info("S3 Delete successful: {}", key);
        } catch (S3Exception ex) {
            log.warn("Could not delete file from S3", ex);
            throw new StorageException("Could not delete file from S3", ex);
        }
    }

    private String detectAndValidate(byte[] bytes, long size) {
        if (bytes.length == 0)
            throw new IllegalArgumentException("File is empty");
        if (size > MAX_FILE_SIZE)
            throw new IllegalArgumentException("File size exceeds 5MB limit");

        String detectedType = tika.detect(bytes);

        if (!ALLOWED_TYPES.contains(detectedType))
            throw new IllegalArgumentException(
                    "Invalid format. Allowed: JPG, PNG, WEBP, SVG. Detected: " + detectedType);

        return detectedType;
    }

    private String extensionFromType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private String buildUrl(String key) {
        return buildBaseUrl() + "/" + key;
    }

    private String buildBaseUrl() {
        String domain = s3Properties.getCloudfrontDomain();
        if (!domain.startsWith("http"))
            domain = "https://" + domain;
        return domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
    }
}

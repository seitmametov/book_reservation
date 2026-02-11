package com.example.library.service;

import com.example.library.properties.MinioProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    // 1. СТАРЫЙ МЕТОД (для книг): теперь он просто вызывает новый
    public String upload(MultipartFile file) {
        // Он берет бакет по умолчанию из твоего application.properties
        return upload(file, properties.getBucket());
    }

    // 2. НОВЫЙ МЕТОД (универсальный): тот, что мы написали для аватарок
    public String upload(MultipartFile file, String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

                // Делаем бакет публичным, чтобы картинки открывались по ссылке
                String policy = "{\n" +
                        "    \"Version\": \"2012-10-17\",\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": {\"AWS\": [\"*\"]},\n" +
                        "            \"Action\": [\"s3:GetObject\"],\n" +
                        "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return properties.getEndpoint() + "/" + bucketName + "/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Minio upload failed", e);
        }
    }
}

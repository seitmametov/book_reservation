package com.example.library.service;


import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioCleanupService {

    private final MinioClient minioClient;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // Запускается, например, раз в неделю в 3 часа ночи
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupUnusedFiles() {
        try {
            log.info("Запуск очистки неиспользуемых файлов в Minio...");

            // 1. Собираем все ссылки из БД
            Set<String> usedFiles = new HashSet<>();

            usedFiles.addAll(bookRepository.findAll().stream()
                    .map(book -> extractFileName(book.getCoverUrl()))
                    .filter(name -> name != null)
                    .collect(Collectors.toSet()));

            usedFiles.addAll(userRepository.findAll().stream()
                    .map(user -> extractFileName(user.getAvatarUrl()))
                    .filter(name -> name != null)
                    .collect(Collectors.toSet()));

            // 2. Получаем список всех файлов в Minio
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String fileName = item.objectName();

                // 3. Если файла нет в БД — удаляем его
                if (!usedFiles.contains(fileName)) {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(fileName)
                                    .build());
                    log.info("Удален неиспользуемый файл: {}", fileName);
                }
            }
            log.info("Очистка Minio завершена.");

        } catch (Exception e) {
            log.error("Ошибка при очистке Minio: ", e);
        }
    }

    // Вспомогательный метод для извлечения имени файла из полного URL
    private String extractFileName(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            // Если URL типа http://minio:9000/bucket/uuid-name.jpg
            return url.substring(url.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return null;
        }
    }
}